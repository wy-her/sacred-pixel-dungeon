# 043. 눈속임 잔상 버그 수정

**날짜**: 2026-04-09

## 개요

결투가의 "눈속임" 스킬 사용 시 적이 잔상을 공격하지 않고 영웅을 공격하는 버그를 수정. `hero.spend(1f)` 호출 위치 오류로 인해 AfterImage가 적보다 먼저 act()를 실행하여 즉시 destroy()되는 문제 해결.

---

## 변경 사항

### 문제 증상

결투가의 "눈속임" 스킬 사용 시:
1. 적의 공격범위 내로 이동해도 적이 잔상을 공격하지 않고 영웅을 공격
2. 적의 공격범위 밖으로 이동해도 적이 영웅을 따라오며 잔상과 타일 겹침

## 근본 원인

`Feint.java`에서 `hero.spend(1f)` 호출 위치 오류

### 버그 코드 (Sacred)
```java
hero.sprite.jump(hero.pos, target, 0, 0.1f, new Callback() {
    @Override
    public void call() {
        // ...
        hero.spendAndNext(1f);  // 콜백 안에서 spend
    }
});

AfterImage image = new AfterImage();
image.syncToHero(hero);  // 이 시점에 hero는 아직 spend 안 함!
```

### 정상 코드 (원본 Shattered)
```java
hero.sprite.jump(hero.pos, target, 0, 0.1f, new Callback() {
    @Override
    public void call() {
        // ...
        hero.next();  // next만 호출
    }
});
hero.spend(1f);  // 콜백 밖에서 먼저 spend

AfterImage image = new AfterImage();
image.syncToHero(hero);  // 이 시점에 hero는 이미 1f spend됨
```

### 발생 메커니즘

Actor 우선순위:
- AfterImage: `HERO_PRIO + 1 = 1` (높을수록 먼저 행동)
- Hero: `HERO_PRIO = 0`
- Mob: `MOB_PRIO = -20`

버그 상황:
1. `syncToHero()` 시점에 영웅이 아직 `spend()`하지 않음
2. AfterImage.time이 제대로 설정되지 않음
3. AfterImage가 적보다 **먼저** act() 실행 → 즉시 destroy()
4. 적의 턴에 AfterImage가 이미 없음 → 영웅을 타겟으로 선택

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/hero/abilities/duelist/Feint.java` | hero.spendAndNext(1f) → hero.spend(1f) 콜백 밖으로 이동, hero.next() 콜백 내 추가 |

---

### 변경 세부사항

### 변경 파일
- `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/hero/abilities/duelist/Feint.java`

### 변경 사항
```diff
 hero.sprite.jump(hero.pos, target, 0, 0.1f, new Callback() {
     @Override
     public void call() {
         if (Dungeon.level.map[hero.pos] == Terrain.OPEN_DOOR) {
             Door.leave( hero.pos );
         }
         hero.pos = target;
         Dungeon.level.occupyCell(hero);
         Invisibility.dispel();
         Dungeon.observe();
         GameScene.updateFog();
-        hero.spendAndNext(1f);
+        hero.next();
     }
 });
+hero.spend(1f);
```

## 수정 효과

| 항목 | 수정 전 | 수정 후 |
|------|---------|---------|
| syncToHero 시점 | hero.time = T | hero.time = T+1 |
| AfterImage.time | T (즉시 act) | T+1 (적과 동시) |
| 적 타겟팅 | 영웅 (AfterImage 없음) | 잔상 (정상) |
| 타일 겹침 | 발생 | 해결 |

---

## 테스트 완료

- 적 공격범위 내 이동 시: 적이 잔상을 공격함 ✓
- 적 공격범위 밖 이동 시: 적이 잔상 위치에서 멈춤 ✓

---
