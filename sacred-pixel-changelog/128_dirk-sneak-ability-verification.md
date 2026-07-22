# 128. 더크 무기능력 로직 검증

## 배경

결투가의 더크(Dirk) 무기능력이 벽 너머로 점멸(blink)되지 않는 현상에 대해, 이것이 버그인지 의도된 동작인지 확인이 필요했습니다.

---

## 조사 내용

### 더크 무기능력 로직

**Dirk.java:81-83**
```java
protected void duelistAbility(Hero hero, Integer target) {
    Dagger.sneakAbility(hero, target, 4, 2+buffedLvl(), this);
}
```

### `sneakAbility()` 핵심 로직

**Dagger.java:115-120**
```java
PathFinder.buildDistanceMap(Dungeon.hero.pos,
    BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null), maxDist);
if (PathFinder.distance[target] == Integer.MAX_VALUE
    || !Dungeon.level.heroFOV[target] || hero.rooted) {
    GLog.w(Messages.get(wep, "ability_target_range"));
    return;
}
```

### 경로 체크 동작

| 조건 | 결과 |
|------|------|
| `PathFinder.distance[target] == Integer.MAX_VALUE` | 걸어서 도달 불가 → 실패 |
| `!Dungeon.level.heroFOV[target]` | 시야 밖 → 실패 |
| `hero.rooted` | 뿌리박힘 → 실패 |
| `Actor.findChar(target) != null` | 위치 점유됨 → 실패 |

`PathFinder.buildDistanceMap()`은 **걸어서 이동 가능한 경로**를 기준으로 거리를 계산합니다. 따라서 벽 너머 타일은 직선거리가 가까워도 경로가 없으면 이동 불가합니다.

---

## 원본 비교

### Shattered Pixel Dungeon 3.3.8 vs Sacred Pixel Dungeon

| 파일 | 비교 결과 |
|------|-----------|
| `Dagger.java` | **완전 동일** |
| `Dirk.java` | **완전 동일** |

### 세부 파라미터 비교

| 항목 | Shattered PD 3.3.8 | Sacred PD |
|------|---------------------|-----------|
| Dirk maxDist | 4 | 4 |
| Dirk invisTurns | 2 + level | 2 + level |
| Dagger maxDist | 5 | 5 |
| Dagger invisTurns | 2 + level | 2 + level |
| 경로 체크 로직 | `PathFinder.buildDistanceMap()` | 동일 |
| 점유 체크 | `Actor.findChar()` | 동일 |

---

## 결론

**벽을 뚫고 점멸되지 않는 것은 버그가 아니라 원본 설계 의도입니다.**

더크/단검의 무기능력은 "순간이동(blink)"이 아닌 "경로 기반 은밀 이동(sneak)"으로 설계되었습니다:

| 동작 | 가능 여부 |
|------|-----------|
| 걸어서 갈 수 있는 빈 칸으로 이동 | O |
| 함정(`avoid`) 타일 위로 이동 | O |
| 벽 너머로 점멸 | X |
| 적이 있는 칸으로 이동 | X |
| 닫힌 문 너머로 이동 | X |

---

## 검증된 파일

| 파일 | 경로 |
|------|------|
| Dirk.java (Sacred) | `core/.../items/weapon/melee/Dirk.java` |
| Dagger.java (Sacred) | `core/.../items/weapon/melee/Dagger.java` |
| Dirk.java (원본) | `wy/reference/shattered-pixel-dungeon-3.3.8/.../Dirk.java` |
| Dagger.java (원본) | `wy/reference/shattered-pixel-dungeon-3.3.8/.../Dagger.java` |

---

## 비고

- 변경 사항 없음 (검증 목적)
- 원본과 동일함을 확인
