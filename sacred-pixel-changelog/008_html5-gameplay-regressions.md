# 008. HTML5 게임플레이 회귀 버그 종합 수정

**날짜**: 2026-03-14

## 개요

9개의 HTML5 관련 버그 수정 및 Actor 턴 시스템 개선. EmoIcon 위치, Bestiary 크래시, Halo 밴딩, 화염정령 소환, Mob 스프라이트 null, Actor 다중 행동, 텐구 ShockerAbility, 아이템 연속 투척, 히어로 대기 중 몹 행동 등 수정.

---

## 변경 사항

## Bug 1: EmoIcon(감정 아이콘) 위치 표시 안됨

**파일:** `core/.../effects/EmoIcon.java`

**증상:** 몹 머리 위의 감정 아이콘(잠자기 Z, 경계 ! 등)이 화면에 표시되지 않음

**원인:** `update()`에서 `camera` 필드를 직접 참조하는데, `Gizmo.camera` 필드는 명시적으로 설정하지 않으면 항상 `null`. 따라서 위치 업데이트 블록 전체가 실행되지 않음.

**수정:** `camera` 필드 대신 부모 체인을 순회하는 `camera()` 메서드를 사용

```java
// BEFORE:
if (camera != null) {
    PointF center = centerPoint();
    x = PixelScene.align(camera, owner.x + owner.width() - center.x);
    y = PixelScene.align(camera, owner.y - center.y);
}

// AFTER:
com.watabou.noosa.Camera c = camera();
if (c != null) {
    PointF center = centerPoint();
    x = PixelScene.align(c, owner.x + owner.width() - center.x);
    y = PixelScene.align(c, owner.y - center.y);
}
```

---

## Bug 2: Bestiary(도감) 크래시 — GWT 리플렉션 누락

**파일:** `html/.../GwtClassRegistry.java`, `core/.../windows/WndJournal.java`

**증상:** 도감에서 특정 몹 카테고리를 열면 크래시 발생

**원인:** GWT는 Java 리플렉션을 지원하지 않아 `GwtClassRegistry`에 모든 클래스를 수동 등록해야 함. `Shaman$RedShaman`, `Elemental$FireElemental` 등 inner class 등록이 누락되어 `Reflection.newInstance()`가 `null` 반환 → NPE 발생.

**수정:**
1. `GwtClassRegistry`에 16개 몹 inner class + 14개 스프라이트 inner class 등록 추가
2. `WndJournal.addGridEntities()`에 null 가드 추가 (미등록 클래스 방어)

```java
// GwtClassRegistry에 추가된 클래스들:
// 몹: Shaman$RedShaman, Shaman$BlueShaman, Shaman$PurpleShaman,
//     Elemental$FireElemental, Elemental$FrostElemental, Elemental$ShockElemental,
//     Elemental$ChaosElemental, Elemental$NewbornFireElemental,
//     YogFist$BurningFist, YogFist$SoiledFist, YogFist$RottingFist,
//     YogFist$RustedFist, YogFist$BrightFist, YogFist$DarkFist,
//     WandOfWarding$Ward$WardSentry
// 스프라이트: ElementalSprite$Fire/NewbornFire/Frost/Shock/Chaos,
//            ShamanSprite$Red/Blue/Purple, FistSprite 6종

// WndJournal 방어 코드:
mob = (Mob) Reflection.newInstance(entityCls);
if (mob == null) continue;  // 미등록 클래스 건너뜀
```

---

## Bug 3: Halo 밴딩 아티팩트

**파일:** `SPD-classes/.../noosa/Halo.java`

**증상:** 캐릭터 주변 빛 효과(Halo)에 동심원 모양의 밴딩이 눈에 띄게 보임

**원인:** Canvas2D는 내부적으로 premultiplied alpha를 사용. 매우 낮은 alpha 값(0x08)이 양자화되어 동심원 경계가 뚜렷하게 나타남.

**수정:** 최소 alpha 값을 `0x08` → `0x10`으로 높이고, 링 간격을 `i+=2` → `i+=4`로 넓혀 전환을 부드럽게 처리

```java
// BEFORE:
pixmap.setColor( 0xFFFFFF08 );
for (int i = 0; i < RADIUS; i+=2) {

// AFTER:
pixmap.setColor( 0xFFFFFF10 );
for (int i = 0; i < RADIUS; i+=4) {
```

---

## Bug 4: 화염정령 소환 실패 — GWT 클래스 미등록

**파일:** `html/.../GwtClassRegistry.java`

**증상:** 의식용 양초로 화염정령을 소환하면 정령이 나타나지 않거나 크래시

**원인:** Bug 2와 동일 — `Elemental$NewbornFireElemental`과 `ElementalSprite$NewbornFire`가 GwtClassRegistry에 미등록

**수정:** Bug 2의 GwtClassRegistry 등록에 포함하여 해결

---

## Bug 5: Mob 스프라이트 null 크래시

**파일:** `core/.../scenes/GameScene.java`

**증상:** 특정 상황에서 몹 추가 시 NPE 발생

**원인:** `addMobSprite()`에서 `mob.sprite()`가 null을 반환할 수 있는데, null 체크 없이 바로 사용

**수정:** null 가드 추가

```java
private synchronized void addMobSprite( Mob mob ) {
    CharSprite sprite = mob.sprite();
    if (sprite == null) return;  // 방어 코드 추가
    sprite.visible = Dungeon.level.heroFOV[mob.pos];
    // ...
}
```

---

## Bug 6: HTML5 Actor 다중 행동 방지 — waitingForCallback 메커니즘

**파일:** `core/.../actors/Actor.java`

**증상:** HTML5에서 몹이 한 턴에 여러 번 행동함 (예: 텐구가 수리검을 연속 발사)

**원인:** 데스크톱은 `act()` → `false` 반환 시 스레드가 블록되어 콜백까지 대기. HTML5는 싱글 스레드라 `process()`가 매 프레임 호출되며, 애니메이션 콜백 전에 같은 액터가 다시 선택되어 중복 행동.

**수정:** `waitingForCallback` 플래그 도입
- `act()`가 `false` 반환 시 해당 액터에 `waitingForCallback = true` 설정
- 액터 선택 루프에서 `waitingForCallback` 액터를 건너뜀
- `next()` 호출 시 플래그 해제

```java
// 새 필드:
private boolean waitingForCallback = false;

// next()에서 해제:
public void next() {
    waitingForCallback = false;
    if (current == this) { current = null; }
}

// process() 액터 선택에서 건너뜀:
if (ThreadCompat.currentThread() == null && actor.waitingForCallback) {
    continue;
}

// act() false 반환 시 설정:
if (ThreadCompat.currentThread() == null && current != null) {
    current.waitingForCallback = true;
}
```

---

## Bug 7: EmoIcon — Bug 1과 동일

Bug 1에서 함께 수정됨.

---

## Bug 8: 텐구 ShockerAbility + Lightning 버그

**파일:** `core/.../actors/mobs/Tengu.java`, `core/.../effects/Lightning.java`

**증상:** 텐구 전투 중 전기 충격 공격에서 게임 프리즈 또는 번개 이펙트가 잘못된 위치에 표시

**원인 (2가지):**
1. `ShockerAbility.act()`에서 target이나 sprite가 null일 때 안전 처리 없이 진행 → NPE로 프리즈
2. `Lightning.Arc.update()`에서 `arc2.y`를 계산할 때 `arc2.origin.x`를 사용 (오타, `.y`여야 함)

**수정:**

Tengu — null 안전 체크 추가:
```java
@Override
public boolean act() {
    if (target == null || target.sprite == null || target.sprite.parent == null) {
        spend(TICK);
        detach();
        return true;
    }
    // ... 기존 로직
}
```

Lightning — 좌표 오타 수정:
```java
// BEFORE:
arc2.y = y2 - arc2.origin.x;

// AFTER:
arc2.y = y2 - arc2.origin.y;
```

---

## Bug 9: 아이템 연속 투척

**파일:** `core/.../items/Item.java`

**증상:** 투척 아이템을 한 번 던지면 같은 아이템이 연속으로 투척됨

**원인:** HTML5에서 `hero.ready` 체크 없이 투척 리스너가 실행되어, 히어로가 아직 이전 투척을 처리 중인데도 새 투척이 트리거됨

**수정:** `hero.ready` 상태 확인 추가

```java
// BEFORE:
if (target != null) {
    curItem.cast( curUser, target );
}

// AFTER:
if (target != null && curUser != null && curUser.ready) {
    curItem.cast( curUser, target );
}
```

---

## Bug 10: HTML5 턴 시스템 — 히어로 대기 중 몹 연속 행동

**파일:** `core/.../actors/Actor.java`

**증상:** 히어로가 플레이어 입력을 기다리는 동안 몹들이 계속 턴을 소비하며 행동

**원인:** Bug 6에서 도입한 `waitingForCallback` 메커니즘의 빈틈. 히어로의 `act()`가 `false`를 반환하면 `waitingForCallback = true`가 설정되지만, 다음 프레임에서 히어로가 건너뛰어지고 다른 몹들이 대신 선택되어 행동함. 데스크톱에서는 스레드 블로킹으로 자동 방지되지만, HTML5에서는 매 프레임 `process()`가 호출되므로 발생.

**수정:** 액터 선택 루프에서 히어로가 `waitingForCallback` 상태이면 `heroWaiting` 플래그를 설정하고, 루프 후 모든 액터 처리를 중단

```java
boolean heroWaiting = false;

synchronized (Actor.class) {
    for (Actor actor : all) {
        if (ThreadCompat.currentThread() == null && actor.waitingForCallback) {
            // 히어로가 입력 대기 중이면 전체 처리 중단
            if (actor == Dungeon.hero) {
                heroWaiting = true;
            }
            continue;
        }
        // ... 액터 선택 로직
    }
}
// 히어로 입력 대기 시 모든 액터 처리 중단
if (heroWaiting) {
    current = null;
}
```

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../effects/EmoIcon.java` | camera() 메서드 사용 |
| `html/.../GwtClassRegistry.java` | 16개 몹 + 14개 스프라이트 inner class 등록 |
| `core/.../windows/WndJournal.java` | null 가드 추가 |
| `SPD-classes/.../noosa/Halo.java` | 최소 alpha 0x10, 링 간격 4 |
| `core/.../scenes/GameScene.java` | addMobSprite null 가드 |
| `core/.../actors/Actor.java` | waitingForCallback 플래그, heroWaiting 플래그 |
| `core/.../actors/mobs/Tengu.java` | null 안전 체크 |
| `core/.../effects/Lightning.java` | 좌표 오타 수정 |
| `core/.../items/Item.java` | hero.ready 체크 |
