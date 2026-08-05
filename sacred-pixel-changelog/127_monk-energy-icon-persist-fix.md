# 127. 수도사 에너지 아이콘 잔존 버그 수정

## 배경

수도사(Monk)로 플레이 중 에너지가 있는 상태에서 사망 후, "다시 플레이하기"로 다른 클래스(예: 결투가)로 재시작하면 수도사의 에너지 아이콘이 그대로 남아있는 버그가 발생했습니다.

---

## 버그 현상

1. 수도사가 에너지가 있는 상태에서 사망
2. "다시 플레이하기"로 결투가(Duelist)로 재시작
3. 에너지 아이콘이 그대로 남아있음
4. 해당 아이콘으로 수도사의 특수능력 사용 가능
5. 능력 사용 후에도 에너지 충전량이 감소하지 않음
6. 상태창에서 에너지가 음수로 표시됨 (-54/10)
7. 타이틀 화면으로 나갔다 재진입하면 사라짐

---

## 원인 분석

### 근본 원인: static 필드 미초기화

`ActionIndicator.action`이 static 필드로 선언되어 있어, 씬 전환 후에도 이전 게임의 `MonkEnergy` 참조가 유지되었습니다.

**ActionIndicator.java:40**
```java
public static Action action;  // ← static 필드
```

### 경로 비교

| 경로 | ActionIndicator.clearAction() 호출 | 결과 |
|------|-------------------------------------|------|
| 타이틀 → HeroSelectScene → 새 게임 | O (line 210) | 정상 |
| 다시 플레이하기 → InterlevelScene | X | 버그 발생 |

"다시 플레이하기" 경로는 `HeroSelectScene`을 우회하여 `ActionIndicator.clearAction()`이 호출되지 않았습니다.

---

## 변경 사항

### WndRanking.java - "다시 플레이하기" 시 ActionIndicator 초기화

새 게임 시작 전에 `ActionIndicator.clearAction()`을 호출하여 이전 게임의 UI 상태를 정리합니다.

```diff
+ import com.sacredpixel.sacredpixeldungeon.ui.ActionIndicator;

  Dungeon.initSeed();

+ // Clear any lingering action from previous game (e.g., MonkEnergy)
+ ActionIndicator.clearAction();
  InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `windows/WndRanking.java:60` | `ActionIndicator` import 추가 |
| `windows/WndRanking.java:557-576` | "다시 플레이하기" 경로에 `clearAction()` 호출 추가 |

---

## 영향

- "다시 플레이하기"로 새 게임 시작 시 이전 게임의 ActionIndicator 상태가 완전히 정리됨
- 수도사 → 다른 클래스 전환 시 에너지 아이콘이 더 이상 잔존하지 않음
- `HeroSelectScene.proceedToGame()`과 동일한 초기화 동작 보장

---

## 관련 코드

- `ActionIndicator.java:40` - static `action` 필드 선언
- `ActionIndicator.clearAction()` - static 필드 초기화 메서드
- `HeroSelectScene.java:210` - 타이틀 경유 시 clearAction() 호출 위치
- `MonkEnergy.java` - ActionIndicator.Action 구현체
