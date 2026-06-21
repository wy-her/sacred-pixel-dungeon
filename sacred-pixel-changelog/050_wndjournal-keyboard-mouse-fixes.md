# 050. WndJournal/JournalScene 키보드 및 마우스 버그 수정

**날짜**: 2026-04-19

## 개요

WndJournal 및 JournalScene에서 발생하는 8개의 키보드/마우스 관련 버그를 수정했습니다.

---

## 변경 사항

### 수정된 이슈

### 이슈 1: Badge 탭 local→global 전환 문제
**파일:** `WndJournal.java` 라인 358-363

**문제:** Badge 탭에서 local 버튼이 선택된 상태에서 global로 이동할 때, Enter를 한 번 눌러도 바로 전환되지 않고 ESC를 먼저 누른 후 다시 Enter를 눌러야 작동함.

**원인:** Enter 키 처리 시 Badge 탭(activeTabIndex == 4)도 `enterContentFocusMode()`를 호출하여 포커스 상태가 혼란해짐.

**수정:** Badge 탭에서는 local/global 버튼이 데이터 전환 목적이므로, content focus mode 진입 조건에서 제외.

```java
// 수정 전
if (activeTabIndex == 2 || activeTabIndex == 3 || activeTabIndex == 4) {
    enterContentFocusMode();
}

// 수정 후
if (activeTabIndex == 2 || activeTabIndex == 3) {
    enterContentFocusMode();
}
```

---

### 이슈 2: Alchemy 탭 컨텐츠 모드에서 pages 포커싱 문제
**파일:** `WndJournal.java` 라인 422

**문제:** Alchemy 탭에서 컨텐츠 모드 진입 후에도 좌우 화살표 키로 pages 버튼이 포커싱됨.

**원인:** `enterContentFocusMode()`에서 `clearFocus()`만 호출하고 `focusableButtons` 리스트는 그대로 유지됨.

**수정:** 컨텐츠 모드 진입 시 focusableButtons 완전 초기화.

```java
private void enterContentFocusMode() {
    contentFocusMode = true;
    clearFocus();
    // 추가: 컨텐츠 모드에서 버튼 포커싱 완전 차단
    focusableButtons.clear();
    ...
}
```

---

### 이슈 3: JournalScene badges 뱃지 설명 중 키보드 네비게이션 문제
**파일:** `BadgesGrid.java` 라인 143-150

**문제:** JournalScene의 badges 탭에서 뱃지 설명(WndBadge)이 표시된 상태에서도 화살표 키로 뱃지 포커싱 이동 가능.

**원인:** `hasOpenWindows()` 체크가 `parentWindow == null` 조건에서만 실행됨.

**수정:** 모든 경우에 `hasOpenWindows()` 체크 수행.

```java
// 수정 전
if (parentWindow == null && Game.scene() instanceof PixelScene) {
    if (((PixelScene) Game.scene()).hasOpenWindows()) {
        return false;
    }
}

// 수정 후
if (Game.scene() instanceof PixelScene) {
    PixelScene scene = (PixelScene) Game.scene();
    if (scene.hasOpenWindows()) {
        return false;
    }
}
```

---

### 이슈 4: JournalScene guide 탭 마우스 선택 안됨
**파일:** `JournalScene.java` 라인 159-175

**문제:** JournalScene의 guide 탭에서 마우스로 항목 선택이 안되고 키보드만 작동.

**원인:** guideTab 생성 시 내부 컴포넌트(list)의 active 상태가 명시적으로 설정되지 않음.

**수정:** guideTab 및 내부 list의 active 상태 명시적 설정.

```java
guideTab.updateList();
// 추가
guideTab.active = true;
if (guideTab.list != null) {
    guideTab.list.active = true;
}
```

---

### 이슈 5: 빈 화면 클릭 시 비활성 탭 항목 선택 문제
**파일:** `WndJournal.java` 라인 152-270

**문제:** WndJournal에서 빈 화면을 클릭했을 때 비활성화된 탭의 해당 위치 항목이 선택되어 설명창이 나옴.

**원인:** 탭 전환 시 내부 컴포넌트(grid, list)의 active/visible 상태가 설정되지 않음.

**수정:** 모든 탭의 select() 메서드에서 내부 컴포넌트 active/visible 동기화.

```java
// Notes 탭
notesTab.active = notesTab.visible = value;
if (notesTab.grid != null) {
    notesTab.grid.active = notesTab.grid.visible = value;
}

// Guide 탭
guideTab.active = guideTab.visible = value;
if (guideTab.list != null) {
    guideTab.list.active = guideTab.list.visible = value;
}

// Alchemy 탭
alchemyTab.active = alchemyTab.visible = value;
if (alchemyTab.list != null) {
    alchemyTab.list.active = alchemyTab.list.visible = value;
}
if (alchemyTab.pageButtons != null) {
    for (int i = 0; i < alchemyTab.pageButtons.length; i++) {
        RedButton btn = alchemyTab.pageButtons[i];
        btn.active = value && Document.ALCHEMY_GUIDE.isPageFound(i);
        btn.visible = value;
    }
}

// Catalog 탭
catalogTab.active = catalogTab.visible = value;
if (catalogTab.grid != null) {
    catalogTab.grid.active = catalogTab.grid.visible = value;
}
if (catalogTab.itemButtons != null) {
    for (RedButton btn : catalogTab.itemButtons) {
        btn.active = value;
        btn.visible = value;
    }
}

// Badges 탭
badgesTab.active = badgesTab.visible = value;
if (badgesTab.badgesLocal != null) {
    badgesTab.badgesLocal.active = badgesTab.badgesLocal.visible = value && !BadgesTab.global;
}
if (badgesTab.badgesGlobal != null) {
    badgesTab.badgesGlobal.active = badgesTab.badgesGlobal.visible = value && BadgesTab.global;
}
```

---

### 이슈 6: WndJournal guide 탭 마우스 선택 안됨
**파일:** `WndJournal.java`

**해결:** 이슈 5의 수정으로 함께 해결됨. Guide 탭 선택 시 내부 list의 active/visible 동기화.

---

### 이슈 7: Alchemy 탭 pages 버튼 마우스 선택 안됨
**파일:** `WndJournal.java`

**해결:** 이슈 5의 수정으로 함께 해결됨. Alchemy 탭 선택 시 pageButtons의 active/visible 동기화.

---

### 이슈 8: Catalog 탭 category 버튼 마우스 선택 안됨
**파일:** `WndJournal.java`

**해결:** 이슈 5의 수정으로 함께 해결됨. Catalog 탭 선택 시 itemButtons의 active/visible 동기화.

---

## 추가 변경사항

### 필드 접근성 변경

| 클래스 | 필드 | 변경 |
|--------|------|------|
| NotesTab | grid | `ScrollingGridPane grid` → `public ScrollingGridPane grid` |
| GuideTab | list | `ScrollingListPane list` → `public ScrollingListPane list` |

### JournalScene 내부 컴포넌트 활성화

모든 탭(Badges, Catalog, Guide, Alchemy)에서 생성 시 내부 컴포넌트 active 상태 명시적 설정.

---

## 수정된 파일 목록

| 파일 | 변경 내용 |
|------|----------|
| `WndJournal.java` | 이슈 1, 2, 5-8 수정, 필드 접근성 변경 |
| `JournalScene.java` | 이슈 4 수정, 모든 탭 내부 컴포넌트 활성화 |
| `BadgesGrid.java` | 이슈 3 수정, hasOpenWindows() 체크 강화 |

---

## 테스트 항목

### WndJournal (게임 내 J 키)
- [ ] Badge 탭: local→global 전환 시 Enter 한 번에 작동
- [ ] Alchemy 탭: 컨텐츠 모드에서 좌우 화살표로 pages 포커싱 안됨
- [ ] Guide 탭: 마우스로 항목 선택 가능
- [ ] Alchemy 탭: pages 버튼 마우스 클릭 가능
- [ ] Catalog 탭: category 버튼 마우스 클릭 가능
- [ ] 빈 화면 클릭 시 비활성 탭 항목 선택 안됨

### JournalScene (메인 메뉴 → Journal)
- [ ] Badges 탭: 뱃지 설명 표시 중 화살표 키 네비게이션 차단
- [ ] Guide 탭: 마우스로 항목 선택 가능
