from dataclasses import dataclass


@dataclass(frozen=True)
class ServerConfig:
    name: str = "TCToRPG"
    host: str = "220.89.226.110"
    port: int = 25565

    @classmethod
    def from_manifest(cls, data: dict | None) -> "ServerConfig":
        if not isinstance(data, dict):
            return cls()
        return cls(
            name=str(data.get("name", "TCToRPG")),
            host=str(data.get("host", "220.89.226.110")),
            port=int(data.get("port", 25565)),
        )

    @property
    def address(self) -> str:
        return f"{self.host}:{self.port}"
