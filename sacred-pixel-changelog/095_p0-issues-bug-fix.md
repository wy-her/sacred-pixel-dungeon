# 095. P0 이슈 버그 수정 (v4.0.3)

**날짜**: 2026-06-15

## 개요

Sacred Pixel Dungeon v4.0.3 릴리스. 6개의 버그 수정 (Critical 1개, High 5개). BitmapText NPE 크래시, GnollGeomancer null safety, TeaVM 저장 race condition, 이벤트 리스너 메모리 누수, Y좌표 오타 수정.

---

## 변경 사항

### Bug Fixes (6)

### Critical (1)

#### [C-1] BitmapText.java - NPE Crash Fix
- **File**: `SPD-classes/src/main/java/com/watabou/noosa/BitmapText.java:125-127`
- **Issue**: Dead code `rect=null` followed by `font.width(rect)` causing NullPointerException
- **Fix**: Changed to `continue;` to skip null characters in font rendering loop

```java
// Before
if (rect == null) {
    rect=null;
}

// After
if (rect == null) {
    continue;
}
```

---

### High (5)

#### [H-1] GnollGeomancer.java - hasSapper() Null Safety
- **File**: `core/src/main/java/.../actors/mobs/GnollGeomancer.java:316-320`
- **Issue**: Double `Actor.findById()` call with missing null check
- **Fix**: Cache result in local variable with null-safe check

```java
// Before
public boolean hasSapper(){
    return sapperID != -1
            && Actor.findById(sapperID) instanceof GnollSapper
            && ((GnollSapper)Actor.findById(sapperID)).isAlive();
}

// After
public boolean hasSapper(){
    if (sapperID == -1) return false;
    Actor sapper = Actor.findById(sapperID);
    return sapper instanceof GnollSapper && ((GnollSapper)sapper).isAlive();
}
```

#### [H-2] GnollGeomancer.java - Array Index Bounds Check
- **File**: `core/src/main/java/.../actors/mobs/GnollGeomancer.java:377-378`
- **Issue**: `path.path.get(12)` called without bounds validation
- **Fix**: Added `path.path.size() > 12` condition

```java
// Before
if (path.dist > 12){
    dashPos = path.path.get(12);
}

// After
if (path.dist > 12 && path.path.size() > 12){
    dashPos = path.path.get(12);
}
```

#### [H-6] TeaVMLauncher.java - Save Race Condition
- **File**: `teavm/src/main/java/.../teavm/TeaVMLauncher.java:343-374`
- **Issue**: `saveInProgress` flag race condition between visibility/pagehide events
- **Fix**: Added `synchronized(saveLock)` block around entire `onSave()` method

```java
// Added
private static final Object saveLock = new Object();

// onSave() body wrapped with
synchronized (saveLock) {
    // ... existing save logic
}
```

#### [H-7/H-16] TeaVMInterstitialAd.java - Event Listener Memory Leak
- **File**: `teavm/src/main/java/.../teavm/TeaVMInterstitialAd.java:138-164`
- **Issue**: Event listeners registered but never removed, causing memory leak
- **Fix**: Added `removeEventListener` calls in callback completion paths

```javascript
// Added in 2 places
window.removeEventListener('message', msgHandler);
```

#### [H-11] PointerEvent.java - Y-Coordinate Typo
- **File**: `SPD-classes/src/main/java/com/watabou/input/PointerEvent.java:67`
- **Issue**: Copy-paste error using `Game.width` instead of `Game.height` for y-coordinate
- **Fix**: Changed to `Game.height/2`

```java
// Before
y = Game.width/2;

// After
y = Game.height/2;
```

---

## 수정된 파일

| File | Changes |
|------|---------|
| `BitmapText.java` | NPE crash fix (line 125-127) |
| `GnollGeomancer.java` | hasSapper() null safety, array bounds check |
| `TeaVMLauncher.java` | Save race condition fix, version update |
| `TeaVMInterstitialAd.java` | Event listener memory leak fix |
| `PointerEvent.java` | Y-coordinate typo fix |
| `build.gradle` | Version update (902, 4.0.3) |

## Deferred Issues (46)

### Summary

| Priority | Count | Reason |
|----------|-------|--------|
| P1 (High) | 12 | No immediate risk, scheduled for v4.0.4 |
| P2 (Medium) | 20 | Cosmetic or high modification risk |
| P3 (Low) | 14 | No gameplay impact |

### NEVER-CHANGE Items
- `TutorialManager.java` - Tutorial state machine
- `TeaVMLauncher.java` (browser compat) - Browser compatibility code
- `FreeTypeFontGenerator.java` - Font rendering core
- `TeaVMCloudSave.java` - Cloud save logic

---

## Build Output

```
Build: teavm/build/dist/cloudflare/webapp/
Command: ./gradlew --no-daemon teavm:buildRelease
```

---
