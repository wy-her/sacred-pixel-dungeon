# 091. 대장장이 퀘스트 축소

**날짜**: 2026-06-14

## 개요

트롤 대장장이 퀘스트의 그라인딩 요소를 줄이기 위해 퀘스트 규모를 약 50% 축소하고, 금-호감도 교환 비율을 2배로 증가시켜 동일한 최대 보상을 유지하면서 플레이 시간을 단축함.

---

## 변경 사항

### 1. MiningLevel.java - 방 및 금 매장량 축소

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| Large 방 | 3개 | 2개 |
| Small 방 | 6-8개 | 3-4개 |
| Secret 방 | 2개 | 1개 |
| 금 매장량 | 45-47개 | 20-22개 |

**경고 임계값 수정:**
- `exit_warn_none`: < 10 → < 5
- `exit_warn_low`: < 20 → < 10
- `exit_warn_med`: < 30 → < 15
- `exit_warn_high`: < 40 → < 20

### 2. Blacksmith.java - 금-호감도 비율 증가

```java
// 변경 전
favor += Math.min(2000, gold.quantity() * 50);

// 변경 후
favor += Math.min(2000, gold.quantity() * 100);
```

**밸런스 계산:**
- 20개 금 × 100 = 2000 호감도 (기존: 40개 × 50 = 2000)
- 보스 처치 보너스: +1000 호감도
- 최대 호감도: 3000 (변화 없음)

### 3. GnollGeomancer.java - sapperSpawns 버그 수정

```java
// 변경 전: int[3] 기본값 0 (유효한 맵 위치로 오인)
sapperSpawns = new int[3];

// 변경 후: -1로 초기화 (빈 슬롯 표시)
sapperSpawns = new int[3];
for (int j = 0; j < sapperSpawns.length; j++) {
    sapperSpawns[j] = -1;
}
```

### 4. Blacksmith.Quest 필드 접근성 변경

테스트를 위해 private 필드를 public으로 변경:
- `spawned`
- `started`
- `bossBeaten`

### 5. 다국어 메시지 수정 (21개 언어)

`actors_*.properties` 파일에서 "40개" → "20개" 변경:
- `intro_quest_start`
- `exit_warn_none`
- `exit_warn_low`
- `exit_warn_med`
- `exit_warn_high`

**수정된 언어:**
영어, 한국어, 독일어, 프랑스어, 스페인어, 포르투갈어, 이탈리아어, 러시아어, 네덜란드어, 체코어, 우크라이나어, 폴란드어, 중국어(간체/번체), 일본어, 베트남어, 헝가리어, 스웨덴어, 터키어, 에스페란토, 인도네시아어, 벨라루스어, 그리스어

### 6. 버전 업데이트

- `TeaVMLauncher.java`: version = "4.0.2-INDEV", versionCode = 901
- `build.gradle`: appVersionName = '4.0.2-INDEV', appVersionCode = 901

## Quest-Specific Impact

### Gnoll Quest
| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| GnollSapper | 3마리 | 2마리 |
| GnollGeomancer Phase | 3회 | 2회 |

→ Large 방 감소로 Sapper 수 감소, 따라서 보스 대시 Phase도 감소

### Crystal Quest
| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| CrystalGuardian | 3마리 | 2마리 |
| CrystalSpire (보스) | 1마리 | 1마리 |

→ Phase 시스템 없음, Guardian 감소로 난이도 완화

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../levels/MiningLevel.java` | 방 및 금 매장량 축소 |
| `core/.../actors/mobs/npcs/Blacksmith.java` | 금-호감도 비율 증가 (50→100) |
| `core/.../actors/mobs/GnollGeomancer.java` | sapperSpawns 버그 수정 |
| `actors_*.properties` (21개) | 금 요구량 메시지 수정 (40→20) |
| `teavm/.../TeaVMLauncher.java` | 버전 업데이트 |
| `build.gradle` | 버전 업데이트 |

---

## Testing

1. 동굴 층(14층)에서 게임 시작
2. 대장장이와 대화하여 퀘스트 시작
3. 광산 입구 사다리로 MiningLevel 진입
4. 금 20개 채굴 및 보스 처치 테스트
5. 호감도 계산 확인 (금 × 100 + 보스 1000)

---
