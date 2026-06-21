# Sacred Pixel Dungeon - Modifications

This document outlines the key modifications from [Shattered Pixel Dungeon v3.3.8](https://github.com/00-Evan/shattered-pixel-dungeon).

---

## Platform: HTML5/Web Build

Sacred Pixel Dungeon is built for **web browsers** using TeaVM (Java-to-JavaScript transpiler) instead of native Android/Desktop.

### Build Targets

| Build | Command | Font Size | Use Case |
|-------|---------|-----------|----------|
| Cloudflare | `buildRelease` | Full (27MB) | Web deployment |
| Capacitor | `buildRelease` + APK | Full (27MB) | Android wrapper |
| Apps In Toss | `buildAppsintoss` | Subset (2MB) | Toss mini-app |
| Debug | `runDebug` | Full (27MB) | Local development |

### Technical Stack

| Component | Technology |
|-----------|------------|
| Game Engine | LibGDX |
| Web Transpiler | TeaVM |
| Build System | Gradle |
| Font Rendering | HTML5 Canvas 2D |
| Storage | localStorage |
| Compression | pako.js (DEFLATE) |

### Platform Compatibility Layer

TeaVM lacks some Java APIs, so custom implementations are provided:

| Java Feature | SPD Implementation |
|--------------|-------------------|
| Reflection | `TeaVMClassRegistry` - pre-registered class factories |
| String.format() | `StringCompat` - full format specifier support |
| GZIP | Disabled (throws `UnsupportedOperationException`) |
| Threading | No-op (single-threaded JavaScript) |
| org.json | GWT-compatible Map-based implementation |
| java.util.Random | `DeterministicRandom` - SplitMix64 PRNG for cross-platform seed consistency |

### Deterministic Random Number Generation

TeaVM's `java.util.Random` produces non-deterministic results even with the same seed, breaking features like "Play Again" from rankings. A custom `DeterministicRandom` class using the **SplitMix64 algorithm** ensures identical dungeon generation across all platforms.

**File:** `SPD-classes/src/main/java/com/watabou/utils/Random.java`

---

## Canvas-Based Font Rendering

FreeType is replaced with **HTML5 Canvas 2D** font rendering:

### How It Works

1. **CSS @font-face** loads WOFF2 fonts in browser
2. **Canvas 2D** renders glyphs via `ctx.fillText()`
3. **WebGL texture upload** via `texImage2D()`
4. **Dynamic atlas building** - glyphs rendered on-demand
5. **Context loss recovery** - rebuilds textures if WebGL context lost

### Language-Aware Font Stack

CSS font-family stack automatically selects optimal font per glyph:

| Language | Primary Font | Fallback |
|----------|--------------|----------|
| Japanese | Noto Sans JP (11MB) | Noto Sans SC |
| Korean | Noto Sans KR (3.8MB) | Noto Sans SC |
| Simplified Chinese | Noto Sans SC (7.8MB) | - |
| Traditional Chinese | Noto Sans TC (5.2MB) | Noto Sans SC |
| Default | Inter (342KB) | Noto Sans SC |

**Note**: Simplified Chinese (SC) chosen as fallback for widest Han coverage.

---

## Keyboard Accessibility

Full keyboard navigation support throughout the game, implemented via a new `Focusable` interface.

### Core System

**Focusable Interface** (`ui/Focusable.java`):
- `setFocused(boolean)` - toggle visual focus state
- `saveFocusState()` / `restoreFocusState()` - preserve appearance
- `click()` - programmatic activation (Enter key support)
- `isActive()` - check if component is enabled

**Window Focus Management** (`ui/Window.java`):
- `focusableButtons` list tracks all navigable components
- Arrow keys move focus with wrapping
- Enter activates focused element
- ESC/Back closes window
- Window stacking: only topmost window receives input

### Visual Feedback

| Component | Focus Effect |
|-----------|-------------|
| StyledButton | Background brightness 1.3x, icon brightness 1.5x |
| IconButton | Icon color/brightness change |
| OptionSlider | Background highlight |

### Scene-Level Navigation

| Scene | Navigation Features |
|-------|-------------------|
| TitleScene | Up/Down arrows navigate menu, Enter activates |
| HeroSelectScene | 2x3 grid navigation, 3 UI layers (heroes, actions, options) |
| SettingsScene | Tab cycling, multi-panel navigation |
| GameScene | Death screen keyboard control |

### Window Navigation

| Window | Navigation Features |
|--------|-------------------|
| WndBag | Arrow keys navigate inventory grid |
| WndTabbed | Tab/Cycle key switches tabs |
| WndSettings | All controls keyboard-accessible |
| All Windows | Arrow keys for buttons, Enter/ESC for confirm/cancel |

### In-Game Controls

| Category | Keys | Action |
|----------|------|--------|
| **Movement** | WAXD+QEZC / Numpad | 8-directional movement |
| | Arrow Keys | 4-directional movement |
| | S / Numpad 5 | Wait or Pickup |
| **Combat** | F | Attack target |
| | G | Use action ability |
| | V | Pick up loot |
| | Space / Tab | Cycle targets |
| **UI** | R | Examine mode |
| | T | Wait one turn |
| | Y | Rest until healed |
| | H | Hero info |
| | J | Journal |
| | 1-6 | Quickslots |
| | 7 / ' | Inventory |
| **Camera** | [ / ] | Zoom out / in |
| | O/K/./; | Pan up/left/down/right |
| | L | Center on hero |
| **Navigation** | Enter | Confirm |
| | ESC / Backspace | Back / Cancel |

---

## Cross-Device Data Sync

Save data can be transferred between devices via URL encoding.

### Export Format (VERSION 0x09)

| Section | Size | Content |
|---------|------|---------|
| Header | 1 byte | Version (0x09) |
| Badges | 24 bytes | 192 badges bitmap |
| Catalog | 50 bytes | 400 items bitmap |
| Bestiary | 25 bytes | 200 monsters bitmap |
| Documents | 16 bytes | 64 pages × 2 bits |
| Rankings | variable | Optimized JSON |

### URL Optimizations

1. **Key shortening**: `__className` → `_c`, `level` → `l`, `quantity` → `q` (60+ mappings)
2. **Class name shortening**: `com.sacredpixel.sacredpixeldungeon.` → `~.`
3. **Backpack trimming**: Only quickslot items kept
4. **DEFLATE compression**: pako.js library
5. **URL-safe Base64**: RFC 4648 with `+/` → `-_`, no padding

### Import Options

- **Merge (Union)**: Combine imported data with local
- **Overwrite**: Replace local data entirely

---

## Gameplay Balance Changes

### Berserker Talent Rework

**Deathless Fury**:
- Changed from level-based cooldown to turn-based cooldown
- +1: 300 turns / +2: 200 turns / +3: 100 turns
- Activation requires 100% rage at all levels

**Endless Rage**:
- Changed from max rage increase to minimum rage floor
- +1: 10% / +2: 20% / +3: 30% minimum rage
- Rage won't decay below minimum (resets to 0 when berserking ends)

### Chaotic Censer Adjustment

| Change | Before | After |
|--------|--------|-------|
| Gas spawn distance | ≥2 tiles | ≥3 tiles |
| ToxicGas amount | 300f | 150f |
| ConfusionGas amount | 300f | 150f |
| Regrowth amount | 200f | 100f |
| All other gases | 100% | 50% |

**Additional**: Added 100-iteration limit to gas selection loop (safety fix).

### Imp Quest Rework

| Aspect | Before (3.3.8) | After |
|--------|---------------|-------|
| Quest Type | Monks OR Golems (based on floor) | Senior Monks only |
| Enemy Spawn | Normal spawns | 5 Senior Monks spawned on accept |
| Token Requirement | 4-5 tokens | 1+ token |
| Quest Tracking | None | `questDepth` field added |

**Reward Quality Scaling**:
| Tokens | Reward |
|--------|--------|
| 1 | +0 cursed ring |
| 2 | +1 cursed ring |
| 3 | +2 cursed ring |
| 4 | +3 cursed ring |
| 5 | +3 uncursed ring |

### Blacksmith Quest Simplification

The Caves blacksmith quest has been streamlined for improved beginner accessibility:

| Aspect | Before | After |
|--------|--------|-------|
| Large Rooms | 3 | 2 |
| Small Rooms | 6-8 | 3-4 |
| Secret Rooms | 2 | 1 |
| Dark Gold Deposits | 45-47 | 20-22 |
| Gold-to-Favor Ratio | 50 | 100 |

**Unchanged**:
- Maximum favor: 3000 (2000 from gold + 1000 from boss)
- Reward tiers and free pickaxe threshold (2500 favor)

**Quest-Specific Impact**:

| Quest Type | Change |
|------------|--------|
| Crystal | 2 CrystalGuardians instead of 3 (no phase system, simpler fight) |
| Gnoll | 2 GnollSappers instead of 3 → GnollGeomancer has 2 dash phases instead of 3 |

**Rationale**: Original quest required significant grinding (40+ gold pieces). Reduced scale maintains same rewards while cutting play time by ~50%.

### Rat King Summoning Altar

A new summoning altar on floor 20 allows players to summon the Rat King after defeating the Dwarf King.

| Aspect | Details |
|--------|---------|
| Location | Floor 20 exit corridor, below Imp shop |
| Requirement | `Statistics.ratKingAwoken == true` (awakened Rat King on floor 5) |
| Trigger | Step on the pedestal to show summoning dialog |
| Result | Rat King teleports to an adjacent empty cell |

**Gameplay Flow**:
1. Awaken Rat King in RatKingRoom on floor 5
2. Defeat Dwarf King on floor 20
3. Find summoning altar room below Imp shop
4. Step on altar → Select "Invoke the Summoning"
5. Trade King's Crown to Rat King → Receive Ratmogrify ability

### Talent System Changes

| Change | File | Effect |
|--------|------|--------|
| CombinedLethality persistence | Talent.java | Weapon choice saved across game saves |
| LIGHT_CLOAK local parameter | Talent.java | Uses `hero` param instead of `Dungeon.hero` |
| Scroll/Runestone Compat wrapper | Talent.java | `Compat.isAssignableFrom()` for TeaVM |
| WarriorFoodImmunity priority | Talent.java | Changed from `HERO_PRIO+1` to `MOB_PRIO-1` |
| storeTalentsInBundle validation | Talent.java | Clamps talents to maxPoints during save |

### Removed Features

- **Daily Challenge**: Removed due to web game characteristics (same seed exploitable across browsers)
- **Monk/Golem Quest Alternative**: Unified to Senior Monk quest only

---

## Interactive Tutorial System

A beginner-friendly tutorial teaches core game mechanics in approximately 1 minute.

### Tutorial Flow (10 Steps)

| Step | State | Description |
|------|-------|-------------|
| 1 | GUIDEBOOK_PLACED | Pick up the guidebook |
| 2 | INTRO_SHOWN | Story introduction window |
| 3 | JOURNAL_HINT | Tap flashing journal button |
| 4 | HERO_INFO_HINT | Tap flashing hero portrait |
| 5 | EXAMINE_HINT | Tap magnifying glass, examine a tile |
| 6 | RAT_COMBAT | Fight a tutorial rat (HP=1) |
| 7 | ITEM_USE | Use a potion (drink or throw) |
| 8 | SEARCH_HINT | Search for hidden door |
| 9 | SNAKE_SURPRISE | Surprise attack a snake |
| 10 | COMPLETED | Tutorial complete → return to title |

### Implementation

| File | Purpose |
|------|---------|
| `tutorial/TutorialState.java` | 17-state enum for progress tracking |
| `tutorial/TutorialManager.java` | State machine and event handling |
| `windows/WndTutorial.java` | Tutorial hint windows |
| `levels/TutorialLevel.java` | 16x16 tutorial map with hidden room |
| `actors/mobs/TutorialRat.java` | Tutorial rat (HP=1) |
| `actors/mobs/TutorialSnake.java` | Tutorial snake (HP=1) |

### Key Features

- **Button flashing**: Visual cues guide players to correct UI elements
- **No save**: Tutorial games don't save to in-progress slots
- **Localized**: All messages available in 23 languages

---

## Loading Optimization

### Lazy Loading Strategy

| Asset Type | Loading Strategy |
|------------|-----------------|
| Glyphs | Rendered on-demand, not preloaded |
| Music (48MB, 31 files) | Loaded when needed, not at startup |
| CJK Fonts | Only required fonts loaded based on browser language |

**Estimated savings**: 63-74% reduction in initial download size.

### Font Subsetting (Apps In Toss)

Python script subsets fonts to Korean-only characters:
- Full fonts: 27MB
- Subset fonts: 2MB
- Reduction: 93%

---

## UI/UX Improvements

### Window Behavior

- **Pattern B closing**: ESC or outside click to close info windows (prevents accidental closure)
- **Window stacking**: Only topmost window receives input
- **Consistent sizing**: 149px width for info windows
- **Cascading closure prevention**: `skipNextClick` flag prevents chain closures

### Input Handling

- **Touch + keyboard hybrid**: Seamless switching between input methods
- **Browser zoom blocking**: Prevents Ctrl+scroll/pinch zoom interference
- **Attack animation blocking**: Hero input blocked during enemy attack animations
- **Improved auto-move**: Hero stops at exact tile position when interrupted

### Quest & Guide Display

- **Guide documents**: Shown directly instead of journal flash notification
- **Quest dialog advancement**: All close methods (ESC/click/Enter) advance quest equally

### Settings Persistence

- All preferences saved to browser localStorage
- Key prefix: `spd_` for all entries

---

## Module Structure

| Module | Purpose |
|--------|---------|
| `SPD-classes` | Watabou framework (LibGDX wrapper) |
| `core` | Game logic (~1180 Java files) |
| `teavm` | TeaVM/HTML5 build target (new) |
| `services` | Update/news services |

### New TeaVM Module Contents

| Directory | Contents |
|-----------|----------|
| `teavm/src/.../teavm/` | TeaVM launcher, platform support |
| `teavm/src/.../teavm/web/` | Data sync (export/import/merge) |
| `teavm/webapp/` | HTML, CSS, fonts, pako.js |

---

## Credits

- **Original Game**: [Shattered Pixel Dungeon](https://shatteredpixel.com/) by Evan Debenham
- **Original Source**: [Pixel Dungeon](https://github.com/watabou/pixel-dungeon) by Watabou

---

*Based on comparison with Shattered Pixel Dungeon v3.3.8*
*See the sacred-pixel-changelog/ directory for detailed development history.*
