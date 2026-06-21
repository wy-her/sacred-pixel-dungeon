# 011. 키바인딩, 셀 타겟팅, 포커스 시스템 통합

**날짜**: 2026-03-20

## 개요

기본 키바인딩 전면 변경(퀵슬롯, 카메라 조작, 전투/행동 키, 인벤토리/가방 키), 카메라 연속 이동 버그 수정, 통합 셀 포커스(타겟팅) 시스템, 버튼 포커스 시스템 통합, 다국어 번역 추가.

---

## 변경 사항

### 기본 키바인딩 변경

### 퀵슬롯 기본 바인딩 추가
- 숫자행 `1`~`6` → 퀵슬롯 1~6

### 카메라 조작 키 전면 재배치 (키보드 우측 배열)
```
8(NW)  9(N)  0(NE)
I(W)   O(영웅) P(E)
K(SW)  L(S)  ;(SE)
```
- 카메라 북쪽: `9`, 서쪽: `I`, 남쪽: `L`, 동쪽: `P`
- 카메라 북서: `8`, 북동: `0`, 남서: `K`, 남동: `;`
- 카메라 영웅 위치 이동: `O` (신규 액션 `CAMERA_HERO`)
- 카메라 대각선 이동 항목 4개 숨김 해제 및 UI 표시

### 전투/행동 키 변경
| 항목 | 새 키 | 이전 키 |
|---|---|---|
| 아이템 집기 | Y | B |
| 특수 행동 | G | V |
| 다음 타겟/다음 탭 | V | G |

### 인벤토리/가방 키 변경
| 항목 | 새 키 | 이전 키 |
|---|---|---|
| 인벤토리 | B | I |
| 1~5번째 가방 | N, M, `,`, `.`, `/` | O, P, `[`, `]`, `\` |
| 배낭 선택 | `[` | K |
| 퀵슬롯 선택 | `]` | L |

### 이동 키 3번째 바인딩 추가
- 방향키 UP/LEFT/DOWN/RIGHT → N/W/S/E (3번째 바인딩)

### 포커스 이동 항목 제거
- 기본키 설정 UI에서 포커스 이동 4개 항목(FOCUS_UP/DOWN/LEFT/RIGHT) 숨김
- 방향키가 이동키 3번째 바인딩으로 통합되어 불필요

### 키바인딩 UI 순서 변경
- 조사/탐색 → 휴식 → 아이템 집기 → 적 공격 → 특수 행동 → 다음 타겟/다음 탭

### 한국어 라벨 수정
- `windows.wndkeybindings.rest`: "대기" → "휴식"

---

## 카메라 연속 이동 버그 수정

### CellSelector.java
- `update()`의 조기 리턴 조건에 `heldCameraAction == SPDAction.NONE` 체크 추가
- 카메라 키만 누르고 있을 때도 연속 패닝 코드에 도달하도록 수정

### 카메라 대각선 및 영웅 위치 이동 지원
- `isCameraPanAction()`: 대각선 4방향 + `CAMERA_HERO` 추가
- `handleCameraPan()`: 대각선 dx/dy 처리, `CAMERA_HERO`는 `camera.panTo(hero.sprite.center())` 호출

---

## 통합 셀 포커스(타겟팅) 시스템

### 기존 문제
- 적 타겟팅 시 QuickSlotButton의 `crossM`(맵 크로스헤어)과 CellSelector의 키보드 커서가 별도로 작동
- 노란색 반투명 오버레이(`ColorBlock`)가 적 타겟팅의 `Icons.TARGET` 크로스헤어와 다른 비주얼

### 통합 변경
- **비주얼 통합**: 노란색 `ColorBlock` → `Icons.TARGET` 크로스헤어 아이콘으로 교체
- **QuickSlotButton `crossM` 제거**: 맵 위 별도 크로스헤어 표시 제거, 버튼 위 `crossB`만 유지
- **자동 초기화**: `GameScene.selectCell()` 호출 시 자동으로 커서 생성
  - 적이 보이면 → 적 위치에서 시작
  - 적이 없으면 → 영웅 위치에서 시작
- **적 스프라이트 추적**: 커서가 적 위치 셀에 있을 때 적 스프라이트 중심에 표시, `update()`에서 매 프레임 추적
- **커서 z-order**: `parent.addToFront()` 사용하여 항상 최상위 렌더링

### 포커스 이동
- **이동 키**: 영웅 이동키(WASD + 대각선)로 8방향 이동
- **적 순환**: V키(CYCLE)로 다음 보이는 적으로 포커스 전환
  - CellSelector의 `cycleTargetEnemy()`에서 처리
  - DangerIndicator의 `onClick()`에서도 `GameScene.moveCellSelectorCursorTo()` 호출하여 이중 안전장치

### 확인 방법
- **Enter 키**: 모든 셀 선택 상황에서 기본 확인 키
- **퀵슬롯 재입력**: 퀵슬롯으로 시작한 타겟팅에서 같은 퀵슬롯 키로 확인 가능
  - CellSelector의 `isQuickslotAction()` 체크
  - QuickSlotButton의 `onClick()`에서 `GameScene.confirmCellSelector()` 호출 (이중 안전장치)

### 커서 정리
- `select()` 호출 시 `hideKeyboardCursor()` 자동 호출
- `cancel()` 호출 시에도 커서 숨김
- 던진 후 커서가 맵에 잔존하는 버그 수정

### 적용 범위
- 아이템 투척, 마법막대 발사, 투척무기 발사 등 `GameScene.selectCell()`을 사용하는 모든 셀 선택 상황

---

## 버튼 포커스 시스템 통합

### Window.java
- 버튼 포커스 이동: FOCUS_UP/DOWN/LEFT/RIGHT → 영웅 이동키(N/NW/W/SW → 이전, S/SE/E/NE → 다음)

### WndBag.java
- 인벤토리 슬롯 포커스: FOCUS_* → 이동키(N계열 → 위, S계열 → 아래, W → 왼쪽, E → 오른쪽)

---

## 아이템 버튼 색상 변경

### WndUseItem.java
- 기본 액션 버튼의 초기 노란색(`TITLE_COLOR`) 하이라이트 제거
- 모든 버튼이 미포커스 시 흰색, 포커스된 버튼만 노란색

---

## 공개 API 추가

### GameScene.java
- `confirmCellSelector()`: 외부에서 셀 선택 확인 (QuickSlotButton 등에서 호출)
- `moveCellSelectorCursorTo(int cell)`: 외부에서 커서 위치 이동 (DangerIndicator 등에서 호출)

### CellSelector.java
- `confirmKeyboardCursor()`: private → public
- `moveCursorTo(int cell)`: 신규 public 메서드

---

## 다국어 번역

### 신규 항목 (22개 언어 전체)
- `camera_up_left`, `camera_up_right`, `camera_down_left`, `camera_down_right`, `camera_hero`
- 지원 언어: en, ko, ja, zh, zh-hant, de, fr, es, pt, ru, it, pl, tr, be, cs, el, eo, hu, in, nl, sv, uk, vi

---

## 수정 파일 목록

| 파일 | 변경 내용 |
|---|---|
| `SPDAction.java` | 액션 순서 변경, CAMERA_HERO 추가, 기본 바인딩 전면 변경, 방향키를 이동키 3번째 바인딩으로 |
| `CellSelector.java` | 통합 셀 포커스 시스템, Icons.TARGET 커서, 적 스프라이트 추적, CYCLE/퀵슬롯 처리 |
| `GameScene.java` | selectCell에서 커서 자동 초기화, confirmCellSelector/moveCellSelectorCursorTo API |
| `QuickSlotButton.java` | crossM 맵 표시 제거, confirmCellSelector 연동 |
| `DangerIndicator.java` | onClick에서 moveCellSelectorCursorTo 호출 |
| `WndKeyBindings.java` | 카메라 대각선 숨김 해제, FOCUS 항목 숨김 |
| `WndUseItem.java` | 기본 액션 노란색 하이라이트 제거 |
| `Window.java` | 버튼 포커스를 이동키로 전환 |
| `WndBag.java` | 인벤토리 포커스를 이동키로 전환 |
| `windows.properties` | 신규 라벨 5개 |
| `windows_ko.properties` | 신규 라벨 5개 + rest 라벨 수정 |
| `windows_*.properties` (20개) | 신규 라벨 5개 번역 |
