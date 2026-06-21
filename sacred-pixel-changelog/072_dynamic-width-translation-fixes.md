# 072. 동적 너비 창 및 번역 수정

**날짜**: 2026-05-27

## 개요

This session added content-based dynamic width expansion to multiple Window classes and fixed translation/formatting inconsistencies across all 23 languages.

---

## 변경 사항

### 1. Dynamic Width Window Implementation

Added dynamic width expansion (149px → 251px) to the following windows:

#### WndOptions.java
- Changed from fixed `WIDTH_P`/`WIDTH_L` to dynamic `WIDTH_MIN`/`WIDTH_MAX`
- Added `targetHeight()` method returning `PixelScene.MIN_HEIGHT_L - 10`
- Window expands in 20px increments in landscape mode when content exceeds target height

#### WndOptionsCondensed.java
- Same dynamic width pattern as WndOptions
- Extracted button layout to `layoutButtons()` method for re-layout during width expansion

#### WndHeroInfo.java
- Added `TARGET_HEIGHT = 152` (matches WndRanking HEIGHT)
- Window expands to accommodate hero info tabs without vertical overflow

#### WndChooseSubclass.java
- Added dynamic width with same pattern
- Stores buttons/infoButtons in ArrayLists for re-layout during width expansion

#### WndChooseAbility.java
- Added dynamic width with same pattern
- Re-layouts ability buttons and info buttons during width expansion

### 2. Translation Formatting Fix — Rankings Message

**File Pattern:** `core/src/main/assets/messages/scenes/scenes_*.properties` (23 files)

Changed `scenes.datascene.rankings` to include `\n` line break before "(Best: %,d)" for consistent two-line display:

| Language | Before | After |
|----------|--------|-------|
| English | `Rankings: %d records (Best: %,d)` | `Rankings: %d records\n(Best: %,d)` |
| Korean | `순위: %d개 (최고: %,d점)` | `순위: %d개\n(최고: %,d점)` |
| German | `Rangliste: %d Einträge (Bester: %,d)` | `Rangliste: %d Einträge\n(Bester: %,d)` |
| ... | ... | ... |

### 3. German Translation Fix — "Hintergrundgeschichte" → "Geschichte"

**Problem:** The German word "Hintergrundgeschichte" (20 characters) was causing text to overflow and wrap incorrectly in the DataScene import preview.

**Solution:** Changed to shorter "Geschichte" for consistency.

**Files Modified:**

#### scenes_de.properties
```properties
# Before
scenes.datascene.lore=Hintergrundgeschichte: %d/%d

# After
scenes.datascene.lore=Geschichte: %d/%d
```

#### windows_de.properties
```properties
# Before
windows.wndjournal$catalogtab.title_lore=Hintergrundgeschichte
windows.wndjournal$catalogtab.not_seen_lore=Du hast bisher bei keinem deiner Läufe diesen Teil der Hintergrundgeschichte gefunden.

# After
windows.wndjournal$catalogtab.title_lore=Geschichte
windows.wndjournal$catalogtab.not_seen_lore=Du hast bisher bei keinem deiner Läufe diesen Teil der Geschichte gefunden.
```

---

## Technical Details

### Dynamic Width Pattern

```java
private static final int WIDTH_MIN = 149;
private static final int WIDTH_MAX = 251;

// In constructor after initial layout:
while (PixelScene.landscape()
        && totalHeight > targetHeight()
        && width < WIDTH_MAX) {
    width += 20;
    // Re-layout all components
}

protected float targetHeight() {
    return PixelScene.MIN_HEIGHT_L - 10;
}
```

### Key Points
- Dynamic width only applies in landscape mode
- Width increases by 20px increments
- Maximum width is 251px
- Target height is `PixelScene.MIN_HEIGHT_L - 10` for most windows
- WndHeroInfo uses `TARGET_HEIGHT = 152` to match WndRanking

---

## Files Modified

| File | Changes |
|------|---------|
| `ui/WndOptions.java` | Dynamic width implementation |
| `ui/WndOptionsCondensed.java` | Dynamic width implementation |
| `windows/WndHeroInfo.java` | Dynamic width implementation |
| `windows/WndChooseSubclass.java` | Dynamic width implementation |
| `windows/WndChooseAbility.java` | Dynamic width implementation |
| `messages/scenes/scenes_*.properties` (23 files) | Rankings `\n` formatting |
| `messages/scenes/scenes_de.properties` | "Geschichte" translation |
| `messages/windows/windows_de.properties` | "Geschichte" translation |

---

## Testing Notes

1. **Dynamic Width Windows**
   - Test WndOptions with long text in landscape mode
   - Test WndHeroInfo tabs don't overflow vertically
   - Test WndChooseSubclass/WndChooseAbility button layout

2. **Rankings Formatting**
   - Verify DataScene shows rankings on two lines
   - Check import preview formatting in all languages

3. **German Translation**
   - Verify DataScene import preview doesn't overflow
   - Check Journal lore tab title displays correctly

---
