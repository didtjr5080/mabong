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
