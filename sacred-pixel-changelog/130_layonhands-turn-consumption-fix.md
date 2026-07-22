# 130. 안수기도(Lay On Hands) 턴 소모 버그 수정

## 배경

성기사(Paladin)의 안수기도 스펠이 턴을 소모하지 않아야 하는데, 실제 게임에서는 1턴을 소모하는 버그가 발생했습니다.

원본 Shattered PD 3.3.8과 비교한 결과, Sacred PD에만 불필요한 `hero.spend(1f)` 호출이 추가되어 있었습니다.

---

## 변경 사항

### LayOnHands.java - 불필요한 턴 소모 코드 제거

**수정 전:**
```java
if (ch == hero){
    hero.spend(1f);              // ← 불필요한 턴 소모
    hero.sprite.operate(ch.pos);
    hero.next();
} else {
    hero.spend(1f);              // ← 불필요한 턴 소모
    hero.sprite.zap(ch.pos);
    hero.next();
}
```

**수정 후:**
```java
if (ch == hero){
    hero.sprite.operate(ch.pos);
    hero.next();
} else {
    hero.sprite.zap(ch.pos);
    hero.next();
}
```

---

## 원인 분석

| 메서드 | 역할 |
|--------|------|
| `hero.spend(1f)` | 명시적으로 1턴 시간 소모 |
| `hero.next()` | 다음 턴으로 진행 (턴 소모 없음) |

Sacred PD에서는 `spend(1f)` + `next()` 조합으로 **의도하지 않은 1턴 추가 소모**가 발생했습니다.

원본 Shattered PD 3.3.8에서는 `next()`만 호출하여 턴을 소모하지 않고 즉시 다음 행동이 가능했습니다.

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `actors/hero/spells/LayOnHands.java:94-100` | `hero.spend(1f)` 2줄 제거 |

---

## 영향

- 안수기도 스펠이 원본과 동일하게 **턴을 소모하지 않음**
- 성기사의 힐/차폐 능력 활용도 향상
- 전투 중 안수기도 사용 시 즉시 다음 행동 가능

---

## 검증 방법

에이전트 3명이 독립적으로 분석 후 교차검증하여 동일한 결론 도출:
- 원본 Shattered PD 3.3.8에는 `hero.spend(1f)` 호출 없음
- Sacred PD에만 해당 코드가 추가되어 있음

---

## 관련 코드

- `ClericSpell.onSpellCast()` - 스펠 마나 소모 처리 (턴 소모 없음)
- `TargetedClericSpell.onCast()` - 타겟팅 UI 처리
- `HolyTome.spendCharge()` - 성서 충전 소모

