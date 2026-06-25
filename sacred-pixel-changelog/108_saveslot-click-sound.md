# 108: SaveSlot 버튼 클릭음 추가

## 개요
게임 시작 화면(StartScene)의 저장 슬롯 버튼에 클릭음이 없던 문제를 수정했습니다.

## 변경 사항

### StartScene.java - SaveSlotButton
- `onPointerDown()` 메서드 추가: 버튼 누를 때 클릭음 재생 및 배경 밝기 변경
- `onPointerUp()` 메서드 추가: 버튼 뗄 때 배경 색상 복원

## 영향 범위
- 진행 중인 게임 슬롯 선택 시 클릭음 재생
- 새 게임 시작 버튼 선택 시 클릭음 재생

## 기술적 세부사항
`SaveSlotButton`은 `Button`을 직접 상속하며, 기본 `Button` 클래스는 빈 `onPointerDown()`을 가지고 있어 클릭음이 재생되지 않았습니다. `StyledButton`이나 `IconButton`처럼 클릭음을 재생하도록 수정했습니다.

```java
@Override
protected void onPointerDown() {
    bg.brightness(1.2f);
    Sample.INSTANCE.play(Assets.Sounds.CLICK);
}

@Override
protected void onPointerUp() {
    bg.resetColor();
}
```
