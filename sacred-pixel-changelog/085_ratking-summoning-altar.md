# 085. 쥐대왕 소환 제단

**날짜**: 2026-06-06

## 개요

20층 CityBossLevel에 쥐 대왕을 소환할 수 있는 고대의 소환 제단을 추가했습니다. 5층에서 쥐 대왕을 깨운 플레이어는 이 제단에서 쥐 대왕을 소환하여 King's Crown으로 Ratmogrify 능력을 획득할 수 있습니다.

---

## 변경 사항

### 변경 사유

기존에는 5층에서 쥐 대왕을 깨운 후 다시 5층으로 돌아가야 King's Crown을 사용할 수 있었습니다. 이는 특히 Ascension 플레이에서 불편함을 초래했습니다. 20층에 소환 제단을 추가하여 플레이어가 더 쉽게 쥐 대왕에게 접근할 수 있게 했습니다.

### 수정 내용

### 1. CityBossLevel.java - 소환 제단 추가

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/CityBossLevel.java`

| 변경 | 내용 |
|------|------|
| 필드 추가 | `public static int summoningAltar` |
| 방 생성 | 임프 상점 아래 9x9 제단 방 |
| 이벤트 | `pressCell()`에서 제단 밟으면 대화창 |
| 소환 | `summonRatKing()` 메서드 추가 |

**소환 조건**:
- `Statistics.ratKingAwoken == true`
- 제단 주변 8칸 중 빈 칸 있음

---

### 2. 로컬라이제이션 추가

**levels.properties (English)**:
```properties
levels.citybosslevel.altar_title=Ancient Summoning Altar
levels.citybosslevel.altar_desc=An ancient altar pulses with otherworldly energy...
levels.citybosslevel.altar_summon=Invoke the Summoning
levels.citybosslevel.altar_cancel=Leave it alone
levels.citybosslevel.altar_no_target=The altar remains dormant...
levels.citybosslevel.altar_no_space=There's no space around the altar...
levels.citybosslevel.altar_summoned=The altar flashes with blinding light!...
```

**levels_ko.properties (한국어)**:
```properties
levels.citybosslevel.altar_title=고대의 소환 제단
levels.citybosslevel.altar_desc=고대의 제단이 이계의 힘으로 맥동하고 있습니다...
levels.citybosslevel.altar_summon=소환한다
levels.citybosslevel.altar_cancel=그냥 둔다
levels.citybosslevel.altar_no_target=제단이 조용합니다...
levels.citybosslevel.altar_no_space=제단 주변에 소환할 공간이 없습니다.
levels.citybosslevel.altar_summoned=제단이 눈부신 빛을 발합니다!...
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../levels/CityBossLevel.java` | 소환 제단 방, 대화창, 소환 로직 |
| `core/.../messages/levels/levels.properties` | 영어 메시지 추가 |
| `core/.../messages/levels/levels_ko.properties` | 한국어 메시지 추가 |

---

## 레벨 구조 변경

```
변경 전:
┌─────────────────┐
│    출구 계단     │
├─────────────────┤
│   임프 상점 방   │
├─────────────────┤
│    DK 투기장    │
└─────────────────┘

변경 후:
┌─────────────────┐
│    출구 계단     │
├─────────────────┤
│   임프 상점 방   │
├─────────────────┤
│   소환 제단 방   │  ← 신규
├─────────────────┤
│    DK 투기장    │
└─────────────────┘
```

---

## 관련 기능

- **Statistics.ratKingAwoken**: 5층에서 쥐 대왕을 깨우면 true
- **King's Crown**: Dwarf King 처치 시 드롭
- **Ratmogrify**: 쥐 대왕에게 왕관 전달 시 획득하는 방어구 능력

---
