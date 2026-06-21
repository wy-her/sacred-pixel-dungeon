# 070. AlchemyScene 키보드 네비게이션, 불안정한 마법책 버그 수정

**날짜**: 2026-05-25

## 개요

AlchemyScene에서 키보드로 UI 요소를 선택할 수 있도록 네비게이션 시스템 구현. 불안정한 마법책(UnstableSpellbook) 사용 시 WndBag이 먼저 나타나는 버그 수정.

---

## 변경 사항

### 1. AlchemyScene 키보드 네비게이션 구현

### 문제
- AlchemyScene(연금술 화면)에서 키보드로 UI 요소 선택 불가
- 마우스로만 조작 가능했음

### 해결책
Scene 레벨에서 키보드 네비게이션 시스템 구현

### 수정 파일
`scenes/AlchemyScene.java`

### 주요 변경 사항

#### Signal.Listener<KeyEvent> 구현
```java
public class AlchemyScene extends PixelScene implements Signal.Listener<KeyEvent> {

    // Keyboard navigation support
    protected ArrayList<Focusable> focusableButtons = new ArrayList<>();
    protected int focusIndex = -1;
```

#### 포커스 가능한 요소 등록
```java
// Register focusable buttons for keyboard navigation
// Order: inputs -> cancel/repeat -> combines/outputs -> guide -> energyAdd -> exit
for (int i = 0; i < inputs.length; i++) {
    focusableButtons.add(inputs[i]);
}
focusableButtons.add(cancel);
focusableButtons.add(repeat);
for (int i = 0; i < combines.length; i++) {
    focusableButtons.add(combines[i]);
    focusableButtons.add(outputs[i]);
}
focusableButtons.add(btnGuide);
focusableButtons.add(energyAdd);
focusableButtons.add(btnExit);

// Register for keyboard events
KeyEvent.addKeyListener(this);
```

#### 키 이벤트 핸들러
```java
@Override
public boolean onSignal(KeyEvent event) {
    if (event.pressed) {
        // Don't handle navigation if a window is open (e.g., guide window)
        if (hasOpenWindows()) {
            return false;
        }

        GameAction action = KeyBindings.getActionForKey(event);

        // Handle navigation keys
        if (action == SPDAction.N || action == SPDAction.NW
                || action == SPDAction.W || action == SPDAction.SW) {
            moveFocus(-1);
            return true;
        } else if (action == SPDAction.S || action == SPDAction.SE
                || action == SPDAction.E || action == SPDAction.NE) {
            moveFocus(1);
            return true;
        } else if (event.code == Input.Keys.ENTER || event.code == Input.Keys.NUMPAD_ENTER) {
            if (focusIndex >= 0) {
                activateFocused();
                return true;
            }
        }
    }
    return false;
}
```

#### Focusable 구현한 내부 클래스들
- **InputButton**: 재료 입력 슬롯
- **CombineButton**: 조합 버튼 (RedButton에 위임)
- **OutputSlot**: 결과물 표시 슬롯

### 조작 방법
- **방향키 (↑↓←→)**: 포커스 이동
- **Enter**: 선택/활성화
- **창이 열려있으면**: 네비게이션 비활성화

---

## 2. Guide 창 열릴 때 AlchemyScene 비활성화

### 문제
- Guide 창이 열려있는 동안에도 AlchemyScene의 키보드 네비게이션이 동작
- 의도치 않은 조작 발생 가능

### 해결책
`hasOpenWindows()` 체크로 창이 열려있으면 네비게이션 무시

### 변경 내용
```java
// onSignal() 시작 부분
if (hasOpenWindows()) {
    return false;
}
```

---

## 3. ItemSlot.item() getter 추가

### 문제
- ItemSlot의 `item` 필드가 protected라 외부에서 접근 불가
- OutputSlot에서 아이템 존재 여부 확인 필요

### 해결책
ItemSlot에 public getter 추가

### 수정 파일
`ui/ItemSlot.java`

### 변경 내용
```java
public Item item() {
    return item;
}
```

---

## 4. 불안정한 마법책 (UnstableSpellbook) 버그 수정

### 문제
- 불안정한 마법책 사용 시 이국적 주문서 선택창(WndOptions)이 나타남
- **선택 전에** WndBag(아이템 선택창)이 먼저 나타나는 버그
- 원인: ExploitHandler의 `actPriority = VFX_PRIO` (100)
- VFX_PRIO 액터는 `heroWaiting` 상태에서도 실행 허용됨
- ExploitHandler.act()가 WndOptions가 열린 상태에서 실행 → scroll.doRead() → WndBag 표시

### 해결책
ExploitHandler.act()에서 창이 열려있는지 확인, 열려있으면 대기

### 수정 파일
`items/artifacts/UnstableSpellbook.java` — ExploitHandler 클래스

### 변경 내용
```java
@Override
public boolean act() {
    // Don't execute while WndOptions is showing - wait for user selection
    if (GameScene.showingWindow()) {
        spend(TICK);
        return true;
    }

    curUser = Dungeon.hero;
    curItem = scroll;
    scroll.anonymize();
    scroll.talentChance = 0;
    Game.runOnRenderThread(new Callback() {
        @Override
        public void call() {
            scroll.doRead();
            Item.updateQuickslot();
        }
    });
    detach();
    return true;
}
```

### 동작 원리
1. **정상 흐름**: WndOptions 열림 → 사용자 선택 → onSelect() → handler.detach() → 창 닫힘
2. **수정 후**: act() 호출 시 창이 열려있으면 `spend(TICK)`으로 1턴 대기
3. **Anti-exploit 유지**: 게임 종료 후 재시작 시 창이 없으면 즉시 실행

### 왜 priority 변경이 아닌 이 방법인가?
- **priority를 BUFF_PRIO로 변경하면**: heroWaiting 중 실행 안 됨 → Anti-exploit 기능 손실
- **Window 체크 방식**: VFX_PRIO 유지하면서 창이 열려있을 때만 대기 → 모든 기능 유지

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `scenes/AlchemyScene.java` | 키보드 네비게이션 시스템, 창 열림 시 비활성화 |
| `ui/ItemSlot.java` | item() getter 추가 |
| `items/artifacts/UnstableSpellbook.java` | ExploitHandler window 체크 추가 |

---

## 테스트 항목

1. **AlchemyScene 키보드 네비게이션**
   - 방향키로 Input/Combine/Output/Guide/Energy/Exit 이동 확인
   - Enter로 선택 확인
   - Guide 창 열린 상태에서 네비게이션 비활성화 확인

2. **불안정한 마법책**
   - 이국적 주문서 선택창에서 WndBag이 먼저 나타나지 않음 확인
   - 선택 후 정상 동작 확인
   - (테스트 어려움) 게임 종료 후 재시작 시 Anti-exploit 동작 확인

---
