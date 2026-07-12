# 124. Intro 모드에서 메인메뉴 버튼 항상 활성화

## 문제
게임 내 저널(가이드북)을 최초로 취득하기 전까지 게임 옵션 메뉴의 '메인메뉴' 버튼이 비활성화되어 있어서, 플레이어가 게임 도중 메인메뉴로 돌아갈 수 없었습니다.

## 원인
`WndGame.java:111`에서 `SPDSettings.intro()`가 true일 때 메인메뉴 버튼을 비활성화하는 코드가 있었습니다:

```java
if (SPDSettings.intro()) curBtn.enable(false);
```

## 수정 내용
해당 조건문을 제거하여 메인메뉴 버튼이 항상 활성화되도록 변경했습니다.

### core/.../windows/WndGame.java

```diff
  addButton(curBtn = new RedButton(Messages.get(this, "menu")) {
      @Override
      protected void onClick() {
          try {
              Dungeon.saveAll();
          } catch (IOException e) {
              SacredPixelDungeon.reportException(e);
          }
          Game.switchScene(TitleScene.class);
      }
  });
- if (SPDSettings.intro()) curBtn.enable(false);
```

## intro 모드의 다른 효과 (영향 없음)

이 수정은 메인메뉴 버튼에만 영향을 미치며, intro 모드의 다른 효과는 그대로 유지됩니다:

| 위치 | 효과 | 유지 여부 |
|------|------|----------|
| `GameScene.java` | 툴바/상태바/인벤토리 숨김 | ✅ 유지 |
| `GameScene.java` | 튜토리얼 이동 메시지 표시 | ✅ 유지 |
| `Hunger.java` | 배고픔 진행 안 됨 | ✅ 유지 |
| `Hero.java` | 숨겨진 문/함정 탐지 확률 0 | ✅ 유지 |
| `RegularPainter.java` | 1층 입구 문 숨김 | ✅ 유지 |
| `EntranceRoom.java` | 가이드북 배치 | ✅ 유지 |
| `HeroSelectScene.java` | 옵션 버튼 숨김 | ✅ 유지 |
| **`WndGame.java`** | **메인메뉴 버튼 비활성화** | ❌ **제거됨** |

## 영향
- 저널을 줍기 전에도 메인메뉴로 나갈 수 있음
- 튜토리얼 경험의 다른 부분은 영향 없음
