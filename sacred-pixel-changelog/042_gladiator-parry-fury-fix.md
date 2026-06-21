# 042. 검투사 콤보 타이밍 및 층간 화면 개선

**날짜**: 2026-04-06

## 개요

검투사의 PARRY(받아치기)와 FURY(분노폭발) 필살기 버그를 수정하고, 6/11/16/21층 interlevel scene을 1층과 동일하게 통일했습니다.

---

## 1. 검투사 PARRY (6스택 받아치기) 수정

### 문제
- PARRY 사용 후 ��이 공격해도 회피가 발생하지 않음
- ParryTracker가 적 공격 전에 만료되어 사라짐

### 원인
- `ParryTracker.actPriority = HERO_PRIO+1 (=1)` 설정
- 몬스터는 `MOB_PRIO (=-2)`
- ParryTracker가 몬스터보다 먼저 act() 실행 → FlavourBuff의 1 TICK ���속시간 소진 ��� detach()
- 그 후 몬스터가 공격���지만 ParryTracker는 이미 사라진 상태

### 해결
```java
// Combo.java - ParryTracker
public static class ParryTracker extends FlavourBuff{
    { actPriority = MOB_PRIO-1;}  // -3, 몬스터보다 나중에 act
    ...
}
```

### 새로운 흐름
1. 영웅 PARRY 사용 → ParryTracker 생성
2. 몬스터 공격 → defenseSkill() → ParryTracker 발견 → INFINITE_EVASION
3. ParryTracker.act() → 반격 실행 또는 detach

---

## 2. 검투사 FURY (10스택 분노폭발) 수정

### 문제
- FURY가 1회 공격만 하고 끝남
- 또는 체인 중간에 적들이 영웅을 공격함

### 원인
- `hero.spend(hero.attackDelay())`가 첫 히트에서 호출됨
- Actor 시스템에서 적들에게 행동 기회가 주어짐
- 체인 공격 중에 적들이 끼어들어 공격

### 해결
```java
// Combo.java - FURY case
case FURY:
    if (count > 0){
        furyHitsLeft = count;
        count = 0;
        // Don't spend here - wait until chain completes
    }
    ...
    if (furyHitsLeft <= 0 || ...) {
        // Chain complete - spend time NOW
        hero.spendAndNext(hero.attackDelay());
    }
```

### 새로운 흐름
1. FURY 시작 → furyHitsLeft 설정, spend() 없음
2. 연속 공격 체인 실행 (적 개입 불가)
3. 체인 완료 → spendAndNext() 호출

---

## 3. InterlevelScene 통일 (6, 11, 16, 21층)

### 문제
- 1층과 6/11/16/21층의 interlevel scene 타이밍이 다름
- 6층 이후: 회색 오버레이 표시, 배경 fade-in 발생

### 해결

#### 회색 오버레이 제거
```java
// InterlevelScene.java - showAdDialog()
// 삭제: stageclearBg = new ColorBlock(...);
```

#### 배경 즉시 표시
```java
// InterlevelScene.java - startPostRegionComplete()
if (background != null) {
    background.visible = true;
    background.alpha(1);  // 즉시 표시 (1층과 동일)
}
```

#### FADE_IN에서 스토리 층 배경 fade-in 제거
```java
// InterlevelScene.java - FADE_IN case
if (exitAfterFadeIn) {  // storyCreatedForFadeIn 조건 제거
    if (background != null) background.alpha(Math.min(1, fadeInAlpha));
}
```

### 새로운 흐름 (1층과 동일)
1. WndRegionComplete 표시
2. 닫으면 → 배경 즉시 표시 (alpha=1)
3. loadingText fade-in
4. 스토리 요소 fade-in

---

## 4. 기타 수정

### 수도사 특성 이름 형식 통일
```properties
# actors_ko.properties
# 검투사와 동일한 형식��로 스킬 이름 강조
1 에너지 - _휘몰아치는 타격_: 주먹으로 재빠르게 공격합니다.
2 에너지 - _집중_: 다음 공격을 회피합니다.
3 에너지 - _돌진_: 근처의 위치로 즉시 돌진합니다.
4 에너지 - _날아차기_: 적을 발로 차 날려 보냅니다.
5 에너지 - _명상_: 상태 이상을 회복하고 마법 막대와 유물을 충전합니다.
```

---

## 수정된 파일 목록

| 파일 | 변경 내용 |
|------|----------|
| `Combo.java` | ParryTracker actPriority, FURY spend 타이밍 |
| `InterlevelScene.java` | 회색 오버레이 제거, 배경 즉시 표시 |
| `actors_ko.properties` | 수도사 스킬 이름 형식 |

---

## 디버그 로그 (제거 필요)

다음 파일에 디버그 로그가 추가되어 있음 (배포 전 제거 필요):
- `Combo.java`: `[COMBO]`, `[FURY]`, `[RIPOSTE]` 로그
- `Hero.java`: `[HERO] defenseSkill` 로그

---

*관련 버그: 검투사 PARRY 미작동, FURY 1회 공격*
