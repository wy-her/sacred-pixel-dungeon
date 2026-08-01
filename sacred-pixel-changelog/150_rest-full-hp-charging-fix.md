# 150. HP 가득 찬 상태에서 휴식 허용

**날짜**: 2026-08-02

## 배경

HP가 가득 찬 상태에서 대기 버튼을 길게 눌러 휴식을 시도하면, 단일 대기로 강제 변환되어 유물이나 마법막대 충전을 위한 자동 휴식이 불가능했습니다. HP 회복이 필요 없더라도 충전 대기를 위해 휴식하는 것은 유효한 플레이 패턴입니다.

---

## 변경 사항

### Hero.java - rest() 메서드 HP 조건 제거

**문제**: HP가 가득 차면 `fullRest = false`로 강제 변환되어 자동 턴 진행이 안 됨

**원인 분석**:

```java
// 기존 코드
boolean canRegenerate = Regeneration.regenOn() && HP < HT && !isStarving();
if (!canRegenerate) {
    fullRest = false;
    // ...
}
```

`HP < HT` 조건으로 인해 HP가 가득 차면 휴식이 차단됨. 그러나 유물/마법막대 충전은 HP 상태와 무관하게 매 턴 자동 진행되므로, 휴식 상태에서 충전 대기가 유효한 플레이 패턴임.

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/hero/Hero.java`

```diff
-		// Prevent full rest when HP regeneration is not possible
+		// Prevent full rest only when regeneration is restricted or starving
+		// HP >= HT is allowed to enable artifact/wand charging while resting
 		if (fullRest) {
-			boolean canRegenerate = Regeneration.regenOn() && HP < HT && !isStarving();
-			if (!canRegenerate) {
-				fullRest = false;
-				if (!Regeneration.regenOn()) {
-					GLog.w(Messages.get(this, "no_rest_regen"));
-				} else if (isStarving()) {
-					GLog.w(Messages.get(this, "no_rest_starving"));
-				}
-				// If HP >= HT, silently convert to single wait (no message needed)
+			if (!Regeneration.regenOn()) {
+				GLog.w(Messages.get(this, "no_rest_regen"));
+				fullRest = false;
+			} else if (isStarving()) {
+				GLog.w(Messages.get(this, "no_rest_starving"));
+				fullRest = false;
 			}
 		}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../actors/hero/Hero.java:1508-1518` | `rest()` 메서드에서 `HP < HT` 조건 제거 |

---

## 영향

### 동작 변경

| 상황 | 변경 전 | 변경 후 |
|------|--------|--------|
| HP 가득 참 | 휴식 불가 (단일 대기) | 휴식 가능 (충전 대기) |
| `Regeneration.regenOn() = false` | 메시지 + 휴식 불가 | 동일 |
| 굶주림 상태 | 메시지 + 휴식 불가 | 동일 |

### 휴식 종료 조건 (변경 없음)

- HP가 `regencap()`에 도달하면 자동 종료 (`Regeneration.act()`)
- 적 발견 시 `interrupt()` 호출로 종료
- 플레이어 입력 시 종료

### 사용 시나리오

- HP가 가득 찬 상태에서 유물(CloakOfShadows, HornOfPlenty 등) 충전 대기
- HP가 가득 찬 상태에서 마법막대 충전 대기
- 굶주림 상태에서 HP는 안 차지만 충전은 되는 플레이 패턴

---

## 관련 코드

- `Regeneration.regenOn()` - 회복 가능 여부 체크 (LockedFloor, VaultLevel)
- `Regeneration.act()` - HP가 regencap()에 도달하면 `resting = false`
- `Hero.interrupt()` - 적 발견/피해 시 휴식 중단
- #102 - Eye DeathGaze 휴식 중단 버그 수정 (`damageInterrupt` 플래그)
