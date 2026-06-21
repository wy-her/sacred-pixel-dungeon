# 006. 게임플레이 프리즈 버그 수정 (액터 시스템 안정성)

**날짜**: 2026-03-13

## 개요

HTML5 빌드에서 발생하는 게임플레이 프리즈 및 UI 버그 9건 수정. 낡은 다트 함정, 연금술 솥, 번개/폭풍/감전 함정, 양떼 함정, 경비병 사슬, 사령술사 해골 소환 등의 버그 수정.

---

## 변경 사항

## 1. 낡은 다트 함정 → 적 대상 시 게임 프리즈

**파일:** `core/.../levels/traps/WornDartTrap.java`

**원인:** MissileSprite 콜백에서 적이 사망하면 예외 발생 → `next()` 호출 안 됨 → Actor 시스템 정지

**수정:**
- 콜백을 `try-finally`로 감싸서 예외 발생 시에도 `next()` 항상 호출
- `finalTarget.sprite != null` 체크 추가 (사망 후 sprite 접근 방지)

**동일 패턴 적용:**
- `PoisonDartTrap.java` — 같은 try-finally + sprite null 체크
- `GrimTrap.java` — 같은 try-finally + sprite/emitter null 체크

---

## 2. 브라우저 탭 백그라운드 → 블랙 스크린

**상태:** 기존 `index.html`에 WebGL context lost/restored 핸들러 이미 존재. 추가 조사 필요.

---

## 3. 연금술 솥 장면 → 게임 프리즈

**파일:** `core/.../scenes/AlchemyScene.java`

**원인:** `destroy()` 메서드에서 `clearSlots()` 호출 → `synchronized(inputs)` 중첩 → GWT 단일 스레드에서 데드락 유사 상태

**수정:**
- `destroy()`에서 `clearSlots()` 호출 대신 슬롯 정리 로직을 직접 인라인
- 중첩 `synchronized` 블록 제거

---

## 4. 번개/폭풍/감전 함정 → 게임 프리즈

**파일:** `core/.../actors/blobs/Electricity.java`

**원인:** `spreadFromCell()` 메서드가 재귀 호출 → 넓은 물 지역에서 GWT의 제한된 JS 콜스택 오버플로

**수정:**
- 재귀 DFS → 반복 BFS (`ArrayDeque` 사용)로 완전 변환
- 스택 오버플로 원천 차단

```java
// Before (재귀)
private void spreadFromCell(int cell, int power) {
    cur[cell] = Math.max(cur[cell], power);
    for (int n : PathFinder.NEIGHBOURS4) {
        if (water[cell + n] && cur[cell + n] < power) {
            spreadFromCell(cell + n, power); // 재귀!
        }
    }
}

// After (반복 BFS)
private void spreadFromCell(int cell, int power) {
    ArrayDeque<int[]> queue = new ArrayDeque<>();
    queue.add(new int[]{cell, power});
    while (!queue.isEmpty()) {
        int[] entry = queue.poll();
        // ... BFS 로직
    }
}
```

---

## 5. 양떼 함정 → 게임 프리즈

**파일:** `core/.../levels/traps/FlockTrap.java`

**원인:** 양 소환 루프 중 타일 위의 다른 함정이 즉시 `activate()` → 연쇄 활성화로 Actor 시스템 꼬임

**수정:**
- 함정을 `ArrayList<Trap>`에 수집 후, 양 전부 배치 완료 후 일괄 활성화

```java
ArrayList<Trap> trapsToActivate = new ArrayList<>();
for (int i : spawnPoints) {
    // 양 소환 + 함정 수집
    trapsToActivate.add(t);
}
// 모든 양 배치 후 함정 활성화
for (Trap t : trapsToActivate) {
    t.activate();
}
```

---

## 6. 일반 콜백 안전성 강화

**파일:**
- `core/.../sprites/MissileSprite.java` — `onComplete()` try-catch 추가
- `core/.../effects/Pushing.java` — `Effect.update()` 콜백에 try-catch 추가

**수정:** 투사체 도착 및 밀어내기 완료 콜백에서 예외 발생 시 게임 전체 프리즈 대신 오류 보고 후 계속 진행

---

## 7. 경비병 사슬 → 영웅이 경비병과 같은 타일로 끌려감

**파일:** `core/.../actors/mobs/Guard.java`

**원인:** 사슬로 끌어당길 위치 탐색 시 적의 현재 위치(`enemy.pos`)를 제외하지 않음

**수정:**
- `i != enemy.pos` 조건 추가

```java
if (i != pos && i != enemy.pos  // enemy.pos 제외 추가
        && !Dungeon.level.solid[i] && Actor.findChar(i) == null
        && (Dungeon.level.openSpace[i] || !Char.hasProp(enemy, Property.LARGE))) {
    newPos = i;
    break;
}
```

---

## 8. 사령술사 해골 소환 → 영웅 위치에 겹침/프리즈

**파일:**
- `core/.../actors/mobs/Necromancer.java`
- `core/.../sprites/NecromancerSprite.java`

**원인:** 소환 위치에 이미 캐릭터(영웅 포함)가 있을 때 처리 로직 부재 → 해골과 영웅 겹침 → 게임 프리즈

**수정 (Necromancer.java — `summonMinion()` 전면 개편):**
1. `Actor.findChar(summoningPos) != null`로 소환 위치 점유 감지
2. 주변 빈 타일 탐색 → 대체 위치에 소환
3. 빈 타일 없으면 blocker 밀어내기 시도
4. 밀어낼 수 없으면 데미지 + 재소환 대기
5. 영웅이 소환 중 해당 타일로 이동한 경우도 동일하게 처리

**수정 (NecromancerSprite.java):**
- `summoningPos` 배열 범위 체크 및 `instanceof Necromancer` 안전 검사 추가

---

## 9. 사망 메뉴 → 인벤토리 확인 후 버튼 사라짐

**상태:** 타이밍 이슈로 추정. 추가 조사 필요.

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|-----------|
| `WornDartTrap.java` | try-finally + sprite null 체크 |
| `PoisonDartTrap.java` | try-finally + sprite null 체크 |
| `GrimTrap.java` | try-finally + sprite/emitter null 체크 |
| `Electricity.java` | 재귀 → 반복 BFS 변환 |
| `FlockTrap.java` | 함정 연쇄 활성화 방지 (지연 활성화) |
| `AlchemyScene.java` | 중첩 synchronized 제거 |
| `Guard.java` | 사슬 끌기 위치에서 enemy.pos 제외 |
| `Necromancer.java` | summonMinion() 전면 개편 |
| `NecromancerSprite.java` | 범위 체크 + 타입 안전 검사 |
| `MissileSprite.java` | 콜백 try-catch 추가 |
| `Pushing.java` | 콜백 try-catch 추가 |
