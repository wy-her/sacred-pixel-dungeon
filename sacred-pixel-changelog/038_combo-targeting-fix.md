# 038. 콤보 타겟팅 수정

**날짜**: 2026-04-04

## 개요

검투사 콤보 필살기가 적 애니메이션 중 타겟 선택이 불가능했던 버그 수정.

---

## 변경 사항

### 1. 콤보 타겟팅 버그 수정

### 문제
- 검투사의 모든 콤보 필살기가 작동하지 않음
- 적이 공격 애니메이션 중일 때 타겟 선택이 차단됨
- PARRY, FURY뿐 아니라 모든 필살기에 영향

### 원인
Changelog 43에서 추가한 `anyEnemyAnimating()` 체크가 콤보 타겟팅까지 차단:

```java
// CellSelector.java - 기존 코드
if (Actor.anyEnemyAnimating()) return;  // 모든 입력 차단
```

리퍼데몬 2회 연속 공격 중 영웅 이동 버그를 막기 위해 추가된 체크였으나, 콤보 타겟팅 모드도 차단하는 문제 발생.

### 해결
타겟팅 모드일 때는 입력을 허용하도록 예외 추가:

#### CellSelector.java - onClick() (line 91-92)
```java
//Block input while any enemy mob is in the middle of an attack animation
//BUT allow targeting selection (combo finishers, etc.) even during enemy animations
if (Actor.anyEnemyAnimating() && !isTargeting()) return;
```

#### CellSelector.java - select() (line 167-172)
```java
//Block input while any enemy mob is in the middle of an attack animation
//BUT allow targeting selection (combo finishers, etc.) even during enemy animations
if (Actor.anyEnemyAnimating() && !isTargeting()) {
    GameScene.cancel();
    return;
}
```

### isTargeting() 메서드 (line 298-300)
```java
public boolean isTargeting(){
    return listener != null && listener != GameScene.defaultCellListener;
}
```

- `listener != defaultCellListener`: 커스텀 리스너 활성화 = 타겟팅 모드
- 콤보 필살기, 지팡이, 투척 무기 등이 타겟팅 모드 사용

### 안전성 분석

| 시나리오 | isTargeting() | 체크 결과 | 동작 |
|---------|--------------|----------|------|
| 일반 이동 클릭 | `false` | `true && true` = `true` | **차단** ✓ |
| 콤보 타겟 선택 | `true` | `true && false` = `false` | **허용** ✓ |

**리퍼데몬 버그 재발 가능성: 없음**
- 일반 이동은 `defaultCellListener` 사용 → `isTargeting() == false` → 여전히 차단
- 타겟팅 클릭은 영웅을 이동시키지 않음 (타겟 셀만 선택)

---

## 수정된 파일 목록

| 파일 | 변경 내용 |
|------|----------|
| `scenes/CellSelector.java` | `onClick()`, `select()`에 `!isTargeting()` 예외 추가 |

---

## 테스트 체크리스트

- [ ] 검투사 CLOBBER (타격) 타겟 선택 작동 확인
- [ ] 검투사 COMBO (연타) 타겟 선택 작동 확인
- [ ] 검투사 PARRY (받아치기) 반격 작동 확인
- [ ] 검투사 FURY (분노폭발) 멀티히트 작동 확인
- [ ] 리퍼데몬 2회 공격 중 영웅 이동 불가 확인 (기존 버그 재발 방지)
- [ ] 경비원 공격 중 영웅 이동 불가 확인

---

## 기술적 배경

### 왜 타겟팅 모드는 허용해도 안전한가?

1. **타겟팅 모드의 동작**
   - 커스텀 `CellSelector.Listener`가 설정됨
   - 클릭 시 `listener.onSelect(cell)` 호출
   - 영웅 이동 없이 타겟 셀만 선택됨

2. **일반 이동의 동작**
   - `GameScene.defaultCellListener` 사용
   - 클릭 시 영웅이 해당 셀로 이동 시도
   - 적 애니메이션 중 이동하면 버그 발생

3. **수정의 핵심**
   - `isTargeting()`으로 두 모드를 구분
   - 타겟팅: 이동 없음 → 허용
   - 일반: 이동 있음 → 차단 유지
