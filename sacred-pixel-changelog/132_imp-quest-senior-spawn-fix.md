# 132. 임프 퀘스트 Senior Monk 소환 위치 개선

## 배경

임프 퀘스트에서 Senior Monk가 소환될 때, 숨겨진 방, 잠긴 문 안쪽, 고립된 공간 등 임프에게 도달할 수 없는 위치에 소환되는 문제가 있었습니다. 또한 소환된 Senior Monk가 랜덤하게 배회하여 퀘스트 진행이 비효율적이었습니다.

---

## 변경 사항

### 1. 도달 가능한 위치에만 소환

`PathFinder`를 사용하여 임프 위치에서 걸어서 도달 가능한 타일에만 Senior Monk가 소환되도록 변경했습니다.

```diff
  public static void spawnSeniorMonks() {
      questDepth = Dungeon.depth;

+     // Find Imp's position on this level
+     int impPos = -1;
+     for (Mob mob : Dungeon.level.mobs) {
+         if (mob instanceof Imp) {
+             impPos = mob.pos;
+             break;
+         }
+     }
+
+     // Build distance map from Imp's position to find reachable cells
+     if (impPos != -1) {
+         PathFinder.buildDistanceMap(impPos, Dungeon.level.passable);
+     }

      ArrayList<Integer> candidates = new ArrayList<>();

-     // Find valid spawn positions (passable cells not occupied)
+     // Find valid spawn positions (passable, not occupied, reachable from Imp)
      for (int i = 0; i < Dungeon.level.length(); i++) {
          if (Dungeon.level.passable[i]
                  && Dungeon.level.findMob(i) == null
                  && i != Dungeon.hero.pos
-                 && Dungeon.level.distance(i, Dungeon.hero.pos) > 4) {
+                 && Dungeon.level.distance(i, Dungeon.hero.pos) > 4
+                 && (impPos == -1 || PathFinder.distance[i] != Integer.MAX_VALUE)) {
              candidates.add(i);
          }
      }
```

### 2. 소환 후 임프 방향으로 이동

소환된 Senior Monk가 임프 방향으로 이동하도록 `beckon()` 메서드를 호출합니다.

```diff
      Senior senior = new Senior();
      senior.pos = pos;
      senior.state = senior.WANDERING;
      GameScene.add(senior);
      ScrollOfTeleportation.appear(senior, pos);
+     // Beckon towards Imp so they move in that direction
+     if (impPos != -1) {
+         senior.beckon(impPos);
+     }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/mobs/npcs/Imp.java:35` | `PathFinder` import 추가 |
| `actors/mobs/npcs/Imp.java:232-281` | `spawnSeniorMonks()` 메서드 수정 |

---

## 영향

- 고립된 방, 잠긴 문 안쪽, 숨겨진 방 등에 Senior Monk가 소환되지 않음
- 소환된 Senior Monk가 임프 방향으로 이동하여 퀘스트 진행이 원활해짐
- 플레이어가 모든 Senior Monk를 처치할 수 있도록 보장

---

## 관련 코드

- `PathFinder.buildDistanceMap()` - 거리 맵 계산
- `PathFinder.distance[]` - 각 타일까지의 거리 (도달 불가 시 `Integer.MAX_VALUE`)
- `Mob.beckon()` - 몬스터를 특정 위치로 유인 (`Mob.java:1046-1054`)
- `Mob.Wandering.continueWandering()` - target 방향 이동 로직 (`Mob.java:1223-1236`)

