# 040. 주술사/흑마법사 Zap 타이밍 수정

**날짜**: 2026-04-05

## 개요
Shaman과 Warlock의 원거리 공격(zap) 타이밍을 개선하여 시각적 피드백과 게임 로직이 일치하도록 수정.

---

## 문제

### 기존 동작
- zap 애니메이션이 시작되면 바로 데미지 처리가 진행됨
- MagicMissile(투사체)이 화면에서 날아가는 도중에 이미 데미지가 적용됨
- 시각적으로 미사일이 아직 도착하지 않았는데 플레이어가 피해를 받는 현상

### 원인
- Sprite의 zap() 메서드에서 애니메이션만 시작하고, 콜백 처리가 Mob 클래스의 doAttack()에서 즉시 실행됨
- MagicMissile의 도착 콜백이 활용되지 않음

---

## 수정 내용

### ShamanSprite.java

MagicMissile 생성 시 도착 콜백을 추가하여 미사일이 목표에 도착했을 때 `onZapComplete()`를 호출하도록 수정:

```java
public void zap( int cell ) {
    super.zap( cell );

    // MagicMissile callback handles game logic when projectile arrives
    // This ensures damage timing matches the visual (missile lands → damage)
    MagicMissile.boltFromChar( parent,
            boltType,
            this,
            cell,
            new Callback() {
                @Override
                public void call() {
                    if (ch != null && ch instanceof Shaman) {
                        ((Shaman)ch).onZapComplete();
                    }
                }
            } );
    Sample.INSTANCE.play( Assets.Sounds.ZAP );
}
```

### WarlockSprite.java

동일한 패턴으로 MagicMissile 콜백 추가:

```java
public void zap( int cell ) {
    super.zap( cell );

    // MagicMissile callback handles game logic when projectile arrives
    // This ensures damage timing matches the visual (missile lands → damage)
    MagicMissile.boltFromChar( parent,
            MagicMissile.SHADOW,
            this,
            cell,
            new Callback() {
                @Override
                public void call() {
                    if (ch != null && ch instanceof Warlock) {
                        ((Warlock)ch).onZapComplete();
                    }
                }
            } );
    Sample.INSTANCE.play( Assets.Sounds.ZAP );
}
```

---

## 기술적 세부사항

### 호출 흐름 (수정 후)

1. `Shaman/Warlock.doAttack()` → `sprite.zap(enemy.pos)` 호출
2. `ShamanSprite/WarlockSprite.zap()` → MagicMissile 생성 + 콜백 등록
3. MagicMissile이 화면에서 이동 (시각적 애니메이션)
4. MagicMissile이 목표에 도착 → 콜백 실행
5. 콜백에서 `Shaman/Warlock.onZapComplete()` 호출
6. `onZapComplete()` → `zap()` (실제 데미지 처리) → `next()` (턴 완료)

### null 안전성

- `ch != null` 체크: Sprite가 Character 없이 존재할 경우 대비
- `ch instanceof Shaman/Warlock` 체크: 타입 안전성 보장

---

## 관련 파일

### 수정된 파일
- `sprites/ShamanSprite.java` - zap() 메서드에 MagicMissile 콜백 추가
- `sprites/WarlockSprite.java` - zap() 메서드에 MagicMissile 콜백 추가

### 관련 파일 (변경 없음)
- `actors/mobs/Shaman.java` - onZapComplete() 메서드 (기존 구현 활용)
- `actors/mobs/Warlock.java` - onZapComplete() 메서드 (기존 구현 활용)

---

## 이전 Changelog와의 관계

- **Changelog 61**: Shaman/Warlock의 `sprite null 체크` 추가 (Mob 클래스 내)
- **Changelog 62 (본 문서)**: Sprite 클래스 자체의 zap 타이밍 개선

이 두 수정은 서로 보완적:
- 61번: 콜백 실행 시 sprite가 null이면 안전하게 처리
- 62번: 콜백 자체의 타이밍을 미사일 도착에 맞춤

---

## 테스트 체크리스트

- [ ] Shaman 원거리 공격 → 미사일 도착 후 데미지 표시
- [ ] Warlock 원거리 공격 → 미사일 도착 후 데미지/디버프 표시
- [ ] FOV 경계에서 Shaman 공격 → 게임 멈춤 없음
- [ ] FOV 경계에서 Warlock 공격 → 게임 멈춤 없음
- [ ] Shaman/Warlock 공격 중 사망 → 턴 정상 완료
