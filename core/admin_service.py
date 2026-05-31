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
