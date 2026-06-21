# 키보드 접근성

**날짜**: 2026-06-04

## 개요

마우스 없이 키보드만으로 게임의 주요 UI를 조작할 수 있도록 키보드 접근성 기능을 구현했습니다. 타이틀 화면, 영웅 선택, 메뉴, 특성, 다양한 게임 창의 네비게이션을 지원합니다.

---

## 변경 사항

### 1. Tab 키 바인딩
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/SPDAction.java`

- Tab 키를 `CYCLE` 액션에 바인딩 추가
- 기존 Space, Numpad 0과 함께 Tab으로도 UI 요소 순환 가능

```java
defaultBindings.put( Input.Keys.TAB, SPDAction.CYCLE );
```

### 2. Button 클래스 - click() 메서드
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/Button.java`

- 키보드 네비게이션에서 프로그래밍 방식으로 버튼 클릭을 트리거할 수 있는 public `click()` 메서드 추가

```java
// Public method to programmatically trigger a click (for keyboard navigation)
public void click() {
    onClick();
}
```

### 3. TitleScene 키보드 네비게이션
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/TitleScene.java`

- 방향키(W/A/X/D, 화살표, Numpad)로 버튼 간 이동
- Enter 키로 선택된 버튼 클릭
- 선택된 버튼은 노란색(TITLE_COLOR)으로 하이라이트

**지원 버튼:**
- Play (게임 시작)
- Rankings (순위)
- Journal (저널)
- Settings (설정)
- About (정보)
- Test Level (디버그 모드 전용)

### 4. HeroSelectScene 키보드 네비게이션
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/HeroSelectScene.java`

- 좌/우 방향키(A/D, ←/→, Numpad 4/6)로 영웅 선택
- Enter 키로 선택한 영웅으로 게임 시작
- 잠긴 영웅은 자동으로 건너뜀

**조작 방법:**
1. ← / → : 영웅 간 이동 (자동으로 선택됨)
2. Enter : 게임 시작

### 5. AmuletScene 키보드 네비게이션
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/AmuletScene.java`

- 상/하 방향키로 Exit/Stay 버튼 간 이동
- Enter 키로 선택
- 승리 화면에서 키보드만으로 게임 종료 또는 계속 플레이 선택 가능

### 6. TalentsPane 키보드 네비게이션
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/TalentsPane.java`

- 2D 그리드 탐색 구현
- 상/하 방향키: 티어 간 이동
- 좌/우 방향키: 같은 티어 내 특성 간 이동
- Enter 키: 선택한 특성 상세 정보/업그레이드
- 선택된 특성은 밝기 증가로 하이라이트

**조작 방법:**
1. ↑ / ↓ : 특성 티어 간 이동 (1티어, 2티어, 3티어...)
2. ← / → : 같은 티어 내 특성 간 이동
3. Enter : 특성 선택 (정보 보기 또는 업그레이드)

---

## 기존 구현 (변경 없음)

다음 기능들은 이미 구현되어 있어 별도 수정 없이 작동합니다:

| 기능 | 상태 | 비고 |
|------|------|------|
| Window 포커스 시스템 | 구현됨 | N/S/E/W 방향 이동 |
| WndBag 인벤토리 | 구현됨 | 방향키 그리드 탐색, Enter 선택 |
| WndOptions 대화창 | 구현됨 | addFocusableButton() 사용 |
| WndChooseSubclass | 구현됨 | 서브클래스 선택 |
| WndChooseAbility | 구현됨 | 갑옷 능력 선택 |
| 퀵슬롯 1-6 | 유지됨 | 숫자키 1-6 |

---

## 키 바인딩 요약

| 키 | 액션 |
|----|------|
| W / ↑ / Numpad 8 | 위로 이동 (N) |
| X / ↓ / Numpad 2 | 아래로 이동 (S) |
| A / ← / Numpad 4 | 왼쪽 이동 (W) |
| D / → / Numpad 6 | 오른쪽 이동 (E) |
| S | 대기 / 아이템 줍기 |
| Enter / Numpad Enter | 선택 / 확인 |
| Escape / Backspace | 뒤로가기 / 취소 |
| Tab / Space / Numpad 0 | 다음 항목 순환 |

---

## 시각적 피드백

- **버튼 포커스**: 텍스트가 노란색(0xFFFF44)으로 변경
- **특성 포커스**: 아이콘과 배경 밝기 130% 증가

### 7. StyledButton 클래스 - 포커스 색상 관리
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/StyledButton.java`

- 키보드 포커스 시 텍스트 색상을 저장/복원하는 메서드 추가

```java
public void saveTextColors() {
    text.saveColors();
}

public void restoreTextColor() {
    text.restoreSavedColors();
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `SPDAction.java` | Tab 키 바인딩 추가 |
| `Button.java` | click() public 메서드 추가 |
| `StyledButton.java` | saveTextColors(), restoreTextColor() 메서드 추가 |
| `TitleScene.java` | 키보드 네비게이션 구현 |
| `HeroSelectScene.java` | 영웅 선택 키보드 지원 |
| `AmuletScene.java` | 승리 화면 키보드 지원 |
| `TalentsPane.java` | 특성 선택 키보드 지원 |

---

## 추가 기능

### 8. Focusable 인터페이스
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/Focusable.java`

Button 외의 컴포넌트도 포커스 시스템에 참여할 수 있도록 하는 인터페이스:

```java
public interface Focusable {
    void setFocused(boolean focused);
    void saveFocusState();
    void restoreFocusState();
    void click();
    boolean isActive();
}
```

### 9. OptionSlider 키보드 조정
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/OptionSlider.java`

- 포커스 상태일 때 좌/우 방향키로 슬라이더 값 조정
- 포커스 시 시각적 피드백 (슬라이더 노드 밝기 증가)
- WndSettings의 모든 OptionSlider에 적용

### 10. WndChallenges 키보드 네비게이션
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndChallenges.java`

- 방향키로 도전과제 CheckBox와 Info 버튼 간 이동
- Enter로 CheckBox 토글 또는 Info 창 열기

### 11. ScrollPane 화살표 키 스크롤 및 Focusable 구현
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/ScrollPane.java`

- 포커스 상태일 때 상/하 방향키로 스크롤
- ZOOM_IN/ZOOM_OUT 키는 항상 동작 (기존 호환성 유지)

**중요 (2026-05-16 수정):** `isActive()` vs `canKeyboardFocus()` 분리

```java
// isActive(): Gizmo의 부모 체인 검사 유지 (클릭 감지용)
@Override
public boolean isActive() {
    return super.isActive();  // 반드시 super 호출!
}

// canKeyboardFocus(): 키보드 포커스 조건 (스크롤 가능할 때만)
public boolean canKeyboardFocus() {
    return active && content.height() > height;
}
```

---

## 추가 기능 (2026-04-19)

### 12. WndJournal / JournalScene 통합 키보드 네비게이션

**파일:** `WndJournal.java`, `JournalScene.java`

#### 버튼 모드 ↔ 컨텐츠 모드 전환
- **버튼 모드:** 탭 내 버튼 간 이동 (Pages, Category, Local/Global)
- **컨텐츠 모드:** 실제 컨텐츠 탐색 (그리드 아이템, 리스트 아이템, 스크롤)
- **Enter:** 버튼 선택 + 컨텐츠 모드 진입
- **ESC:** 컨텐츠 모드 종료 -> 버튼 모드로 복귀

#### 탭별 컨텐츠 모드 동작

| 탭 | 컨텐츠 모드 |
|----|------------|
| Notes | ScrollingGridPane 그리드 탐색 |
| Guide | ScrollingListPane 리스트 탐색 |
| Alchemy | ScrollPane 스크롤 (방향키) |
| Catalog | ScrollingGridPane 그리드 탐색 |
| Badges | BadgesGrid 그리드 탐색 |

### 13. 포커스 표시 통일

**변경:** 노란색 텍스트 대신 배경/아이콘 밝기로 포커스 표시

| 상태 | 표시 |
|------|----------|
| 포커스됨 | `bg.brightness(1.3f~1.5f)`, `icon.brightness(1.3f~1.5f)` |
| 선택됨 | 노란색 텍스트 (TITLE_COLOR) |

### 14. RankingsScene 키보드 네비게이션

**파일:** `RankingsScene.java`

- 방향키로 런 기록 간 이동
- Enter로 상세 정보 확인
- 포커스 시 shield, classIcon, steps(계단) 아이콘 모두 하이라이트

---

## 전체 키보드 네비게이션 요약

### 글로벌 키

| 키 | 액션 |
|----|------|
| 방향키 (W/A/X/D, 화살표, Numpad) | UI 요소 간 이동 |
| Enter / Numpad Enter | 선택 / 확인 / 컨텐츠 모드 진입 |
| ESC / Backspace | 뒤로가기 / 취소 / 컨텐츠 모드 종료 |
| Tab / Space | 다음 탭 순환 |

### WndJournal / JournalScene 전용

| 키 | 버튼 모드 | 컨텐츠 모드 |
|----|----------|------------|
| 방향키 | 버튼 간 이동 | 아이템 탐색 / 스크롤 |
| Enter | 버튼 선택 + 컨텐츠 모드 진입 | 아이템 상세 보기 |
| ESC | 창 닫기 | 버튼 모드로 복귀 |
| Tab/Space | 탭 전환 | 탭 전환 |

---

## 향후 개선 사항

| 항목 | 우선순위 | 설명 |
|------|---------|------|
| 색맹 사용자 대체 지표 | 낮음 | 노란색 외 추가 시각 지표 |

---
