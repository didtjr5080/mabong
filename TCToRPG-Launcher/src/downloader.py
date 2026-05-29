from __future__ import annotations

import shutil
from pathlib import Path
from typing import Callable
from urllib.parse import quote

import requests

from src.manifest import ManifestFile
from src.verifier import HashVerifier


class DownloadError(RuntimeError):
    pass


ProgressCallback = Callable[[int, int, str], None]
LogCallback = Callable[[str], None]


class FileDownloader:
    def __init__(self, install_dir: Path, progress_callback: ProgressCallback | None = None, log_callback: LogCallback | None = None):
        self.install_dir = install_dir
        self.download_cache = install_dir / "cache" / "downloads"
        self.progress_callback = progress_callback
        self.log_callback = log_callback

    def update_files(self, files: list[ManifestFile]) -> None:
        self.download_cache.mkdir(parents=True, exist_ok=True)
        total = max(1, len(files))
        for index, manifest_file in enumerate(files, start=1):
            try:
                self._update_one(manifest_file)
            except Exception as exc:
                self._cleanup_failed(manifest_file)
                message = f"{manifest_file.path} 업데이트 실패: {exc}"
                if manifest_file.required:
                    raise DownloadError(message) from exc
                self._log("경고: " + message)
            self._progress(index, total, manifest_file.path)

    def _update_one(self, manifest_file: ManifestFile) -> None:
        target = self.install_dir / Path(manifest_file.path)
        target.parent.mkdir(parents=True, exist_ok=True)

        if HashVerifier.matches(target, manifest_file.sha256):
            self._log(f"최신 파일 확인: {manifest_file.path}")
            return

        temp_name = quote(manifest_file.path.replace("\\", "/"), safe="") + ".download"
        temp_path = self.download_cache / temp_name
        self._download(manifest_file.url, temp_path)

        if not HashVerifier.matches(temp_path, manifest_file.sha256):
            temp_path.unlink(missing_ok=True)
            target.unlink(missing_ok=True)
            raise DownloadError("SHA-256 검증 실패")

        shutil.move(str(temp_path), str(target))
        self._log(f"업데이트 완료: {manifest_file.path}")

    def _download(self, url: str, target: Path) -> None:
        try:
            with requests.get(url, stream=True, timeout=30) as response:
                response.raise_for_status()
                with target.open("wb") as stream:
                    for chunk in response.iter_content(chunk_size=1024 * 256):
                        if chunk:
                            stream.write(chunk)
        except requests.RequestException as exc:
            target.unlink(missing_ok=True)
            raise DownloadError(f"다운로드 실패: {exc}") from exc
        except OSError as exc:
            target.unlink(missing_ok=True)
            raise DownloadError(f"파일 저장 실패: {exc}") from exc

    def _cleanup_failed(self, manifest_file: ManifestFile) -> None:
        (self.install_dir / Path(manifest_file.path)).unlink(missing_ok=True)

    def _progress(self, current: int, total: int, path: str) -> None:
        if self.progress_callback:
            self.progress_callback(current, total, path)

    def _log(self, message: str) -> None:
        if self.log_callback:
            self.log_callback(message)
