# TCToRPG

Minecraft 1.21.1 + NeoForge 기반 전용 액션 RPG 클라이언트/서버 모드팩입니다. 클라이언트는 HUD, 키 입력, 화면 표시만 담당하고 서버가 직업, 스탯, 스킬, 쿨타임, 장비, 퀘스트, 상점, 이벤트, 저장을 최종 검증합니다.

## 개발 환경

- Java: 21
- Minecraft: 1.21.1
- NeoForge: 21.1.231
- Build: Gradle
- Mod ID: `tctorpg`
- Package: `com.tcto.rpg`
- 기본 서버: `localhost:25565`
- 서버 소유자 닉네임: `stone_0401`

## 빌드와 배포

전체 배포 파일 생성:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\prepare-release.ps1
```

생성 결과:

- `dist/TCToRPG-Client.zip`
- `dist/TCToRPG-Server.zip`
- `TCToRPG-Client/TCToRPG-Client.exe`

## 전용 클라이언트 실행

1. `dist/TCToRPG-Client.zip`을 압축 해제합니다.
2. `TCToRPG-Client.exe`를 실행합니다.
3. 런처가 Prism Launcher 인스턴스 `TCToRPG-Client`를 구성하고 실행합니다.
4. 서버 목록의 `TCToRPG Local Server` 또는 직접 연결로 `localhost:25565`에 접속합니다.

Prism Launcher에서 직접 가져올 때는 `TCToRPG-Client` 폴더를 인스턴스로 import하면 됩니다.

## 전용 서버 실행

1. `dist/TCToRPG-Server.zip`을 서버 PC에 압축 해제합니다.
2. Java 21이 설치되어 있는지 확인합니다.
3. `server/run.bat`을 실행합니다.
4. 기본 포트는 `25565`입니다.

서버 주소 예시:

- 같은 PC 테스트: `localhost:25565`
- 내부망 테스트: `192.168.0.x:25565` 또는 `192.168.1.x:25565`
- 실제 도메인: `play.example.com:25565`
- 기본 포트 `25565`를 쓰면 포트는 생략 가능합니다.

## 키바인드

- `R`: 스킬 슬롯 1
- `Z`: 스킬 슬롯 2
- `X`: 스킬 슬롯 3
- `C`: 스킬 슬롯 4
- `V`: 스킬 슬롯 5
- `G`: 궁극기
- `Left Alt`: 회피
- `K`: 스킬 장착창
- `J`: 스탯/직업창
- `B`: 장비창
- `L`: 퀘스트창
- `O`: 운영자 화면

## HUD

- 왼쪽 하단: HP/MP/ST 바
- 하단 중앙: 스킬 슬롯 1~5 + 궁극기
- 오른쪽: 퀘스트 추적
- 상단: 보스 HP 바
- 중앙 근처: 데미지 숫자
- 상태이상: 아이콘과 남은 시간 표시

## 데이터 구조

내장 데이터:

```text
src/main/resources/data/tctorpg/rpg/
  jobs/
  skills/
  items/
  monsters/
  bosses/
  dungeons/
  quests/
  shops/
  events/
  regions/
  npcs/
  drops/
  exp_table.json
```

외부 콘텐츠 팩:

```text
content_packs/
  default/
    pack.json
    jobs/
    skills/
    items/
    monsters/
    bosses/
    dungeons/
    quests/
    shops/
    events/
```

`default` 팩은 기본 팩이며, `enabled: false`인 팩은 로드 대상에서 제외됩니다. priority가 높은 팩은 나중에 적용되어 override 기준이 됩니다.

## 운영자 권한

`config/tctorpg/operators.json`에서 권한을 관리합니다. `stone_0401`은 닉네임 기준으로 항상 OWNER 권한을 받습니다.

권한 레벨:

- `OWNER`: 모든 기능
- `ADMIN`: reload, validate, player 관리, event, backup
- `DESIGNER`: template, docs, 데이터 설계 기능
- `BALANCER`: 수치 조정용
- `MODERATOR`: 플레이어 상태 확인과 복구
- `TESTER`: 테스트 소환과 테스트 전투

서버는 관리자 GUI 또는 패킷을 신뢰하지 않고, 명령/액션마다 서버에서 권한을 다시 검사합니다. 운영자 액션은 `logs/tctorpg-admin.log`에 기록됩니다.

## 운영자 명령

주요 MVP 명령:

```text
/tctorpg admin
/tctorpg validate [category]
/tctorpg reload [category]
/tctorpg template <kind> <id> [option]
/tctorpg backup create
/tctorpg backup list
/tctorpg docs generate
/tctorpg player info <player>
/tctorpg player setlevel <player> <level>
/tctorpg player addexp <player> <amount>
/tctorpg player setjob <player> <jobId>
/tctorpg player setstat <player> <statId> <amount>
/tctorpg player unlockskill <player> <skillId>
/tctorpg spawnmob <monsterId>
/tctorpg spawnboss <bossId>
/tctorpg event start <eventId>
/tctorpg event stop <eventId>
```

템플릿 예시:

```text
/tctorpg template monster cave_spider ruined_mine
/tctorpg template event warrior_week class_boost
/tctorpg template job dark_knight
/tctorpg template item blood_staff weapon
```

## 리소스 경로

HUD:

```text
src/main/resources/assets/tctorpg/textures/gui/hud/
```

스킬 아이콘:

```text
src/main/resources/assets/tctorpg/textures/gui/skills/
```

화면 배경:

```text
src/main/resources/assets/tctorpg/textures/gui/screens/
```

## 테스트 체크리스트

- `prepare-release.ps1` 빌드 성공
- `server/run.bat` 서버 부팅 성공
- 클라이언트 exe로 Prism 인스턴스 실행
- `localhost:25565` 접속
- HUD 텍스처 표시
- `R/Z/X/C/V/G` 입력 시 슬롯 기반 스킬 요청
- `O` 키로 관리자 화면 표시
- `stone_0401` OWNER 권한 확인
- `/tctorpg validate`
- `/tctorpg reload`
- `/tctorpg backup create`
- `/tctorpg docs generate`

## 현재 구현 범위

1차 MVP 기반 구조가 포함되어 있습니다. 단, NeoForge 1.21.1의 실제 payload 네트워크 등록은 다음 단계에서 정식 구현해야 합니다. 현재 빌드 호환을 위해 네트워크 채널은 호환 래퍼 형태이며, 서버 권한/명령/데이터 도구와 클라이언트 화면 구조를 우선 연결해 둔 상태입니다.
