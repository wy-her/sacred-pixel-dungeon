# 099. 시드 고정 및 타겟팅 마크 버그 수정

**날짜**: 2026-06-21

## 개요

랭킹 "다시 플레이하기" 기능의 시드 고정 미작동 버그와 원거리 공격 시 포커싱 마크가 사라지지 않는 버그 수정.

---

## 변경 사항

### Bug Fixes (2)

---

### [B-1] Random.java - TeaVM 환경 java.util.Random 비결정론적 동작 수정

**문제**: "다시 플레이하기" 시 시드가 표시는 올바르게 되지만 실제 던전 생성에 다른 시드가 사용됨

**원인 분석**:
- InterlevelScene에서 customSeed가 설정되어 있을 때 initSeed() 스킵 로직은 정상 동작
- **진짜 원인**: TeaVM 환경에서 `java.util.Random`이 동일한 시드로도 다른 결과 반환
- `seedForDepth()` 함수가 `Random.pushGenerator(seed)`로 새 generator 생성 시 매번 다른 시퀀스 생성

**파일**: `SPD-classes/src/main/java/com/watabou/utils/Random.java`

**수정**: `java.util.Random` 대신 결정론적 **SplitMix64 PRNG** 구현

```java
// 기존: java.util.Random 사용 (TeaVM에서 비결정론적)
private static ArrayDeque<java.util.Random> generators;

// 수정: 커스텀 DeterministicRandom 클래스
private static class DeterministicRandom {
    private long state;

    public DeterministicRandom(long seed) {
        this.state = seed;
    }

    // SplitMix64 algorithm - 플랫폼 독립적, 결정론적
    private long nextSplitMix64() {
        long z = (state += 0x9e3779b97f4a7c15L);
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    public long nextLong() { return nextSplitMix64(); }
    public int nextInt() { return (int)(nextSplitMix64() >>> 32); }
    // ... nextFloat(), nextDouble() 등
}

private static ArrayDeque<DeterministicRandom> generators;
```

**추가 수정 (InterlevelScene.java)**: customSeed가 설정된 경우 initSeed() 스킵
```java
if (Dungeon.hero == null) {
    Mob.clearHeldAllies();
    if (SPDSettings.customSeed().isEmpty()) {
        Dungeon.initSeed();  // fallback만 호출
    }
    Dungeon.init();
```

---

### [B-2] 원거리 타겟팅 크로스헤어가 근접 공격 후 사라지지 않는 문제 수정

**문제**: 원거리 무기(Spirit Bow 등) 타겟팅 모드에서 AttackIndicator로 근접 공격 시 크로스헤어가 사라지지 않음

**원인 분석**:
1. Spirit Bow 클릭 시 `GameScene.selectCell(shooter)` 호출로 셀 선택 모드 진입
2. AttackIndicator 클릭 시 `QuickSlotButton.cancel()` 호출하지만 셀 선택 모드는 취소 안됨
3. 공격 완료 후 `Hero.checkVisibleMobs()`가 `target()` 재호출하여 마크 재생성

**수정 내용**:

**1. QuickSlotButton.java** - `targetingCancelled` 플래그 추가
```java
private static boolean targetingCancelled = false;

// target() - TargetHealthIndicator만 플래그로 제어, lastTarget은 항상 업데이트
public static void target( Char target ) {
    if (target != null && target.alignment != Char.Alignment.ALLY) {
        lastTarget = target;
        // targetingCancelled일 때 TargetHealthIndicator만 숨김
        if (!targetingCancelled) {
            TargetHealthIndicator.instance.target( target );
        }
        InventoryPane.lastTarget = target;
    }
}

// cancel() - 플래그 설정
public static void cancel() {
    // 크로스헤어 제거
    for (QuickSlotButton btn : instance) {
        if (btn != null) {
            btn.crossB.visible = false;
            btn.crossM.remove();
        }
    }
    targetingSlot = -1;
    TargetHealthIndicator.instance.target(null);
    targetingCancelled = true;
}

// useTargeting() - 명시적 타겟팅 시 플래그 리셋
private void useTargeting() {
    if (lastTarget != null && ...) {
        targetingCancelled = false;  // 사용자가 명시적으로 타겟팅 시작
        ...
    }
}
```

**2. AttackIndicator.java** - `GameScene.cancel()` 호출 추가
```java
protected synchronized void onClick() {
    if (Dungeon.hero.ready && lastTarget != null && ...) {
        // 셀 선택 모드 먼저 취소 (Spirit Bow 등의 타겟팅 해제)
        GameScene.cancel();
        if (Dungeon.hero.handle( lastTarget.pos )) {
            Dungeon.hero.next();
            enable(false);
            QuickSlotButton.cancel();
            InventoryPane.cancelTargeting();
        }
    }
}
```

**3. Hero.java** - `lastTarget`은 항상 업데이트 (auto-aim 유지)
```java
// isTargetingCancelled() 체크 제거 - lastTarget은 항상 업데이트
// target()이 TargetHealthIndicator 표시만 제어
if (target != null && (lastTarget == null || ...)){
    QuickSlotButton.target(target);
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `SPD-classes/.../Random.java` | java.util.Random → SplitMix64 DeterministicRandom 교체 |
| `InterlevelScene.java` | customSeed가 비어있을 때만 initSeed() 호출 |
| `QuickSlotButton.java` | targetingCancelled 플래그 추가, cancel()/target()/useTargeting() 수정 |
| `AttackIndicator.java` | GameScene.cancel() 호출 추가 |
| `Hero.java` | checkVisibleMobs()에서 lastTarget 항상 업데이트 |

---

## 테스트 방법

### 시드 고정 테스트
1. 게임 완료 후 랭킹에서 "다시 플레이하기" 선택
2. 영웅정보 창에서 시드 확인
3. 첫 번째 층의 방 배치, 아이템 위치가 원본과 **정확히 동일**한지 확인

### 크로스헤어 테스트
1. 사냥꾼으로 Spirit Bow 장착
2. 적과 인접한 상태에서 Spirit Bow 퀵슬롯 클릭 (크로스헤어 표시)
3. **AttackIndicator 버튼(우측 하단)으로 근접 공격** 실행
4. 공격 후 크로스헤어가 **사라지는지** 확인
5. Spirit Bow 다시 클릭 시 크로스헤어가 **적 위치에** 나타나는지 확인

---
