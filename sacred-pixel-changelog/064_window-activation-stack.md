# 064. Window 활성화 스택 및 UI 차단 시스템

**날짜**: 2026-05-23

## 개요

Window 계층 구조에서 발생하는 입력 버그를 수정하고, HeroSelectScene의 옵션 패널 동작을 개선했습니다. (버전: 3.3.8-HTML5-indev)

---

## 변경 사항

### 수정된 버그

### 1. CellSelector 드래그 차단 (인게임)
- **문제**: WndHero/WndHeroInfo 등 창이 열려있을 때 인게임 카메라 드래그 가능
- **해결**: `onPointerDown()`, `onDrag()`에 `GameScene.interfaceBlockingHero()` 체크 추가
- **파일**: `CellSelector.java:198-203, 247-250`

### 2. Window 활성화 스택 구현
- **문제**:
  - 텐구의 가면 직업선택 창에서 info 열면 뒤쪽 창 여전히 클릭 가능
  - 드워프 왕관 갑옷 능력 선택 창에서도 동일 문제
  - 가방창에서 아이템 창 열려있는데 뒤쪽 가방 클릭 가능
- **해결**:
  - `Window.onAddedToScene()`: 새 창 열릴 때 다른 모든 창 비활성화
  - `Window.hide()`: 창 닫힐 때 이전 최상위 창 재활성화
- **파일**: `Window.java:179-202, 274-293`

### 3. HeroSelectScene 버튼 차단
- **문제**: 옵션 패널이 열려있을 때 바깥 버튼(시작, 영웅선택, 나가기) 클릭 가능
- **해결**: 모든 버튼에 `hasOpenWindows()` 및 `optionsPane.visible` 체크 추가
- **파일**: `HeroSelectScene.java:179-180, 202-203, 320-323, 340, 835, 853, 886`

### 4. 옵션 패널 바깥 클릭 시 닫기
- **문제**: 옵션 패널이 열려있을 때 바깥 클릭 시 버튼 이펙트만 나오고 패널이 닫히지 않음
- **해결**: `optionsPaneBlocker` PointerArea 추가 - 바깥 클릭 시 패널 자동 닫힘
- **파일**: `HeroSelectScene.java:228-252, 265, 662`

### 5. Test Level 버튼 표시 조건 수정
- **문제**: 버전명에 "indev"가 있어도 Test Level 버튼이 안 나타남
- **해결**: `DeviceCompat.isDebug()` 외에 `Game.version.contains("INDEV")` 조건 추가
- **파일**: `TitleScene.java:271`

### 6. 버전명 동기화
- **문제**: `build.gradle`과 `TeaVMLauncher.java`의 버전명 불일치
- **해결**: `TeaVMLauncher.java`에 "-indev" 추가
- **파일**: `TeaVMLauncher.java:58`

---

## 기술적 세부사항

### Window 활성화 스택 메커니즘

```
┌─────────────────────────────────────────┐
│  Scene                                  │
│  ├─ Window A (active=false)             │
│  │   └─ blocker (inactive)              │
│  └─ Window B (active=true) ← 최상위     │
│       └─ blocker (active, priority)     │
└─────────────────────────────────────────┘

Window B가 닫히면:
1. hide()에서 parent.erase() 호출 전에 Window A 재활성화
2. Window A의 blocker priority 복원
3. Window A가 정상적으로 클릭 이벤트 수신
```

### optionsPaneBlocker 동작

```
┌─────────────────────────────────────────┐
│  HeroSelectScene                        │
│  ├─ heroBtns, startBtn, etc.            │
│  ├─ optionsPaneBlocker (전체 화면)      │ ← 패널 열릴 때 active
│  └─ optionsPane (옵션 패널)             │
└─────────────────────────────────────────┘

바깥 클릭 시:
1. optionsPaneBlocker가 클릭 이벤트 수신
2. 클릭 좌표가 optionsPane 영역 밖인지 확인
3. btnOptions 위치가 아니면 패널 닫기
```

---

## 영향 받는 모든 Window

Window.java의 수정이 모든 Window 서브클래스에 자동 적용됩니다:
- WndBag, WndUseItem, WndInfoItem
- WndHero, WndHeroInfo
- WndChooseSubclass, WndInfoSubclass
- WndChooseAbility, WndInfoArmorAbility
- WndJournal, WndDocument, WndStory
- WndSettings, WndKeyBindings
- WndRanking, WndChallenges
- 기타 모든 Window 서브클래스

모든 서브클래스가 `super.hide()` 또는 `super.destroy()`를 호출하므로 재활성화 로직이 정상 작동합니다.

---

## 테스트 체크리스트

- [x] WndHero 열고 카메라 드래그 시도 → 차단됨
- [x] WndHeroInfo 열고 닫으면 WndHero 정상 작동
- [x] WndChooseSubclass → info 버튼 → 뒤쪽 창 클릭 안됨
- [x] WndChooseAbility → info 버튼 → 뒤쪽 창 클릭 안됨
- [x] WndBag → 아이템 클릭 → WndUseItem → 뒤쪽 WndBag 클릭 안됨
- [x] HeroSelectScene 옵션 패널 열림 → 바깥 클릭 → 패널 닫힘
- [x] HeroSelectScene 옵션 패널 열림 → 영웅 버튼/시작 버튼 클릭 → 패널만 닫힘 (버튼 실행 안됨)
- [x] 타이틀 화면에서 버전 "v3.3.8-HTML5-indev" 표시
- [x] 타이틀 화면에서 Test Level 버튼 표시

---

## 수정된 파일

| File | Changes |
|------|---------|
| `CellSelector.java` | onPointerDown(), onDrag()에 창 체크 추가 |
| `Window.java` | onAddedToScene(), hide() 수정 |
| `HeroSelectScene.java` | optionsPaneBlocker 추가, 버튼 체크 추가 |
| `TitleScene.java` | Test Level 버튼 조건 수정 |
| `TeaVMLauncher.java` | 버전명 indev 추가 |
| `build.gradle` | 버전명 indev 추가 |

---
