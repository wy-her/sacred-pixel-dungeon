# 010. 조작키 설정 전면 개편 및 키보드 UX 강화

**날짜**: 2026-03-19

## 개요

조작키 설정 UI 전면 개편, 기본 키 바인딩 재설계, 키보드 기반 UI 탐색 시스템 구축, 인벤토리 키보드 선택, 타겟팅 커서 키보드 조작, 퀵슬롯 시각적 순번 수정, 설정 슬라이더 UI 겹침 해결, 다국어 번역 추가.

---

## 수정된 파일

### Java 소스

| 파일 | 변경 내용 |
|------|-----------|
| `core/.../SPDAction.java` | 액션 필드 순서 재배치, 기본 키 바인딩 전면 교체, FOCUS_UP/DOWN/LEFT/RIGHT 액션 추가 |
| `core/.../ui/Window.java` | 액션 기반 포커스 내비게이션 (UP/DOWN → FOCUS_UP/DOWN, LEFT/RIGHT 추가) |
| `core/.../windows/WndKeyBindings.java` | WAIT, 카메라 대각선 액션을 키 설정 UI에서 숨김 |
| `core/.../scenes/CellSelector.java` | 카메라 연속 패닝, 카메라 대각선 제거, 키보드 타겟팅 커서 시스템 추가 |
| `core/.../scenes/GameScene.java` | `defaultCellListener` 접근 제한자 `private` → `public` |
| `core/.../ui/Toolbar.java` | 퀵슬롯 좌→우 순번 고정 (툴바 뒤집기 시에도 내부 순서 유지) |
| `core/.../windows/WndBag.java` | 인벤토리 키보드 탐색 (화살표 이동, Enter 선택, 하이라이트) |
| `core/.../ui/OptionSlider.java` | 설정 바 제목 2줄 영역 확보, 수치 값 겹침 방지 |

### 리소스 (.properties)

| 파일 | 변경 내용 |
|------|-----------|
| `windows.properties` (영어) | 카메라 방향명 North/South/West/East, 대각선 제거, focus 항목 추가 |
| `windows_ko.properties` (한국어) | 카메라 방향명 북쪽/남쪽/서쪽/동쪽, 대각선 제거, 포커스 항목 추가 |
| `windows_*.properties` (21개 언어) | 카메라 방향명 + 포커스 항목 번역 추가 (be, cs, de, el, eo, es, fr, hu, in, it, ja, nl, pl, pt, ru, sv, tr, uk, vi, zh, zh-hant) |

---

## 변경 사항

### 1. 기본 조작키 전면 재설계 (`SPDAction.java`)

**이전 → 이후 기본 키 바인딩:**

| 행동 | 이전 키 | 변경 후 키 |
|------|---------|-----------|
| 북쪽 이동 | W, ↑, Numpad8 | W, Numpad8 |
| 서쪽 이동 | A, ←, Numpad4 | A, Numpad4 |
| 남쪽 이동 | S, ↓, Numpad2 | **X**, Numpad2 |
| 동쪽 이동 | D, →, Numpad6 | D, Numpad6 |
| 북서쪽 이동 | Numpad7 | **Q**, Numpad7 |
| 북동쪽 이동 | Numpad9 | **E**, Numpad9 |
| 남서쪽 이동 | Numpad1 | **Z**, Numpad1 |
| 남동쪽 이동 | Numpad3 | **C**, Numpad3 |
| 대기/아이템 획득 | Space, Numpad5 | **S**, Numpad5 |
| 인벤토리 | F, I | **I** |
| 1~5번째 가방 | F1~F5 | **O, P, [, ], \\** |
| 조사, 탐색 | E | **R** |
| 대기 (REST) | Z | **T** |
| 적 공격 | Q | **F** |
| 다음 타겟/탭 | Tab | **G** |
| 특수 행동 | X | **V** |
| 아이템 집기 | C, Enter | **B** |
| 행동 계속하기 | R | **Space** |
| 배낭 선택 | *(없음)* | **K** |
| 퀵슬롯 선택 | *(없음)* | **L** |
| 카메라 북/서/남/동 | F6/F8/F7/F9 | **8/7/9/0** |
| 퀵슬롯 1~6 | 1~6 | *(기본 해제)* |
| 화살표 ↑↓←→ | 이동 | **포커스 이동** |
| Enter | 아이템 집기 | **UI 확인/선택** |

**핵심 변경 원칙:**
- WASD + QEZC로 8방향 이동 (S는 대기/획득으로 이동)
- 화살표 키는 이동에서 해제 → UI 포커스 전용
- Enter는 게임 액션에서 해제 → UI 확인/대화 진행 전용
- 숫자 행 키(7890)를 카메라 이동에 배정

### 2. 액션 표시 순서 재배치 (`SPDAction.java`)

키 설정 화면의 행동 표시 순서를 논리적으로 재구성:

```
이동 (N, W, S, E, NW, NE, SW, SE, 대기/획득)
  ↓
인벤토리 & 가방 (인벤토리, 1~5번째 가방)
  ↓
조사 & 대기 (조사/탐색, 대기)
  ↓
전투 (적 공격, 다음 타겟, 특수 행동, 아이템 집기, 행동 계속)
  ↓
정보 (영웅 정보, 일지)
  ↓
선택 (배낭 선택, 퀵슬롯 선택)
  ↓
포커스 이동 (위/아래/왼쪽/오른쪽)
  ↓
줌 & 카메라 (확대, 축소, 카메라 북/서/남/동)
```

**숨겨진 항목:** WAIT (REST와 중복 레이블), 카메라 대각선 4종

### 3. 키보드 포커스 내비게이션 시스템 (`Window.java`, `SPDAction.java`)

모든 윈도우에서 키보드로 버튼을 탐색하고 선택할 수 있는 시스템:

- **새 액션 4개:** `FOCUS_UP` (↑), `FOCUS_DOWN` (↓), `FOCUS_LEFT` (←), `FOCUS_RIGHT` (→)
- **Window.onSignal:** 기존 raw 키코드 체크(UP/DOWN)를 액션 기반으로 전환
- **LEFT/RIGHT:** FOCUS_LEFT = 이전 버튼, FOCUS_RIGHT = 다음 버튼
- **Enter:** 포커스된 버튼 활성화 (기존 동작 유지)
- 키 설정 UI에서 바인딩 변경 가능

### 4. 인벤토리 키보드 탐색 (`WndBag.java`)

인벤토리 창에서 키보드로 아이템을 탐색하고 선택:

- **화살표 키:** 5열 그리드에서 상하좌우 이동 (순환 탐색)
- **Enter:** 포커스된 아이템 선택 (사용/장착/선택기 등)
- **시각 피드백:** 반투명 노란색 하이라이트 (`0x44FFFF44`)
- `inventorySlots` / `inventoryItems` 리스트로 슬롯-아이템 매핑 추적

### 5. 키보드 타겟팅 커서 (`CellSelector.java`)

아이템 투척 등 셀 선택이 필요한 상황에서 키보드로 타겟 지정:

- **타겟팅 모드 감지:** `isTargeting()` — listener가 defaultCellListener가 아닐 때
- **커서 이동:** 이동 키(WASD/QEZC/Numpad)로 게임 맵상 커서 이동
- **커서 확정:** Enter 또는 S(WAIT_OR_PICKUP)로 타겟 셀 확정
- **자동 시작 위치:** 영웅 현재 위치에서 시작
- **시각 피드백:** 반투명 노란색 블록 (`0x44FFFF00`, 16×16px)
- **카메라 추적:** 커서 이동 시 카메라가 커서 위치로 패닝
- **cancel() 시 자동 숨김:** 타겟팅 취소 시 커서 제거

### 6. 카메라 연속 이동 (`CellSelector.java`)

카메라 이동 키를 누르고 있으면 연속으로 패닝:

- `heldCameraAction` / `cameraPanDelay` 필드 추가
- 키 누름 → 즉시 1회 패닝 + initialDelay 대기
- 키 홀드 → 0.05초 간격으로 반복 패닝 (약 20회/초)
- 키 해제 → 즉시 정지
- `resetKeyHold()` 시 카메라 홀드도 초기화

### 7. 카메라 대각선 제거 (`CellSelector.java`, `WndKeyBindings.java`)

- `isCameraPanAction()`: UP/DOWN/LEFT/RIGHT만 체크 (대각선 제거)
- `handleCameraPan()`: 대각선 분기 제거, 4방향만 처리
- `WndKeyBindings`: 대각선 액션 5개 필터링 (키 설정 UI에서 숨김)
- 액션 정의 자체는 저장 호환성을 위해 유지

### 8. 카메라 방향명 변경 (`.properties` 23개 파일)

| 이전 | 이후 (한국어) | 이후 (영어) |
|------|-------------|------------|
| 카메라 위로 이동 | 카메라 북쪽 이동 | Pan Camera North |
| 카메라 아래로 이동 | 카메라 남쪽 이동 | Pan Camera South |
| 카메라 왼쪽으로 이동 | 카메라 서쪽 이동 | Pan Camera West |
| 카메라 오른쪽으로 이동 | 카메라 동쪽 이동 | Pan Camera East |

모든 21개 번역 언어에도 방향명 업데이트 적용.

### 9. 퀵슬롯 좌→우 순번 고정 (`Toolbar.java`)

**문제:** 툴바 뒤집기(flip) 설정에 따라 퀵슬롯 1번이 오른쪽에 나타남

**수정:**
- 퀵슬롯 배치 순서를 항상 **좌측부터 1, 2, 3, 4** 로 고정
- `endingSlot`을 인벤토리 버튼 옆(우측)에, `startingSlot`을 가장 왼쪽에 배치
- 툴바 뒤집기 시: 개별 미러링 대신 **그룹 전체를 이동** (내부 순서 유지)
- SPLIT, GROUP, CENTER 세 가지 모드 모두 수정
- 보더/프레임: `startingSlot` = 왼쪽 끝 프레임, `endingSlot` = 오른쪽 끝 프레임

### 10. 설정 슬라이더 UI 겹침 수정 (`OptionSlider.java`)

**문제:** 긴 제목의 언어에서 설정 항목 제목과 바 양쪽 수치 값이 겹침

**수정:**
- `title.maxWidth`를 `width - 4`로 설정 (전체 너비 활용, 줄바꿈 허용)
- `titleAreaBottom` 계산: `y + 2 + max(title.height(), 14)` (최소 2줄 높이 확보)
- `minTxt` / `maxTxt`의 Y 좌표: `max(titleAreaBottom + 1, 기존 위치)`
- 제목이 2줄이어도 수치 값과 겹치지 않음

### 11. 다국어 번역 추가 (21개 언어)

새로 추가된 키 설정 항목에 대한 번역:

| 항목 | 한국어 | 영어 |
|------|--------|------|
| focus_up | 위쪽 포커스 이동 | Focus Up |
| focus_down | 아래쪽 포커스 이동 | Focus Down |
| focus_left | 왼쪽 포커스 이동 | Focus Left |
| focus_right | 오른쪽 포커스 이동 | Focus Right |

번역된 언어: be, cs, de, el, eo, es, fr, hu, in, it, ja, ko, nl, pl, pt, ru, sv, tr, uk, vi, zh, zh-hant

---

## 기술적 고려사항

### 저장 호환성
- 액션 필드 재배치로 `GameAction.code()` 값 변경됨
- 키 바인딩 저장/로드는 `a.name()` 기반이므로 호환성 유지
- 숨겨진 액션(WAIT, 카메라 대각선)도 정의 유지 → 기존 사용자 설정 손실 없음

### GWT/HTML5 호환성
- 새 액션은 `SPDAction` 정적 초기화 → GWT 자동 처리
- `GameAction`은 Bundlable이 아니므로 GwtClassRegistry 변경 불필요

### 키 충돌 방지
- ENTER: `defaultBindings`에서 제거, `hardBinding(ENTER, NONE)` 유지
- 화살표 키: 이동에서 제거, FOCUS 액션에 배정
- Window.onSignal에서 ENTER는 raw 키코드로 체크 (Alt+Enter 풀스크린과 공존)

---

## 빌드 검증

```
core:compileJava    — BUILD SUCCESSFUL
desktop:compileJava — BUILD SUCCESSFUL
```
