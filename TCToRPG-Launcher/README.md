# TCToRPG Launcher

Windows용 TCToRPG 전용 Minecraft 런처 MVP입니다. Microsoft 로그인, Minecraft 원본 파일 다운로드, Java 자동 설치, Fabric 자동 설치는 포함하지 않습니다. 이 런처는 개발자가 배포한 모드, 리소스팩, 설정 파일을 SHA-256으로 검증해 설치 폴더에 배치하고 Minecraft Launcher 실행을 보조합니다.

## 기본 서버

- 서버 이름: TCToRPG
- 주소: `220.89.226.110:25565`
- 포트포워딩 대상 포트: `25565`
- 현재 WAN은 동적 IP 환경이므로 장기 운영 시 DDNS 사용을 권장합니다.

## 개발 환경

```bat
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

요구 패키지:

- PyQt6
- requests
- packaging

## 실행

```bat
python launcher.py
```

최초 실행 시 `launcher-config.json`이 생성됩니다.

```json
{
  "installDir": "%USERPROFILE%/TCToRPG",
  "minecraftLauncherPath": "",
  "manifestUrl": "https://example.com/tctorpg/update-manifest.json",
  "serverHost": "220.89.226.110",
  "serverPort": 25565,
  "lastClientVersion": "0.0.0"
}
```

## Manifest 작성

`sample/update-manifest.json`을 기준으로 작성합니다. `files[].path`는 설치 폴더 기준 상대 경로입니다.

예:

```json
{
  "path": "mods/tctorpg-client.jar",
  "url": "https://example.com/tctorpg/mods/tctorpg-client.jar",
  "sha256": "PUT_SHA256_HERE",
  "required": true
}
```

실제 저장 위치는 `%USERPROFILE%/TCToRPG/mods/tctorpg-client.jar`입니다.

## SHA-256 계산

PowerShell:

```powershell
Get-FileHash .\mods\tctorpg-client.jar -Algorithm SHA256
```

Python:

```bat
python -c "import hashlib, pathlib; p=pathlib.Path('mods/tctorpg-client.jar'); print(hashlib.sha256(p.read_bytes()).hexdigest())"
```

## 업데이트 배포 방법

1. 배포 서버에 모드, 리소스팩, 설정 파일을 업로드합니다.
2. 각 파일의 SHA-256을 계산합니다.
3. `update-manifest.json`의 `url`, `sha256`, `clientVersion`을 갱신합니다.
4. 런처의 `launcher-config.json`에서 `manifestUrl`을 실제 manifest URL로 변경합니다.
5. 플레이어는 런처에서 “업데이트 확인” 후 “업데이트 실행”을 누릅니다.

## 서버 주소 변경

`launcher-config.json` 또는 원격 manifest의 server 값을 변경합니다.

```json
"server": {
  "name": "TCToRPG",
  "host": "220.89.226.110",
  "port": 25565
}
```

DDNS를 쓰게 되면 `"host": "tctorpg.example.com"`처럼 바꾸면 됩니다.

## exe 빌드

```bat
build.bat
```

결과물:

```text
dist/TCToRPG-Launcher.exe
```

## 현재 MVP 범위

- 원격 manifest 다운로드
- 파일 존재 및 SHA-256 검사
- 누락/불일치 파일 다운로드
- 임시 다운로드 경로 `cache/downloads`
- 설치 폴더 자동 생성
- 로그 파일 `logs/launcher.log`
- 서버 주소 표시와 클립보드 복사
- Minecraft Launcher 실행 보조
- `launcher-config.json` 저장

Minecraft 자동 로그인 및 직접 서버 접속은 후속 단계에서 Minecraft 실행 인자 또는 전용 모드 연동으로 확장할 수 있습니다.
