# 133. 프리러너 Momentum 타이밍 불일치 버그 수정

## 배경

프리러너가 특정 액션(곡괭이로 금 채굴, 사다리 이동, Beacon of Returning 사용) 후 이동이 불가능해지는 버그가 있었습니다. 다른 영웅에서는 발생하지 않고 오직 프리러너에서만 발생했으며, 지팡이 사용이나 아이템 던지기 등 다른 행동을 하면 다시 이동이 가능해지는 특이한 현상이었습니다.

---

## 원인 분석

### Momentum 버프의 특수성

프리러너의 Momentum 버프는 다음과 같은 특수한 특성을 가집니다:

1. **actPriority = HERO_PRIO+1**: 영웅보다 먼저 행동 (다른 버프들은 영웅보다 나중에 행동)
2. **gainStack()에서 postpone() 호출**: 이동 시 Momentum의 다음 행동 시간을 연기
3. **movedLastTurn 플래그**: Momentum.act()에서 이 플래그가 false면 스택 감소

### 버그 발생 메커니즘

```
Turn N:
├─ Momentum.act() 실행 (HERO_PRIO+1)
│  └─ movedLastTurn = false로 설정
└─ Hero.act() 실행
   └─ 이동이 아닌 액션 수행 (채굴, 사다리, 비컨)
      └─ gainStack() 호출되지 않음!
      └─ movedLastTurn이 false로 유지
      └─ Momentum.time이 갱신되지 않음

Turn N+1:
└─ Momentum.act() 실행
   └─ movedLastTurn == false 확인
   └─ 타이밍 불일치로 인해 영웅 행동이 차단됨
```

채굴, 사다리, 비컨은 이동이 아니므로 `gainStack()`을 호출하지 않았고, 이로 인해 Momentum의 타이밍이 영웅과 동기화되지 않아 이동이 차단되었습니다.

---

## 변경 사항

### 1. Momentum.onNonMovementAction() 메서드 추가

비이동 액션 후 Momentum 상태를 동기화하는 새 메서드를 추가했습니다. `gainStack()`과 달리 스택을 추가하지 않고 타이밍만 동기화합니다.

```diff
  public void gainStack(){
      movedLastTurn = true;
      if (freerunCooldown <= 0 && !freerunning()){
          postpone(target.cooldown()+(1/target.speed()));
          momentumStacks = Math.min(momentumStacks + 1, 10);
          ActionIndicator.setAction(this);
          BuffIndicator.refreshHero();
      }
  }

+ /**
+  * Called when hero performs a non-movement action that should prevent momentum decay.
+  * Unlike gainStack(), this does NOT add stacks - only prevents decay and syncs timing.
+  * Used for actions like Mining, Ladder climbing, and Beacon usage.
+  */
+ public void onNonMovementAction(){
+     movedLastTurn = true;
+     if (freerunCooldown <= 0 && !freerunning()){
+         postpone(target.cooldown()+(1/target.speed()));
+     }
+ }
```

### 2. Hero.actMine()에서 호출 추가

```diff
  // Crystal cascade 완료 후
  for (int i : PathFinder.NEIGHBOURS9) {
      GameScene.updateMap( action.dst+i );
  }
+ Momentum momentum = buff(Momentum.class);
+ if (momentum != null) momentum.onNonMovementAction();
  spendAndNext(TICK);
  ready();

  // Crystal이 없는 경우
  } else {
+     Momentum momentum = buff(Momentum.class);
+     if (momentum != null) momentum.onNonMovementAction();
      spendAndNext(TICK);
      ready();
  }
```

### 3. Hero.actTransition()에서 호출 추가

```diff
  if (Dungeon.level.activateTransition(this, transition)){
+     Momentum momentum = buff(Momentum.class);
+     if (momentum != null) momentum.onNonMovementAction();
      curAction = null;
  }
```

### 4. BeaconOfReturning에서 호출 추가

```diff
  // setBeacon()
  Notes.add(Notes.Landmark.BEACON_LOCATION, tracker.returnDepth);
+
+ Momentum momentum = hero.buff(Momentum.class);
+ if (momentum != null) momentum.onNonMovementAction();
  hero.spend( 1f );
  hero.busy();

  // returnBeacon() 같은 층 귀환
  if (ScrollOfTeleportation.teleportToLocation(hero, tracker.returnPos)){
+     Momentum momentum = hero.buff(Momentum.class);
+     if (momentum != null) momentum.onNonMovementAction();
      hero.spendAndNext( 1f );
  }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/buffs/Momentum.java:108-118` | `onNonMovementAction()` 메서드 추가 |
| `actors/hero/Hero.java:1384-1385` | 채굴 crystal cascade 후 호출 추가 |
| `actors/hero/Hero.java:1391-1392` | 채굴 일반 완료 후 호출 추가 |
| `actors/hero/Hero.java:1425-1426` | 레벨 전환 후 호출 추가 |
| `items/spells/BeaconOfReturning.java:33` | Momentum import 추가 |
| `items/spells/BeaconOfReturning.java:137-138` | 비컨 설정 시 호출 추가 |
| `items/spells/BeaconOfReturning.java:194-195` | 같은 층 귀환 시 호출 추가 |

---

## 영향

- 프리러너가 금 채굴 후 이동이 가능해짐
- 프리러너가 사다리 이동 후 이동이 가능해짐
- 프리러너가 Beacon of Returning 사용 후 이동이 가능해짐
- Momentum 스택은 비이동 액션으로 증가하지 않음 (밸런스 유지)
- 다른 영웅에게는 영향 없음 (Momentum 버프가 없으면 호출 무시)

---

## 검증

에이전트 5명 × 4 스프린트 분석을 통해 근본 원인을 확정하고 교차 검증:

| 검증 항목 | 결과 |
|----------|------|
| 채굴 후 이동 가능 | O |
| 사다리 이동 후 이동 가능 | O |
| 비컨 사용 후 이동 가능 | O |
| 일반 이동 시 스택 증가 유지 | O |
| 비이동 액션으로 스택 증가 없음 | O |
| 다른 영웅 영향 없음 | O |

---

## 관련 코드

- `Momentum.gainStack()` - 이동 시 스택 증가 및 타이밍 동기화
- `Momentum.postpone()` - Actor의 다음 행동 시간 연기
- `Momentum.act()` - movedLastTurn 플래그 확인 및 스택 감소 로직
- `Actor.process()` - actPriority에 따른 행동 순서 결정
- 관련 changelog: #122 (프리러너 세이브/로드 이동 버그 수정)
