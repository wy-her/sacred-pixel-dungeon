# 097. 버전 4.0.5 폴리시 및 버그 수정

**날짜**: 2026-06-16

## 개요

버전 4.0.5 릴리스 준비를 위한 폴리싱 및 버그 수정. 클리어 화면 텍스트 개선, 업적 조건 단순화, 튜토리얼 버그 수정 등 진행.

---

## 변경 사항

### 1. 버전 업데이트 (4.0.4 → 4.0.5)

**Files Changed:**
- `build.gradle`
- `teavm/src/main/java/.../teavm/TeaVMLauncher.java`

**Details:**
```
appVersionCode: 903 → 904
appVersionName: "4.0.4" → "4.0.5"
```

---

### 2. SurfaceScene 클리어 버튼 텍스트 변경

**Issue:** 역행 완료 후 클리어 화면의 버튼 텍스트가 "Game Over"로 되어 있어 승리 상황에 부적절함.

**Solution:** "Victory" 및 각 언어별 승리 의미의 텍스트로 변경.

**Files Changed:**
- `core/src/main/assets/messages/scenes/scenes.properties` (23개 언어 파일)

**Changes (23 Languages):**

| Language | Before | After |
|----------|--------|-------|
| en | Game Over | Victory |
| ko | 게임 오버 | 승리 |
| ja | ゲームオーバー | 勝利 |
| zh | 游戏结束 | 胜利 |
| zh-hant | 結束遊戲 | 勝利 |
| de | Game Over | Sieg |
| fr | Partie Terminée | Victoire |
| es | Fin del Juego | Victoria |
| it | Game Over | Vittoria |
| pt | Game Over | Vitória |
| nl | Einde Spel | Overwinning |
| ru | Игра окончена | Победа |
| uk | Кінець гри | Перемога |
| pl | Koniec gry | Zwycięstwo |
| cs | Konec hry | Vítězství |
| hu | Játék vége | Győzelem |
| tr | Oyun Bitti | Zafer |
| sv | Spelet är slut | Seger |
| vi | Trò chơi kết thúc | Chiến thắng |
| el | Τέλος παιχνιδιού | Νίκη |
| be | Гульня скончана | Перамога |
| eo | Ludo finita | Venko |
| in | Game Over | Kemenangan |

---

### 3. VICTORY_RANDOM 업적 조건 단순화

**Issue:** 랜덤 승리 업적이 영웅/특성/서브클래스/갑옷능력 모두 랜덤을 요구했으나, 특성/서브클래스/갑옷능력 랜덤 UI가 제거되어 달성 불가능해짐.

**Solution:** 영웅 랜덤 선택 + 클리어만으로 업적 획득 가능하도록 조건 단순화.

**Files Changed:**

**Code Changes:**
- `Badges.java` - 조건 단순화
  ```java
  // Before
  if (Statistics.qualifiedForRandomVictoryBadge
          && Dungeon.hero.subClass != null
          && Dungeon.hero.armorAbility != null)

  // After
  if (Statistics.qualifiedForRandomVictoryBadge)
  ```

- `WndChooseSubclass.java` - 무효화 코드 제거
- `WndChooseAbility.java` - 무효화 코드 제거
- `TalentButton.java` - 무효화 코드 제거
- `RatKing.java` - 무효화 코드 제거

**Message Changes (23 Languages):**
- `core/src/main/assets/messages/misc/misc*.properties`
- Key: `badges$badge.victory_random.desc`

| Language | Before | After |
|----------|--------|-------|
| en | Obtain the Amulet of Yendor with a randomized hero, talent upgrades, subclass, and armor ability | Obtain the Amulet of Yendor with a randomized hero |
| ko | 무작위로 생성된 영웅, 특성 업그레이드, 보조 직업, 갑옷 능력을 가지고 옌더의 아뮬렛 획득 | 무작위로 선택된 영웅으로 옌더의 아뮬렛 획득 |
| (other languages) | (similar simplification) | (similar simplification) |

---

### 4. 튜토리얼 영웅 초상화 플래시 시간 조정

**Issue:** 튜토리얼에서 영웅 초상화 플래시가 3초로 설정되어 있어, 힌트 창을 읽는 동안 깜빡임이 멈출 수 있음. 다른 곳에서는 10초 사용.

**Solution:** 10초로 통일.

**File Changed:**
- `tutorial/TutorialManager.java`

```java
// Before
StatusPane.talentBlink = 3.0f;

// After
StatusPane.talentBlink = 10f;
```

---

### 5. 튜토리얼 실패 가이드 표시 버그 수정

**Issue:** 실제 게임에서 죽은 후 튜토리얼에 진입하면 "실패에 대처하는 법" 가이드가 잘못 표시됨.

**Root Cause:**
- `GameScene.java`에서 `Rankings.INSTANCE.totalNumber > 0` 조건으로 가이드 표시
- `Rankings`는 글로벌 상태로 튜토리얼에서 격리되지 않음
- 튜토리얼에서 `Document`가 리셋되어 가이드가 미읽음 상태가 되면서 조건 충족

**Solution:** 튜토리얼에서는 실패 가이드를 표시하지 않도록 조건 추가.

**File Changed:**
- `scenes/GameScene.java`

```java
// Before
if (!SPDSettings.intro() &&
        Rankings.INSTANCE.totalNumber > 0 &&
        !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_DIEING)){
    GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_DIEING);
}

// After
if (!SPDSettings.intro() &&
        !TutorialManager.isTutorialLevel() &&
        Rankings.INSTANCE.totalNumber > 0 &&
        !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_DIEING)){
    GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_DIEING);
}
```

---

## Testing Checklist

- [ ] 버전 번호 확인 (4.0.5 / 904)
- [ ] 역행 클리어 후 "Victory" 버튼 텍스트 확인
- [ ] 영웅 랜덤 선택 후 클리어 시 업적 획득 확인
- [ ] 튜토리얼 영웅 초상화 깜빡임 지속 시간 확인
- [ ] 실제 게임 사망 후 튜토리얼 진입 시 실패 가이드 미표시 확인
- [ ] 실제 게임에서 사망 후 재시작 시 실패 가이드 정상 표시 확인

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `build.gradle` | Version update (904, 4.0.5) |
| `TeaVMLauncher.java` | Version update |
| `scenes_*.properties` (23개) | 클리어 버튼 텍스트 "Victory"로 변경 |
| `misc_*.properties` (23개) | 랜덤 승리 업적 설명 단순화 |
| `Badges.java` | VICTORY_RANDOM 조건 단순화 |
| `WndChooseSubclass.java` | 무효화 코드 제거 |
| `WndChooseAbility.java` | 무효화 코드 제거 |
| `TalentButton.java` | 무효화 코드 제거 |
| `RatKing.java` | 무효화 코드 제거 |
| `TutorialManager.java` | 영웅 초상화 플래시 시간 조정 |
| `GameScene.java` | 튜토리얼 실패 가이드 표시 방지 |

---
