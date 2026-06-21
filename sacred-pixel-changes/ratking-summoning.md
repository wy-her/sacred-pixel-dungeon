# 쥐 대왕 소환 제단

**날짜**: 2026-06-06

## 개요

드워프 왕 처치 후 20층에 고대 소환 제단을 추가하여 플레이어가 쥐 대왕을 소환할 수 있습니다. 5층에서 쥐 대왕을 깨운 플레이어는 이 제단을 사용하여 쥐 대왕을 소환하고 왕의 왕관으로 Ratmogrify 능력을 얻을 수 있습니다.

---

## 변경 사항

### 소환 제단

| 항목 | 설명 |
|------|------|
| **위치** | 20층 출구 복도, 임프 상점 아래 |
| **조건** | `Statistics.ratKingAwoken == true` (5층에서 쥐 대왕을 깨움) |
| **트리거** | 제단(PEDESTAL) 위를 밟으면 소환 대화창 표시 |

### 소환 대화창

| 언어 | 제목 | 설명 |
|------|------|------|
| English | Ancient Summoning Altar | An ancient altar pulses with otherworldly energy... |
| 한국어 | 고대의 소환 제단 | 고대의 제단이 이계의 힘으로 맥동하고 있습니다... |

### 소환 결과

- 소환 성공 시 쥐 대왕이 제단 주변 8칸 중 빈 칸에 텔레포트로 등장
- 소환 실패 조건:
  - 쥐 대왕을 깨우지 않음 -> "소환 대상 없음" 메시지
  - 주변에 빈 공간 없음 -> "공간 부족" 메시지

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|---------|
| `core/src/main/java/com/sacredpixel/sacredpixeldungeon/levels/CityBossLevel.java` | 제단 방 생성, occupyCell 감지, 소환 대화창 및 로직 |
| `core/src/main/assets/messages/levels/levels.properties` | 영어 현지화 |
| `core/src/main/assets/messages/levels/levels_ko.properties` | 한국어 현지화 |

---

## 코드 변경 상세

### CityBossLevel.java

#### 1. 제단 방 생성 (build 메서드)

```java
//summoning altar room (same structure as impShop corridor - chasm at edges)
//Corridor is 7 tiles wide (columns 4-10), columns 3 and 11 remain as CHASM
Painter.fill(this, end.left+4, end.top+22, 7, 9, Terrain.EMPTY);

//summoning altar at center with vault entrance visuals (5x5 area)
Point altarCenter = new Point(end.left+7, end.top+26);  // center of room
summoningAltar = altarCenter.x + altarCenter.y * width();

// EMPTY tile at center for interaction
Painter.set(this, summoningAltar, Terrain.EMPTY);
```

#### 2. 제단 위 이동 감지 (occupyCell 메서드)

```java
@Override
public void occupyCell( Char ch ) {
    super.occupyCell(ch);

    // Check if hero stepped on the summoning altar (only if not already used)
    if (ch == Dungeon.hero && ch.pos == summoningAltar && !altarUsed) {
        showSummoningDialog();
    }
}
```

#### 3. 소환 대화창 표시 (showSummoningDialog 메서드)

```java
private void showSummoningDialog() {
    Game.runOnRenderThread(new Callback() {
        @Override
        public void call() {
            GameScene.show(new WndOptions(
                    Icons.SUMMONING_ALTAR.get(),
                    Messages.get(CityBossLevel.class, "altar_title"),
                    Messages.get(CityBossLevel.class, "altar_desc"),
                    Messages.get(CityBossLevel.class, "altar_summon"),
                    Messages.get(CityBossLevel.class, "altar_cancel")
            ) {
                @Override
                protected void onSelect(int index) {
                    if (index == 0) {
                        if (Statistics.ratKingAwoken) {
                            summonRatKing();
                        } else {
                            GLog.w(Messages.get(CityBossLevel.class, "altar_no_target"));
                        }
                    }
                }
            });
        }
    });
}
```

#### 4. 쥐 대왕 소환 (summonRatKing 메서드)

```java
private void summonRatKing() {
    ArrayList<Integer> candidates = new ArrayList<>();
    for (int i : PathFinder.NEIGHBOURS8) {
        int p = summoningAltar + i;
        if (Actor.findChar(p) == null && (passable[p] || avoid[p])) {
            candidates.add(p);
        }
    }

    if (candidates.isEmpty()) {
        GLog.w(Messages.get(CityBossLevel.class, "altar_no_space"));
        return;
    }

    int spawnPos = Random.element(candidates);
    RatKing ratKing = new RatKing();
    ratKing.state = ratKing.WANDERING;
    ratKing.pos = spawnPos;
    GameScene.add(ratKing, 1f);
    ScrollOfTeleportation.appear(ratKing, spawnPos);
    occupyCell(ratKing);

    // Mark altar as used so it can't be used again
    altarUsed = true;

    GLog.p(Messages.get(CityBossLevel.class, "altar_summoned"));
}
```

---

## 현지화

### levels.properties (영어)

```properties
levels.citybosslevel.altar_title=Ancient Summoning Altar
levels.citybosslevel.altar_desc=An ancient altar pulses with otherworldly energy. Its power can reach across dimensions to summon a being of royalty from another realm.\n\n_Do you wish to invoke the summoning ritual?_
levels.citybosslevel.altar_summon=Invoke the Summoning
levels.citybosslevel.altar_cancel=Leave it alone
levels.citybosslevel.altar_no_target=The altar remains dormant... Perhaps you haven't awakened any royal beings.
levels.citybosslevel.altar_no_space=There's no space around the altar for the summoning.
levels.citybosslevel.altar_summoned=The altar flashes with blinding light! A small figure materializes before you...
```

### levels_ko.properties (한국어)

```properties
levels.citybosslevel.altar_title=고대의 소환 제단
levels.citybosslevel.altar_desc=고대의 제단이 이계의 힘으로 맥동하고 있습니다. 이 힘은 차원을 넘어 다른 세계의 왕을 소환할 수 있습니다.\n\n_소환 의식을 행하시겠습니까?_
levels.citybosslevel.altar_summon=소환한다
levels.citybosslevel.altar_cancel=그냥 둔다
levels.citybosslevel.altar_no_target=제단이 조용합니다... 아직 깨어난 왕이 없는 것 같습니다.
levels.citybosslevel.altar_no_space=제단 주변에 소환할 공간이 없습니다.
levels.citybosslevel.altar_summoned=제단이 눈부신 빛을 발합니다! 작은 형체가 당신 앞에 나타났습니다...
```

---

## 레벨 구조

```
20층 (CityBossLevel)
+------------------+
|    출구 계단      | (EXIT)
+------------------+
|   임프 상점 방    | (ImpShopRoom, 9x9)
+------------------+
|   소환 제단 방    | (신규, 7x9)
|  +-----------+  |
|  |           |  |
|  |   [제단]   |  |
|  |           |  |
|  +-----------+  |
+------------------+
|    DK 투기장     | (arena)
+------------------+
|    입구 방       | (entry)
+------------------+
```

---

## 게임플레이 흐름

```
1. 5층 RatKingRoom에서 쥐 대왕과 대화 (깨움)
   +-- Statistics.ratKingAwoken = true

2. 20층 드워프 왕 처치 후 상단으로 이동

3. 임프 상점 아래의 소환 제단 방 발견

4. 제단 위로 이동 -> 대화창 표시

5. "소환한다" 선택
   +-- 성공: 쥐 대왕이 텔레포트로 등장
   +-- 실패: 조건 미충족 메시지

6. 쥐 대왕에게 King's Crown 전달 -> Ratmogrify 획득
```

---

## 참조: 왕의 왕관

- **획득**: Dwarf King 처치 시 드롭
- **용도**: 쥐 대왕에게 전달 -> 방어구에 Ratmogrify 능력 부여
- **Ratmogrify**: 영웅을 쥐 대왕 분신으로 변신시키는 특수 능력

---

## 테스트 체크리스트

- [ ] 5층에서 쥐 대왕을 깨운 후 20층 제단에서 소환 가능
- [ ] 쥐 대왕을 깨우지 않으면 소환 불가 메시지 표시
- [ ] 소환 시 쥐 대왕이 제단 주변에 정상 등장
- [ ] King's Crown으로 Ratmogrify 획득 가능
- [ ] 영어/한국어 메시지 정상 표시

---
