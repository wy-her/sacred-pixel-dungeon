# 광전사 특성 변경

**날짜**: 2026-04-17

## 개요

광전사 서브클래스의 두 핵심 특성을 재설계하여 사용이 더 쉬워졌습니다. 불사의 분노는 턴 기반 쿨다운을 사용하고, 끝없는 분노는 분노 100% 초과 대신 최소 분노 하한선을 제공합니다.

---

## 변경 사항

### 1. 불사의 분노 (Deathless Fury)

#### 변경 전
| 특성 레벨 | 발동 조건 | 쿨다운 |
|-----------|----------|--------|
| +1 | 분노 100% 이상 | 영웅 레벨 3 |
| +2 | 분노 100% 이상 | 영웅 레벨 2 |
| +3 | 분노 100% 이상 | 영웅 레벨 1 |

#### 변경 후
| 특성 레벨 | 발동 조건 | 쿨다운 |
|-----------|----------|--------|
| +1 | 분노 **100%** 이상 | **300턴** |
| +2 | 분노 **100%** 이상 | **200턴** |
| +3 | 분노 **100%** 이상 | **100턴** |

#### 설계 의도
- 기존: 영웅 레벨 기반 쿨다운 (레벨업해야 재사용 가능)
- 변경: 턴 기반 쿨다운으로 변경하여 예측 가능하고 일관된 재사용 대기시간 제공
- **효과**: 쿨다운이 턴 단위로 표시되어 직관적이며, 특성 레벨을 올리면 쿨다운이 감소

---

### 2. 끝없는 분노 (Endless Rage)

#### 변경 전
| 특성 레벨 | 효과 |
|-----------|-----|
| +1 | 분노 최대치 116% |
| +2 | 분노 최대치 133% |
| +3 | 분노 최대치 150% |

100%를 초과하는 분노 1%당:
- 광포화 시 방어막 +1%
- 쿨다운 -1%
- (추가 피해량은 +50% 이상 증가하지 않음)

#### 변경 후
| 특성 레벨 | 효과 |
|-----------|-----|
| +1 | 분노 최소값 **10%** (자연 감소 시) |
| +2 | 분노 최소값 **20%** (자연 감소 시) |
| +3 | 분노 최소값 **30%** (자연 감소 시) |

- 분노가 자연 감소할 때 해당 최소값 미만으로 내려가지 않음
- **광포화 종료 시에는 최소값 적용 안됨** (분노가 0으로 초기화)

#### 설계 의도
- 기존: 분노 100% 초과 시 방어막/쿨다운 보너스 (복잡하고 활용 어려움)
- 변경: 분노가 일정 수치 이하로 떨어지지 않음
- **효과**: 전투 사이에 분노를 유지하기 쉬워져 다음 전투 준비가 수월해짐

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/buffs/Berserk.java` | 불사의 분노 턴 기반 쿨다운, 끝없는 분노 최소 하한선 |
| `core/src/main/assets/messages/actors/actors.properties` | 특성 설명 업데이트 (영어) |
| `core/src/main/assets/messages/actors/actors_ko.properties` | 특성 설명 업데이트 (한국어) |

---

## 코드 변경 상세

### Berserk.java

#### 1. 새 헬퍼 메소드 추가

```java
// Returns the rage threshold for Deathless Fury activation: always 100% for all levels
private float getDeathlessFuryThreshold(){
    int talentLevel = ((Hero)target).pointsInTalent(Talent.DEATHLESS_FURY);
    if (talentLevel > 0) {
        return 1.0f;  // 100% rage required for all talent levels
    }
    return 999f;  // No talent = impossible threshold (rage max is 100%)
}

// Returns the turn-based cooldown for Deathless Fury: 300/200/100 turns at level 1/2/3
private int getDeathlessFuryCooldown(){
    int talentLevel = ((Hero)target).pointsInTalent(Talent.DEATHLESS_FURY);
    switch (talentLevel){
        case 1: return 300;
        case 2: return 200;
        case 3: return 100;
        default: return 0;
    }
}

// Returns the minimum rage floor that doesn't decay: 10%/20%/30% at talent level 1/2/3
private float getEndlessRageMinimum(){
    int talentLevel = ((Hero)target).pointsInTalent(Talent.ENDLESS_RAGE);
    switch (talentLevel){
        case 1: return 0.10f;  // 10%
        case 2: return 0.20f;  // 20%
        case 3: return 0.30f;  // 30%
        default: return 0f;    // No talent = no minimum
    }
}
```

#### 2. 불사의 분노 발동 조건 (라인 206)

```java
&& power >= getDeathlessFuryThreshold()  // 100% for all levels
```

#### 3. 불사의 분노 턴 기반 쿨다운 (startBerserking 메소드)

```java
// Deathless Fury: Turn-based cooldown (300/200/100 turns at level 1/2/3)
// No level-based cooldown - only turn-based recovery
turnRecovery = getDeathlessFuryCooldown();
turnRecoveryMax = turnRecovery;
levelRecovery = 0;
```

#### 4. 분노 자연 감소 시 최소값 적용 (라인 144-168)

```java
float minPower = getEndlessRageMinimum();
float previousPower = power;
power -= GameMath.gate(0.1f, power, 1f) * 0.05f * Math.pow((target.HP / (float) target.HT), 2);

// Endless Rage protection:
// - If at or above minimum: don't decay below minimum
// - If below minimum (e.g., after talent upgrade): don't decay at all
if (minPower > 0) {
    float effectiveFloor = previousPower < minPower ? previousPower : minPower;
    if (power < effectiveFloor) {
        power = effectiveFloor;
    }
}
```

#### 5. 분노 최대치 100%로 고정 (라인 295)

```java
float maxPower = 1.0f; // Max rage is always 100%
```

#### 6. 100% 초과 분노 보너스 제거

```java
// Endless Rage no longer increases power above 100%, so no bonus multiplier
```

---

## 영향 범위

### 영향받는 시스템
- 광전사 분노 축적/감소 시스템
- 불사의 분노 자동 발동 조건
- 끝없는 분노 분노 상한선/하한선
- 광포화 쿨다운 계산
- 분노 버프 아이콘 표시

### 영향받지 않는 시스템
- 분노 축적 방식 (피해를 받을 때 축적 - 변경 없음)
- 분노에 따른 피해량 증가 (최대 +50% - 변경 없음)
- 광포화 방어막 기본 계산 (체력 비율에 따른 배율 - 변경 없음)
- 수동 광포화 활성화 (분노 100%에서 버튼 클릭 - 변경 없음)
- 광분의 촉매 (Enraged Catalyst) 특성 (변경 없음)
- 다른 서브클래스 및 게임 시스템 (영향 없음)

---

## 호환성 참고

### 세이브 파일
- `power` 값이 1.0을 초과하는 기존 세이브 파일은 로드 시 1.0으로 제한됨
- `levelRecovery` 값이 1.0을 초과하는 경우 기존 값 유지 (자연 감소로 해결)
- 게임 플레이에 실질적인 영향 없음

### 특성 상호작용
- **광분의 촉매 (Enraged Catalyst)**: 분노에 비례한 마법 발동률 증가 - 변경 없이 정상 작동
- **가르기 (Cleave)** 등 다른 광전사 특성: 영향 없음

---
