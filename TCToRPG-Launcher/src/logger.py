from __future__ import annotations

from datetime import datetime
from pathlib import Path
from typing import Callable


class LauncherLogger:
    def __init__(self, log_path: Path, ui_callback: Callable[[str], None] | None = None):
        self.log_path = log_path
        self.ui_callback = ui_callback
        self.log_path.parent.mkdir(parents=True, exist_ok=True)

    def log(self, message: str) -> None:
        line = f"[{datetime.now():%Y-%m-%d %H:%M:%S}] {message}"
        self.log_path.parent.mkdir(parents=True, exist_ok=True)
        with self.log_path.open("a", encoding="utf-8") as stream:
            stream.write(line + "\n")
        if self.ui_callback:
            self.ui_callback(line)
