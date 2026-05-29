from __future__ import annotations

from pathlib import Path

from PyQt6.QtCore import QThread, pyqtSignal
from PyQt6.QtWidgets import (
    QApplication,
    QFileDialog,
    QGridLayout,
    QGroupBox,
    QHBoxLayout,
    QLabel,
    QLineEdit,
    QMainWindow,
    QMessageBox,
    QPlainTextEdit,
    QProgressBar,
    QPushButton,
    QTextEdit,
    QVBoxLayout,
    QWidget,
)

from src.config import AppConfig
from src.downloader import FileDownloader
from src.launcher_runner import LauncherRunner
from src.logger import LauncherLogger
from src.manifest import ManifestClient, ManifestError, UpdateManifest
from src.server_config import ServerConfig


class ManifestWorker(QThread):
    log = pyqtSignal(str)
    failed = pyqtSignal(str)
    completed = pyqtSignal(object)

    def __init__(self, manifest_url: str):
        super().__init__()
        self.manifest_url = manifest_url

    def run(self) -> None:
        try:
            self.log.emit("manifest 다운로드를 시작합니다.")
            manifest = ManifestClient().fetch(self.manifest_url)
            self.completed.emit(manifest)
        except ManifestError as exc:
            self.failed.emit(str(exc))
        except Exception as exc:
            self.failed.emit(f"알 수 없는 manifest 오류: {exc}")


class UpdateWorker(QThread):
    log = pyqtSignal(str)
    progress = pyqtSignal(int, str)
    failed = pyqtSignal(str)
    completed = pyqtSignal()

    def __init__(self, config: AppConfig, manifest: UpdateManifest):
        super().__init__()
        self.config = config
        self.manifest = manifest

    def run(self) -> None:
        try:
            self.config.ensure_directories()
            downloader = FileDownloader(
                self.config.install_path,
                progress_callback=self._progress,
                log_callback=self.log.emit,
            )
            downloader.update_files(self.manifest.files)
            self.completed.emit()
        except Exception as exc:
            self.failed.emit(str(exc))

    def _progress(self, current: int, total: int, path: str) -> None:
        value = int(current / max(1, total) * 100)
        self.progress.emit(value, path)


class MainWindow(QMainWindow):
    def __init__(self, config: AppConfig):
        super().__init__()
        self.config = config
        self.manifest: UpdateManifest | None = None
        self.server = ServerConfig(host=config.serverHost, port=config.serverPort)
        self.manifest_worker: ManifestWorker | None = None
        self.update_worker: UpdateWorker | None = None

        self.config.ensure_directories()
        self.logger = LauncherLogger(self.config.install_path / "logs" / "launcher.log", self.append_log)

        self.setWindowTitle("TCToRPG Launcher")
        self.resize(820, 620)
        self._build_ui()
        self._load_config_to_ui()
        self.log("런처를 시작했습니다.")

    def _build_ui(self) -> None:
        root = QWidget(self)
        layout = QVBoxLayout(root)

        title = QLabel("TCToRPG")
        title.setStyleSheet("font-size: 30px; font-weight: 700;")
        layout.addWidget(title)

        status_group = QGroupBox("상태")
        status_layout = QGridLayout(status_group)
        self.current_version_label = QLabel("0.0.0")
        self.latest_version_label = QLabel("-")
        self.server_label = QLabel("220.89.226.110:25565")
        self.server_label.setStyleSheet("font-size: 18px; font-weight: 600; color: #c63;")
        status_layout.addWidget(QLabel("현재 클라이언트 버전"), 0, 0)
        status_layout.addWidget(self.current_version_label, 0, 1)
        status_layout.addWidget(QLabel("최신 클라이언트 버전"), 1, 0)
        status_layout.addWidget(self.latest_version_label, 1, 1)
        status_layout.addWidget(QLabel("서버 주소"), 2, 0)
        status_layout.addWidget(self.server_label, 2, 1)
        layout.addWidget(status_group)

        config_group = QGroupBox("설정")
        config_layout = QGridLayout(config_group)
        self.install_dir_edit = QLineEdit()
        self.launcher_path_edit = QLineEdit()
        self.manifest_url_edit = QLineEdit()
        self.install_button = QPushButton("설치 폴더 선택")
        self.launcher_button = QPushButton("Minecraft Launcher 경로 선택")
        self.install_button.clicked.connect(self.choose_install_dir)
        self.launcher_button.clicked.connect(self.choose_launcher_path)
        config_layout.addWidget(QLabel("설치 폴더"), 0, 0)
        config_layout.addWidget(self.install_dir_edit, 0, 1)
        config_layout.addWidget(self.install_button, 0, 2)
        config_layout.addWidget(QLabel("Minecraft Launcher"), 1, 0)
        config_layout.addWidget(self.launcher_path_edit, 1, 1)
        config_layout.addWidget(self.launcher_button, 1, 2)
        config_layout.addWidget(QLabel("Manifest URL"), 2, 0)
        config_layout.addWidget(self.manifest_url_edit, 2, 1, 1, 2)
        layout.addWidget(config_group)

        notice_group = QGroupBox("공지사항")
        notice_layout = QVBoxLayout(notice_group)
        self.notice_text = QTextEdit()
        self.notice_text.setReadOnly(True)
        self.notice_text.setMinimumHeight(90)
        notice_layout.addWidget(self.notice_text)
        layout.addWidget(notice_group)

        buttons = QHBoxLayout()
        self.check_button = QPushButton("업데이트 확인")
        self.update_button = QPushButton("업데이트 실행")
        self.launch_button = QPushButton("Minecraft 실행")
        self.copy_button = QPushButton("서버 주소 복사")
        self.update_button.setEnabled(False)
        self.launch_button.setEnabled(False)
        self.check_button.clicked.connect(self.check_update)
        self.update_button.clicked.connect(self.run_update)
        self.launch_button.clicked.connect(self.launch_minecraft)
        self.copy_button.clicked.connect(self.copy_server_address)
        for button in (self.check_button, self.update_button, self.launch_button, self.copy_button):
            buttons.addWidget(button)
        layout.addLayout(buttons)

        self.progress = QProgressBar()
        self.progress.setValue(0)
        layout.addWidget(self.progress)

        log_group = QGroupBox("로그")
        log_layout = QVBoxLayout(log_group)
        self.log_text = QPlainTextEdit()
        self.log_text.setReadOnly(True)
        self.log_text.setMinimumHeight(170)
        log_layout.addWidget(self.log_text)
        layout.addWidget(log_group)

        self.setCentralWidget(root)

    def _load_config_to_ui(self) -> None:
        self.install_dir_edit.setText(self.config.installDir)
        self.launcher_path_edit.setText(self.config.minecraftLauncherPath)
        self.manifest_url_edit.setText(self.config.manifestUrl)
        self.current_version_label.setText(self.config.lastClientVersion)
        self.update_server_label()

    def save_config_from_ui(self) -> None:
        self.config.installDir = self.install_dir_edit.text().strip() or self.config.installDir
        self.config.minecraftLauncherPath = self.launcher_path_edit.text().strip()
        self.config.manifestUrl = self.manifest_url_edit.text().strip() or self.config.manifestUrl
        self.config.serverHost = self.server.host
        self.config.serverPort = self.server.port
        self.config.save()
        self.config.ensure_directories()

    def choose_install_dir(self) -> None:
        selected = QFileDialog.getExistingDirectory(self, "설치 폴더 선택", str(self.config.install_path))
        if selected:
            self.install_dir_edit.setText(selected)
            self.save_config_from_ui()
            self.logger = LauncherLogger(self.config.install_path / "logs" / "launcher.log", self.append_log)
            self.log(f"설치 폴더를 변경했습니다: {selected}")

    def choose_launcher_path(self) -> None:
        selected, _ = QFileDialog.getOpenFileName(
            self,
            "Minecraft Launcher 선택",
            "C:/",
            "Executable (*.exe *.lnk);;All files (*.*)",
        )
        if selected:
            self.launcher_path_edit.setText(selected)
            self.save_config_from_ui()
            self.log(f"Minecraft Launcher 경로를 저장했습니다: {selected}")

    def check_update(self) -> None:
        self.save_config_from_ui()
        self.check_button.setEnabled(False)
        self.update_button.setEnabled(False)
        self.log("업데이트 확인을 시작합니다.")
        self.manifest_worker = ManifestWorker(self.config.manifestUrl)
        self.manifest_worker.log.connect(self.log)
        self.manifest_worker.failed.connect(self.on_manifest_failed)
        self.manifest_worker.completed.connect(self.on_manifest_loaded)
        self.manifest_worker.start()

    def on_manifest_loaded(self, manifest: UpdateManifest) -> None:
        self.manifest = manifest
        self.server = manifest.server
        self.config.serverHost = self.server.host
        self.config.serverPort = self.server.port
        self.latest_version_label.setText(manifest.clientVersion)
        self.notice_text.setPlainText(f"{manifest.noticeTitle}\n\n{manifest.noticeBody}".strip())
        self.update_server_label()
        self.update_button.setEnabled(True)
        self.check_button.setEnabled(True)
        self.log(f"manifest 확인 완료: client {manifest.clientVersion}, files {len(manifest.files)}개")
        self.save_config_from_ui()

    def on_manifest_failed(self, message: str) -> None:
        self.check_button.setEnabled(True)
        self.log("오류: " + message)
        QMessageBox.warning(self, "업데이트 확인 실패", message)

    def run_update(self) -> None:
        if self.manifest is None:
            QMessageBox.information(self, "업데이트 필요", "먼저 업데이트 확인을 실행하세요.")
            return
        self.save_config_from_ui()
        self.update_button.setEnabled(False)
        self.launch_button.setEnabled(False)
        self.progress.setValue(0)
        self.log("업데이트를 시작합니다.")
        self.update_worker = UpdateWorker(self.config, self.manifest)
        self.update_worker.log.connect(self.log)
        self.update_worker.progress.connect(self.on_update_progress)
        self.update_worker.failed.connect(self.on_update_failed)
        self.update_worker.completed.connect(self.on_update_completed)
        self.update_worker.start()

    def on_update_progress(self, value: int, path: str) -> None:
        self.progress.setValue(value)
        self.log(f"진행률 {value}%: {path}")

    def on_update_completed(self) -> None:
        if self.manifest:
            self.config.lastClientVersion = self.manifest.clientVersion
            self.config.save()
            LauncherRunner(self.config).write_launcher_config(self.server)
        self.current_version_label.setText(self.config.lastClientVersion)
        self.progress.setValue(100)
        self.update_button.setEnabled(True)
        self.launch_button.setEnabled(True)
        self.log("업데이트 완료")
        QMessageBox.information(self, "업데이트 완료", "업데이트가 완료되었습니다.")

    def on_update_failed(self, message: str) -> None:
        self.update_button.setEnabled(True)
        self.launch_button.setEnabled(False)
        self.log("오류: " + message)
        QMessageBox.critical(self, "업데이트 실패", message)

    def launch_minecraft(self) -> None:
        self.save_config_from_ui()
        runner = LauncherRunner(self.config)
        launcher_path = runner.find_launcher()
        if launcher_path is None:
            self.log("Minecraft Launcher 경로를 찾지 못했습니다. 직접 선택하세요.")
            self.choose_launcher_path()
            launcher_path = runner.find_launcher()
        if launcher_path is None:
            QMessageBox.warning(self, "실행 실패", "Minecraft Launcher 경로가 필요합니다.")
            return
        try:
            runner.write_launcher_config(self.server)
            runner.launch(Path(launcher_path))
            self.log(f"Minecraft Launcher 실행: {launcher_path}")
        except Exception as exc:
            self.log("오류: " + str(exc))
            QMessageBox.critical(self, "실행 실패", str(exc))

    def copy_server_address(self) -> None:
        QApplication.clipboard().setText(self.server.address)
        self.log(f"서버 주소를 복사했습니다: {self.server.address}")

    def update_server_label(self) -> None:
        self.server_label.setText(self.server.address)

    def log(self, message: str) -> None:
        self.logger.log(message)

    def append_log(self, line: str) -> None:
        self.log_text.appendPlainText(line)
