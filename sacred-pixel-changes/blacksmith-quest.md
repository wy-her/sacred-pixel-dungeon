# 대장장이 퀘스트 간소화

**날짜**: 2026-06-15

## 개요

동굴 지역의 대장장이 퀘스트를 간소화하여 초보자 접근성을 향상시켰습니다. 기존 퀘스트는 40개 이상의 금을 채굴해야 했지만, 이번 업데이트로 규모가 약 50% 축소되었으며 보상은 동일하게 유지됩니다.

---

## 변경 사항

### 채굴 레벨 규모 축소

| 항목 | 변경 전 | 변경 후 | 비율 |
|------|---------|---------|------|
| 대형 방 | 3 | 2 | 67% |
| 소형 방 | 6-8 | 3-4 | 50% |
| 비밀 방 | 2 | 1 | 50% |
| 어둠의 금 매장량 | 45-47 | 20-22 | 45% |

### 호감도 시스템 조정

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 금당 호감도 비율 | 50 | 100 |
| 최대 호감도 | 3000 | 3000 (변경 없음) |
| 무료 곡괭이 기준 | 2500 | 2500 (변경 없음) |

**결과**: 금 20개 채굴 시 호감도 2000 획득 (기존에는 금 40개 필요)

### 퀘스트 전투 영향

| 퀘스트 유형 | 변경 내용 |
|------------|----------|
| 크리스탈 퀘스트 | CrystalGuardians 3 -> 2 (페이즈 시스템 없음, 전투 단순화) |
| 놀 퀘스트 | GnollSappers 3 -> 2 -> GnollGeomancer 대시 페이즈 3 -> 2 |

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/MiningLevel.java` | 방 수 및 금 매장량 축소 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/mobs/npcs/Blacksmith.java` | 금당 호감도 비율 2배 증가 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/painters/MiningLevelPainter.java` | 무한 루프 방지 추가 |

---

## 코드 변경 상세

### MiningLevel.java

**라인 104**: 대형 방 수
```java
int rooms = 2;  // 3 -> 2 (퀘스트 축소)
```

**라인 111**: 소형 방 수
```java
rooms = Random.NormalIntRange(3, 4);  // 6-8 -> 3-4 (퀘스트 축소)
```

**라인 118**: 비밀 방 수
```java
rooms = 1;  // 2 -> 1 (퀘스트 축소)
```

**라인 149-150**: 어둠의 금 매장량
```java
return new MiningLevelPainter()
        .setGold(Random.NormalIntRange(20, 22))  // 45-47 -> 20-22 (퀘스트 축소)
```

### Blacksmith.java

**라인 471**: 금당 호감도 비율
```java
favor += Math.min(2000, gold.quantity()*100);  // 50 -> 100 (퀘스트 축소로 금당 호감도 2배)
```

### MiningLevelPainter.java

**라인 71-100**: 무한 루프 방지
```java
int maxAttempts = 50;  // 무한 루프 방지
int attempts = 0;
do {
    // 금 배치 로직
    if (goldToAdd == goldBefore) {
        attempts++;
        if (attempts >= maxAttempts) {
            break;
        }
    }
} while (goldToAdd > 0);
```

---

## 보상 시스템 (변경 없음)

| 호감도 | 보상 |
|--------|------|
| 500 | 재단조 (Reforge) |
| 1000 | 강화 (Harden) |
| 2000 | +1 강화 (Upgrade) |
| 2500 | 무료 곡괭이 반환 |
| 3000 | 대장간 제작 (Smith) |

---

## 설계 의도

1. **접근성 향상**: 초보자가 지루한 채굴 작업 없이 퀘스트 완료 가능
2. **밸런스 유지**: 최대 보상은 동일하게 유지
3. **시간 단축**: 평균 플레이 시간 50% 감소
4. **전투 단순화**: Guardian/Sapper 수 감소로 전투 난이도 완화

---

*기반 버전: Shattered Pixel Dungeon v3.3.8*
