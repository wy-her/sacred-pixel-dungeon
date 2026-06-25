# 109: Continue 모드 1층 배경 이미지 버그 수정

## 개요
1층에서 저장한 게임을 Continue할 때 SEWERS 배경 대신 ENTRANCE 배경이 표시되던 버그를 수정했습니다.

## 문제점
기존 조건 `(Dungeon.hero == null && loadingDepth == 1)`은 새 게임(DESCEND)과 Continue 모드를 구분하지 못했습니다.

- 새 게임: `Dungeon.hero = null` 설정 후 `Mode.DESCEND`
- Continue: `Dungeon.hero = null` 설정 후 `Mode.CONTINUE`

두 경우 모두 `Dungeon.hero == null`이 true가 되어, 1층 Continue 시에도 ENTRANCE 배경이 표시되었습니다.

## 변경 사항

### InterlevelScene.java
```java
// 수정 전
if (tutorialLevel || isTestLevelOnly || (Dungeon.hero == null && loadingDepth == 1)) {

// 수정 후
if (tutorialLevel || isTestLevelOnly || (mode == Mode.DESCEND && loadingDepth == 1)) {
```

## 영향 범위
- 1층에서 저장한 게임 Continue 시: `entrance.jpg` → `sewers.jpg`
- 새 게임 시작, 튜토리얼, 기타 모드: 변경 없음
