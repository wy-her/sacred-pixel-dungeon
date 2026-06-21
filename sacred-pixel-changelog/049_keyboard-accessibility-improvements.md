# 049. 키보드 접근성 고도화 및 버그 수정

**날짜**: 2026-04-18

## 개요

WndJournal, JournalScene, RankingsScene 등의 키보드 네비게이션을 대폭 개선하고 다양한 버그를 수정했습니다. 포커스 표시 방식을 노란색 텍스트 대신 배경/아이콘 밝기로 통일.

---

## 변경 사항

### 포커스 표시 방식 통일

**변경 내용:** 포커스 표시를 노란색 텍스트 대신 배경/아이콘 밝기로 통일

**영향받는 파일:**
- `RankingsScene.java` - Record.setFocused()
- `StartScene.java` - SaveSlotButton.setFocused()
- `TitleScene.java` - moveFocus()

**상세:**
- 포커스 시 `bg.brightness(1.3f~1.5f)` 사용
- 아이콘도 함께 밝게 표시 (`icon.brightness(1.3f~1.5f)`)
- 노란색 텍스트는 "선택됨" 상태에만 사용

### RankingsScene 개선

**파일:** `RankingsScene.java`

- 개별 런 포커스 시 shield, classIcon, steps(계단) 아이콘 모두 하이라이트
- 텍스트 색상은 변경하지 않음

```java
public void setFocused(boolean focused) {
    if (focused) {
        shield.brightness(1.5f);
        classIcon.brightness(1.5f);
        steps.brightness(1.5f);
    } else {
        shield.resetColor();
        classIcon.resetColor();
        steps.resetColor();
        // 특수 상태 복원 (daily, customSeed, ascending 등)
    }
}
```

### WndJournal 키보드 네비게이션 개선

**파일:** `WndJournal.java`

#### 3.1 컨텐츠 모드 진입
- **Alchemy 탭:** Enter로 페이지 선택 시 컨텐츠 모드 진입, 키보드 스크롤 가능
- **Catalog 탭:** Enter로 카테고리 선택 시 그리드 네비게이션 활성화
- **Badges 탭:** Enter로 local/global 선택 시 배지 그리드 네비게이션 활성화

#### 3.2 자식 창 열림 시 키 차단
- `isTopmost()` 체크로 설명 창이 열려 있을 때 부모 창 네비게이션 차단
- ESC/BACK 키만 허용 (자식 창 닫기용)
- 게임 이동 키도 모두 차단

#### 3.3 Enter 키로 WndJournal 닫힘 방지
- Alchemy, Catalog, Badges 탭에서 Enter 키 입력 시 창이 닫히지 않도록 수정

### JournalScene 개선

**파일:** `JournalScene.java`

- WndJournal과 동일한 컨텐츠 모드 시스템 적용
- Badges (0), Catalog (1), Alchemy (3) 탭에서 Enter로 컨텐츠 모드 진입
- 창이 열려 있을 때 모든 키 차단 (ESC 제외)

### 비활성 탭 클릭 차단

**파일:** `ScrollPane.java`

**문제:** 비활성 탭의 아이템이 클릭되는 버그

**해결:** PointerController.onClick()에서 전체 부모 체인 확인

```java
@Override
protected void onClick(PointerEvent event) {
    // isVisible()과 isActive()는 부모 체인 전체를 확인
    if (!ScrollPane.this.isVisible() || !ScrollPane.this.isActive()) return;

    PointF p2 = content.camera.screenToCamera(...);
    ScrollPane.this.onClick(p2.x, p2.y);
}
```

### 스크롤 타이틀 표시 수정

**파일:** `ScrollingGridPane.java`, `ScrollingListPane.java`

**문제:** 스크롤을 내렸다가 다시 올리면 타이틀이 안 보임

**해결:** 첫 번째 아이템으로 포커스 이동 시 스크롤을 0으로 설정

```java
private void scrollToItem(int index) {
    if (itemTop < content.camera.scroll.y) {
        // 첫 번째 아이템이면 맨 위로 스크롤하여 타이틀 표시
        if (index == 0) {
            scrollTo(0, 0);
        } else {
            scrollTo(0, itemTop - 2);
        }
    }
}
```

### 카탈로그 서브섹션 네비게이션 개선

**파일:** `ScrollingGridPane.java`

**문제:** 상하 이동 시 항상 첫 번째 아이템으로 이동

**해결:** 서브섹션(헤더) 경계를 넘을 때만 첫 번째 아이템으로, 같은 섹션 내에서는 X 위치 유지

```java
private int getSubsectionIndex(int gridItemIndex) {
    // 헤더 개수를 세어 서브섹션 인덱스 반환
}

private void moveFocusGrid(int dx, int dy) {
    int targetSubsection = getSubsectionIndex(firstOnTargetRow);
    if (targetSubsection != currentSubsection) {
        // 서브섹션 경계 - 첫 번째 아이템으로
        candidateIndex = firstOnTargetRow;
    } else {
        // 같은 섹션 - X 위치 유지
        // 가장 가까운 X 좌표의 아이템 찾기
    }
}
```

### 설명 창 Enter로 닫기

**파일:** `WndStory.java`, `WndJournalItem.java`, `WndBadge.java`

- ESC뿐만 아니라 Enter 키로도 설명 창을 닫을 수 있도록 추가

```java
@Override
public boolean onSignal(KeyEvent event) {
    if (event.pressed && (event.code == Input.Keys.ENTER
            || event.code == Input.Keys.NUMPAD_ENTER)) {
        onBackPressed();
        return true;
    }
    return super.onSignal(event);
}
```

### 컨텐츠 모드에서 버튼 포커스 차단

**파일:** `WndJournal.java`

**문제:** 컨텐츠 모드(배지 그리드, 알케미 스크롤 등)에서 방향키로 버튼 포커스가 가능

**해결:** 컨텐츠 모드일 때 방향키가 `super.onSignal()`에 전달되지 않도록 차단

```java
// 컨텐츠 모드에서 방향키 차단
if (contentFocusMode && event.pressed) {
    if (action == SPDAction.N || action == SPDAction.S || ...) {
        return true; // 차단 - 컨텐츠 컴포넌트가 처리
    }
}
```

### JournalScene 배지 설명 창 열림 시 네비게이션 차단

**파일:** `BadgesGrid.java`, `PixelScene.java`

**문제:** JournalScene에서 배지 설명 창(WndBadge)이 열려 있을 때도 배지 네비게이션 가능

**해결:** BadgesGrid에서 Scene에 열린 창이 있는지 확인

```java
// 부모 Window가 없고 Scene에 창이 열려 있으면 네비게이션 차단
if (parentWindow == null && Game.scene() instanceof PixelScene) {
    if (((PixelScene) Game.scene()).hasOpenWindows()) {
        return false;
    }
}
```

**PixelScene에 추가된 메서드:**
```java
public synchronized boolean hasOpenWindows(){
    if (members == null) return false;
    for (Gizmo g : members.toArray(new Gizmo[0])){
        if (g instanceof Window){
            return true;
        }
    }
    return false;
}
```

### 비활성 탭 클릭 차단 강화

**파일:** `ScrollPane.java`, `Button.java`, `ScrollingListPane.java`, `ScrollingGridPane.java`, `BadgesList.java`

**문제:** 비활성 탭의 아이템/버튼이 마우스로 클릭됨

**해결:** 모든 클릭 핸들러에서 부모 체인 가시성 확인

**ScrollPane.PointerController.onClick():**
```java
// 부모 체인 명시적 순회
com.watabou.noosa.Group p = ScrollPane.this.parent;
while (p != null) {
    if (!p.visible || !p.active) {
        return; // 부모가 비활성화됨
    }
    p = p.parent;
}
```

**Button.update():**
```java
// isVisible()과 isActive()로 부모 체인 확인
hotArea.active = isVisible() && isActive();
```

**ScrollingListPane.onClick(), ScrollingGridPane.onClick(), BadgesList.onClick():**
```java
// isVisible()과 isActive() 사용
if (!isVisible() || !isActive()) return;
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `RankingsScene.java` | 포커스 시 steps 아이콘 하이라이트 추가 |
| `WndJournal.java` | 컨텐츠 모드, 자식 창 키 차단, Enter 처리 개선, 컨텐츠 모드 방향키 차단 |
| `JournalScene.java` | 컨텐츠 모드, 창 열림 시 키 차단, Alchemy 스크롤 |
| `ScrollPane.java` | 부모 체인 가시성 명시적 확인 |
| `ScrollingGridPane.java` | 서브섹션 네비게이션, 스크롤 타이틀 수정, 부모 체인 가시성 확인 |
| `ScrollingListPane.java` | 스크롤 타이틀 수정, 부모 체인 가시성 확인 |
| `BadgesList.java` | 부모 체인 가시성 확인 추가 |
| `BadgesGrid.java` | Scene 창 확인 추가 |
| `Button.java` | hotArea.active에 부모 체인 가시성 확인 |
| `PixelScene.java` | hasOpenWindows() 메서드 추가 |
| `WndStory.java` | Enter로 닫기 추가 |
| `WndJournalItem.java` | Enter로 닫기 추가 |
| `WndBadge.java` | Enter로 닫기 추가 |

---

## 키보드 조작 요약

### WndJournal / JournalScene

| 키 | 동작 |
|----|------|
| Tab / Space | 탭 전환 |
| 방향키 | 버튼 간 이동 (버튼 모드) / 아이템 탐색 (컨텐츠 모드) |
| Enter | 버튼 선택 + 컨텐츠 모드 진입 / 아이템 상세 보기 |
| ESC | 컨텐츠 모드 종료 / 창 닫기 |

### 컨텐츠 모드 동작

| 탭 | 컨텐츠 모드 동작 |
|----|-----------------|
| Notes | 그리드 탐색 (방향키), 아이템 선택 (Enter) |
| Guide | 리스트 탐색 (방향키), 문서 열기 (Enter) |
| Alchemy | 스크롤 (방향키) |
| Catalog | 그리드 탐색 (방향키), 아이템 상세 (Enter) |
| Badges | 그리드 탐색 (방향키), 배지 상세 (Enter) |
