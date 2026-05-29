import sys

from PyQt6.QtWidgets import QApplication

from src.config import AppConfig
from src.ui.main_window import MainWindow


def main() -> int:
    app = QApplication(sys.argv)
    config = AppConfig.load_or_create()
    window = MainWindow(config)
    window.show()
    return app.exec()
