# 086. 임프 퀘스트 토큰 시스템

**날짜**: 2026-06-06

## 개요

임프 퀘스트를 토큰 수집 기반의 가변 보상 시스템으로 변경했습니다. 퀘스트 시작 시 Senior Monk 5마리가 스폰되고, 수집한 Dwarf Token 개수에 따라 보상의 강화 수치와 저주 여부가 달라집니다.

---

## 변경 사항

### 변경 사유

기존 임프 퀘스트는 Senior Monk 또는 골렘을 한 마리만 처치하면 고정 보상을 받는 단순한 구조였습니다. 새로운 시스템은:

1. **위험-보상 균형**: 더 많은 적을 처치할수록 더 좋은 보상
2. **선택의 여지**: 일부만 처치하고 넘어가는 전략적 선택 가능
3. **명확한 목표**: 토큰 5개를 모으면 저주 없는 +3 반지

### 수정 내용

### 1. Imp.java - 퀘스트 시스템 개편

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/mobs/npcs/Imp.java`

| 변경 | 내용 |
|------|------|
| 필드 추가 | `questDepth` - 퀘스트 시작 층 기록 |
| 메서드 추가 | `spawnSeniorMonks()` - Senior 5마리 스폰 |
| 메서드 수정 | `process()` - 해당 층에서만 토큰 드롭 |
| 메서드 추가 | `getAdjustedReward()` - 토큰 기반 보상 계산 |

**보상 계산**:
```
토큰 1개: +0 저주된 반지
토큰 2개: +1 저주된 반지
토큰 3개: +2 저주된 반지
토큰 4개: +3 저주된 반지
토큰 5개: +3 반지 (저주 없음)
```

---

### 2. WndImp.java - 토큰 기반 UI

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndImp.java`

| 변경 | 내용 |
|------|------|
| 메서드 추가 | `getRewardMessageKey()` - 토큰 수별 메시지 키 |
| 메서드 수정 | `takeReward()` - 가변 보상 처리 |

---

### 3. AmbitiousImpRoom.java - 방 단순화

**파일**: `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/rooms/quest/AmbitiousImpRoom.java`

| 변경 | 내용 |
|------|------|
| 제거 | EXIT 타일 및 금고 입구 |
| 제거 | QuestEntrance 시각 효과 |
| 유지 | 9x9 고정 크기, 4개 석상 기둥 |

---

### 4. 로컬라이제이션 추가

**actors.properties / actors_ko.properties**:
- `seniors_spawned`: Senior 스폰 알림
- `seniors_1`: 퀘스트 설명 (토큰 수집)
- `seniors_2`: 진행 중 대화

**windows.properties / windows_ko.properties**:
- `message_1` ~ `message_5`: 토큰 수별 보상 메시지
- `reward`: 보상 버튼 텍스트

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../actors/mobs/npcs/Imp.java` | 토큰 시스템 로직 |
| `core/.../windows/WndImp.java` | 토큰 기반 UI |
| `core/.../levels/rooms/quest/AmbitiousImpRoom.java` | 방 단순화 |
| `core/.../messages/actors/actors.properties` | 영어 메시지 |
| `core/.../messages/actors/actors_ko.properties` | 한국어 메시지 |
| `core/.../messages/windows/windows.properties` | 영어 메시지 |
| `core/.../messages/windows/windows_ko.properties` | 한국어 메시지 |

---

## 게임플레이 비교

### 변경 전
```
임프 대화 → Senior/골렘 중 선택 → 1마리 처치 → 고정 보상
```

### 변경 후
```
임프 대화 → Senior 5마리 스폰 → 원하는 만큼 처치 → 토큰 수집 → 가변 보상
```

---

## 층 고정 처리

`questDepth` 필드로 퀘스트 시작 층을 기록합니다:

```java
public static void process( Mob mob ) {
    if (spawned && given && !completed && Dungeon.depth == questDepth) {
        if (mob instanceof Senior) {
            Dungeon.level.drop( new DwarfToken(), mob.pos ).sprite.drop();
        }
    }
}
```

다른 층에서 Senior Monk를 처치해도 토큰이 드롭되지 않습니다.

---

## 금고 퀘스트 분리

기존 AmbitiousImpRoom에 포함되어 있던 금고 퀘스트 입구(EXIT 타일)는 제거되었습니다. 금고 퀘스트는 별도 시스템으로 이동하여 현재 비활성화 상태입니다 (changelog 107 참조).

---
