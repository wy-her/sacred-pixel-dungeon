# 129. SpiritArrow 누락 메서드 추가 및 SpectralBlades 검증

## 배경

원본 Shattered PD 3.3.8과 Sacred PD의 코드 비교 중, `SpiritBow.SpiritArrow` 내부 클래스에서 3개의 방어적 프로그래밍 메서드가 누락된 것을 발견했습니다.

또한 사냥꾼의 갑옷 능력 `SpectralBlades`(유령 칼날)의 다중 타겟 메카니즘이 원본과 동일한지 검증했습니다.

---

## 변경 사항

### 1. SpiritArrow 누락 메서드 추가

원본에 있으나 Sacred PD에 누락되었던 3개 메서드를 추가했습니다.

**SpiritBow.java - SpiritArrow 내부 클래스**

```java
// 추가 1: 빈 액션 목록 반환 (인벤토리 UI 방지)
@Override
public ArrayList<String> actions(Hero hero) {
    return new ArrayList<>();
}

// 추가 2: 기본 액션 없음 (자동 실행 방지)
@Override
public String defaultAction() {
    return null;
}

// 추가 3: 분할 불가 (스택 분리 방지)
@Override
public Item split(int amount) {
    return null;
}
```

### 메서드 역할

| 메서드 | 반환값 | 목적 |
|--------|--------|------|
| `actions()` | 빈 ArrayList | 인벤토리에서 액션 메뉴 표시 방지 |
| `defaultAction()` | null | Item.execute() 자동 실행 방지 |
| `split()` | null | 아이템 스택 분리 방지 |

### 에이전트 교차검증 결과

| 에이전트 | 결론 | 근거 |
|----------|------|------|
| Agent 1 | 안전 | SpiritArrow는 인벤토리에 도달 불가, 메서드 호출 경로 없음 |
| Agent 2 | 안전 | 호출부에서 null/빈 배열 처리 패턴 이미 확립됨 |
| Agent 3 | 안전 | 원본과 일치시키면 방어적 프로그래밍 강화 |

---

### 2. SpectralBlades 원본 비교 검증

유령 칼날의 다중 적 공격 메카니즘을 원본과 비교 검증했습니다.

| 항목 | 비교 결과 |
|------|-----------|
| Ballistica 타겟팅 | **완전 동일** ✓ |
| ConeAOE 부채꼴 범위 (30° × 특성 레벨) | **완전 동일** ✓ |
| findChar() 벽 관통 로직 | **완전 동일** ✓ |
| PROJECTING_BLADES 특성 적용 (2 × 레벨) | **완전 동일** ✓ |
| 데미지 분배 (주 타겟 100%, 부채꼴 50%) | **완전 동일** ✓ |
| 타겟 수 제한 (1 + 특성 레벨) | **완전 동일** ✓ |

**결론**: SpectralBlades.java는 원본과 100% 동일하며 수정 불필요.

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `items/weapon/SpiritBow.java:38` | `Item` import 추가 |
| `items/weapon/SpiritBow.java:306-314` | `actions()`, `defaultAction()` 메서드 추가 |
| `items/weapon/SpiritBow.java:387-390` | `split()` 메서드 추가 |

---

## 영향

- SpiritArrow가 원본과 동일한 방어적 프로그래밍 패턴 적용
- 만약 SpiritArrow가 예기치 않게 인벤토리 시스템에 도달하더라도 안전하게 동작
- 실제 게임플레이에는 영향 없음 (SpiritArrow는 임시 객체로만 사용)

---

## 검증된 파일 (변경 없음)

| 파일 | 상태 |
|------|------|
| `actors/hero/abilities/huntress/SpectralBlades.java` | 원본과 동일 확인 ✓ |

---

## 관련 코드

- `SpiritBow.knockArrow()` - SpiritArrow 생성 유일 경로
- `MissileWeapon.split()` - 부모 클래스의 null 처리 패턴
- `Item.execute()` - defaultAction() null 체크 (Line 188)
