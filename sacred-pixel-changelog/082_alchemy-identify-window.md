# 082. 연금술 감정 창 구현

**날짜**: 2026-06-02

## 개요

연금술 장면(AlchemyScene)에서 미확인 아이템을 에너지화(energize)할 때 아이템이 자동으로 감정되며, 이를 시각적으로 표시하는 기능을 구현했습니다.

---

## 변경 사항

### AlchemyScene.java

#### 새로운 클래스 추가: `WndAlchemyIdentify`
- 아이템 감정 시 화면 중앙에 표시되는 창
- 아이템 아이콘과 이름을 함께 표시 (IconTitle 사용)
- 애니메이션 효과:
  1. 기존 이름 표시 (0.5초)
  2. 페이드 아웃 → 새 이름으로 전환 → 페이드 인 (0.5초)
  3. 새 이름 유지 (1.5초)
  4. 페이드 아웃 후 창 닫힘 (0.5초)

#### 동적 너비 적용
- 세로모드(Portrait): 최대 149px
- 가로모드(Landscape): 최대 251px
- 다른 창들(WndJournal, WndSettings 등)과 동일한 너비 규격 적용

#### 긴 이름 처리
- 아이템 이름이 길 경우 자동 줄바꿈
- 기존 이름과 새 이름 중 긴 쪽에 맞춰 창 높이 결정
- 텍스트 세로 중앙 정렬

#### 타이밍 문제 해결
- `showIdentify()` 호출 시 즉시 창을 열지 않고, `pendingIdentifyItem` 변수에 저장
- 다음 `update()` 프레임에서 창을 열어 WndBag(아이템 선택창) 앞에 표시되도록 함

### WndEnergizeItem.java

- 디버그 로그 제거
- `showIdentify()` 호출 로직 유지 (미확인 아이템일 경우에만)

## Technical Details

### 문제 상황
1. 초기 구현에서 scene에 직접 overlay 요소를 추가했으나 표시되지 않음
2. WndMessage 사용 시 아이템 선택창(WndBag) 뒤에 숨겨지는 문제 발생
3. 화면 크기가 작을 때 배너가 표시되지 않는 문제

### 해결 방안
1. `update()` 메서드에서 다음 프레임에 창을 추가하여 z-order 문제 해결
2. Window 기반으로 변경하여 안정적인 렌더링 보장
3. 화면 모드에 따른 동적 최대 너비 적용

## 수정된 파일

| File | Changes |
|------|---------|
| `scenes/AlchemyScene.java` | WndAlchemyIdentify 클래스 추가, 감정 창 표시 로직 |
| `windows/WndEnergizeItem.java` | 디버그 로그 제거, showIdentify() 호출 |

---

## 원본 Shattered Pixel Dungeon (v3.3.8)과의 비교

### 원본 구현 방식

원본 Shattered Pixel Dungeon 3.3.8의 `AlchemyScene.java` (lines 904-971):

```java
private void showIdentify(Item item) {
    // Scene에 직접 요소 추가 (Window 사용 안 함)
    NinePatch BG = Chrome.get(Chrome.Type.TOAST_TR);
    BG.hardlight(0x222222);
    add(BG);

    // 2개의 IconTitle 사용 (기존 이름 / 새 이름)
    IconTitle oldTitle = new IconTitle();
    oldTitle.icon(new ItemSprite(item));
    oldTitle.label(Messages.titleCase(item.title()));
    add(oldTitle);

    IconTitle newTitle = new IconTitle();
    newTitle.icon(new ItemSprite(item));
    newTitle.label(Messages.titleCase(item.title()));
    newTitle.alpha(0);
    add(newTitle);

    // 동시에 3개 애니메이션 실행
    oldTitle.addAction(Actions.sequence(
        Actions.delay(0.5f),
        Actions.fadeOut(0.25f)
    ));
    newTitle.addAction(Actions.sequence(
        Actions.delay(0.5f),
        Actions.fadeIn(0.25f),
        Actions.delay(1.5f),
        Actions.fadeOut(0.5f)
    ));
    BG.addAction(Actions.sequence(
        Actions.delay(2.75f),
        Actions.fadeOut(0.5f),
        Actions.run(() -> {
            oldTitle.remove();
            newTitle.remove();
            BG.remove();
        })
    ));
}
```

### Sacred Pixel Dungeon 구현 방식

현재 Sacred Pixel Dungeon의 `AlchemyScene.java` (lines 1019-1174):

```java
// pendingIdentifyItem으로 지연 표시
private Item pendingIdentifyItem = null;

private void showIdentify(Item item) {
    pendingIdentifyItem = item;  // 다음 프레임에서 처리
}

@Override
public void update() {
    super.update();
    if (pendingIdentifyItem != null) {
        Item item = pendingIdentifyItem;
        pendingIdentifyItem = null;
        WndAlchemyIdentify wnd = new WndAlchemyIdentify(item);
        add(wnd);  // WndBag보다 나중에 추가되어 앞에 표시됨
    }
}

// 별도의 Window 클래스로 구현
public static class WndAlchemyIdentify extends Window {
    private static final float[] PHASE = {0.5f, 0.5f, 1.5f, 0.5f};
    private int phase = 0;
    private float timer = 0;

    // 4단계 애니메이션 관리
    @Override
    public void update() {
        timer += Game.elapsed;
        if (timer >= PHASE[phase]) {
            timer = 0;
            phase++;
            // 각 단계별 페이드 처리
        }
    }
}
```

### 주요 차이점

| 항목 | 원본 Shattered (3.3.8) | Sacred Pixel Dungeon |
|------|------------------------|----------------------|
| **구현 방식** | Scene에 직접 요소 추가 | Window 클래스 사용 |
| **요소 구성** | NinePatch BG + IconTitle 2개 | Window 내 IconTitle 1개 |
| **애니메이션** | Actions API로 동시 실행 | update()에서 phase별 수동 관리 |
| **타이밍** | 총 3.25초 (0.5+0.25+1.5+0.5+0.5) | 총 3.0초 (0.5+0.5+1.5+0.5) |
| **z-order 처리** | 별도 처리 없음 | pendingIdentifyItem으로 지연 표시 |
| **반응형 너비** | 고정 너비 | Portrait/Landscape별 동적 너비 |

### 변경 이유

1. **z-order 문제 해결**: 원본 방식은 WndBag(아이템 선택창) 뒤에 숨겨지는 문제 발생. `pendingIdentifyItem` 패턴으로 다음 프레임에서 창을 추가하여 해결.

2. **Window 기반 전환**: Scene 직접 요소는 TeaVM 환경에서 렌더링 문제 발생 가능성. Window 기반으로 안정적인 렌더링 보장.

3. **반응형 디자인**: 모바일/웹 환경을 위해 화면 모드별 동적 너비 적용.

4. **단일 IconTitle**: 기존/새 이름 전환을 하나의 IconTitle에서 처리하여 코드 단순화.

---
