# 094. 광산 레벨 금 배치 수정

**날짜**: 2026-06-14

## 개요

128번 퀘스트 축소 이후 MiningLevel 진입 시 무한 루프로 인한 "저장된 게임을 읽을 수 없습니다" 오류 수정.

---

## 변경 사항

### 문제점

### 증상
- 13층에서 대장장이 퀘스트 수락 후 광산으로 내려갈 때 "저장된 게임을 읽을 수 없습니다" 오류 발생

### 원인 분석
128번 changelog에서 방 개수를 축소함:
- Large room: 3 → 2
- Small room: 6-8 → 3-4
- Secret room: 2 → 1
- Gold: 45-47 → 20-22

방 개수가 줄어들면서 벽 공간이 부족해짐. `MiningLevelPainter.generateGold()`의 do-while 루프가 벽에 금을 배치할 수 없어 무한 루프에 빠짐.

```java
// 문제 코드 (무한 루프)
do {
    // ... 벽에 금 배치 시도
} while (goldToAdd > 0);  // goldToAdd가 0이 안되면 영원히 반복
```

---

## 수정 내용

**파일:** `MiningLevelPainter.java`

### 변경 사항

1. **시도 횟수 카운터 추가**
```java
int maxAttempts = 50;  // 무한 루프 방지
int attempts = 0;
```

2. **진행 없으면 카운터 증가**
```java
// 한 바퀴 돌았는데 금이 하나도 배치되지 않았다면
if (goldToAdd == goldBefore) {
    attempts++;
    // ...
} else {
    attempts = 0;  // 금이 배치되었으면 카운터 리셋
}
```

3. **바닥 배치 폴백**
```java
if (attempts >= maxAttempts) {
    // 벽에 배치할 수 없는 남은 금은 바닥에 DarkGold로 배치
    while (goldToAdd > 0) {
        for (Room r : rooms) {
            if (r instanceof MineSecretRoom) continue;
            for (Point p : r.getPoints()) {
                int cell = level.pointToCell(p);
                if (goldToAdd > 0 && level.insideMap(cell)
                        && (map[cell] == Terrain.EMPTY || map[cell] == Terrain.EMPTY_SP
                        || map[cell] == Terrain.GRASS || map[cell] == Terrain.HIGH_GRASS)
                        && level.heaps.get(cell) == null) {
                    level.drop(new DarkGold(), cell);
                    goldToAdd--;
                    if (goldToAdd <= 0) break;
                }
            }
            if (goldToAdd <= 0) break;
        }
        break;  // 안전장치
    }
    break;
}
```

### 동작 방식

| 상황 | 처리 |
|------|------|
| 벽에 배치 가능 | 기존대로 WALL_DECO로 변환 |
| 벽 공간 부족 | 50회 시도 후 바닥에 DarkGold 아이템으로 배치 |
| 바닥 공간도 부족 | 가능한 만큼만 배치 (이론상 발생 안함) |

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `MiningLevelPainter.java` | generateGold() 무한 루프 방지 및 폴백 추가 |

## 테스트 항목

1. 새 게임 시작 → 13층까지 진행 → 대장장이 퀘스트 수락 → 광산 진입
2. 광산에서 금 총량이 퀘스트 요구량(20-22)만큼 있는지 확인
3. 벽에 금이 있고, 부족하면 바닥에 DarkGold 아이템이 있는지 확인

---

## 관련 이슈

- 128번 changelog의 퀘스트 축소로 인한 regression

---
