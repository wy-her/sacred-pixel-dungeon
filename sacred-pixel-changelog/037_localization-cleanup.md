# 037. 로컬라이제이션 정리 및 텍스트 렌더링 수정

**날짜**: 2026-04-03

## 개요

광전사(Berserker) 코드를 원본으로 롤백하고, 전체 언어 파일의 서식 문제 수정, 텍스트 렌더링 줄바꿈 버그 수정.

---

## 변경 사항

### 1. 광전사(Berserker) 롤백

### 롤백 사유
51_CHANGELOG-berserker-berserk-overhaul.md에서 변경한 광전사 메커니즘을 원본(Shattered Pixel Dungeon 3.3.8)으로 복원.

### 롤백된 파일

#### Java 코드
- `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/buffs/Berserk.java`

#### 번역 파일 (전체 23개 언어)
- 모든 `actors_*.properties` 파일의 광전사 관련 키 원복

### 롤백된 키
- `actors.buffs.berserk.angered_desc`
- `actors.hero.subclass.berserker.short_desc`
- `actors.hero.subclass.berserker.desc`
- `actors.hero.talent.endless_rage.desc`

---

## 2. 한국어 로컬라이제이션 수정

### expanding_wave.desc (확장된 충격파)
전각 스페이스 제거:
- `_6타일_ 로` → `_6타일_로`
- `_7타일_ 로` → `_7타일_로`
- `_8타일_ 로` → `_8타일_로`
- `_9타일_ 로` → `_9타일_로`

### elemental_power.desc (원소의 힘)
이중 공백 제거:
- `위력이  _25%_` → `위력이 _25%_`

### 기타 수정 (actors_ko.properties)
- `shockwave.short_desc`: `_충격파_ 를` → `_충격파_를`
- `double_mark.desc`, `focused_meal.desc`, `deadly_followup.desc` 등 이중 공백 수정

---

## 3. 전체 언어 파일 이중 공백 수정

`_  ` 패턴 (밑줄 서식 뒤 이중 공백) 수정.

### 수정된 파일 (20개)
- **actors**: nl, eo, hu, in, tr, es, vi, pl, de, pt, cs, ko
- **items**: cs, it, de, pt, vi, tr
- **windows**: pt
- **plants**: sv

---

## 4. 텍스트 렌더링 줄바꿈 버그 수정 ⭐

### 문제
한국어에서 `_25%_ 증가합니다` 같은 패턴에서 불필요한 줄바꿈 발생.
- "원소의 힘"에서 `위력이`와 `25%` 사이 줄바꿈
- "충격력"에서 `20%`와 `증가하며` 사이 줄바꿈

### 원인
`RenderedTextBlock.java`에서:
1. 하이라이트(`_`) 종료 후 공백이 건너뛰어짐 (trailing space 제거 로직)
2. `justEndedHighlight` 플래그가 공백 처리 시 리셋됨
3. 다음 단어에 `HIGHLIGHT_BRIDGE` 마커가 추가되지 않음
4. 결과: `25%`와 `증가합니다`가 하나의 단위로 묶여 너비 계산 → 줄바꿈 발생

### 수정 내용

#### 수정 #1: HIGHLIGHT_BRIDGE를 줄바꿈 가능 지점으로 처리
**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/RenderedTextBlock.java`
**위치**: 331-337줄

```java
// 변경 전
while (...&& words.get(j) != SPACE && words.get(j) != NEWLINE){
    if (words.get(j) == HIGHLIGHT_BRIDGE) { j++; continue; }
    fullWidth += words.get(j).width() - charGap;
    j++;
}

// 변경 후
while (...&& words.get(j) != SPACE && words.get(j) != NEWLINE
        && words.get(j) != HIGHLIGHT_BRIDGE){  // 줄바꿈 가능 지점으로 처리
    fullWidth += words.get(j).width() - charGap;
    j++;
}
```

#### 수정 #2: 공백 건너뛰기 시 justEndedHighlight 유지
**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/RenderedTextBlock.java`
**위치**: 147-152줄

```java
// 변경 전
} else if (str.equals(" ")){
    if (!justEndedHighlight) {
        words.add(SPACE);
    }
    justEndedHighlight = false;  // 항상 리셋

// 변경 후
} else if (str.equals(" ")){
    if (!justEndedHighlight) {
        words.add(SPACE);
        justEndedHighlight = false;
    }
    // 공백이 건너뛰어지면 justEndedHighlight 유지
    // → 다음 단어에 HIGHLIGHT_BRIDGE 추가됨
```

### 결과
- `위력이 _25%_ 증가합니다` 처리:
  - words 배열: `[위력이, SPACE, 25%, HIGHLIGHT_BRIDGE, 증가합니다]`
  - `25%`와 `증가합니다`가 별도 단위로 너비 계산
  - 불필요한 줄바꿈 방지

---

## 5. 검투사(Gladiator) PARRY/FURY 버그 수정 ⭐

### 문제
- **PARRY (받아치기)**: 반격이 실행되지 않음
- **FURY (분노폭발)**: 1회만 공격하고 종료됨

### 근본 원인
`Combo.java`의 `moveBeingUsed`와 `furyHitsLeft`가 **static 필드**로 선언되어 있어, HTML5의 프레임 단위 처리에서 상태 오염 발생.

**데스크톱 (원본)**: 스레드 기반 블로킹 대기로 FURY 공격이 연속 실행되어 문제 없음.

**HTML5 (Sacred PD)**: 프레임 단위 처리로 FURY 애니메이션 중 다른 액터(예: RiposteTracker)가 `moveBeingUsed`를 덮어써서 상태 오염.

### 수정 내용
**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/buffs/Combo.java`
**위치**: 343-344줄

```java
// 변경 전
private static ComboMove moveBeingUsed;
private static int furyHitsLeft = 0;

// 변경 후
private ComboMove moveBeingUsed;
private int furyHitsLeft = 0;
```

### 영향 범위
- 리퍼데몬/경비원 수정사항에 영향 없음 (완전히 독립적인 수정)
- VFX 블로킹, waitingForCallback 메커니즘과 무관

---

## 수정된 파일

| File | Changes |
|------|---------|
| `actors/buffs/Berserk.java` | 원본으로 롤백 |
| `ui/RenderedTextBlock.java` | 줄바꿈 버그 수정, HIGHLIGHT_BRIDGE 처리 개선 |
| `actors/buffs/Combo.java` | PARRY/FURY static 필드를 instance 필드로 변경 |
| `actors_*.properties` (23개) | 광전사 관련 키 원복 |
| `items_*.properties` (6개) | 이중 공백 수정 |
| `windows_*.properties` (1개) | 이중 공백 수정 |
| `plants_*.properties` (1개) | 이중 공백 수정 |
