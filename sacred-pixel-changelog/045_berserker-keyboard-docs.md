# 045. 버서커 문서 동기화 및 키보드 접근성

**날짜**: 2026-04-17

## 개요

기획 문서와 실제 소스코드 간의 불일치를 발견하여 문서를 최신 코드 상태에 맞게 업데이트했습니다.

---

## 변경 사항

### 광전사 (Berserker) 문서 수정

### 발견된 차이점

`게임 로직 수정 - 광전사.md` 문서와 실제 `Berserk.java` 코드 간에 **불사의 분노 (Deathless Fury)** 특성 설명이 일치하지 않았습니다.

#### 문서에 기재되어 있던 내용
| 특성 레벨 | 발동 조건 | 쿨다운 |
|-----------|----------|--------|
| +1 | 분노 100% 이상 | 영웅 레벨 1 |
| +2 | 분노 75% 이상 | 영웅 레벨 1 |
| +3 | 분노 50% 이상 | 영웅 레벨 1 |

#### 실제 코드 (Berserk.java)
| 특성 레벨 | 발동 조건 | 쿨다운 |
|-----------|----------|--------|
| +1 | 분노 100% 이상 | **300턴** |
| +2 | 분노 100% 이상 | **200턴** |
| +3 | 분노 100% 이상 | **100턴** |

### 코드 근거

```java
// Berserk.java:208-215
private float getDeathlessFuryThreshold(){
    int talentLevel = ((Hero)target).pointsInTalent(Talent.DEATHLESS_FURY);
    if (talentLevel > 0) {
        return 1.0f;  // 100% rage required for all talent levels
    }
    return 999f;  // No talent = impossible threshold
}

// Berserk.java:217-226
private int getDeathlessFuryCooldown(){
    int talentLevel = ((Hero)target).pointsInTalent(Talent.DEATHLESS_FURY);
    switch (talentLevel){
        case 1: return 300;
        case 2: return 200;
        case 3: return 100;
        default: return 0;
    }
}
```

### 수정 내용

- 발동 조건: 모든 레벨에서 100%로 수정
- 쿨다운: 영웅 레벨 기반에서 턴 기반(300/200/100턴)으로 수정
- 메시지 문자열 예시도 실제 코드와 일치하도록 수정

---

## 키보드 접근성 문서 수정

### 추가된 내용

`키보드 접근성 기획 - 반영 결과.md` 문서에 누락되었던 `StyledButton` 클래스의 포커스 관련 메서드를 추가했습니다.

#### StyledButton.java 추가 메서드
```java
public void saveTextColors() {
    text.saveColors();
}

public void restoreTextColor() {
    text.restoreSavedColors();
}
```

이 메서드들은 `TitleScene.java`와 `AmuletScene.java`에서 키보드 포커스 시 텍스트 색상을 노란색으로 변경하고, 포커스 해제 시 원래 색상으로 복원하는 데 사용됩니다.

### 수정된 파일 목록 업데이트

문서의 "수정된 파일 목록"에 `StyledButton.java`를 추가했습니다.

---

## 수정된 파일

| File | Changes |
|------|---------|
| `게임 로직 수정 - 광전사.md` | 불사의 분노 특성 설명 수정 (발동 조건, 쿨다운) |
| `키보드 접근성 기획 - 반영 결과.md` | StyledButton 메서드 추가 (saveTextColors, restoreTextColor) |

---

## 참고 사항

이번 변경은 **문서 동기화**만 수행하였으며, 실제 게임 코드에는 변경이 없습니다.

---
