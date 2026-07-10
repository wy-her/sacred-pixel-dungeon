# 118. 적 시야 내 영웅 행동 불가 버그 수정

**날짜**: 2026-07-10

## 개요

적이 시야 내에 있을 때 아이템 줍기, 이동 등 영웅 행동이 불가능한 버그 수정. Actor 시스템의 `heroWaiting` 가드 조건이 영웅 자체도 차단하는 문제 해결.

---

## 증상

- 적이 시야 내에 있을 때:
  - 아이템 줍기 불가
  - 특정 이동 불가
  - 기타 영웅 행동 불가
- 적이 시야에서 벗어나면 정상 동작

---

## 문제 분석

### 원인: 영웅이 가드 조건에서 차단됨

`heroWaiting` 가드 조건이 영웅 자체도 차단:

```java
// 기존 코드 (버그)
if (heroWaiting && current != null
        && current.time >= heroTime      // 영웅도 해당!
        && current.actPriority < VFX_PRIO) {
    current = null;  // 영웅도 차단됨
}
```

### 로그 증거

```
Actor: BLOCKING Hero (time=2.0) because heroWaiting=true, heroTime=2.0
```

영웅의 time(2.0)이 heroTime(2.0)보다 크거나 같아서 영웅이 차단됨.

---

## 변경 사항

### Actor.java - 영웅 차단 방지

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/Actor.java`

**수정**: 가드 조건에서 영웅 자체는 차단하지 않도록 수정

```java
// 기존 코드
if (heroWaiting && current != null
        && current.time >= heroTime
        && current.actPriority < VFX_PRIO) {
    current = null;
}

// 수정 코드
if (heroWaiting && current != null
        && current != Dungeon.hero  // 영웅은 차단하지 않음
        && current.time >= heroTime
        && current.actPriority < VFX_PRIO) {
    current = null;
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/Actor.java` | 가드 조건에 `current != Dungeon.hero` 추가 |

---

## 수정 효과

| 항목 | Before | After |
|------|--------|-------|
| 아이템 줍기 | 적 시야 시 불가 | 항상 가능 |
| 이동 | 적 시야 시 불가 | 항상 가능 |
| 영웅 차단 | 가드에서 차단됨 | 가드에서 제외됨 |

---

## 기술적 배경

### heroWaiting 메커니즘

- 영웅이 플레이어 입력 대기 중(`hero.ready=true`)일 때 설정
- `heroWaiting=true`면 `time >= heroTime`인 적의 행동을 차단
- 원래 의도: 적만 차단, 영웅은 허용

### 왜 버그가 발생했나?

`hero.ready` 체크를 추가하면서 `heroWaiting`이 설정되는 시점이 변경됨. 이 때 영웅 자체도 가드 조건에 걸려 차단되는 문제 발생.

---

## 관련 파일

- `Actor.java` - HTML5 액터 시스템
- `Hero.java` - 영웅 클래스 (ready 상태)

---
