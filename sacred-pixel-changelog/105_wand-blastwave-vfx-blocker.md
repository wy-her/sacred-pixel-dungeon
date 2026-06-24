# 105. 충격파의 마법막대 밀치기 중 적 행동 버그 수정

**날짜**: 2026-06-24

## 개요

충격파의 마법막대(Wand of Blast Wave)로 적을 밀쳤을 때, 적이 밀려나기 전 위치에서 영웅을 공격하는 버그 수정.

---

## 증상

충격파의 마법막대로 적을 밀쳤을 때, 적이 밀려난 후에도 밀리기 전 위치에서 영웅을 공격하는 현상이 발생했다. 턴 순서상 밀치기 효과가 적의 행동보다 늦게 적용되는 것처럼 보였다.

---

## 문제 분석

### 원인

`WandOfBlastWave.throwChar()`에서 `Pushing` 액터 생성 시 `setBlockingVfx()` 호출이 누락됨.

### 버그 메커니즘

```
Turn T: 영웅이 충격파 마법막대 시전
├─ throwChar() → new Pushing(적, pos=10, newPos=20) 생성
│  └─ setBlockingVfx() 호출 없음 → blockingVfx = false
│
├─ Pushing.act() 실행
│  └─ if (blockingVfx) Actor.addVfxBlocker() → 실행 안 됨!
│
├─ VFX 블로커 없음 → Actor.process() 계속 진행
│
├─ 적.act() 실행 ← 버그 발생!
│  └─ 적.pos는 여전히 10 (구 위치)
│  └─ canAttack(hero) → TRUE → 영웅 공격!
│
└─ 0.15초 후 Effect 완료 → callback.call()
   └─ ch.pos = 20 (새 위치로 변경) ← 너무 늦음!
```

### 다른 코드와의 비교

**정상 구현 예시:**

```java
// RipperDemon.java (라인 227-229)
Pushing p = new Pushing(RipperDemon.this, leapPos, endPos);
p.setBlockingVfx();  // ✅ VFX 블로커 활성화
Actor.add(p);

// Ghoul.java (라인 290)
Actor.add(new Pushing(ghoul, ghoul.pos, newPos).setBlockingVfx());  // ✅

// EtherealChains.java (라인 214-215)
pushing.setBlockingVfx();  // ✅
Actor.add(pushing);
```

**문제 코드 (WandOfBlastWave.java):**

```java
Actor.add(new Pushing(ch, ch.pos, newPos, new Callback() {
    // ...
}));
// ❌ setBlockingVfx() 호출 없음
```

---

## 변경 사항

### Bug Fix

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/items/wands/WandOfBlastWave.java`

**수정**: `Pushing` 생성 후 `setBlockingVfx()` 호출 추가

```java
// 기존 코드 (라인 162)
Actor.add(new Pushing(ch, ch.pos, newPos, new Callback() {
    public void call() {
        // 콜백 내용
    }
}));

// 수정 코드 (라인 162-199)
Pushing p = new Pushing(ch, ch.pos, newPos, new Callback() {
    public void call() {
        // 콜백 내용
    }
});
p.setBlockingVfx();
Actor.add(p);
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `items/wands/WandOfBlastWave.java:162-199` | `setBlockingVfx()` 호출 추가 |

---

## 수정 효과

| 항목 | Before | After |
|------|--------|-------|
| `blockingVfx` | `false` (기본값) | `true` |
| VFX 블로커 | 비활성 | 활성 |
| Mob.act() 실행 | 애니메이션 중 가능 | 애니메이션 완료 후 |
| 적 공격 위치 | 밀리기 전 위치 (버그) | 밀린 후 위치 (정상) |

---

## 관련 Changelog

- [100_wand-blastwave-push-fix.md](100_wand-blastwave-push-fix.md) - VFX 블로커 타이밍 수정 (부분 수정)
- 본 수정으로 100번 수정의 누락된 부분 완성

---
