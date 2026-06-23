# 100. 충격파의 마법막대 밀치기 버그 수정

**날짜**: 2026-06-22

## 개요

충격파의 마법막대(Wand of Blast Wave)로 적을 밀쳤을 때 발생하는 두 가지 버그 수정 및 마법 지팡이 onHit() 행운 보너스 미적용 버그 수정.

---

## 변경 사항

### Bug Fixes (4)

---

### [B-1] Pushing.java - VFX 블로커 타이밍 수정

**문제**: 적을 밀쳤을 때 적이 밀려나기 전에 먼저 행동하는 경우 발생

**원인 분석**:
- `Pushing.act()`에서 `Actor.remove(this)`가 먼저 호출된 후 VFX 블로커 추가
- 이 타이밍 갭 동안 `Actor.process()`가 Mob의 `act()`를 호출할 수 있음

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/effects/Pushing.java`

**수정**: VFX 블로커를 Actor.remove() 전에 추가

```java
@Override
protected boolean act() {
    if (sprite != null && sprite.parent != null) {
        //Add VFX blocker FIRST to prevent Mob.act() from running during animation
        if (blockingVfx) Actor.addVfxBlocker();
        if (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to]){
            sprite.visible = true;
        }
        if (effect == null) {
            new Effect();
        }
    } else {
        blockingVfx = false;
        if (callback != null) callback.call();
        return true;
    }

    //Remove AFTER VFX blocker is added to prevent timing gap
    Actor.remove( Pushing.this );

    for ( Actor actor : Actor.all() ){
        if (actor instanceof Pushing && actor.cooldown() == 0)
            return true;
    }
    return false;
}
```

---

### [B-2] WandOfBlastWave.java - throwChar() 콜백 안전성 수정

**문제**: 적을 밀쳤을 때 즉시 원래 위치로 돌아가는 현상 발생

**원인 분석**:
- `throwChar()` 콜백에서 `initialpos != ch.pos` 조건이 과도하게 엄격함
- Pushing 애니메이션 중 Mob의 `act()`가 호출되어 `ch.pos`가 변경될 수 있음

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/items/wands/WandOfBlastWave.java`

**수정**: `initialpos` 조건 제거 및 스프라이트 동기화 보장

```java
// 기존 코드
if (ch.pos != initialpos || Actor.findChar(newPos) != null) {
    ch.sprite.place(ch.pos);
    return;
}

// 수정 코드
Actor.add(new Pushing(ch, ch.pos, newPos, new Callback() {
    public void call() {
        if (Actor.findChar(newPos) != null) {
            ch.sprite.place(ch.pos);
            return;
        }
        int oldPos = ch.pos;
        ch.pos = newPos;
        ch.sprite.place(ch.pos);
        // ...
    }
}));
```

---

### [B-3] WandOfBlastWave.java - onHit() 행운 보너스 적용

**문제**: 13잎 클로버(ThirteenLeafClover) 장착 시 마법 지팡이 onHit() 데미지에 행운 보너스 미적용

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/items/wands/WandOfBlastWave.java`

**수정**: `Hero.heroDamageIntRange()` 사용

```java
// 기존
int dmg = Random.NormalIntRange(8+2*buffedLvl(), 12+3*buffedLvl());

// 수정
int dmg = Hero.heroDamageIntRange(8+2*buffedLvl(), 12+3*buffedLvl());
```

---

### [B-4] WandOfFireblast.java - onHit() 행운 보너스 적용

**문제**: B-3과 동일

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/items/wands/WandOfFireblast.java`

**수정**: `Hero.heroDamageIntRange()` 사용

```java
// 기존
ch.damage(Math.round(powerMulti*Random.NormalIntRange(2 + 2*buffedLvl(), 8 + 4*buffedLvl())), this);

// 수정
ch.damage(Math.round(powerMulti*Hero.heroDamageIntRange(2 + 2*buffedLvl(), 8 + 4*buffedLvl())), this);
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `effects/Pushing.java:91-117` | VFX 블로커를 Actor.remove() 전에 추가 |
| `items/wands/WandOfBlastWave.java:33` | `Hero` import 추가 |
| `items/wands/WandOfBlastWave.java:163-173` | throwChar() 콜백 안전성 수정 |
| `items/wands/WandOfBlastWave.java:208` | `Hero.heroDamageIntRange()` 사용 |
| `items/wands/WandOfFireblast.java:36` | `Hero` import 추가 |
| `items/wands/WandOfFireblast.java:206` | `Hero.heroDamageIntRange()` 사용 |

---
