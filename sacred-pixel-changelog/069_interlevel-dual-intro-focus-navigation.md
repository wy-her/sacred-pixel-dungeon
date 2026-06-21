# 069. InterlevelScene 이중 소개, 키보드 네비게이션, 포커스 수정

**날짜**: 2026-05-25

## 개요

Floor 1 진입 시 "던전" 소개와 "하수구" 소개가 순차적으로 표시되는 이중 소개 시스템 구현. 키보드 네비게이션 및 포커스 관련 수정 포함.

---

## 변경 사항

### 1. InterlevelScene — Floor 1 이중 소개 시스템

### 문제
- "던전 소개" 텍스트(Document.INTROS index 0)가 웹 버전에서 표시되지 않음
- 기존에는 WelcomeScene에서만 표시되었으나, 웹 버전은 첫 설치 개념이 없음
- 새 게임 시작 시 "하수구" 소개만 나오고 "던전" 소개가 누락됨

### 해결책
Floor 1 진입 시 "던전" 소개 → "하수구" 소개가 순차적으로 표시되도록 구현

### 수정 파일
`scenes/InterlevelScene.java`

### 주요 변경 사항

#### 새 필드 추가 (line ~134)
```java
private boolean hasPendingSecondStory = false;
private int pendingSecondRegion = 0;
```

#### Floor 1 이중 소개 생성 (descend() 내부, line ~308-322)
```java
if (Dungeon.hero == null) {
    // Floor 1: show "Dungeon" intro (region 0) first, then "Sewers" intro (region 1)
    createStoryElements(0);  // Dungeon intro
    hasPendingSecondStory = true;
    pendingSecondRegion = region;  // Sewers intro (region 1)
}
```

#### 페이드 아웃 완료 시 두 번째 소개 표시 (line ~508-541)
```java
if (storyFadeOutTime >= STORY_FADE_OUT_DURATION) {
    storyFadingOut = false;
    btnContinue.destroy();
    storyMessage.destroy();
    storyBG.destroy();
    btnContinue = null;
    storyMessage = null;
    storyBG = null;

    if (hasPendingSecondStory) {
        hasPendingSecondStory = false;
        createStoryElements(pendingSecondRegion);
        btnContinue.visible = true;
        btnContinue.enable(true);
        btnContinue.alpha(0);
        textFadingIn = true;
        storyFadeOutTime = 0f;
    } else {
        phase = Phase.FADE_OUT;
        timeLeft = fadeTime;
    }
}
```

### 동작 흐름
1. 새 게임 시작 → descend() 호출
2. `Dungeon.hero == null`이므로 Floor 1로 판단
3. "던전" 소개(region 0) 표시, `hasPendingSecondStory = true`
4. 사용자가 Continue 클릭 → 페이드 아웃
5. 페이드 아웃 완료 시 `hasPendingSecondStory` 체크
6. "하수구" 소개(region 1) 표시
7. 다시 Continue 클릭 → 게임 시작

### 참고
- Floor 6(감옥), Floor 11(동굴) 등 기존 소개 로직은 변경 없음
- WelcomeScene은 그대로 유지 (웹에서는 사용 안 함)

---

## 2. InterlevelScene — MINIMUM_DISPLAY_TIME 단축

### 문제
- 로딩 화면이 필요 이상으로 오래 표시됨
- `MINIMUM_DISPLAY_TIME`(1.2f)이 `FAST_FADE`(0.5f)보다 길어 ~0.2초 추가 대기 발생

### 해결책
`MINIMUM_DISPLAY_TIME`을 1.2f에서 1.0f로 변경

### 수정 파일
`scenes/InterlevelScene.java` — line ~74

### 변경 내용
```java
// Before
private static final float MINIMUM_DISPLAY_TIME = 1.2f;

// After
private static final float MINIMUM_DISPLAY_TIME = 1.0f;
```

### 효과
- 로딩 완료 후 추가 대기 시간 ~0.2초 감소
- 빠른 전환 시 체감 속도 향상

---

## 3. WndChallenges — [i] 버튼 포커스 잔류 수정

### 문제
- [i] (정보) 버튼을 마우스로 클릭하면 포커스 효과가 남아있음
- 키보드 네비게이션용 포커스가 마우스 클릭에도 적용되어 시각적 불일치 발생

### 해결책
마우스 클릭 시에는 포커스를 저장하지 않고 바로 클리어

### 수정 파일
`windows/WndChallenges.java` — line ~89-108

### 변경 내용
```java
IconButton info = new IconButton(Icons.get(Icons.INFO)){
    @Override
    protected void onClick() {
        super.onClick();
        descriptionWindow = new WndMessage(Messages.get(Challenges.class, challenge+"_desc"));
        SacredPixelDungeon.scene().add(descriptionWindow);
        // Only save if keyboard focus exists (don't save for mouse clicks)
        if (focusIndex >= 0) {
            savedFocusIndex = focusIndex;
        }
        clearFocus();
    }
};
```

### 이전 코드 (문제가 있던 버전)
```java
// else block이 마우스 클릭에도 포커스를 저장함
if (focusIndex >= 0) {
    savedFocusIndex = focusIndex;
} else {
    savedFocusIndex = focusedButtons.indexOf(this);  // 이 라인 제거
}
clearFocus();
```

---

## 4. WndChooseSubclass — [i] 버튼 키보드 네비게이션 추가

### 문제
- 서브클래스 선택 창에서 [i] (정보) 버튼이 키보드로 포커스 불가
- Tab/방향키로 서브클래스 버튼만 선택 가능, 정보 버튼은 마우스로만 클릭 가능

### 해결책
`addFocusableButton(clsInfo)` 호출 추가

### 수정 파일
`windows/WndChooseSubclass.java` — line ~141

### 변경 내용
```java
IconButton clsInfo = new IconButton(Icons.get(Icons.INFO)){
    @Override
    protected void onClick() {
        GameScene.show(new WndInfoSubclass(Dungeon.hero.heroClass, subCls));
    }
};
clsInfo.setRect(WIDTH-20, btnCls.top() + (btnCls.height()-20)/2, 20, 20);
add(clsInfo);
addFocusableButton(clsInfo);  // 추가됨
```

### 효과
- Tab/Shift+Tab으로 서브클래스 버튼과 [i] 버튼 사이 이동 가능
- Enter로 정보 창 열기 가능

---

## 5. WndChooseAbility — [i] 버튼 키보드 네비게이션 추가

### 문제
- 갑옷 능력 선택 창에서도 동일하게 [i] 버튼이 키보드로 포커스 불가

### 해결책
`addFocusableButton(abilityInfo)` 호출 추가

### 수정 파일
`windows/WndChooseAbility.java` — line ~153

### 변경 내용
```java
IconButton abilityInfo = new IconButton(Icons.get(Icons.INFO)){
    @Override
    protected void onClick() {
        GameScene.show(new WndInfoArmorAbility(Dungeon.hero.heroClass, ability));
    }
};
abilityInfo.setRect(WIDTH-20, abilityButton.top() + (abilityButton.height()-20)/2, 20, 20);
add(abilityInfo);
addFocusableButton(abilityInfo);  // 추가됨
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `scenes/InterlevelScene.java` | Floor 1 이중 소개 시스템, MINIMUM_DISPLAY_TIME 1.0f |
| `windows/WndChallenges.java` | [i] 버튼 마우스 클릭 시 포커스 잔류 수정 |
| `windows/WndChooseSubclass.java` | [i] 버튼 키보드 네비게이션 추가 |
| `windows/WndChooseAbility.java` | [i] 버튼 키보드 네비게이션 추가 |

---
