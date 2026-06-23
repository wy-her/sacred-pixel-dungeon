# 103. 튜토리얼 물약/주문서 레슨 추가

**날짜**: 2026-06-24

## 개요

튜토리얼에 나무 바리케이드, 불의 장벽(EternalFire), 그리고 물약/주문서 사용 레슨을 추가. 플레이어가 화염 물약으로 바리케이드를 태우고, 서리 물약으로 불을 끄고, 공포의 주문서를 사용하는 과정을 단계별로 학습. 튜토리얼 텍스트를 21개 언어로 번역.

---

## 변경 사항

### 1. 새로운 튜토리얼 흐름

기존 물약/주문서 단계를 세분화된 3단계로 교체:

| 기존 상태 | 새로운 상태 | 설명 |
|----------|------------|------|
| SCROLL_HINT | LIQUID_FLAME_HINT | 화염 물약으로 바리케이드 태우기 |
| SCROLL_USE | FROST_HINT | 서리 물약으로 불 끄기 |
| POTION_HINT | SCROLL_HINT | 공포의 주문서 사용하기 |
| POTION_USE | (제거됨) | - |

### 2. 튜토리얼 맵 구조 변경

튜토리얼 방 아래에 **별도의 불꽃 방** 추가 (MagicalFireRoom 스타일, 7x7 크기):

```
     0 1 2 3 4 5 6 7 8 9 ...
   3 # # # . . . . . . . #    ← 메인 튜토리얼 방 (x=3-9, y=3-9)
   4 # # # . . . . . . . #
   5 # # # . . . . . . . #
   6 # # # . . . H . . . #    ← H: 영웅 스폰 (6,6)
   7 # # # . . . . . . . #
   8 # # # . . . G . . . #    ← G: 가이드북 (6,8)
   9 # # # . . . . . . . #
  10 # # # B # # # # # # #    ← B: 바리케이드 (3,10) - 불꽃 방 입구
  11 # . . . . . . . # # #    ← 불꽃 방 내부 (x=1-7, 7칸)
  12 # . . . . . . . # # #
  13 # . . . . . . . # # #
  14 # 🔥🔥🔥🔥🔥🔥🔥# # #    ← EternalFire 장벽 (y=14, x=1-7, 7칸)
  15 # . . . . . . . # # #    ← EMPTY_SP (아이템 보호)
  16 # . . . . . . . # # #
  17 # 📜. . . . . . # # #    ← 주문서 위치 (1,17) - 불 뒤 왼쪽 하단 구석
  18 # # # # # # # # # # #
```

**맵 크기:** 16x20 (기존 16x16에서 확장)

**좌표 상수:**
```java
public static final int BARRICADE_POS = 10 * W + 3;   // (3, 10) - 입구
public static final int FIRE_LINE_Y = 14;             // 불 장벽 Y좌표 (중앙)
public static final int FIRE_ROOM_LEFT = 1;           // 불꽃 방 좌측
public static final int FIRE_ROOM_RIGHT = 7;          // 불꽃 방 우측
public static final int FIRE_ROOM_TOP = 11;           // 불꽃 방 상단
public static final int FIRE_ROOM_BOTTOM = 17;        // 불꽃 방 하단
public static final int SCROLL_POS = 17 * W + 1;      // (1, 17) - 불 뒤 왼쪽 하단 구석
```

### 3. 단계 전환 타이밍 변경

**기존:** 1칸 이동 후 다음 단계로 전환
**변경:** 5칸 이동 후 다음 단계로 전환

```java
private static final int MOVES_REQUIRED = 5;  // 다음 단계까지 필요한 이동 횟수
```

### 4. 물약 리스폰 타이밍 변경

**기존:** 1칸 이동 후 즉시 리스폰
**변경:** 5칸 이동 후 리스폰

```java
private static final int RESPAWN_DELAY_MOVES = 5;  // 리스폰까지 딜레이 (이동 횟수)
```

### 5. 주문서 힌트 타이밍

불꽃 장벽 제거 후 **1칸 이동** 후 힌트 표시 (다른 단계보다 빠르게):

```java
private static final int SCROLL_HINT_DELAY = 1;  // 주문서 힌트까지 필요한 이동 횟수
```

### 6. 미감정 상태 힌트 (효과 미공개)

모든 물약/주문서는 미감정 상태이므로, 힌트 메시지에서 **효과를 미리 알려주지 않음**:

**화염 물약 힌트:**
```
물약을 발견했습니다! 물약은 던지거나 마실 수 있습니다. 앞에 있는 나무더미에 던져보세요.
물약은 미감정 상태로 나타납니다. 물약을 사용하면 종류가 감정됩니다.
```

**서리 물약 힌트:**
```
또 다른 물약을 발견했습니다! 길을 막고 있는 불에 던져보세요.
물약은 미감정 상태로 나타납니다. 물약을 사용하면 종류가 감정됩니다.
```

**공포의 주문서 힌트:**
```
주문서를 발견했습니다! 주워서 인벤토리에서 읽어보세요.
주문서는 미감정 상태로 나타납니다. 주문서를 사용하면 종류가 감정됩니다.
```

### 7. 주문서 종류 변경

**기존:** 마법 지도의 주문서 (ScrollOfMagicMapping)
**변경:** 공포의 주문서 (ScrollOfTerror)

### 8. 변수명 변경

기존 `MAGIC_MAP_HINT`/`MAGIC_MAP_USED` 변수명을 `SCROLL_HINT`/`SCROLL_USED`로 변경:

| 기존 | 변경 |
|------|------|
| `TutorialState.MAGIC_MAP_HINT` | `TutorialState.SCROLL_HINT` |
| `TutorialAction.MAGIC_MAP_USED` | `TutorialAction.SCROLL_USED` |
| `WndTutorial.createMagicMapHint()` | `WndTutorial.createScrollHint()` |
| `windows.wndtutorial.magic_map_title` | `windows.wndtutorial.scroll_title` |
| `windows.wndtutorial.magic_map_msg` | `windows.wndtutorial.scroll_msg` |

### 9. 다국어 번역

튜토리얼 텍스트를 게임이 지원하는 21개 언어로 번역:

| 언어 코드 | 언어 |
|----------|------|
| be | 벨라루스어 |
| cs | 체코어 |
| de | 독일어 |
| el | 그리스어 |
| eo | 에스페란토 |
| es | 스페인어 |
| fr | 프랑스어 |
| hu | 헝가리어 |
| in | 인도네시아어 |
| it | 이탈리아어 |
| ja | 일본어 |
| nl | 네덜란드어 |
| pl | 폴란드어 |
| pt | 포르투갈어 |
| ru | 러시아어 |
| sv | 스웨덴어 |
| tr | 터키어 |
| uk | 우크라이나어 |
| vi | 베트남어 |
| zh | 중국어 간체 |
| zh-hant | 중국어 번체 |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `tutorial/TutorialState.java` | LIQUID_FLAME_HINT, FROST_HINT, SCROLL_HINT 추가 |
| `tutorial/TutorialManager.java` | 5칸 이동 요구사항, 5칸 리스폰 딜레이, 상태 기반 체크 로직, 주문서 힌트 1칸 후 표시 |
| `levels/TutorialLevel.java` | 7x7 불꽃 방 구조 (맵 크기 16x20), EternalFire 7칸 장벽, 공포의 주문서 |
| `windows/WndTutorial.java` | createLiquidFlameHint(), createFrostHint(), createScrollHint() |
| `items/scrolls/ScrollOfTerror.java` | SCROLL_HINT 상태에서 튜토리얼 트리거 추가 |
| `items/scrolls/ScrollOfMagicMapping.java` | 튜토리얼 트리거 제거 |
| `items/potions/PotionOfLiquidFlame.java` | FIRE_POTION_USED 액션 트리거 |
| `items/potions/PotionOfFrost.java` | FROST_POTION_USED 액션 트리거 |
| `messages/windows/windows.properties` | 효과 미공개 힌트 메시지 |
| `messages/windows/windows_ko.properties` | 한국어 힌트 메시지 |
| `messages/windows/windows_*.properties` | 21개 언어 튜토리얼 번역 추가 |

---

## Behavior

### Before

| 시나리오 | 결과 |
|---------|------|
| 물약/주문서 사용 | "바리케이드를 태워보세요", "불을 꺼보세요" 등 효과 힌트 |
| 단계 전환 | 1칸 이동 후 즉시 |
| 물약 리스폰 | 1칸 이동 후 즉시 |

### After

| 시나리오 | 결과 |
|---------|------|
| 화염 물약 사용 | "나무더미에 던져보세요" (효과 미공개) |
| 서리 물약 사용 | "불에 던져보세요" (효과 미공개) |
| 단계 전환 | 5칸 이동 후 |
| 물약 리스폰 | 5칸 이동 후 |
| 주문서 힌트 | 불꽃 장벽 제거 후 1칸 이동 후 표시 |
| 불꽃 방 크기 | 7x7 (MagicalFireRoom과 동일) |
| 주문서 위치 | 불꽃 방 왼쪽 하단 구석 (1,17) |
| 주문서 종류 | 공포의 주문서 |

---

## Design Intent

1. **미감정 경험**: 효과를 미리 알려주지 않아 실제 게임처럼 발견의 재미 유지
2. **적절한 페이싱**: 5칸 이동 요구로 플레이어가 서두르지 않고 학습
3. **리스폰 딜레이**: 물약 낭비 후 5칸 이동 후 리스폰
4. **인게임 경험 반영**: MagicalFireRoom과 동일한 7x7 크기의 불꽃 방
5. **주문서 힌트 빠른 표시**: 불꽃 장벽 제거 후 1칸 이동 후 즉시 힌트 표시

## Technical Reference

### MagicalFireRoom.EternalFire 특성
- `Freezing` 또는 `Blizzard` 블롭과 인접하면 소멸
- 물(`water` 지형)에서 증발
- 인접한 가연성 셀에 일반 `Fire` 확산
- 인접한 캐릭터에게 `Burning` 버프 적용 (4f)
- `EMPTY_SP` 지형 위의 아이템은 타지 않음
- 일부가 제거되면 전체 장벽이 소멸 (`fullyClear()`)

---

## Testing

### 맵 구조
- [ ] 불꽃 방이 7x7 크기로 생성됨
- [ ] 바리케이드가 입구(3,10)를 막고 있음
- [ ] EternalFire 장벽이 수평으로 7칸(x=1-7, y=14) 배치됨
- [ ] 공포의 주문서가 왼쪽 하단 구석(1,17)에 위치함

### 물약/주문서 사용
- [ ] 화염 물약 던지기 → 바리케이드 파괴
- [ ] 서리 물약 던지기 → EternalFire 소멸
- [ ] 공포의 주문서 사용 → 공포 효과 발동

### 타이밍
- [ ] 각 단계 전환이 5칸 이동 후 발생
- [ ] 물약 리스폰이 5칸 이동 후 발생
- [ ] 주문서 힌트가 불꽃 장벽 제거 후 1칸 이동 후 표시됨

### 힌트 메시지
- [ ] 화염 물약 힌트에서 "태워보세요" 없음
- [ ] 서리 물약 힌트에서 "꺼보세요" 없음
- [ ] 효과가 미리 공개되지 않음

---
