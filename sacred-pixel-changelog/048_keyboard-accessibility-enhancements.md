# 048. 키보드 접근성 확장 (입력 차단 및 포커스 개선)

**날짜**: 2026-04-18

## 개요

추가 키보드 접근성 기능 구현: OptionSlider 키보드 조정, WndChallenges 키보드 탐색, ScrollPane 방향키 스크롤. 새로운 Focusable 인터페이스 도입으로 Button 외의 컴포넌트도 포커스 시스템에 참여 가능.

---

## 변경 사항

### Core System Changes

### 1. Focusable Interface (`Focusable.java`) - NEW

UI 컴포넌트가 키보드 포커스를 받을 수 있도록 하는 새 인터페이스 추가:

```java
public interface Focusable {
    void setFocused(boolean focused);
    void saveFocusState();
    void restoreFocusState();
    void click();
    boolean isActive();
}
```

**구현 클래스:**
- `Button` - 기존 포커스 메서드에 `isActive()` 추가
- `OptionSlider` - 슬라이더 값 조정을 위한 포커스 지원
- `ScrollPane` - 방향키 스크롤을 위한 포커스 지원

### 2. Window.java - Focusable 지원 확장

`focusableButtons`를 `ArrayList<Button>`에서 `ArrayList<Focusable>`로 변경하여 Button 외의 컴포넌트도 포커스 시스템에 참여할 수 있도록 함:

```java
protected ArrayList<Focusable> focusableButtons = new ArrayList<>();

// 기존 호환성 유지
public void addFocusableButton(Button btn) { ... }

// 새 메서드
public void addFocusable(Focusable focusable) { ... }
```

---

### Feature Implementations

#### OptionSlider 키보드 조정 (`OptionSlider.java`)

**기능:**
- 포커스 상태일 때 좌/우 방향키(A/D, ←/→)로 슬라이더 값 조정
- 포커스 시 시각적 피드백: 슬라이더 노드 밝기 1.5배, 배경 밝기 1.2배

**구현:**
- `Focusable` 인터페이스 구현
- 자체 `KeyEvent` 리스너로 방향키 처리
- `setFocused()`, `saveFocusState()`, `restoreFocusState()` 메서드 추가

**WndSettings 통합:**
- Display 탭: Brightness, Visual Grid, Camera Follow, Screen Shake 슬라이더
- UI 탭: UI Mode, UI Scale 슬라이더
- Audio 탭: Music Volume, SFX Volume 슬라이더

#### WndChallenges 키보드 탐색 (`WndChallenges.java`)

**기능:**
- 방향키로 도전과제 CheckBox와 Info 버튼 간 이동
- Enter 키로 CheckBox 토글 또는 Info 창 열기

**구현:**
- `infoButtons` ArrayList 추가로 IconButton 참조 저장
- `rebuildFocusableButtons()` 메서드 추가
- CheckBox와 Info 버튼을 쌍으로 등록 (checkbox, info, checkbox, info, ...)

#### ScrollPane 방향키 스크롤 (`ScrollPane.java`)

**기능:**
- 포커스 상태일 때 상/하 방향키(W/S, ↑/↓)로 스크롤
- ZOOM_IN/ZOOM_OUT 키는 포커스 상태와 무관하게 항상 동작 (기존 호환성)

**구현:**
- `Focusable` 인터페이스 구현
- 포커스 상태일 때만 방향키 스크롤 활성화
- `isActive()`: 스크롤 가능한 경우(content.height > height)에만 true 반환
- 포커스 시 썸 스크롤바 불투명도 증가 (0.5 → 1.0)

---

## Key Bindings Summary

### OptionSlider (포커스 상태)
| 키 | 동작 |
|----|------|
| A / ← / Numpad 4 | 값 감소 |
| D / → / Numpad 6 | 값 증가 |

### ScrollPane (포커스 상태)
| 키 | 동작 |
|----|------|
| W / ↑ / Numpad 8 | 위로 스크롤 |
| S / ↓ / Numpad 2 | 아래로 스크롤 |
| +/- (항상) | 스크롤 (기존 동작) |

### WndChallenges
| 키 | 동작 |
|----|------|
| W/S / ↑/↓ | 항목 간 이동 |
| Enter | CheckBox 토글 / Info 버튼 클릭 |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../ui/Focusable.java` | NEW - Focusable 인터페이스 정의 |
| `core/.../ui/Button.java` | isActive() 추가 |
| `core/.../ui/OptionSlider.java` | Focusable 구현, 방향키 처리 |
| `core/.../ui/ScrollPane.java` | Focusable 구현, 방향키 스크롤 |
| `core/.../ui/TalentsPane.java` | saveFocusState → saveTalentFocusState |
| `core/.../ui/Window.java` | ArrayList<Button> → ArrayList<Focusable> |
| `core/.../windows/WndHero.java` | 메서드 호출 업데이트 |
| `core/.../windows/WndSettings.java` | 슬라이더 포커스 등록 |
| `core/.../windows/WndChallenges.java` | 체크박스/정보 버튼 포커스 등록 |

---

### Architecture Notes

### Focusable Interface Design

`Focusable` 인터페이스는 Button 외의 컴포넌트도 Window의 포커스 시스템에 참여할 수 있게 합니다:

1. **Button**: 기존 포커스 시스템 그대로 사용
2. **OptionSlider**: 포커스 시 방향키로 값 조정
3. **ScrollPane**: 포커스 시 방향키로 스크롤

이 설계는 향후 다른 컴포넌트(예: ColorPicker, RangeSelector 등)도 쉽게 포커스 시스템에 통합할 수 있게 합니다.

### Keyboard Event Priority

키보드 이벤트는 등록 역순으로 처리됩니다 (스택 방식):
1. 컴포넌트 리스너 (OptionSlider, ScrollPane)
2. Window 리스너

컴포넌트가 이벤트를 소비하면(`return true`) Window로 전파되지 않습니다. 이를 통해 포커스된 OptionSlider의 좌/우 키가 Window의 포커스 이동과 충돌하지 않습니다.
