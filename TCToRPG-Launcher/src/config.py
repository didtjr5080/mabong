from __future__ import annotations

import json
import os
from dataclasses import asdict, dataclass
from pathlib import Path


CONFIG_FILE = Path("launcher-config.json")
DEFAULT_SERVER_HOST = "220.89.226.110"
DEFAULT_SERVER_PORT = 25565


@dataclass
class AppConfig:
    installDir: str
    minecraftLauncherPath: str
    manifestUrl: str
    serverHost: str
    serverPort: int
    lastClientVersion: str

    @classmethod
    def default(cls) -> "AppConfig":
        return cls(
            installDir="%USERPROFILE%/TCToRPG",
            minecraftLauncherPath="",
            manifestUrl="https://example.com/tctorpg/update-manifest.json",
            serverHost=DEFAULT_SERVER_HOST,
            serverPort=DEFAULT_SERVER_PORT,
            lastClientVersion="0.0.0",
        )

    @classmethod
    def load_or_create(cls, path: Path = CONFIG_FILE) -> "AppConfig":
        if not path.exists():
            config = cls.default()
            config.save(path)
            return config
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            config = cls.default()
            config.save(path)
            return config

        default = asdict(cls.default())
        default.update({key: value for key, value in data.items() if key in default})
        return cls(**default)

    def save(self, path: Path = CONFIG_FILE) -> None:
        path.write_text(json.dumps(asdict(self), ensure_ascii=False, indent=2), encoding="utf-8")

    @property
    def install_path(self) -> Path:
        expanded = os.path.expandvars(os.path.expanduser(self.installDir))
        return Path(expanded)

    @property
    def server_address(self) -> str:
        return f"{self.serverHost}:{self.serverPort}"

    def ensure_directories(self) -> None:
        root = self.install_path
        for relative in ("mods", "resourcepacks", "config", "logs", "cache/downloads"):
            (root / relative).mkdir(parents=True, exist_ok=True)
