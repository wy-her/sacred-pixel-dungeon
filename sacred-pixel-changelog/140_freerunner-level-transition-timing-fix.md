# 140. 프리러너 Freerunning/Recovering 상태 층 이동 및 채굴 후 이동 불가 버그 수정

**날짜**: 2026-07-25

## 배경

프리러너가 **Freerunning 상태**(가속 중) 또는 **Recovering 상태**(쿨다운 중)에서 다음 행동을 하면 이동이 불가능해지는 버그가 있었습니다:
- 층 이동(사다리, Beacon of Returning 등)
- 채굴(Mining)

턴을 소비하는 행동(은신, 아이템 던지기 등)을 하면 다시 이동이 가능해졌습니다.

**추가 증상**:
- 층 이동 후 **적 인디케이터가 이전 층 상태로 고정**됨
- 예: 15층에서 적 2마리 → 16층 이동 → 적이 없는데 인디케이터에 2 표시
- 턴 소모 행동 후 인디케이터가 정상 업데이트됨

**참고**:
- #134에서 `hero.ready = true` 추가로 "모든 직업의 레벨 전환 후 이동 불가" 버그를 수정했으나, Momentum 축적 중이 아닌 상태에서는 여전히 문제가 발생했습니다.
- #136에서 채굴 콜백 타이밍 문제를 수정했으나, Freerunning/Recovering 상태에서는 여전히 문제가 발생했습니다.

---

## 원인 분석

### Root Cause 1: 조건부 `postpone()` 호출

`Momentum.gainStack()`과 `Momentum.onNonMovementAction()` 모두에서 `postpone()` 호출이 조건부로 제한되어 있었습니다:

```java
// gainStack() - 이동 시 호출
public void gainStack(){
    movedLastTurn = true;
    if (freerunCooldown <= 0 && !freerunning()){  // ← 조건
        postpone(target.cooldown()+(1/target.speed()));
        momentumStacks = Math.min(momentumStacks + 1, 10);
        // ...
    }
}

// onNonMovementAction() - 채굴, 사다리, 비콘 등 비이동 액션 시 호출
public void onNonMovementAction(){
    movedLastTurn = true;
    if (freerunCooldown <= 0 && !freerunning()){  // ← 동일한 조건
        postpone(target.cooldown()+(1/target.speed()));
    }
}
```

### Root Cause 2: `switchLevel()` 후 `checkVisibleMobs()` 미호출

`Dungeon.switchLevel()`에서 `observe()`를 호출하여 FOV를 업데이트하지만, `hero.checkVisibleMobs()`를 호출하지 않아:
- `visibleEnemies` 리스트가 이전 층 상태로 유지
- DangerIndicator/AttackIndicator가 이전 층의 적 정보를 표시

### 상태별 조건 충족 여부

| 상태 | `freerunCooldown <= 0` | `!freerunning()` | `postpone()` 호출 |
|------|------------------------|------------------|-------------------|
| Momentum 축적 중 | TRUE | TRUE | **호출됨** |
| Freerunning (가속 중) | TRUE | **FALSE** | 미호출 |
| Recovering (쿨다운 중) | **FALSE** | TRUE | 미호출 |

### 버그 발생 메커니즘

```
1. 레벨 전환 직후
   ├─ Actor.init() → Hero.time = 0, Momentum.time = 0
   ├─ observe() → FOV 업데이트
   └─ checkVisibleMobs() 미호출 → visibleEnemies 이전 층 상태 유지

2. 사용자 이동 입력 → gainStack() 호출
   ├─ Freerunning/Recovering 상태: 조건 FALSE
   └─ postpone() 미호출 → Momentum.time 갱신 안됨

3. 다음 턴 Actor 선택
   ├─ Momentum.time(0) < Hero.time(0.5)
   └─ Momentum이 Hero보다 먼저 선택됨 (턴 순서 역전)

4. 결과
   ├─ Hero.act() 미호출 → checkVisibleMobs() 미호출
   └─ 이동 불가 + 인디케이터 고정
```

---

## 변경 사항

### 1. Momentum.gainStack() - minDelay로 postpone 보장

**문제**: 레벨 전환 후 Momentum.time=1, Hero.time=0일 때 `postpone(0.5)` 호출이 실패함.
- postpone 조건: `if (this.time < now + time)` → `if (1 < 0 + 0.5)` → FALSE
- Momentum이 이미 앞서 있어서 postpone가 무시됨

**해결**: `minDelay = cooldown() + 0.01`을 사용하여 항상 현재 시간을 넘도록 보장

```diff
  public void gainStack(){
      movedLastTurn = true;
-     postpone(target.cooldown()+(1/target.speed()));
+     // Sync timing - delay must exceed current cooldown() to actually push forward
+     // After level transition: Momentum.time=1, Hero.time=0, now=0
+     // Without minDelay: postpone(0.5) fails because 1 < 0.5 is false
+     // With minDelay: postpone(1.01) succeeds because 1 < 1.01 is true
+     float delay = target.cooldown() + 1/target.speed();
+     float minDelay = cooldown() + 0.01f;
+     postpone(Math.max(delay, minDelay));
      if (freerunCooldown <= 0 && !freerunning()){
          momentumStacks = Math.min(momentumStacks + 1, 10);
          ActionIndicator.setAction(this);
          BuffIndicator.refreshHero();
      }
  }
```

### 2. Momentum.onNonMovementAction() - 동일한 minDelay 적용

```diff
  public void onNonMovementAction(){
      movedLastTurn = true;
-     postpone(target.cooldown()+(1/target.speed()));
+     // Same logic as gainStack() - must exceed current cooldown to push forward
+     float delay = target.cooldown() + 1/target.speed();
+     float minDelay = cooldown() + 0.01f;
+     postpone(Math.max(delay, minDelay));
  }
```

### 3. Dungeon.switchLevel() - FOV 동기화 및 checkVisibleMobs() 호출 추가

```diff
  observe();
+ // Sync hero's FOV reference and update visible enemies immediately after level transition
+ // This fixes the enemy indicator staying stuck at previous floor count
+ if (level != null && level.mobs != null) {
+     hero.fieldOfView = level.heroFOV;
+     hero.checkVisibleMobs();
+ }
  try {
      saveAll();
```

### 왜 안전한가

1. **`postpone()`의 멱등성**: `if (this.time < now + time)` 조건으로 중복 호출 시 첫 번째 호출만 적용
2. **스택 로직 분리**: 스택 증가는 여전히 조건부, 타이밍 동기화만 무조건 실행
3. **기존 동작 유지**: Momentum 축적 중에는 기존과 동일하게 작동
4. **`fieldOfView` 동기화**: `Hero.act()`와 동일하게 `hero.fieldOfView = level.heroFOV` 할당 후 호출
5. **Null 안전성**: `level`과 `level.mobs` null 체크로 초기화 중 오류 방지

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/buffs/Momentum.java:98-108` | `gainStack()`에서 `postpone()` 호출을 조건문 외부로 이동 |
| `actors/buffs/Momentum.java:115-120` | `onNonMovementAction()`에서 조건문 제거, `postpone()` 항상 호출 |
| `Dungeon.java:507-513` | `observe()` 후 FOV 동기화 및 `hero.checkVisibleMobs()` 호출 추가 |

---

## 영향

- Freerunning 상태에서 층 이동 후 즉시 이동 가능
- Recovering 상태에서 층 이동 후 즉시 이동 가능
- Freerunning/Recovering 상태에서 채굴 후 즉시 이동 가능
- **층 이동 후 적 인디케이터 즉시 업데이트**
- Momentum 축적 중에는 기존과 동일하게 작동
- 속도 보너스, 스택 축적, 쿨다운 처리에 영향 없음

---

## 관련 코드

- `Momentum.gainStack()` - 이동 시 스택 증가 및 타이밍 동기화
- `Momentum.onNonMovementAction()` - 비이동 액션 시 타이밍 동기화
- `Momentum.act()` - actPriority = HERO_PRIO+1로 Hero보다 먼저 행동
- `Actor.postpone()` - Actor의 다음 행동 시간 조정
- `Hero.checkVisibleMobs()` - 보이는 적 리스트 업데이트
- `Dungeon.switchLevel()` - 레벨 전환 처리
- `Dungeon.observe()` - FOV 업데이트
- 관련 changelog: #133, #134, #136
