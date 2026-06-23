# 102. Eye DeathGaze 휴식 중단 버그 수정

**날짜**: 2026-06-23

## 개요

악마의 눈(Eye)의 DeathGaze 원거리 광선 공격이 휴식 중인 영웅을 중단시키지 않는 버그 수정. `damageInterrupt` 플래그가 `rest()` 메서드에서 설정되지 않아 발생.

---

## 변경 사항

### Bug Fixes (1)

---

### [B-1] Hero.java - 휴식 시 damageInterrupt 플래그 설정

**문제**: 휴식(rest) 중 Eye의 DeathGaze 광선 데미지를 받아도 휴식이 중단되지 않음

**증상**:
- 휴식 중 광선 데미지가 여러 번 들어옴
- 체력이 낮아져 "심각한 데미지" 조건 충족 시에만 휴식 중단
- 또는 Eye가 인접 타일까지 접근해야 휴식 중단

**원인 분석**:
- `rest()` 메서드가 `damageInterrupt` 플래그를 설정하지 않음
- `damageInterrupt`가 이전 상태(`false`)로 유지됨
- 데미지 인터럽트 로직이 플래그 체크 후 무시

```java
// Hero.java - 데미지 인터럽트 조건
if (!(src instanceof Hunger || src instanceof Viscosity.DeferedDamage) && damageInterrupt) {
    interrupt();
}
```

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/hero/Hero.java`

**수정**: `rest()` 메서드에 `damageInterrupt = true` 추가

```java
// 수정 전
if (!fullRest) {
    if (sprite != null) {
        sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "wait"));
    }
}
resting = fullRest;

// 수정 후
if (!fullRest) {
    if (sprite != null) {
        sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "wait"));
    }
}
damageInterrupt = true;
resting = fullRest;
```

**damageInterrupt 플래그 동작**:

| 값 | 동작 |
|---|------|
| `true` | 데미지 받으면 현재 행동 중단 (휴식, 일반 대기) |
| `false` | 데미지 받아도 행동 유지 (자동 이동/탐색 중) |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../actors/hero/Hero.java:1517` | `damageInterrupt = true;` 추가 |

---
