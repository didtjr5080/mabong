from __future__ import annotations

import json
import os
import subprocess
from pathlib import Path

from src.config import AppConfig
from src.server_config import ServerConfig


class LauncherRunError(RuntimeError):
    pass


class LauncherRunner:
    WINDOWS_CANDIDATES = (
        Path("C:/XboxGames/Minecraft Launcher/Content/Minecraft.exe"),
        Path("C:/Program Files (x86)/Minecraft Launcher/MinecraftLauncher.exe"),
        Path("C:/Program Files/Minecraft Launcher/MinecraftLauncher.exe"),
    )

    def __init__(self, config: AppConfig):
        self.config = config

    def find_launcher(self) -> Path | None:
        configured = Path(os.path.expandvars(os.path.expanduser(self.config.minecraftLauncherPath)))
        if self.config.minecraftLauncherPath and configured.exists():
            return configured

        for path in self.WINDOWS_CANDIDATES:
            if path.exists():
                return path

        appdata = Path(os.environ.get("APPDATA", ""))
        start_menu = appdata / "Microsoft" / "Windows" / "Start Menu" / "Programs"
        if start_menu.exists():
            for shortcut in start_menu.rglob("*Minecraft*"):
                if shortcut.suffix.lower() in {".lnk", ".exe"}:
                    return shortcut
        return None

    def write_launcher_config(self, server: ServerConfig) -> Path:
        path = self.config.install_path / "launcher-config.json"
        data = {
            "server": {
                "name": server.name,
                "host": server.host,
                "port": server.port,
                "address": server.address,
            },
            "installDir": str(self.config.install_path),
            "lastClientVersion": self.config.lastClientVersion,
        }
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
        return path

    def launch(self, launcher_path: Path) -> None:
        if not launcher_path.exists():
            raise LauncherRunError(f"Minecraft Launcher 경로를 찾을 수 없습니다: {launcher_path}")
        try:
            subprocess.Popen([str(launcher_path)], cwd=str(launcher_path.parent), shell=False)
        except OSError as exc:
            raise LauncherRunError(f"Minecraft Launcher 실행 실패: {exc}") from exc
