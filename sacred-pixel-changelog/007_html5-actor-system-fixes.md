# 007. HTML5 액터 턴 시스템 버그 수정

**날짜**: 2026-03-13

## 개요

LibGDX + GWT HTML5 포팅 과정에서 발생한 버그들의 수정 기록. WornDartTrap 프리즈, AlchemyScene 복귀 후 입력 불능, Guard 체인 애니메이션 프리즈, Necromancer 소환 프리즈 등 수정.

---

## 변경 사항

## Bug A: WornDartTrap 프리즈

**파일:** `core/.../levels/traps/WornDartTrap.java`

**증상:** 함정이 발동되면 게임이 멈춤 (HTML5 환경)

**원인:** 원래 코드는 MissileSprite의 비동기 콜백에서 데미지를 적용했는데, HTML5(GWT)는 싱글 스레드이므로 콜백이 Actor 턴 시스템과 충돌하여 교착 상태 발생.

**수정:**
- 데미지를 콜백이 아닌 동기적으로 즉시 적용
- 다트 시각 효과는 fire-and-forget으로 처리 (데미지 적용 후 시각 전용)
- `VFX_PRIO` actor로 감싸서 시각 효과와 턴 실행이 겹치지 않도록 함

```java
// 데미지를 동기적으로 적용
int dmg = Random.NormalIntRange(4, 8) - finalTarget.drRoll();
finalTarget.damage(dmg, WornDartTrap.this);
// 시각 효과는 별도로 (콜백에 게임 로직 없음)
```

---

## Bug B: AlchemyScene 복귀 후 마우스 입력 불능

**파일:** `core/.../scenes/GameScene.java`

**증상:** 연금술 화면에서 나온 뒤 마우스/터치로 타일 클릭이 불가능 (키보드는 작동)

**원인:**
1. `Game.switchScene(GameScene.class)` → 새 GameScene 생성 → 새 CellSelector (listener=null)
2. Hero의 `ready` 필드는 이미 `true` (씬 전환 전 값 유지)
3. `Hero.act()`에서 `ready == true`이면 `ready()`를 호출하지 않음
4. `GameScene.ready()` → `selectCell(defaultCellListener)`가 실행되지 않아 listener가 null 유지
5. 키보드는 `moveFromActions()`를 통해 listener 없이도 작동하므로 영향 없음

**수정:** `GameScene.create()` 끝에서 hero가 이미 ready 상태인 경우 `ready()`를 호출하여 CellSelector의 listener를 초기화

```java
// GameScene.create() 끝부분
if (Dungeon.hero.ready) {
    ready();
}
```

---

## Bug C: WndSettings 관련 수정

**증상:** 설정 창에서의 입력 처리 문제

*이전 세션에서 수정 완료. 구체적 변경 내용은 세션 기록 참조.*

---

## Bug D: Guard 체인 애니메이션 중 프리즈

**파일:** `core/.../actors/mobs/Guard.java`

**증상:** Guard가 체인을 사용할 때 게임이 멈춤 (HTML5 환경)

**원인:** 체인 + 밀기 애니메이션 동안 Actor 턴 시스템이 다음 턴을 처리하려 하면서 교착 상태 발생. 데스크톱에서는 별도 스레드로 문제가 없지만, HTML5 싱글 스레드에서는 애니메이션 완료를 기다리는 동안 전체가 블록됨.

**수정:** `VFX_PRIO` 우선순위의 임시 Actor를 추가하여 애니메이션 동안 턴 실행을 차단. 애니메이션 완료 후 해당 Actor를 제거.

```java
final Actor chainBlocker = new Actor() {
    { actPriority = VFX_PRIO; }
    @Override
    protected boolean act() { return false; }
};
Actor.add(chainBlocker);
// ... 체인/밀기 애니메이션 ...
// 완료 콜백에서:
Actor.remove(chainBlocker);
```

---

## Bug E: Guard 체인 위치 겹침

**파일:** `core/.../actors/mobs/Guard.java`

**증상:** Guard가 체인으로 적을 끌어올 때 Guard 자신의 위치나 적의 현재 위치에 배치하려는 버그

**수정:** 체인 끌기 대상 위치 검색 시 Guard의 위치(`pos`)와 적의 현재 위치(`enemy.pos`)를 제외

```java
if (i != pos && i != enemy.pos
    && !Dungeon.level.solid[i] && Actor.findChar(i) == null
    && (Dungeon.level.openSpace[i] || !Char.hasProp(enemy, Property.LARGE))){
```

---

## Bug F: Necromancer 스켈레톤 소환 프리즈

**파일:** `core/.../actors/mobs/Necromancer.java`

**증상:** Necromancer가 스켈레톤을 소환할 때 대상 위치에 다른 캐릭터가 있으면 게임이 멈춤

**원인:** 소환 위치가 이미 점유되어 있을 때 처리 로직 부재로 무한 대기

**수정:** 단계적 대응 로직 추가:
1. 점유 캐릭터를 인접 빈 칸으로 밀어냄 (push blocker)
2. IMMOVABLE 속성이면 대체 소환 위치를 탐색
3. 밀기 실패 시 인접 타일에서 대체 소환 위치 탐색
4. 최후 수단으로 점유 캐릭터에 데미지

---

## 공통 인프라 수정

### HTML5 동기 Actor 처리

**파일:** `core/.../scenes/GameScene.java`

HTML5에서는 멀티스레딩이 불가능하므로 `Actor.process()`를 메인 스레드에서 동기적으로 호출:

```java
if (DeviceCompat.isHTML5()) {
    Actor.process();
} else {
    // 기존 멀티스레드 처리
}
```

### Gizmo 카메라 참조 정리

**파일:** `SPD-classes/.../noosa/Gizmo.java`

Scene 전환 시 카메라 참조가 남아 좌표 변환 오류를 유발하는 문제 수정:
- `destroy()`에서 `camera = null` 추가
- `camera()` 메서드에서 캐싱 제거 (항상 부모 체인을 통해 탐색)

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../levels/traps/WornDartTrap.java` | 데미지 동기 적용, VFX_PRIO actor 사용 |
| `core/.../scenes/GameScene.java` | hero ready 상태 시 ready() 호출 |
| `core/.../actors/mobs/Guard.java` | VFX_PRIO 블로커, enemy.pos 제외 |
| `core/.../actors/mobs/Necromancer.java` | 점유 캐릭터 처리 로직 추가 |
| `SPD-classes/.../noosa/Gizmo.java` | 카메라 참조 정리 |
