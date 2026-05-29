from __future__ import annotations

import hashlib
from pathlib import Path


class HashVerifier:
    @staticmethod
    def sha256(path: Path) -> str:
        digest = hashlib.sha256()
        with path.open("rb") as stream:
            for chunk in iter(lambda: stream.read(1024 * 1024), b""):
                digest.update(chunk)
        return digest.hexdigest()

    @classmethod
    def matches(cls, path: Path, expected_sha256: str) -> bool:
        if not path.exists() or not expected_sha256:
            return False
        return cls.sha256(path).lower() == expected_sha256.lower()
