# 136. 프리러너 채굴 Momentum 콜백 타이밍 버그 수정

## 배경

#133에서 프리러너 Momentum 타이밍 불일치 버그를 수정했으나, 금 채굴(actMine) 후 여전히 이동이 불가능한 문제가 남아 있었습니다. 5명의 에이전트가 3 스프린트에 걸쳐 원인을 분석한 결과, **콜백 타이밍**이 근본 원인임을 확인했습니다.

---

## 원인 분석

### #133 수정의 한계

#133에서는 `onNonMovementAction()`을 `sprite.attack()` 콜백 **내부**에서 호출했습니다:

```java
sprite.attack(action.dst, new Callback() {
    @Override
    public void call() {
        // ... 채굴 로직 ...
        Momentum momentum = buff(Momentum.class);
        if (momentum != null) momentum.onNonMovementAction();  // 여기서 호출
        spendAndNext(TICK);
        ready();
    }
});
```

### 콜백 타이밍 문제

`sprite.attack()` 콜백은 **비동기적**으로 실행됩니다. 애니메이션이 완료된 후에야 콜백이 호출되는데, 이 시점에는 이미 다음 턴이 시작되어 Momentum이 먼저 행동(actPriority = HERO_PRIO+1)한 후일 수 있습니다.

```
시간 흐름:
├─ sprite.attack() 호출 (애니메이션 시작)
├─ Actor.process() 대기 중... (waitingForCallback = true)
│
│  ... 애니메이션 재생 중 ...
│
├─ 콜백 실행
│  └─ onNonMovementAction() 호출 ← 너무 늦음!
│     └─ Momentum이 이미 act()에서 movedLastTurn=false 확인 후 decay 완료
```

### 해결 방안

`onNonMovementAction()`을 `sprite.attack()` 호출 **이전**에 동기적으로 실행해야 합니다. 이렇게 하면 Momentum의 타이밍이 먼저 동기화되어, 다음 턴에서 Momentum.act()가 실행될 때 movedLastTurn이 true로 유지됩니다.

---

## 변경 사항

### Hero.actMine() - 콜백 외부로 이동

```diff
  private boolean actMine(HeroAction.Mine action){
      if (Dungeon.level.adjacent(pos, action.dst)){
          path = null;

+         // Sync Momentum timing BEFORE starting animation to prevent decay
+         Momentum momentum = buff(Momentum.class);
+         if (momentum != null) momentum.onNonMovementAction();

          if ((Dungeon.level.map[action.dst] == Terrain.WALL
                  || Dungeon.level.map[action.dst] == Terrain.WALL_DECO
                  || Dungeon.level.map[action.dst] == Terrain.MINE_CRYSTAL
                  || Dungeon.level.map[action.dst] == Terrain.MINE_BOULDER)
              && Dungeon.level.insideMap(action.dst)){
              sprite.attack(action.dst, new Callback() {
                  @Override
                  public void call() {
                      // ... 채굴 로직 ...

-                     Momentum momentum = buff(Momentum.class);
-                     if (momentum != null) momentum.onNonMovementAction();
                      spendAndNext(TICK);
                      ready();
                  }
              });
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/hero/Hero.java:1297-1299` | `onNonMovementAction()` 호출을 콜백 외부로 이동 |
| `actors/hero/Hero.java:1389-1390` | 콜백 내 중복 호출 제거 |
| `actors/hero/Hero.java:1394-1395` | 콜백 내 중복 호출 제거 |

---

## 영향

- 프리러너가 금 채굴 후 즉시 이동 가능
- Momentum 스택 감소 없이 타이밍만 동기화
- 다른 영웅에게는 영향 없음

---

## 검증

에이전트 5명 × 3 스프린트 분석을 통해 근본 원인 확정:

| 검증 항목 | 결과 |
|----------|------|
| 콜백 타이밍이 근본 원인 | O |
| sprite.attack() 이전 호출로 해결 | O |
| 다른 actMine 분기에도 적용 | O (단일 위치에서 처리) |
| 부작용 없음 | O |

---

## 관련 코드

- `Momentum.onNonMovementAction()` - 비이동 액션 시 타이밍 동기화
- `Momentum.act()` - movedLastTurn 플래그 확인
- `Actor.process()` - waitingForCallback 상태 관리
- 관련 changelog: #133 (최초 Momentum 타이밍 수정 시도)
