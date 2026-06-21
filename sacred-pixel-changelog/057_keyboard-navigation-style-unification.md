# 057. 키보드 네비게이션 및 스타일 통일

**날짜**: 2026-05-07

## 개요

모든 Window 클래스에 키보드 네비게이션 지원을 추가하고, 유사한 창들 간의 스타일(폰트 크기, 여백)을 통일했습니다.

---

## 변경 사항

### 1. 키보드 네비게이션 확장

### 1.1 기본 인프라 (Window.java)

기존 `Window` 클래스에 이미 구현된 키보드 네비게이션 인프라:
- `ArrayList<Focusable> focusableButtons` - 포커싱 가능한 버튼 목록
- `int focusIndex` - 현재 포커스 인덱스
- `onSignal(KeyEvent)` - 방향키(N/S/E/W/NW/NE/SW/SE) 및 ENTER 처리
- `moveFocus(int direction)` - 포커스 이동 및 시각적 하이라이트
- `activateFocused()` - 포커스된 버튼 활성화

### 1.2 ItemButton에 Focusable 인터페이스 구현

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/ItemButton.java`

```java
public class ItemButton extends Component implements Focusable {
    protected float savedBrightness = 1f;
    protected boolean isFocused = false;

    @Override
    public void setFocused(boolean focused) {
        isFocused = focused;
        if (focused) {
            bg.brightness(1.5f);
        } else {
            bg.resetColor();
        }
    }

    @Override
    public void saveFocusState() { savedBrightness = 1f; }

    @Override
    public void restoreFocusState() {
        bg.resetColor();
        isFocused = false;
    }

    @Override
    public void click() { onClick(); }

    @Override
    public boolean isActive() { return active && visible; }
}
```

### 1.3 키보드 네비게이션이 추가/확인된 Window 클래스들

| 클래스 | 상태 | 변경 내용 |
|--------|------|-----------|
| WndGame | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndOptions | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndUseItem | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndChallenges | 기존 구현 | 체크박스 + 정보 버튼 등록 |
| WndUpgrade | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndChooseSubclass | 리팩토링 | 커스텀 구현 제거, Window 기본 클래스 사용 |
| WndChooseAbility | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndBlacksmith | 수정 | `addFocusable(btnItem1/2)`, WndSmith에 `addFocusable(btnReward)` |
| WndTradeItem | 기존 구현 | 모든 거래 버튼 등록 |
| WndWandmaker | 수정 | `addFocusable(btnWand1/2)` 추가 |
| WndResurrect | 수정 | `addFocusable(btnItem1/2)` 추가 |
| WndSadGhost | 수정 | `addFocusable(btnWeapon/btnArmor)` 추가 |
| WndMonkAbilities | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndClericSpells | 수정 | SpellButton에 `setFocused()` 오버라이드, 주문 상태별 하이라이트 |
| WndHeroInfo | 기존 구현 | `rebuildFocusableButtons()` 탭별 동적 등록 |
| WndEnergizeItem | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndCombo | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndImp | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndTextInput | 기존 구현 | Copy/Paste/확인/취소 버튼 등록 |
| WndSupportPrompt | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndRegionComplete | 리팩토링 | 커스텀 키 리스너 제거, `addFocusableButton()` 사용 |
| WndHardNotification | 수정 | `onConfirm()` 오버라이드로 타이머 체크 |
| WndInfoTalent | 기존 구현 | `addFocusableButton()` 이미 호출 |
| WndGameInProgress | 기존 구현 | challenges/continue/erase 버튼 등록 |
| WndOptionsCondensed | 기존 구현 | WndOptions 상속으로 자동 지원 |

---

## 2. 스타일 통일

### 2.1 폰트 크기 통일

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/DataScene.java`

WndImportPreview 제목 폰트 크기를 WndOptions와 동일하게 변경:
```java
// 변경 전
RenderedTextBlock titleText = PixelScene.renderTextBlock(..., 9);

// 변경 후
RenderedTextBlock titleText = PixelScene.renderTextBlock(..., 8);
```

### 2.2 상단 여백 통일

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndOptions.java`

IconTitle 생성자에 상단 여백 추가:
```java
// 변경 전
float pos = 0;

// 변경 후
float pos = MARGIN;
```

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/DataScene.java`

WndImportPreview 상단 여백 조정:
```java
// 변경 전
titleText.setPos(5, 5);

// 변경 후
titleText.setPos(5, 2);
```

### 2.3 하드코딩된 여백을 MARGIN 상수로 변경

**파일:** `WndClericSpells.java`
```java
// MARGIN 상수 추가
private static final int MARGIN = 2;

// 변경 전
msg.setPos(0, title.bottom()+4);
int top = (int)msg.bottom()+4;

// 변경 후
msg.setPos(0, title.bottom() + 2*MARGIN);
int top = (int)msg.bottom() + 2*MARGIN;
```

**파일:** `WndKeyBindings.java`
```java
// MARGIN 상수 추가
private static final int MARGIN = 2;

// 변경 전
controllerInfo.setPos(0, 2);
y = (int)controllerInfo.bottom()+3;

// 변경 후
controllerInfo.setPos(0, MARGIN);
y = (int)controllerInfo.bottom() + MARGIN + 1;
```

**파일:** `WndTextInput.java`
```java
// 변경 전 (MARGIN = 1 이미 존재)
float pos = 2;
txtTitle.setPos((width - txtTitle.width()) / 2, 2);

// 변경 후
float pos = 2*MARGIN;
txtTitle.setPos((width - txtTitle.width()) / 2, 2*MARGIN);
```

---

## 3. 버그 수정

### 3.1 WndHardNotification 컴파일 오류 수정

**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndHardNotification.java`

`onClick()`이 protected여서 외부에서 호출 불가:
```java
// 변경 전
btnOkay.onClick();

// 변경 후
hide();
```

---

## 4. 키보드 사용법

### 네비게이션
- **방향키** (↑↓←→ 또는 WASD): 버튼 간 포커스 이동
- **ENTER**: 포커스된 버튼 활성화
- **ESC**: 창 닫기

### 시각적 피드백
- **RedButton**: 배경 밝기 증가 (1.3f)
- **IconButton**: 아이콘 밝기 증가 (1.5f)
- **ItemButton**: 배경 밝기 증가 (1.5f)
- **SpellButton**: 주문 상태에 따른 밝기 조절

---

## 5. 테스트 체크리스트

- [ ] WndOptions 계열 창에서 방향키로 버튼 이동
- [ ] WndBlacksmith에서 아이템 버튼 키보드 선택
- [ ] WndWandmaker에서 지팡이 버튼 키보드 선택
- [ ] WndClericSpells에서 주문 버튼 키보드 선택
- [ ] Import 미리보기 창 스타일 확인 (폰트 크기 8, 상단여백 2)
- [ ] Play Again 창 스타일 확인 (상단여백 MARGIN)
- [ ] WndHardNotification ENTER 키 동작 (타이머 만료 후에만 작동)

---

## 수정된 파일

| File | Changes |
|------|---------|
| `ItemButton.java` | Focusable 인터페이스 구현 |
| `WndBlacksmith.java` | ItemButton 포커스 등록 |
| `WndWandmaker.java` | addFocusable 호출 추가 |
| `WndResurrect.java` | addFocusable 호출 추가 |
| `WndSadGhost.java` | addFocusable 호출 추가 |
| `WndClericSpells.java` | MARGIN 상수, SpellButton 포커스 |
| `WndKeyBindings.java` | MARGIN 상수 |
| `WndTextInput.java` | MARGIN 사용 |
| `WndOptions.java` | 상단여백 추가 |
| `WndHardNotification.java` | onConfirm 수정 |
| `WndRegionComplete.java` | 커스텀 키 리스너 제거 |
| `WndChooseSubclass.java` | 커스텀 구현 제거 |
| `DataScene.java` | WndImportPreview 스타일 수정 |

---
