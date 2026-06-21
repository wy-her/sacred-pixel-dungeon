# 046. 광전사 텍스트 업데이트 및 끝없는 분노 버그 수정

**날짜**: 2026-04-17

## 개요

광전사(Berserker) 관련 텍스트 수정 및 끝없는 분노(Endless Rage) 특성 버그 수정. 분노가 최소 임계값 아래일 때 데미지를 받으면 즉시 최소값으로 점프하는 버그를 해결.

---

## 변경 사항

### 1. Berserker Class Description Update
**File:** `actors_*.properties` (line 836)

- **Before:** "광전사가 광포화 한 이후에는 회복을 할 시간이 필요합니다."
- **After:** "광전사가 광포화 한 이후에는 100턴의 회복 시간이 필요합니다."

All languages updated to explicitly mention "100 turns" recovery time.

### 2. Deathless Fury Talent Description Format Change
**File:** `actors_*.properties` (line 935)

Changed format from:
```
[Common description]
+1: [cooldown only]
+2: [cooldown only]
+3: [cooldown only]
```

To unified format (matching other talents):
```
+1: [Full description with 300 turn cooldown]
+2: [Full description with 200 turn cooldown]
+3: [Full description with 100 turn cooldown]

[Common warning about death at 0 HP]
```

### 3. Multilingual Support
All language files updated with consistent format:
- `actors.properties` (English)
- `actors_ko.properties` (Korean)
- `actors_de.properties` (German)
- `actors_fr.properties` (French)
- `actors_ja.properties` (Japanese)
- `actors_zh.properties` (Simplified Chinese)
- `actors_zh-hant.properties` (Traditional Chinese)
- And all other supported languages

### 4. Endless Rage Bug Fix
**File:** `Berserk.java`

**Problem:** When Endless Rage talent was active, taking damage would instantly set rage to the minimum threshold (10%/20%/30%), even if current rage was below that threshold.

**Solution:** Modified the rage decay logic in `act()` method:
```java
// Endless Rage: Only prevent decay below minimum if we were already at or above minimum
// This prevents instantly gaining rage to minimum when hit with low rage
if (previousPower >= minPower && power < minPower) {
    power = minPower;
}
```

**Behavior after fix:**
- Minimum rage threshold only applies during natural decay
- If rage is below minimum, it does NOT jump to minimum when hit
- When berserking ends, rage always resets to 0 (regardless of Endless Rage talent)

---

## Technical Details

### Files Modified
- `core/src/main/assets/messages/actors/actors*.properties` (all language files)
- `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/buffs/Berserk.java`

### Key Code Changes in Berserk.java
1. `act()` method (lines 140-152): Added `previousPower >= minPower` condition
2. Berserk end (lines 122-123, 132-133): `power = 0f;` ensures rage resets to 0

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/src/main/assets/messages/actors/actors*.properties` | 모든 언어 파일에 버서커 설명 및 특성 텍스트 업데이트 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/buffs/Berserk.java` | act() 메서드에 previousPower 조건 추가, 버서킹 종료 시 power=0 설정 |

---

## Testing Notes
- Verify rage does not jump to minimum when hit with low rage
- Verify rage decays normally and stops at minimum threshold
- Verify rage resets to 0 after berserking ends
- Check all language files display correctly in-game

---
