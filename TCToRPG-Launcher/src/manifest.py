from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

import requests

from src.server_config import ServerConfig


class ManifestError(RuntimeError):
    pass


@dataclass(frozen=True)
class ManifestFile:
    path: str
    url: str
    sha256: str
    required: bool = True


@dataclass(frozen=True)
class UpdateManifest:
    launcherVersion: str
    clientVersion: str
    minecraftVersion: str
    loader: str
    loaderVersion: str
    server: ServerConfig
    noticeTitle: str
    noticeBody: str
    files: list[ManifestFile] = field(default_factory=list)

    @classmethod
    def from_json(cls, data: dict[str, Any]) -> "UpdateManifest":
        try:
            notice = data.get("notice") or {}
            files = [
                ManifestFile(
                    path=str(item["path"]),
                    url=str(item["url"]),
                    sha256=str(item["sha256"]).lower(),
                    required=bool(item.get("required", True)),
                )
                for item in data.get("files", [])
            ]
            return cls(
                launcherVersion=str(data.get("launcherVersion", "0.0.0")),
                clientVersion=str(data.get("clientVersion", "0.0.0")),
                minecraftVersion=str(data.get("minecraftVersion", "")),
                loader=str(data.get("loader", "")),
                loaderVersion=str(data.get("loaderVersion", "")),
                server=ServerConfig.from_manifest(data.get("server")),
                noticeTitle=str(notice.get("title", "")),
                noticeBody=str(notice.get("body", "")),
                files=files,
            )
        except (KeyError, TypeError, ValueError) as exc:
            raise ManifestError(f"manifest 형식이 올바르지 않습니다: {exc}") from exc


class ManifestClient:
    def __init__(self, timeout: int = 20):
        self.timeout = timeout

    def fetch(self, url: str) -> UpdateManifest:
        try:
            response = requests.get(url, timeout=self.timeout)
            response.raise_for_status()
        except requests.RequestException as exc:
            raise ManifestError(f"manifest 다운로드 실패: {exc}") from exc

        try:
            data = response.json()
        except ValueError as exc:
            raise ManifestError(f"manifest JSON 파싱 실패: {exc}") from exc

        if not isinstance(data, dict):
            raise ManifestError("manifest 루트는 JSON object여야 합니다.")
        return UpdateManifest.from_json(data)
