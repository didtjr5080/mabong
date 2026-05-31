from __future__ import annotations

from pathlib import Path
import textwrap

ROOT = Path.cwd()
CORE_DIR = ROOT / "core"
UI_DIR = ROOT / "ui"

CORE_DIR.mkdir(exist_ok=True)
UI_DIR.mkdir(exist_ok=True)

def write_file(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(textwrap.dedent(content).lstrip(), encoding="utf-8")
    print(f"[WRITE] {path}")

def replace_once(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    if old not in text:
        print(f"[SKIP] pattern not found in {path}: {old[:60]!r}")
        return
    text = text.replace(old, new, 1)
    path.write_text(text, encoding="utf-8")
    print(f"[PATCH] {path}")

# ---------------------------------------------------------------------
# 1) core/admin_service.py 생성
# ---------------------------------------------------------------------
write_file(
    CORE_DIR / "admin_service.py",
    r'''
    from __future__ import annotations

    import json
    import shutil
    from datetime import datetime
    from pathlib import Path
    from typing import Any, Dict, List, Tuple

    from core.data_store import DataStore


    class AdminService:
        """개발자/운영자용 JSON 관리 서비스.

        역할:
        - data/*.json 읽기/쓰기
        - 저장 전 JSON 문법 검사
        - 저장 후 전체 데이터 참조 검증
        - 백업 생성
        - 기본 템플릿 삽입
        """

        DATA_FILES = {
            "players": "players.json",
            "skills": "skills.json",
            "monsters": "monsters.json",
            "items": "items.json",
            "bosses": "bosses.json",
            "dungeons": "dungeons.json",
        }

        def __init__(self, data_dir: Path) -> None:
            self.data_dir = Path(data_dir)

        # ------------------------------------------------------------------
        # 파일 I/O
        # ------------------------------------------------------------------
        def file_path(self, key: str) -> Path:
            if key not in self.DATA_FILES:
                raise KeyError(f"알 수 없는 데이터 파일 키입니다: {key}")
            return self.data_dir / self.DATA_FILES[key]

        def read_text(self, key: str) -> str:
            path = self.file_path(key)
            if not path.exists():
                return "{}\n"
            return path.read_text(encoding="utf-8")

        def write_text_atomic(self, key: str, text: str) -> None:
            path = self.file_path(key)
            path.parent.mkdir(parents=True, exist_ok=True)
            tmp = path.with_suffix(path.suffix + ".tmp")
            tmp.write_text(text, encoding="utf-8")
            tmp.replace(path)

        def parse_json(self, text: str) -> Dict[str, Any]:
            data = json.loads(text)
            if not isinstance(data, dict):
                raise ValueError("최상위 JSON은 객체(dict)여야 합니다.")
            return data

        def format_json(self, text: str) -> str:
            data = self.parse_json(text)
            return json.dumps(data, ensure_ascii=False, indent=2) + "\n"

        # ------------------------------------------------------------------
        # 백업/검증
        # ------------------------------------------------------------------
        def backup_all(self, backup_root: Path | None = None) -> Path:
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            backup_root = backup_root or (self.data_dir.parent / "backups")
            target = backup_root / f"data_backup_{timestamp}"
            target.mkdir(parents=True, exist_ok=True)

            for key, filename in self.DATA_FILES.items():
                src = self.data_dir / filename
                if src.exists():
                    shutil.copy2(src, target / filename)

            return target

        def validate_all(self) -> List[str]:
            """DataStore 기본 검증 + bosses/dungeons 참조 검증."""
            messages: List[str] = []

            store = DataStore()
            store.load_all(self.data_dir)
            messages.append("DataStore 기본 검증 통과")

            dungeons_path = self.data_dir / "dungeons.json"
            dungeons = self._load_json_if_exists(dungeons_path)

            bosses_path = self.data_dir / "bosses.json"
            bosses = self._load_json_if_exists(bosses_path)

            self._validate_dungeons(dungeons, store, messages)
            self._validate_bosses(bosses, store, messages)

            messages.append("전체 참조 검증 통과")
            return messages

        def _load_json_if_exists(self, path: Path) -> Dict[str, Any]:
            if not path.exists():
                return {}
            with path.open("r", encoding="utf-8") as f:
                data = json.load(f)
            if not isinstance(data, dict):
                raise ValueError(f"{path.name}: 최상위 JSON은 객체여야 합니다.")
            return data

        def _validate_dungeons(self, dungeons: Dict[str, Any], store: DataStore, messages: List[str]) -> None:
            zones = dungeons.get("zones", {})
            if not isinstance(zones, dict):
                raise ValueError("dungeons.json: zones는 객체여야 합니다.")

            for zone_id, zone in zones.items():
                stages = zone.get("stages", {})
                if not isinstance(stages, dict):
                    raise ValueError(f"dungeons.json: zone {zone_id}의 stages는 객체여야 합니다.")

                for stage_id, stage in stages.items():
                    pool = stage.get("monster_pool", [])
                    if pool is None:
                        pool = []
                    if not isinstance(pool, list):
                        raise ValueError(f"dungeons.json: zone {zone_id} stage {stage_id}의 monster_pool은 배열이어야 합니다.")
                    for monster_id in pool:
                        if monster_id not in store.monsters:
                            raise ValueError(
                                f"dungeons.json: zone {zone_id} stage {stage_id}가 존재하지 않는 monster '{monster_id}'를 참조합니다."
                            )

                    boss_id = stage.get("boss_id")
                    if boss_id and not store.get_boss(boss_id):
                        raise ValueError(
                            f"dungeons.json: zone {zone_id} stage {stage_id}가 존재하지 않는 boss '{boss_id}'를 참조합니다."
                        )

            messages.append("dungeons.json 참조 검증 통과")

        def _validate_bosses(self, bosses: Dict[str, Any], store: DataStore, messages: List[str]) -> None:
            grouped = []
            grouped.extend(bosses.get("dungeon_bosses", {}).items())
            grouped.extend(bosses.get("special_bosses", {}).items())

            allowed_legacy_triggers = {"every_n_turns", "hp_below"}
            allowed_new_triggers = {"EVERY_TURNS", "EVERY_SECONDS", "HP_BELOW", "ON_START", "ON_DEATH"}
            allowed_actions = {
                "apply_effect",
                "APPLY_EFFECT",
                "MESSAGE",
                "BOSS_LOG",
                "BUFF_SELF",
                "DEBUFF_PLAYER",
                "HEAL_SELF",
            }

            for boss_id, boss in grouped:
                for skill_id in boss.get("skills", []) or []:
                    if skill_id not in store.skills:
                        raise ValueError(f"bosses.json: boss '{boss_id}'가 존재하지 않는 skill '{skill_id}'를 참조합니다.")

                drop_table = boss.get("drop_table")
                if drop_table and drop_table not in store.drop_tables:
                    raise ValueError(f"bosses.json: boss '{boss_id}'가 존재하지 않는 drop_table '{drop_table}'를 참조합니다.")

                gimmicks = boss.get("gimmicks", []) or []
                if not isinstance(gimmicks, list):
                    raise ValueError(f"bosses.json: boss '{boss_id}'의 gimmicks는 배열이어야 합니다.")

                for idx, gimmick in enumerate(gimmicks):
                    if not isinstance(gimmick, dict):
                        raise ValueError(f"bosses.json: boss '{boss_id}' gimmick[{idx}]는 객체여야 합니다.")

                    trigger = gimmick.get("trigger")
                    if isinstance(trigger, dict):
                        trigger_type = trigger.get("type")
                        if trigger_type not in allowed_new_triggers:
                            raise ValueError(f"bosses.json: boss '{boss_id}' gimmick[{idx}] trigger.type이 잘못되었습니다: {trigger_type}")
                    else:
                        if trigger not in allowed_legacy_triggers:
                            raise ValueError(f"bosses.json: boss '{boss_id}' gimmick[{idx}] trigger가 잘못되었습니다: {trigger}")

                    actions = gimmick.get("actions")
                    if actions is None and "action" in gimmick:
                        actions = [gimmick["action"]]
                    if actions is None:
                        actions = []
                    if not isinstance(actions, list):
                        raise ValueError(f"bosses.json: boss '{boss_id}' gimmick[{idx}] actions는 배열이어야 합니다.")

                    for a_idx, action in enumerate(actions):
                        action_type = action.get("type")
                        if action_type not in allowed_actions:
                            raise ValueError(
                                f"bosses.json: boss '{boss_id}' gimmick[{idx}].actions[{a_idx}] type이 잘못되었습니다: {action_type}"
                            )

            messages.append("bosses.json 참조 검증 통과")

        # ------------------------------------------------------------------
        # 템플릿
        # ------------------------------------------------------------------
        def append_template(self, key: str) -> Tuple[str, str]:
            """선택된 JSON에 샘플 항목을 추가하고 정렬된 JSON 문자열 반환."""
            text = self.read_text(key)
            data = self.parse_json(text)

            if key == "players":
                root = data.setdefault("players", {})
                new_id = self._unique_id(root, "char_admin_test")
                root[new_id] = {
                    "name": "운영자 테스트 캐릭터",
                    "class": "Admin",
                    "base_stats": {
                        "attack": 10,
                        "magic": 10,
                        "defense": 5,
                        "magic_resist": 5,
                        "max_hp": 100
                    },
                    "skills": ["__basic__"]
                }
                data.setdefault("default_player_id", new_id)
                return new_id, json.dumps(data, ensure_ascii=False, indent=2) + "\n"

            if key == "skills":
                root = data.setdefault("skills", {})
                new_id = self._unique_id(root, "admin_test_skill")
                root[new_id] = {
                    "name": "운영자 테스트 스킬",
                    "type": "physical",
                    "base_physical": 20,
                    "base_magic": 0,
                    "scale": {"attack": 0.5, "magic": 0.0},
                    "cost": 0,
                    "cooldown": 0,
                    "apply_effect": None
                }
                return new_id, json.dumps(data, ensure_ascii=False, indent=2) + "\n"

            if key == "monsters":
                root = data.setdefault("monsters", {})
                new_id = self._unique_id(root, "admin_test_monster")
                root[new_id] = {
                    "name": "운영자 테스트 몬스터",
                    "ai": "basic",
                    "stats": {
                        "attack": 5,
                        "magic": 0,
                        "defense": 1,
                        "magic_resist": 1,
                        "max_hp": 50
                    },
                    "skills": ["__basic__"],
                    "drop_table": None
                }
                return new_id, json.dumps(data, ensure_ascii=False, indent=2) + "\n"

            if key == "items":
                root = data.setdefault("items", {})
                new_id = self._unique_id(root, "admin_test_potion")
                root[new_id] = {
                    "name": "운영자 테스트 포션",
                    "type": "consumable",
                    "stats": {
                        "attack": 0,
                        "magic": 0,
                        "defense": 0,
                        "magic_resist": 0,
                        "max_hp": 0
                    },
                    "use_effect": {
                        "type": "heal",
                        "target": "self",
                        "amount": 30
                    }
                }
                data.setdefault("drop_tables", {}).setdefault("admin_test_drop", [{"item": new_id, "chance": 1.0, "min": 1, "max": 1}])
                return new_id, json.dumps(data, ensure_ascii=False, indent=2) + "\n"

            if key == "bosses":
                root = data.setdefault("special_bosses", {})
                new_id = self._unique_id(root, "admin_test_boss")
                root[new_id] = {
                    "name": "운영자 테스트 보스",
                    "ai": "boss",
                    "stats": {
                        "attack": 12,
                        "magic": 5,
                        "defense": 3,
                        "magic_resist": 3,
                        "max_hp": 200
                    },
                    "skills": ["__basic__"],
                    "drop_table": None,
                    "gimmicks": [
                        {
                            "id": "test_stun_50",
                            "trigger": {
                                "type": "HP_BELOW",
                                "value": 50,
                                "once": True
                            },
                            "actions": [
                                {
                                    "type": "MESSAGE",
                                    "text": "운영자 테스트 보스가 패턴을 사용합니다."
                                },
                                {
                                    "type": "APPLY_EFFECT",
                                    "target": "player",
                                    "effect": "stun",
                                    "duration": 1,
                                    "power": 0
                                }
                            ]
                        }
                    ]
                }
                return new_id, json.dumps(data, ensure_ascii=False, indent=2) + "\n"

            if key == "dungeons":
                zones = data.setdefault("zones", {})
                new_id = self._unique_id(zones, "admin_test_zone")
                zones[new_id] = {
                    "name": "운영자 테스트 던전",
                    "stages": {
                        "1": {
                            "name": "테스트 1스테이지",
                            "exp": 10,
                            "monster_pool": []
                        }
                    }
                }
                return new_id, json.dumps(data, ensure_ascii=False, indent=2) + "\n"

            raise ValueError(f"템플릿 추가를 지원하지 않는 파일입니다: {key}")

        def _unique_id(self, root: Dict[str, Any], base: str) -> str:
            if base not in root:
                return base
            idx = 2
            while f"{base}_{idx}" in root:
                idx += 1
            return f"{base}_{idx}"
    '''
)

# ---------------------------------------------------------------------
# 2) ui/admin_view.py 생성
# ---------------------------------------------------------------------
write_file(
    UI_DIR / "admin_view.py",
    r'''
    from __future__ import annotations

    from pathlib import Path
    from typing import Optional

    from PyQt6 import QtWidgets, QtCore

    from core.admin_service import AdminService


    BASE_DIR = Path(__file__).resolve().parents[1]
    DATA_DIR = BASE_DIR / "data"


    class AdminView(QtWidgets.QWidget):
        """개발자/운영자용 데이터 관리 화면."""

        back_main = QtCore.pyqtSignal()
        data_saved = QtCore.pyqtSignal()

        def __init__(self, controller, parent=None):
            super().__init__(parent)
            self.controller = controller
            self.service = AdminService(DATA_DIR)
            self.current_key: Optional[str] = None

            root = QtWidgets.QVBoxLayout(self)

            title = QtWidgets.QLabel("개발자 / 운영자 관리")
            title.setStyleSheet("font-size: 22px; font-weight: bold; color: #ffd37a;")
            root.addWidget(title)

            desc = QtWidgets.QLabel(
                "data/*.json을 직접 수정합니다. 저장 시 JSON 문법 검사와 전체 참조 검증을 수행합니다."
            )
            desc.setStyleSheet("color:#a5c7ff;")
            root.addWidget(desc)

            splitter = QtWidgets.QSplitter(QtCore.Qt.Orientation.Horizontal)
            root.addWidget(splitter, 1)

            # 좌측 파일 목록
            left = QtWidgets.QWidget()
            left_layout = QtWidgets.QVBoxLayout(left)
            self.file_list = QtWidgets.QListWidget()
            self.file_list.setMinimumWidth(220)
            left_layout.addWidget(QtWidgets.QLabel("데이터 파일"))
            left_layout.addWidget(self.file_list, 1)

            self.btn_reload = QtWidgets.QPushButton("파일 새로고침")
            self.btn_backup = QtWidgets.QPushButton("전체 백업")
            self.btn_validate = QtWidgets.QPushButton("전체 검증")
            self.btn_back = QtWidgets.QPushButton("메인으로")
            for btn in [self.btn_reload, self.btn_backup, self.btn_validate, self.btn_back]:
                left_layout.addWidget(btn)

            splitter.addWidget(left)

            # 우측 편집기
            right = QtWidgets.QWidget()
            right_layout = QtWidgets.QVBoxLayout(right)

            self.file_label = QtWidgets.QLabel("파일을 선택하세요.")
            self.file_label.setStyleSheet("font-weight:bold;color:#ffd37a;")
            right_layout.addWidget(self.file_label)

            self.editor = QtWidgets.QPlainTextEdit()
            self.editor.setLineWrapMode(QtWidgets.QPlainTextEdit.LineWrapMode.NoWrap)
            self.editor.setStyleSheet(
                "QPlainTextEdit { background:#07111f; color:#e8f1ff; border:1px solid #24405f; "
                "font-family: Consolas, 'D2Coding', monospace; font-size:13px; }"
            )
            right_layout.addWidget(self.editor, 1)

            btn_row = QtWidgets.QHBoxLayout()
            self.btn_format = QtWidgets.QPushButton("JSON 정렬")
            self.btn_template = QtWidgets.QPushButton("템플릿 추가")
            self.btn_save = QtWidgets.QPushButton("저장 + 검증")
            btn_row.addWidget(self.btn_format)
            btn_row.addWidget(self.btn_template)
            btn_row.addWidget(self.btn_save)
            right_layout.addLayout(btn_row)

            self.log = QtWidgets.QTextEdit()
            self.log.setReadOnly(True)
            self.log.setMinimumHeight(120)
            self.log.setStyleSheet("background:#0c1522;color:#a5c7ff;border:1px solid #1e2d45;")
            right_layout.addWidget(self.log)

            splitter.addWidget(right)
            splitter.setStretchFactor(1, 1)

            # 시그널
            self.file_list.currentItemChanged.connect(self._on_file_selected)
            self.btn_reload.clicked.connect(self.refresh)
            self.btn_backup.clicked.connect(self.backup_all)
            self.btn_validate.clicked.connect(self.validate_all)
            self.btn_back.clicked.connect(self.back_main.emit)
            self.btn_format.clicked.connect(self.format_current)
            self.btn_template.clicked.connect(self.add_template)
            self.btn_save.clicked.connect(self.save_current)

            self.refresh()

        # ------------------------------------------------------------------
        # UI 헬퍼
        # ------------------------------------------------------------------
        def append_log(self, text: str) -> None:
            self.log.append(text)

        def refresh(self) -> None:
            self.file_list.blockSignals(True)
            self.file_list.clear()

            for key, filename in self.service.DATA_FILES.items():
                item = QtWidgets.QListWidgetItem(f"{key}  ·  {filename}")
                item.setData(QtCore.Qt.ItemDataRole.UserRole, key)
                self.file_list.addItem(item)

            self.file_list.blockSignals(False)

            if self.file_list.count() > 0:
                self.file_list.setCurrentRow(0)

            self.append_log("[정보] 관리자 파일 목록을 새로고침했습니다.")

        def _on_file_selected(self, current, previous=None) -> None:
            if not current:
                return
            key = current.data(QtCore.Qt.ItemDataRole.UserRole)
            self.current_key = key
            try:
                text = self.service.read_text(key)
                self.editor.setPlainText(text)
                self.file_label.setText(f"편집 중: {self.service.DATA_FILES[key]}")
                self.append_log(f"[로드] {self.service.DATA_FILES[key]}")
            except Exception as exc:
                QtWidgets.QMessageBox.critical(self, "로드 실패", str(exc))
                self.append_log(f"[오류] 로드 실패: {exc}")

        # ------------------------------------------------------------------
        # 기능
        # ------------------------------------------------------------------
        def format_current(self) -> None:
            if not self.current_key:
                return
            try:
                formatted = self.service.format_json(self.editor.toPlainText())
                self.editor.setPlainText(formatted)
                self.append_log("[성공] JSON 정렬 완료")
            except Exception as exc:
                QtWidgets.QMessageBox.warning(self, "JSON 오류", str(exc))
                self.append_log(f"[오류] JSON 정렬 실패: {exc}")

        def add_template(self) -> None:
            if not self.current_key:
                return
            try:
                new_id, text = self.service.append_template(self.current_key)
                self.editor.setPlainText(text)
                self.append_log(f"[성공] 템플릿 추가: {new_id}")
            except Exception as exc:
                QtWidgets.QMessageBox.warning(self, "템플릿 추가 실패", str(exc))
                self.append_log(f"[오류] 템플릿 추가 실패: {exc}")

        def backup_all(self) -> None:
            try:
                path = self.service.backup_all()
                QtWidgets.QMessageBox.information(self, "백업 완료", f"백업 위치:\n{path}")
                self.append_log(f"[성공] 전체 백업 완료: {path}")
            except Exception as exc:
                QtWidgets.QMessageBox.critical(self, "백업 실패", str(exc))
                self.append_log(f"[오류] 백업 실패: {exc}")

        def validate_all(self) -> None:
            try:
                messages = self.service.validate_all()
                self.append_log("[성공] 전체 검증 완료")
                for msg in messages:
                    self.append_log(f"  - {msg}")
                QtWidgets.QMessageBox.information(self, "검증 완료", "전체 데이터 검증을 통과했습니다.")
            except Exception as exc:
                QtWidgets.QMessageBox.critical(self, "검증 실패", str(exc))
                self.append_log(f"[오류] 검증 실패: {exc}")

        def save_current(self) -> None:
            if not self.current_key:
                return

            key = self.current_key
            old_text = self.service.read_text(key)
            new_text = self.editor.toPlainText()

            try:
                # 1. 문법 검사 및 보기 좋게 정렬
                formatted = self.service.format_json(new_text)

                # 2. 임시 저장
                self.service.write_text_atomic(key, formatted)

                # 3. 전체 참조 검증
                messages = self.service.validate_all()

                # 4. 컨트롤러 데이터 재로드
                self._reload_controller_data()

                self.editor.setPlainText(formatted)
                self.append_log(f"[성공] 저장 완료: {self.service.DATA_FILES[key]}")
                for msg in messages:
                    self.append_log(f"  - {msg}")

                self.data_saved.emit()
                QtWidgets.QMessageBox.information(self, "저장 완료", "저장 및 전체 검증이 완료되었습니다.")

            except Exception as exc:
                # 검증 실패 시 기존 파일 복구
                try:
                    self.service.write_text_atomic(key, old_text)
                    self.editor.setPlainText(old_text)
                    self.append_log("[복구] 검증 실패로 기존 파일을 복구했습니다.")
                except Exception as restore_exc:
                    self.append_log(f"[치명적 오류] 복구 실패: {restore_exc}")

                QtWidgets.QMessageBox.critical(self, "저장 실패", str(exc))
                self.append_log(f"[오류] 저장 실패: {exc}")

        def _reload_controller_data(self) -> None:
            """저장 후 현재 컨트롤러가 들고 있는 데이터 참조를 갱신."""
            self.controller.data_store.load_all(DATA_DIR)
            self.controller.data_items = self.controller.data_store.items
            self.controller.data_drop_tables = self.controller.data_store.drop_tables
            self.controller.data_bosses = self.controller.data_store.bosses
            self.controller.data_dungeons = self.service._load_json_if_exists(DATA_DIR / "dungeons.json")
            self.controller.sync_player_hp()
            self.append_log("[정보] 컨트롤러 데이터 재로드 완료")
    '''
)

# ---------------------------------------------------------------------
# 3) ui/main_view.py 패치
# ---------------------------------------------------------------------
main_view = UI_DIR / "main_view.py"
if main_view.exists():
    replace_once(
        main_view,
        "    change_player = QtCore.pyqtSignal()\n",
        "    change_player = QtCore.pyqtSignal()\n    go_admin = QtCore.pyqtSignal()\n",
    )

    replace_once(
        main_view,
        '''        btn_special = QtWidgets.QPushButton("특수 보스")
        btn_change = QtWidgets.QPushButton("캐릭터 변경")
''',
        '''        btn_special = QtWidgets.QPushButton("특수 보스")
        btn_change = QtWidgets.QPushButton("캐릭터 변경")
        btn_admin = QtWidgets.QPushButton("개발자/운영자 관리")
        btn_admin.setStyleSheet("background-color:#4a3214; border-color:#d29b3d; color:#ffd37a;")
''',
    )

    replace_once(
        main_view,
        "        for i, btn in enumerate([btn_dungeon, btn_inventory, btn_stats, btn_special, btn_change]):\n",
        "        for i, btn in enumerate([btn_dungeon, btn_inventory, btn_stats, btn_special, btn_change, btn_admin]):\n",
    )

    replace_once(
        main_view,
        '''        btn_special.clicked.connect(self.go_special_boss.emit)
        btn_change.clicked.connect(self.change_player.emit)
''',
        '''        btn_special.clicked.connect(self.go_special_boss.emit)
        btn_change.clicked.connect(self.change_player.emit)
        btn_admin.clicked.connect(self.go_admin.emit)
''',
    )
else:
    print("[WARN] ui/main_view.py 없음")

# ---------------------------------------------------------------------
# 4) ui/main_window.py 패치
# ---------------------------------------------------------------------
main_window = UI_DIR / "main_window.py"
if main_window.exists():
    replace_once(
        main_window,
        "from .special_boss_view import SpecialBossView\n",
        "from .special_boss_view import SpecialBossView\nfrom .admin_view import AdminView\n",
    )

    replace_once(
        main_window,
        "        self.special_boss_view = SpecialBossView()\n",
        "        self.special_boss_view = SpecialBossView()\n        self.admin_view = AdminView(self.controller)\n",
    )

    replace_once(
        main_window,
        "        for view in [self.main_view, self.dungeon_view, self.battle_view, self.inventory_view, self.stats_view, self.special_boss_view]:\n",
        "        for view in [self.main_view, self.dungeon_view, self.battle_view, self.inventory_view, self.stats_view, self.special_boss_view, self.admin_view]:\n",
    )

    replace_once(
        main_window,
        "        self.main_view.change_player.connect(self.choose_player)\n",
        "        self.main_view.change_player.connect(self.choose_player)\n        self.main_view.go_admin.connect(self.show_admin)\n",
    )

    replace_once(
        main_window,
        '''        self.special_boss_view.enter_boss.connect(self.enter_special_boss)
        self.special_boss_view.back_main.connect(self.show_main)
''',
        '''        self.special_boss_view.enter_boss.connect(self.enter_special_boss)
        self.special_boss_view.back_main.connect(self.show_main)

        self.admin_view.back_main.connect(self.show_main)
        self.admin_view.data_saved.connect(self._after_admin_data_saved)
''',
    )

    replace_once(
        main_window,
        '''    def show_special_boss(self):
        self.refresh_special_boss()
        self.stack.setCurrentWidget(self.special_boss_view)

    def show_battle(self):
''',
        '''    def show_special_boss(self):
        self.refresh_special_boss()
        self.stack.setCurrentWidget(self.special_boss_view)

    def show_admin(self):
        self.admin_view.refresh()
        self.stack.setCurrentWidget(self.admin_view)

    def show_battle(self):
''',
    )

    replace_once(
        main_window,
        '''    def refresh_main(self):
        summary = self.controller.player_summary()
''',
        '''    def _after_admin_data_saved(self):
        """관리자 화면에서 데이터 저장 후 모든 화면을 안전하게 갱신."""
        self.refresh_main()
        self.refresh_dungeon()
        self.refresh_inventory()
        self.refresh_stats()
        self.refresh_special_boss()
        self._update_hud()

    def refresh_main(self):
        summary = self.controller.player_summary()
''',
    )
else:
    print("[WARN] ui/main_window.py 없음")

print("")
print("[DONE] 개발자/운영자 UI 패치 적용 완료")
print("다음 명령어로 실행 테스트:")
print("  python app.py")
