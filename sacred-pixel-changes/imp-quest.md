# 임프 퀘스트 토큰 시스템

**날짜**: 2026-06-06

## 개요

임프 퀘스트의 토큰 수집 방식을 개선했습니다. 퀘스트 수락 시 장로 수도승 5마리가 해당 층에 즉시 스폰되고, 수집한 드워프 토큰 개수에 따라 보상 품질이 결정됩니다.

---

## 변경 사항

### 보상 테이블

| 토큰 수 | 보상 |
|:------:|------|
| 1 | +0 저주된 반지 |
| 2 | +1 저주된 반지 |
| 3 | +2 저주된 반지 |
| 4 | +3 저주된 반지 |
| 5+ | +3 반지 (저주 없음) |

---

### 변경 전/후 비교

#### 변경 사항 (Sacred)

1. 17-19층 어딘가에 임프 등장
2. 퀘스트 수락 시 **해당 층에 장로 수도승 5마리 즉시 스폰**
3. 각 장로 수도승 처치 시 드워프 토큰 드롭
4. 토큰 1개 이상 보유 상태로 임프에게 대화
5. **토큰 개수에 따라** 보상 품질 결정

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/mobs/npcs/Imp.java` | Quest.spawn(), spawnSeniorMonks(), process(), getAdjustedReward() |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/windows/WndImp.java` | 토큰 기반 메시지 선택, 보상 처리 |
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/rooms/quest/AmbitiousImpRoom.java` | 방 단순화 (금고 입구 제거) |
| `core/src/main/assets/messages/actors/actors.properties` | 영어 현지화 |
| `core/src/main/assets/messages/actors/actors_ko.properties` | 한국어 현지화 |
| `core/src/main/assets/messages/windows/windows.properties` | 영어 창 메시지 |
| `core/src/main/assets/messages/windows/windows_ko.properties` | 한국어 창 메시지 |

---

## 코드 변경 상세

### 1. Imp.java

#### Quest.spawn() - 보상 생성

```java
public static ArrayList<Room> spawn( ArrayList<Room> rooms ) {
    if (!spawned && Dungeon.depth > 16 && Random.Int( 20 - Dungeon.depth ) == 0) {
        rooms.add(new AmbitiousImpRoom());
        spawned = true;
        given = false;

        do {
            reward = (Ring)Generator.random( Generator.Category.RING );
        } while (reward.cursed);
        // Base reward is +0 cursed ring, will be upgraded based on token count
        reward.cursed = true;
    }
    return rooms;
}
```

#### Quest.spawnSeniorMonks() - 장로 수도승 스폰

```java
public static void spawnSeniorMonks() {
    questDepth = Dungeon.depth;

    ArrayList<Integer> candidates = new ArrayList<>();

    // Find valid spawn positions (passable cells not occupied)
    for (int i = 0; i < Dungeon.level.length(); i++) {
        if (Dungeon.level.passable[i]
                && Dungeon.level.findMob(i) == null
                && i != Dungeon.hero.pos
                && Dungeon.level.distance(i, Dungeon.hero.pos) > 4) {
            candidates.add(i);
        }
    }

    // Spawn 5 Senior Monks
    int toSpawn = Math.min(5, candidates.size());
    for (int i = 0; i < toSpawn; i++) {
        int idx = Random.index(candidates);
        int pos = candidates.remove(idx);

        Senior senior = new Senior();
        senior.pos = pos;
        senior.state = senior.WANDERING;
        GameScene.add(senior);
        ScrollOfTeleportation.appear(senior, pos);
    }

    // Notify the player
    GLog.w(Messages.get(Imp.class, "seniors_spawned"));
}
```

#### Quest.process() - 토큰 드롭 처리

```java
public static void process( Mob mob ) {
    // Only process Senior monks on the quest floor
    if (spawned && given && !completed && Dungeon.depth == questDepth) {
        if (mob instanceof Senior) {
            Dungeon.level.drop( new DwarfToken(), mob.pos ).sprite.drop();
        }
    }
}
```

#### Quest.getAdjustedReward() - 토큰 기반 보상 계산

```java
public static Ring getAdjustedReward(int tokenCount) {
    if (reward == null) return null;

    // Calculate upgrade level and curse status based on token count
    // 1 token: +0 cursed, 2 tokens: +1 cursed, 3 tokens: +2 cursed
    // 4 tokens: +3 cursed, 5+ tokens: +3 uncursed
    int upgradeLevel;
    boolean cursed;

    if (tokenCount >= 5) {
        upgradeLevel = 3;
        cursed = false;
    } else {
        upgradeLevel = tokenCount - 1; // 1->0, 2->1, 3->2, 4->3
        cursed = true;
    }

    reward.level(upgradeLevel);
    reward.cursed = cursed;

    return reward;
}
```

---

### 2. WndImp.java

#### 토큰 개수에 따른 메시지 선택

```java
private String getRewardMessageKey(int tokenCount) {
    if (tokenCount >= 5) {
        return "message_5"; // +3 uncursed
    } else if (tokenCount == 4) {
        return "message_4"; // +3 cursed
    } else if (tokenCount == 3) {
        return "message_3"; // +2 cursed
    } else if (tokenCount == 2) {
        return "message_2"; // +1 cursed
    } else {
        return "message_1"; // +0 cursed
    }
}
```

---

## 현지화

### actors.properties (영어)

```properties
actors.mobs.npcs.imp.seniors_spawned=You hear the sound of robes rustling throughout the floor...
actors.mobs.npcs.imp.seniors_1=I need you to deal with some troublesome monks for me. Kill the Senior Monks on this floor and bring me their tokens. The more you bring, the better your reward!
actors.mobs.npcs.imp.seniors_2=Still looking for tokens? The Senior Monks should drop them.
```

### actors_ko.properties (한국어)

```properties
actors.mobs.npcs.imp.seniors_spawned=층 전체에서 옷자락 스치는 소리가 들려옵니다...
actors.mobs.npcs.imp.seniors_1=귀찮은 수도승들을 좀 처리해줘. 이 층에 있는 장로 수도승들을 처치하고 그들의 토큰을 가져와. 많이 가져올수록 보상이 좋아질 거야!
actors.mobs.npcs.imp.seniors_2=아직 토큰을 찾고 있어? 장로 수도승들이 떨어뜨릴 거야.
```

### windows.properties (영어)

```properties
windows.wndimp.message_1=Only one token? Well, a deal's a deal. Here's a ring, but don't expect much...
windows.wndimp.message_2=Two tokens, not bad. This ring has a bit more power, though it's still cursed.
windows.wndimp.message_3=Three tokens! You're getting serious. This ring packs quite a punch, curse included.
windows.wndimp.message_4=Four tokens, impressive! This ring is powerful, though the curse remains.
windows.wndimp.message_5=All five tokens! You've outdone yourself. Take this pristine ring - no curse, maximum power!
windows.wndimp.reward=Claim Reward
```

### windows_ko.properties (한국어)

```properties
windows.wndimp.message_1=토큰 하나뿐이야? 뭐, 약속은 약속이니까. 반지를 줄게, 기대는 하지 마...
windows.wndimp.message_2=토큰 두 개, 나쁘지 않아. 이 반지는 좀 더 힘이 있어, 저주는 걸려있지만.
windows.wndimp.message_3=토큰 세 개! 제법인데. 이 반지는 꽤 강력해, 저주가 있긴 하지만.
windows.wndimp.message_4=토큰 네 개, 대단해! 이 반지는 강력해, 저주는 여전히 있지만.
windows.wndimp.message_5=토큰 다섯 개 전부! 정말 대단해. 이 깨끗한 반지를 가져가 - 저주 없이 최대 강화야!
windows.wndimp.reward=보상 받기
```

---

## 상세 보상 테이블

| 토큰 | 강화 | 저주 | 설명 |
|:----:|:----:|:----:|------|
| 1 | +0 | O | 최소 보상, 저주 해제 필요 |
| 2 | +1 | O | 약간의 강화, 저주 해제 필요 |
| 3 | +2 | O | 중간 보상, 저주 해제 필요 |
| 4 | +3 | O | 높은 강화, 저주 해제 필요 |
| 5 | +3 | X | 최대 보상, 저주 없음 |

---

## 게임플레이 흐름

```
1. 17-19층에서 임프 발견 (AmbitiousImpRoom)

2. 임프와 대화 -> 퀘스트 수락
   └─ 장로 수도승 5마리 즉시 스폰 (텔레포트 이펙트)
   └─ "옷자락 스치는 소리" 알림

3. 해당 층에서 장로 수도승 처치
   └─ 각각 드워프 토큰 1개 드롭
   └─ 최대 5개 수집 가능

4. 토큰 1개 이상 보유 상태로 임프와 대화

5. 토큰 개수에 따른 메시지 표시 후 보상 수령
   └─ 모든 토큰 소모
   └─ 반지 자동 감정

6. 임프 퇴장, 퀘스트 완료
```

---

## 설계 의도

### 위험-보상 균형
- 5마리 전부 처치해야 최상의 보상
- 일부만 처치하고 넘어가는 선택지 제공
- 저주된 반지 -> 추가 Remove Curse 스크롤 필요

### 층 고정 스폰
- `questDepth` 추적으로 해당 층에서만 토큰 드롭
- 다른 층의 장로 수도승은 토큰 드롭하지 않음

### 금고 퀘스트 분리
- 기존 AmbitiousImpRoom의 금고 입구 제거
- 금고 퀘스트는 별도 시스템으로 이동 (현재 비활성화)

---

## 테스트 체크리스트

- [ ] 17-19층에서 임프 방 정상 생성
- [ ] 퀘스트 수락 시 장로 수도승 5마리 스폰
- [ ] 장로 수도승 처치 시 드워프 토큰 드롭
- [ ] 토큰 1-5개에 따른 메시지 정상 표시
- [ ] 보상 강화 수치 정상 적용 (+0 ~ +3)
- [ ] 5개 토큰 시 저주 없는 반지 지급
- [ ] 다른 층에서 장로 처치 시 토큰 드롭 안 함

---
