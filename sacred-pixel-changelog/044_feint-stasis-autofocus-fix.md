# 044. 눈속임 잔상 버그, 정지 주문 번역, 오토포커싱 시스템 수정

**날짜**: 2026-04-12

## 개요

결투가의 눈속임(Feint) 잔상 버그 수정, 정지(Stasis) 주문 한국어 번역 오류 수정, 긍정적 마법 및 조사 버튼의 오토포커싱 시스템 수정.

---

## 변경 사항

### 1. 눈속임(Feint) 잔상 버그 수정

### 문제 증상
- 결투가가 눈속임으로 이동 후에도 적이 잔상 대신 영웅을 계속 공격
- 영웅이 적의 공격범위 내에 있을 때 발생

### 근본 원인
HTML5/TeaVM 환경에서 Actor 우선순위 문제:

| Actor | 기존 우선순위 | 행동 순서 |
|-------|-------------|----------|
| AfterImage | +1 (HERO_PRIO+1) | **1번째** |
| Hero | 0 (HERO_PRIO) | 2번째 |
| Mob | -20 (MOB_PRIO) | 3번째 |

- 잔상이 몹보다 먼저 act() 실행 → destroy() → 삭제됨
- 몹이 chooseEnemy() 실행 시 잔상이 이미 없음 → 영웅 타겟팅

### 수정 내용
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/hero/abilities/duelist/Feint.java`

```java
// 변경 전 (line 179)
actPriority = Actor.HERO_PRIO+1;  // 1

// 변경 후
actPriority = Actor.MOB_PRIO-1;   // -21
```

### 수정 후 동작
| Actor | 수정 후 우선순위 | 행동 순서 |
|-------|---------------|----------|
| Hero | 0 | 1번째 |
| Mob | -20 | **2번째** |
| AfterImage | -21 | **3번째** |

몹이 먼저 행동 → 잔상 공격 → 그 후 잔상 사라짐

---

## 2. 정지(Stasis) 주문 한국어 번역 오류 수정

### 문제 증상
- 인게임에서 정지 주문의 충전 소모량이 2인데, 한국어 설명에 1로 표시됨

### 수정 내용
**파일:** `core/src/main/assets/messages/actors/actors_ko.properties`

```
// 변경 전 (line 1292)
... 충전 소모 : 1 ...

// 변경 후
... 충전 소모 : 2 ...
```

---

## 3. 긍정적 마법 오토포커싱 시스템 수정

### 문제 증상
- 축복, 안수 등 긍정적 효과 마법 사용 시 초기 커서가 적에게 포커싱됨
- 적을 공격한 적이 없어도, 시야에 적이 보이면 자동으로 적에게 포커싱

### 근본 원인
1. `Hero.java:1731-1737`에서 시야에 적이 보이면 자동으로 `QuickSlotButton.lastTarget` 설정
2. `CellSelector.initKeyboardCursor()`가 항상 `lastTarget`에 커서 시작
3. 긍정적/부정적 마법 구분 없이 동일한 로직 적용

### 수정 내용

#### 3.1 CellSelector.Listener에 initialCell() 메서드 추가
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/CellSelector.java`

```java
public static abstract class Listener {
    // ... 기존 메서드 ...

    //Returns the preferred initial cursor cell for this targeting mode.
    //Return -1 to use default behavior (lastTarget or hero.pos).
    public int initialCell() { return -1; }
}
```

#### 3.2 initKeyboardCursor() 수정
```java
public void initKeyboardCursor(){
    if (keyboardCursorCell == -1 && Dungeon.hero != null) {
        //first check if the listener specifies an initial cell
        int preferredCell = listener != null ? listener.initialCell() : -1;
        if (preferredCell != -1 && preferredCell < Dungeon.level.length()) {
            keyboardCursorCell = preferredCell;
        } else if (QuickSlotButton.lastTarget != null ...) {
            // ... 기존 로직 ...
        }
    }
}
```

#### 3.3 TargetedClericSpell에 initialTargetCell() 헬퍼 추가
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/actors/hero/spells/TargetedClericSpell.java`

```java
protected int initialTargetCell() {
    return -1; //default: use lastTarget or hero.pos
}
```

#### 3.4 긍정적 효과 마법 수정

**BlessSpell.java:**
```java
@Override
protected int initialTargetCell() {
    return Dungeon.hero.pos; //beneficial spell, start cursor on hero
}
```

**LayOnHands.java:**
```java
@Override
protected int initialTargetCell() {
    return Dungeon.hero.pos; //beneficial spell, start cursor on hero
}
```

**WallOfLight.java:**
```java
@Override
protected int initialTargetCell() {
    return Dungeon.hero.pos; //defensive spell, start cursor on hero
}
```

### 수정 효과
- 축복, 안수, 빛의 벽 사용 시 초기 커서가 **영웅 위치**에서 시작
- 다른 공격 마법들은 기존 동작 유지 (적 타겟팅)

---

## 4. 조사(Examine) 버튼 오토포커싱 수정

### 문제 증상
- 조사 버튼을 눌렀을 때 초기 커서가 적에게 포커싱됨
- 적을 조사하려는 것이 아니라 주변 환경을 살펴보려는 경우에도 적에게 커서 시작

### 수정 내용
**파일:** `core/src/main/java/com/sacredpixel/sacredpixeldungeon/ui/Toolbar.java`

```java
private static CellSelector.Listener informer = new CellSelector.Listener() {
    @Override
    public void onSelect( Integer cell ) {
        if (instance != null) {
            instance.examining = false;
            GameScene.examineCell(cell);
        }
    }
    @Override
    public String prompt() {
        return Messages.get(Toolbar.class, "examine_prompt");
    }
    @Override
    public int initialCell() {
        return Dungeon.hero.pos; //examine starts at hero's position
    }
};
```

### 수정 효과
- 조사 버튼 사용 시 초기 커서가 **영웅 위치**에서 시작
- 기존 `CellSelector.Listener.initialCell()` 시스템 활용

---

## 수정된 파일

| File | Changes |
|------|---------|
| `Feint.java` | actPriority 변경 (HERO_PRIO+1 → MOB_PRIO-1) |
| `actors_ko.properties` | 정지 주문 충전 소모량 번역 수정 (1 → 2) |
| `CellSelector.java` | initialCell() 메서드 추가, initKeyboardCursor() 수정 |
| `TargetedClericSpell.java` | initialTargetCell() 헬퍼 추가 |
| `BlessSpell.java` | initialTargetCell() 오버라이드 (hero.pos 반환) |
| `LayOnHands.java` | initialTargetCell() 오버라이드 (hero.pos 반환) |
| `WallOfLight.java` | initialTargetCell() 오버라이드 (hero.pos 반환) |
| `Toolbar.java` | informer에 initialCell() 메서드 추가 |

---
