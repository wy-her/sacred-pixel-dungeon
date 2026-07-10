# 117. 자장가의 주문서 수면 버그 수정

**날짜**: 2026-07-09

## 개요

자장가의 주문서(Scroll of Lullaby) 사용 후 잠든 영웅이 피해를 받아도 깨어나지 않는 버그 수정. HTML5 타이밍 차이로 인해 Drowsy 버프가 MagicalSleep을 재적용하는 문제 해결.

---

## 증상

- 자장가의 주문서 사용 → 영웅이 MagicalSleep 상태로 수면
- 적에게 공격받아 피해 발생
- **예상 동작**: 피해로 인해 잠에서 깨어남
- **실제 동작**: 피해를 받아도 계속 수면 상태 유지 (HTML5 빌드에서만 발생)

---

## 문제 분석

### 원인: HTML5 Actor 시스템 타이밍 차이

Desktop과 HTML5의 Actor 처리 방식 차이로 인해 버그 발생:

```
Timeline (버그 상황):

Turn T: 자장가의 주문서 사용
├─ Buff.affect(hero, Drowsy.class) → 5턴 후 MagicalSleep 적용 예정
│
Turn T+5: Drowsy.act() 실행
├─ Buff.affect(target, MagicalSleep.class) → 영웅 수면
│
Turn T+6: 적 공격
├─ Char.damage() 호출
│  └─ Buff.detach(this, MagicalSleep.class) → 수면 해제 ✓
│
├─ HTML5 Actor.process() 계속 진행...
│
├─ Drowsy.act() 다시 실행 (아직 detach 안 됨)
│  └─ Buff.affect(target, MagicalSleep.class) → 수면 재적용! ❌
│
└─ 결과: 영웅이 다시 잠듦
```

### Desktop vs HTML5 차이점

| 환경 | Actor 처리 | 타이밍 |
|------|-----------|--------|
| Desktop | 멀티스레드 | damage() 후 Drowsy 즉시 만료 |
| HTML5 | 동기 프레임 처리 | damage() 후에도 Drowsy.act() 실행 가능 |

### 핵심 문제

`Drowsy.act()`가 `MagicalSleep` 존재 여부를 확인하지 않고 무조건 적용:

```java
// 기존 코드 (버그)
@Override
public boolean act(){
    Buff.affect(target, MagicalSleep.class);  // 무조건 적용
    return super.act();
}
```

---

## 변경 사항

### Bug Fix #1: Drowsy.java - 근본 원인 수정

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/buffs/Drowsy.java`

**수정**: MagicalSleep 존재 여부 확인 후 적용

```java
// 기존 코드 (버그)
@Override
public boolean act(){
    Buff.affect(target, MagicalSleep.class);
    return super.act();
}

// 수정 코드
@Override
public boolean act(){
    // Only apply MagicalSleep if not already present, to prevent re-application
    // after damage wakes the target (fixes HTML5 timing issue)
    if (target.buff(MagicalSleep.class) == null) {
        Buff.affect(target, MagicalSleep.class);
    }
    return super.act();
}
```

### Bug Fix #2: Char.java - 방어적 수정

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/Char.java`

**수정**: damage() 시 Drowsy도 함께 제거

```java
// 기존 코드
if (this.buff(MagicalSleep.class) != null){
    Buff.detach(this, MagicalSleep.class);
}

// 수정 코드
if (this.buff(MagicalSleep.class) != null){
    Buff.detach(this, MagicalSleep.class);
}
if (this.buff(Drowsy.class) != null){
    Buff.detach(this, Drowsy.class);
}
```

**import 추가**:
```java
import com.sacredpixel.sacredpixeldungeon.actors.buffs.Drowsy;
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/buffs/Drowsy.java:56-65` | MagicalSleep 존재 여부 확인 추가 |
| `actors/Char.java:913-919` | damage() 시 Drowsy 버프 제거 추가 |
| `actors/Char.java` (import) | Drowsy import 추가 |

---

## 수정 효과

| 항목 | Before | After |
|------|--------|-------|
| 피해 시 MagicalSleep 해제 | O | O |
| 피해 시 Drowsy 해제 | X | O |
| MagicalSleep 재적용 | 가능 (버그) | 불가능 |
| 수면 해제 동작 | 불안정 (HTML5) | 안정적 |

---

## 기술적 배경

### FlavourBuff 동작

`Drowsy`는 `FlavourBuff`를 상속:
- 지정된 시간(DURATION = 5f) 후 `act()` 실행
- `act()`에서 `super.act()` → `detach()` 호출하여 자동 소멸

### MagicalSleep 동작

- `attachTo()`: `target.paralysed++`로 마비 효과
- `detach()`: `target.paralysed--`로 마비 해제
- `damage()` 시 `detach()` 호출로 즉시 깨어남

### 왜 Desktop에서는 문제없나?

Desktop 빌드는 멀티스레드 환경에서 Actor가 더 예측 가능하게 스케줄링되어, `damage()` 호출 시점에 Drowsy가 이미 만료되어 있을 확률이 높음.

HTML5는 동기적 프레임 처리로 인해 같은 프레임 내에서 `damage()` → `Drowsy.act()` 순서로 실행될 수 있음.

---

## 관련 파일

- `Drowsy.java` - 졸음 버프 (MagicalSleep 전 단계)
- `MagicalSleep.java` - 마법 수면 버프
- `Char.java` - 캐릭터 기본 클래스 (damage 처리)
- `Actor.java` - HTML5 액터 시스템

---
