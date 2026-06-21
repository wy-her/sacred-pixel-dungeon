# 093. 연금술 및 도구 폴리시

**날짜**: 2026-06-14

## 개요

연금술 씬의 감정 윈도우 UX 개선, 도구상자 UI 정리, 번역 수정.

---

## 변경 사항

## 1. WndAlchemyIdentify 닫기 방지

### 문제점
에너지화로 아이템 감정 시 표시되는 `WndAlchemyIdentify` 윈도우가 ESC 키나 바깥 클릭으로 실수로 닫힐 수 있어 감정 결과를 확인하지 못하는 경우 발생

### 수정 내용

**파일:** `AlchemyScene.java`

```java
// Window for showing item identification in alchemy
// This window cannot be closed by user (ESC, outside click, etc.) - it closes automatically
public static class WndAlchemyIdentify extends Window {

    // Prevent closing by ESC key or clicking outside the window
    @Override
    public void onBackPressed() {
        // Do nothing - window closes automatically after animation completes
    }

    // ... 기존 코드
}
```

### 동작 방식

| 닫기 시도 | 이전 | 수정 후 |
|----------|------|---------|
| ESC 키 | 즉시 닫힘 | 무시됨 |
| 바깥 클릭/탭 | 즉시 닫힘 | 무시됨 |
| 애니메이션 완료 (3초) | 자동 닫힘 | 자동 닫힘 |

---

## 2. AlchemistsToolkit 버튼 아이콘 제거

### 문제점
AC_ENERGIZE 윈도우의 버튼들에 에너지 결정 아이콘이 표시되었으나, 모든 버튼에 동일한 아이콘이라 정보 가치가 없음

### 수정 내용

**파일:** `AlchemistsToolkit.java`

**삭제된 코드:**
```java
@Override
protected boolean hasIcon(int index) {
    return true;
}

@Override
protected Image getIcon(int index) {
    return new ItemSprite(ItemSpriteSheet.ENERGY);
}
```

**삭제된 import:**
```java
import com.watabou.noosa.Image;
```

### 변경 전/후

**변경 전:**
```
┌─────────────────────────────────────┐
│ [⚡] 6 에너지: +1 레벨               │
├─────────────────────────────────────┤
│ [⚡] 18 에너지: +3 레벨              │
└─────────────────────────────────────┘
```

**변경 후:**
```
┌─────────────────────────────────────┐
│ 6 에너지: +1 레벨                    │
├─────────────────────────────────────┤
│ 18 에너지: +3 레벨                   │
└─────────────────────────────────────┘
```

---

## 3. ac_energize 번역 수정

### 문제점
- 한국어 "에너지 전환"은 형태 변환을 암시하여 실제 기능(에너지 소비하여 강화)과 맞지 않음
- 일본어 "エネルギー"는 동사 없이 명사만 있어 액션 의미가 불명확

### 수정 내용

**파일:** `items_ko.properties`
```properties
# 변경 전
items.artifacts.alchemiststoolkit.ac_energize=에너지 전환

# 변경 후
items.artifacts.alchemiststoolkit.ac_energize=에너지 주입
```

**파일:** `items_ja.properties`
```properties
# 변경 전
items.artifacts.alchemiststoolkit.ac_energize=エネルギー

# 변경 후
items.artifacts.alchemiststoolkit.ac_energize=エネルギー注入
```

---

## 4. 튜토리얼 비밀문 버그 수정 (129번에서 계속)

### 추가 수정 내용

**파일:** `GameScene.java`
- TutorialLevel import 추가
- `endIntro()`에서 튜토리얼 레벨일 경우 비밀문 자동 발견 건너뛰기

**파일:** `TutorialManager.java`
- Terrain import 추가
- `SEARCH_GUIDE_CLOSED` 핸들러에서 문이 이미 발견된 경우 `SEARCH_HINT` 단계 건너뛰기

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `AlchemyScene.java` | WndAlchemyIdentify.onBackPressed() 추가 |
| `AlchemistsToolkit.java` | hasIcon/getIcon 제거, Image import 제거 |
| `items_ko.properties` | ac_energize 번역 변경 |
| `items_ja.properties` | ac_energize 번역 변경 |
| `GameScene.java` | TutorialLevel 예외 처리 (129번 관련) |
| `TutorialManager.java` | 조기 발견 시 단계 건너뛰기 (129번 관련) |

## 테스트 항목

1. 연금술 씬에서 미감정 아이템 에너지화 시 감정 윈도우가 ESC/클릭으로 닫히지 않는지 확인
2. 감정 윈도우가 3초 후 자동으로 닫히는지 확인
3. 도구상자 에너지 주입 윈도우에 버튼 아이콘이 없는지 확인
4. 한국어/일본어에서 "에너지 주입" 텍스트가 올바르게 표시되는지 확인
5. 튜토리얼에서 비밀문 관련 진행이 정상 작동하는지 확인

---
