# 061. 윈도우 닫기 동작 통일 및 Import 정렬 수정

**날짜**: 2026-05-17

## 개요

정보 표시 윈도우의 닫기 동작을 Pattern B (ESC/바깥 클릭)로 통일. WndTabbed 하위 창 열림 시 탭 전환 차단, WndRanking Stats 탭 한글 번역 추가, WebDataMerger Import 시 랭킹 정렬 버그 수정.

---

## 변경 사항

### WndTabbed 하위 창 탭 전환 차단

### 문제
WndRanking에서 WndScoreBreakdown, 특성 설명, 아이템 설명 등 하위 창이 열려 있는 동안에도 Space 키로 탭이 변경됨

### 원인
`WndTabbed.tabListener`가 부모 창의 `active` 상태를 체크하지 않음

### 수정
**파일:** `WndTabbed.java:55-60`

```java
KeyEvent.addKeyListener(tabListener = new Signal.Listener<KeyEvent>() {
    @Override
    public boolean onSignal(KeyEvent keyEvent) {
        // Don't handle tab cycling if this window is inactive (sub-window is open)
        if (!WndTabbed.this.active) return false;

        if (!keyEvent.pressed && KeyBindings.getActionForKey(keyEvent) == SPDAction.CYCLE){
            // ... tab cycling logic
        }
        return false;
    }
});
```

---

### WndRanking Stats 탭 한글 번역

### 문제
한글 언어 설정에서도 Stats 탭이 영어로 표시됨

### 원인
`windows_ko.properties`에 `WndRanking$StatsTab` 내부 클래스용 번역 키 누락

### 수정
**파일:** `windows_ko.properties`

추가된 키:
```properties
windows.wndranking$statstab.title=레벨 %1$d %2$s
windows.wndranking$statstab.score=점수
windows.wndranking$statstab.str=힘
windows.wndranking$statstab.duration=게임 시간
windows.wndranking$statstab.depth=최대 도달 층수
windows.wndranking$statstab.ascent=최고 역행 기록
windows.wndranking$statstab.seed=던전 시드
windows.wndranking$statstab.custom_seed=_시드 고정_
windows.wndranking$statstab.enemies=처치한 적
windows.wndranking$statstab.gold=수집한 골드
windows.wndranking$statstab.food=섭취한 음식
windows.wndranking$statstab.alchemy=제작한 아이템
```

---

### 윈도우 닫기 동작 통일 (Pattern A → B)

### 배경
정보 표시 윈도우 간 닫기 동작 불일치:
- **Pattern A**: Enter/어디 클릭이든 닫힘 (WndStory, WndBadge, WndJournalItem)
- **Pattern B**: ESC/바깥 클릭만 닫힘 (WndScoreBreakdown, WndInfoTalent 등 대부분)

### 결정
Pattern B로 통일 (3개 → 14개+ 변경보다 효율적, 실수로 닫힘 방지)

### 수정된 파일

| 파일 | 제거된 코드 |
|------|-------------|
| `WndStory.java` | 전체화면 PointerArea blocker, onSignal() Enter 핸들링 |
| `WndBadge.java` | 전체화면 PointerArea blocker, onSignal() Enter 핸들링 |
| `WndJournalItem.java` | 전체화면 PointerArea blocker, onSignal() Enter 핸들링 |

### 변경 전후 비교

| 동작 | 변경 전 (Pattern A) | 변경 후 (Pattern B) |
|------|---------------------|---------------------|
| Enter 키 | 닫힘 | 무반응 |
| 창 내부 클릭 | 닫힘 | 무반응 |
| ESC 키 | 닫힘 | 닫힘 ✅ |
| 창 바깥 클릭 | 닫힘 | 닫힘 ✅ |

---

### WebDataMerger 랭킹 정렬 버그

### 문제
Import 시 시드 고정 게임도 점수만 높으면 1위로 표시됨

### 원인
`WebDataMerger.mergeRankings()`에서 단순 점수 정렬 사용:
```java
Collections.sort(merged, (a, b) -> b.score - a.score);  // customSeed 무시
```

### 수정
**파일:** `WebDataMerger.java:172-173`

```java
// Sort using Rankings.scoreComparator (considers customSeed + score)
Collections.sort(merged, Rankings.scoreComparator);
```

`Rankings.scoreComparator` 동작:
1. customSeed 체크: 시드 고정 게임은 일반 게임보다 아래로 정렬
2. 점수 비교: 같은 카테고리 내에서 점수 높은 순

---

### WebDataMerger Import 후 하이라이트 버그

### 문제
Import 후 `lastRecord`가 업데이트되지 않아 잘못된 기록이 하이라이트됨

### 수정
**파일:** `WebDataMerger.java:184`

```java
Rankings.INSTANCE.records = finalList;
Rankings.INSTANCE.lastRecord = -1;  // Clear highlight after import (no "newest" record)
Rankings.INSTANCE.save();
```

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `WndTabbed.java` | active 상태 체크 추가 |
| `windows_ko.properties` | Stats 탭 한글 번역 추가 |
| `WndStory.java` | Pattern A → B 변경 |
| `WndBadge.java` | Pattern A → B 변경 |
| `WndJournalItem.java` | Pattern A → B 변경 |
| `WebDataMerger.java` | scoreComparator 사용, lastRecord = -1 |

---

## 테스트 결과

| 테스트 케이스 | 결과 |
|---------------|------|
| WndRanking 하위 창 열림 시 Space 키 → 탭 변경 안됨 | ✅ |
| 한글 설정 WndRanking Stats 탭 | ✅ 한글 표시 |
| WndStory ESC/바깥 클릭으로 닫기 | ✅ |
| WndBadge ESC/바깥 클릭으로 닫기 | ✅ |
| WndJournalItem ESC/바깥 클릭으로 닫기 | ✅ |
| Import 후 시드 고정 게임 정렬 | ✅ 일반 게임 아래로 |
| Import 후 하이라이트 | ✅ 없음 (정상) |

---
