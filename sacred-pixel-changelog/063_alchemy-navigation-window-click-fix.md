# 063. 연금술 탭 네비게이션 및 창 외부 클릭 수정

**날짜**: 2026-05-18

## 개요

This changelog covers two main areas:
1. **JournalScene/WndJournal AlchemyTab Recipe Keyboard Navigation** - Enhanced keyboard navigation for recipe items
2. **Window Click-Outside-to-Close Cascading Fix** - Fixed issue where clicking outside nested windows caused multiple windows to close

---

## 변경 사항

### AlchemyTab Recipe Keyboard Navigation

#### Changes Made

#### QuickRecipe.java
Added keyboard navigation support for recipe items:

```java
// Keyboard navigation support
private int focusedItemIndex = -1;
private com.watabou.noosa.ColorBlock focusHighlight;

// Get total number of focusable items (inputs + output)
public int getItemCount() {
    return inputs.size() + 1; // +1 for output
}

// Set focus on item at index (0 to getItemCount()-1)
public void setFocusedItem(int index) { ... }
public void clearItemFocus() { ... }
public int getFocusedItemIndex() { ... }
public void activateFocusedItem() { ... }
```

#### WndJournal.java AlchemyTab
- Added `focusedRecipeIndex`, `focusedItemInRecipe`, `recipeNavigationActive` fields
- `activateRecipeNavigation()`: Starts with no focus (Layer1 unfocused initially)
- `moveFocusVertical()`: Moving up/down focuses first item of new recipe
- `moveFocusHorizontal()`: Moving left/right within a recipe
- Blocked up/down keys for page button navigation (only left/right allowed)

```java
// For Alchemy (2) and Catalog (3) tabs, only use left/right for page button navigation
if (!contentFocusMode && event.pressed && (activeTabIndex == 2 || activeTabIndex == 3)) {
    com.watabou.input.GameAction action = KeyBindings.getActionForKey(event);
    if (action == SPDAction.N || action == SPDAction.S) {
        return true; // Block up/down keys
    }
}
```

#### JournalScene.java
- Fixed `enterContentFocusMode()` to call `alchemyTab.activateRecipeNavigation()` instead of scroll pane focus
- Added key handling for recipe navigation in content focus mode
- Blocked up/down keys for page button navigation

#### Behavior Summary

| Key | Page Button Mode | Content Focus Mode (Alchemy) |
|-----|------------------|------------------------------|
| Left/Right | Change page | Move within recipe items |
| Up/Down | **Blocked** | Move between recipes (focus first item) |
| Enter | Enter content mode | View item details |
| ESC | Close window | Exit content mode |

---

### Window Click-Outside-to-Close Fix

#### Problem Description

When nested windows were open (e.g., WndHeroInfo → WndInfoSubclass → WndInfoTalent), clicking outside to close one window would:
1. Close the topmost window correctly
2. But the click event would propagate to parent windows
3. Causing cascading closures or preventing subsequent clicks from working

#### Solution: blockerHandledClose Flag

#### Window.java Changes

Added a new flag to track when a blocker handles a close:

```java
// Track if blocker handled a closing click (to stop event propagation)
private boolean blockerHandledClose = false;
```

Modified blocker's `onSignal()`:
```java
@Override
public boolean onSignal( PointerEvent event ) {
    // Reset the handled flag before processing
    blockerHandledClose = false;

    boolean result = super.onSignal(event);

    // If this blocker handled a window close, return true to stop event propagation
    if (blockerHandledClose) {
        return true;
    }
    return result;
}
```

Modified blocker's `onClick()`:
```java
@Override
protected void onClick( PointerEvent event ) {
    // Skip this click if flagged (to prevent cascading window closures)
    if (skipNextClick) {
        skipNextClick = false;
        blockerHandledClose = true;  // Stop propagation
        return;
    }

    if (Window.this.parent != null && !Window.this.chrome.overlapsScreenPoint(...)) {
        blockerHandledClose = true;  // Mark as handled before closing
        onBackPressed();
    }
}
```

Modified `refreshBlockerPriority()`:
```java
public void refreshBlockerPriority() {
    if (blocker != null) {
        blocker.reset();
        blocker.givePointerPriority();
        // Note: skipNextClick no longer set here - blockerHandledClose handles propagation
    }
}
```

#### How It Works

1. When a window closes due to click-outside, `onClick()` sets `blockerHandledClose = true`
2. After `onClick()` returns, `onSignal()` checks `blockerHandledClose`
3. If true, returns `true` to stop Signal dispatch propagation
4. Parent windows' blockers never see the event

#### Event Flow Diagram

```
Click outside WndInfoTalent:
  │
  ├─ DOWN event dispatched to all blockers
  │   └─ All blockers store curEvent
  │
  ├─ UP event dispatched (LIFO order):
  │   ├─ WndInfoTalent.blocker.onSignal()
  │   │   └─ onClick() → blockerHandledClose = true → onBackPressed() → hide()
  │   │       └─ destroy() → callback → WndInfoSubclass.active = true
  │   │   └─ Returns TRUE (blockerHandledClose is true)
  │   │
  │   └─ DISPATCH STOPS (onSignal returned true)
  │       └─ WndInfoSubclass.blocker never sees this event
  │
  └─ Next click on WndInfoSubclass works normally
```

---

### WndTabbed Tab Area Click Fix

#### Problem

After closing a child window (e.g., WndInfoSubclass), clicking on tab icons in the parent window (WndHeroInfo) would close the entire window instead of switching tabs.

#### Root Cause

1. `givePointerPriority()` moves blocker to FRONT of pointer listeners
2. Blocker receives DOWN event before tab buttons
3. `chrome.overlapsScreenPoint()` returns `false` for tab area (tabs are BELOW chrome)
4. Blocker interprets click as "outside window" and closes it

#### Solution: Override isInsideWindowArea() in WndTabbed

Added `isInsideWindowArea()` method to Window.java (base class):
```java
// Check if a screen point is inside the window's interactive area
// Subclasses (like WndTabbed) can override to include additional areas like tabs
protected boolean isInsideWindowArea(int screenX, int screenY) {
    return chrome.overlapsScreenPoint(screenX, screenY);
}
```

Override in WndTabbed.java to include tab area:
```java
@Override
protected boolean isInsideWindowArea(int screenX, int screenY) {
    // Check chrome area
    if (chrome.overlapsScreenPoint(screenX, screenY)) return true;

    // Check tab area (tabs are below chrome, from y=height to y=height+tabHeight())
    float localX = (screenX - camera.x) / camera.zoom + camera.scroll.x;
    float localY = (screenY - camera.y) / camera.zoom + camera.scroll.y;

    float tabLeft = -chrome.marginLeft();
    float tabRight = width + chrome.marginRight();
    float tabTop = height;
    float tabBottom = height + tabHeight();

    return localX >= tabLeft && localX <= tabRight
        && localY >= tabTop && localY <= tabBottom;
}
```

Modified blocker's `onSignal()` to pass through clicks inside window area:
```java
if (event != null && event.type == PointerEvent.Type.DOWN) {
    insideWindowArea = isInsideWindowArea((int)event.current.x, (int)event.current.y);
    // If click is inside window area, DON'T process it - let child components handle it
    if (insideWindowArea && Window.this.active) {
        return false;  // PASS THROUGH to child components
    }
}
```

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `QuickRecipe.java` | Added keyboard navigation support for recipe items |
| `WndJournal.java` | Recipe navigation methods, page button up/down blocking |
| `JournalScene.java` | Recipe navigation integration, page button up/down blocking |
| `Window.java` | Added `blockerHandledClose` flag, `isInsideWindowArea()` method |
| `WndTabbed.java` | Override `isInsideWindowArea()` to include tab area bounds |

---

## Testing Checklist

### AlchemyTab Navigation
- [ ] Enter JournalScene → Alchemy tab → Press Enter
- [ ] Verify no item is focused initially (Layer1 unfocused)
- [ ] Press Up/Down to navigate between recipes
- [ ] Verify first item of each recipe is focused when moving vertically
- [ ] Press Left/Right to navigate within a recipe
- [ ] Press Enter to view item details
- [ ] Verify page buttons only respond to Left/Right keys (Up/Down blocked)

### Window Click-Outside (Cascading)
- [ ] Open WndHeroInfo → Subclass tab → Click info button
- [ ] Click a talent to open WndInfoTalent
- [ ] Click outside WndInfoTalent to close it
- [ ] Click outside WndInfoSubclass
- [ ] Verify WndInfoSubclass closes (not cascading)
- [ ] Verify WndHeroInfo remains open

### Tab Click After Child Close
- [ ] Open WndHeroInfo → Subclass tab → Click info button
- [ ] Click outside WndInfoSubclass to close it
- [ ] Click on another tab icon (e.g., Talents tab)
- [ ] Verify tab switches correctly (window doesn't close)
- [ ] Repeat test with WndRanking and its tabs

---

## Related Changes

- Changelog #71-74: Previous keyboard accessibility improvements
- Changelog #85-86: Previous window close unification and focus fixes

---
