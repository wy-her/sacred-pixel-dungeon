# 071. 키보드 네비게이션 개선, 창 열림 시 줌 차단

**날짜**: 2026-05-26

## 개요

WndHero BuffsTab 포커스 효과를 brightness 방식으로 변경하고, WndJournal Badges탭 키보드 네비게이션 추가. 창이 열려있을 때 맵 줌을 차단하는 기능 구현.

---

## 변경 사항

### 1. WndHero BuffsTab 포커스 효과 변경

### 문제
- BuffsTab의 버프 항목 포커스 효과가 ColorBlock 방식이었음
- WndRanking의 Badges탭 포커스 효과와 일관성 없음

### 해결책
ColorBlock 대신 `icon.brightness(1.5f)` + `txt.hardlight()` 방식으로 변경

### 수정 파일
`windows/WndHero.java`

### 주요 변경 사항

#### 변경 전
```java
private class BuffSlot extends Component {
    private com.watabou.noosa.ColorBlock highlight;

    public BuffSlot( Buff buff ){
        highlight = new com.watabou.noosa.ColorBlock(1, 1, 0x33FFFFFF);
        highlight.visible = false;
        add(highlight);
        // ...
    }

    public void setFocused(boolean focused) {
        highlight.visible = focused;
    }
}
```

#### 변경 후
```java
private class BuffSlot extends Component {
    // ColorBlock 제거

    public void setFocused(boolean focused) {
        if (focused) {
            icon.brightness(1.5f);
            txt.hardlight(Window.TITLE_COLOR);
        } else {
            icon.resetColor();
            txt.resetColor();
        }
    }
}
```

---

## 2. WndJournal Badges탭 키보드 네비게이션

### 문제
- WndJournal의 Badges탭에서 "이번 도전"/"전체 도전" 버튼을 Enter로 선택해도 배지 아이콘을 키보드로 네비게이션 불가
- BadgesGrid/BadgesList의 `moveFocus()`, `activateFocused()` 메서드가 private이라 외부 호출 불가

### 해결책
1. BadgesGrid/BadgesList의 메서드를 public으로 변경
2. WndJournal.onSignal()에서 Badges탭 네비게이션을 명시적으로 처리

### 수정 파일
- `ui/BadgesGrid.java`
- `ui/BadgesList.java`
- `windows/WndJournal.java`

### 주요 변경 사항

#### BadgesGrid.java / BadgesList.java
```java
// private → public 변경
public void moveFocus(int dx, int dy) { ... }  // BadgesGrid
public void moveFocus(int direction) { ... }   // BadgesList
public void activateFocused() { ... }
```

#### WndJournal.java - Badges탭 네비게이션 추가
```java
// Handle Badges tab navigation
if (activeTabIndex == 4 && badgesTab != null) {
    Component badges = BadgesTab.global ? badgesTab.badgesGlobal : badgesTab.badgesLocal;
    if (badges instanceof BadgesGrid) {
        BadgesGrid grid = (BadgesGrid) badges;
        if (action == SPDAction.W || action == SPDAction.SW) {
            grid.moveFocus(-1, 0);
            return true;
        } else if (action == SPDAction.E || action == SPDAction.NE) {
            grid.moveFocus(1, 0);
            return true;
        } else if (action == SPDAction.N || action == SPDAction.NW) {
            grid.moveFocus(0, -1);
            return true;
        } else if (action == SPDAction.S || action == SPDAction.SE) {
            grid.moveFocus(0, 1);
            return true;
        }
    } else if (badges instanceof BadgesList) {
        BadgesList list = (BadgesList) badges;
        if (action == SPDAction.N || ...) {
            list.moveFocus(-1);
            return true;
        } else if (action == SPDAction.S || ...) {
            list.moveFocus(1);
            return true;
        }
    }
}
```

#### WndJournal.java - Badges탭 Enter 처리 추가
```java
// Handle Badges tab - activate focused badge
if (activeTabIndex == 4 && badgesTab != null) {
    Component badges = BadgesTab.global ? badgesTab.badgesGlobal : badgesTab.badgesLocal;
    if (badges instanceof BadgesGrid) {
        ((BadgesGrid) badges).activateFocused();
        return true;
    } else if (badges instanceof BadgesList) {
        ((BadgesList) badges).activateFocused();
        return true;
    }
}
```

---

## 3. WndRanking StatsTab GAP 정리

### 문제
- GAP 값이 시드 유무에 따라 4 또는 3으로 변동
- 섹션 구분 GAP이 명확하지 않음

### 해결책
- GAP을 항상 3px로 고정 (static final)
- 섹션 구분에 `+GAP+2` 적용

### 수정 파일
`windows/WndRanking.java`

### 주요 변경 사항

#### 변경 전
```java
private int GAP = 4;

public void init() {
    if (Dungeon.seed != -1) {
        GAP--;  // GAP = 3
    }
    // ...
    pos += GAP;  // 섹션 구분
}
```

#### 변경 후
```java
private static final int GAP = 3;  // 항상 3px

public void init() {
    // 조건부 GAP 변경 제거
    // ...
    pos += GAP + 2;  // 섹션 구분 (Score 뒤, Seed 뒤)
}
```

---

## 4. 창 열림 시 맵 줌 차단

### 문제
- 인게임에서 창(WndHero 등)이 열려있을 때 마우스 휠, 터치패드, 키보드로 맵 줌 가능
- 드래그 차단(`onPointerDown`, `onDrag`)은 이미 구현되어 있었음

### 해결책
`onScroll()` 및 키보드 줌 처리에 `GameScene.interfaceBlockingHero()` 체크 추가

### 수정 파일
`scenes/CellSelector.java`

### 주요 변경 사항

#### onScroll() - 마우스 휠 줌 차단
```java
@Override
protected void onScroll( ScrollEvent event ) {
    // Block scroll zoom when windows are open
    if (GameScene.interfaceBlockingHero()) {
        return;
    }

    float diff = event.amount/10f;
    // ... existing zoom logic
}
```

#### keyListener - 키보드 줌 차단
```java
if (!event.pressed){
    // Block keyboard zoom when windows are open
    if (!GameScene.interfaceBlockingHero()) {
        if (action == SPDAction.ZOOM_IN){
            zoom( camera.zoom+1 );
            mouseZoom = camera.zoom;
            return true;
        } else if (action == SPDAction.ZOOM_OUT){
            zoom( camera.zoom-1 );
            mouseZoom = camera.zoom;
            return true;
        }
    }
}
```

### 차단되는 줌 방식

| 줌 방식 | 차단 위치 | 상태 |
|---------|-----------|------|
| 마우스 휠 | `onScroll()` | 이번에 추가 |
| 터치 핀치 | `onDrag()` | 기존 구현 |
| 키보드 (+/-) | `keyListener` | 이번에 추가 |

---

## 요약

| 항목 | 수정 파일 | 변경 내용 |
|------|-----------|-----------|
| BuffsTab 포커스 | `WndHero.java` | ColorBlock → brightness 방식 |
| Badges 네비게이션 | `BadgesGrid.java`, `BadgesList.java`, `WndJournal.java` | 명시적 키보드 네비게이션 처리 |
| StatsTab GAP | `WndRanking.java` | GAP=3 고정, 섹션 구분 +GAP+2 |
| 줌 차단 | `CellSelector.java` | 창 열림 시 휠/키보드 줌 차단 |

---

---

*관련 NEVER-CHANGE.md 업데이트 필요: CellSelector 줌 차단*
