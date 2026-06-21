# 047. 키보드 접근성 기초 수정 (탭 네비게이션)

**날짜**: 2026-04-18

## 개요

Fixed multiple keyboard navigation issues across various windows to ensure consistent focus management and visual feedback. Button focus system refactored to support both RedButton and IconButton types.

---

## 변경 사항

### Core System Changes

### Button Focus System Refactoring (`Window.java`, `Button.java`, `StyledButton.java`, `IconButton.java`)

The keyboard navigation system was refactored to support both `RedButton` (text-based buttons) and `IconButton` (icon-only buttons):

1. **Button.java** - Added base focus methods:
   - `setFocused(boolean)` - Called when a button gains/loses focus
   - `saveFocusState()` - Save current visual state before focus change
   - `restoreFocusState()` - Restore saved visual state when unfocused

2. **StyledButton.java** - Implements focus via text color change (yellow highlight)

3. **IconButton.java** - Implements focus via icon brightness increase

4. **Window.java** - Changed `focusableButtons` from `ArrayList<RedButton>` to `ArrayList<Button>` to support both button types

---

### Window-Specific Fixes

#### WndHero.java (In-game Hero Stats Window)
- Added `infoButton` field to `StatsTab` class for keyboard access
- Added `rebuildFocusableButtons()` method to register the info button
- Added `clearFocus()` and `rebuildFocusableButtons()` calls to all three tab select callbacks (Stats, Talents, Buffs)

#### WndHeroInfo.java (Hero Info Window from Title/Character Select)
- Changed `subClsInfos` and `abilityInfos` arrays from `private` to package-private for access
- Added `rebuildFocusableButtons()` method to register subclass and ability info IconButtons
- Added `clearFocus()` and `rebuildFocusableButtons()` calls to all four tab select callbacks

#### WndRanking.java (Ranking Details Window)
- Added `scoreInfo` button registration for keyboard navigation
- Modified `RankingTab.select()` to call `clearFocus()` and `rebuildFocusableButtons()` on tab change
- Added empty `rebuildFocusableButtons()` method stub

#### WndJournal.java (Journal Window)
- Added `clearFocus()` before `rebuildFocusableButtons()` in all five tab select callbacks (Notes, Guide, Alchemy, Catalog, Badges)

#### WndSettings.java (Settings Window)
- Added `clearFocus()` before `rebuildFocusableButtons()` in all five tab select callbacks (Display, UI, Input, Audio, Languages)
- Toolbar settings sub-window already had button registrations for:
  - `btnSplit`, `btnGrouped`, `btnCentered`
  - `chkQuickSwapper`, `chkFlipToolbar`, `chkFlipTags`

---

### Key Pattern: Tab Change Focus Management

The core fix pattern applied across all tabbed windows:

```java
protected void select(boolean value) {
    super.select(value);
    // ... tab visibility logic ...
    if (value) {
        clearFocus();           // Restore previous button's visual state
        rebuildFocusableButtons();  // Re-register buttons for current tab
    }
}
```

This ensures:
1. Previously focused button's highlight color is restored when switching tabs
2. Focus index is reset to -1 (no focus)
3. Focusable buttons list is rebuilt for the current tab's content

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../ui/Button.java` | Added base focus methods |
| `core/.../ui/StyledButton.java` | Implements focus via text color change |
| `core/.../ui/IconButton.java` | Implements focus via icon brightness |
| `core/.../ui/Window.java` | Changed focusableButtons to ArrayList<Button> |
| `core/.../windows/WndHero.java` | Added rebuildFocusableButtons() method |
| `core/.../windows/WndHeroInfo.java` | Added focus registration for subclass/ability buttons |
| `core/.../windows/WndRanking.java` | Added scoreInfo button registration |
| `core/.../windows/WndJournal.java` | Added clearFocus() in tab callbacks |
| `core/.../windows/WndSettings.java` | Added clearFocus() in tab callbacks |

---
