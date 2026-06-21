# 096. 튜토리얼 저널 분리

**날짜**: 2026-06-16

## 개요

튜토리얼과 실제 게임 간의 일지(Journal) 시스템을 완전히 분리하여, 튜토리얼이 게임 진행에 영향을 주지 않고 게임 진행도 튜토리얼에 영향을 주지 않도록 개선.

---

## 변경 사항

### Problem

기존 동작에서 발생하던 문제:

1. **튜토리얼 → 게임 오염**: 튜토리얼에서 Guidebook을 픽업하면 7개의 가이드 페이지가 전역 일지에 저장되어, 실제 게임에서 직접 발견하는 재미가 사라짐

2. **게임 → 튜토리얼 간섭**: 첫 게임을 바로 시작하고 죽으면 "실패에 대처하는 법(Dieing)" 가이드가 발견됨. 이후 튜토리얼에 진입하면 시작하자마자 이 가이드가 표시되어 튜토리얼 흐름이 깨짐

```
게임 시작 → 죽음 → "Dieing" 가이드 발견
    ↓
튜토리얼 진입
    ↓
시작하자마자 "Dieing" 팝업 표시 ← 문제!
```

## Solution

**B안: 임시 상태로 교체 (완전 분리)** 구현

### 핵심 개념

```
튜토리얼 진입 시:
  1. 현재 일지 상태(Document, Catalog, Bestiary) 백업
  2. 빈 상태로 초기화
  3. 튜토리얼 진행 (저장 비활성화)

튜토리얼 종료 시:
  1. 원본 일지 상태 복원
  2. 튜토리얼에서 발견한 내용은 폐기
```

### 구현 흐름

```
┌─────────────────────────────────────────────────────────────┐
│                    튜토리얼 플레이                            │
│  ┌───────────────┐    ┌───────────────┐    ┌─────────────┐ │
│  │enterTutorial()│───→│  임시 일지    │───→│ 저장 차단   │ │
│  │ 원본 백업     │    │ (빈 상태)     │    │ (격리됨)    │ │
│  └───────────────┘    └───────────────┘    └─────────────┘ │
│                                                             │
│                    튜토리얼 종료                             │
│  ┌───────────────┐    ┌───────────────┐                    │
│  │exitTutorial() │───→│ 원본 일지     │                    │
│  │ 상태 복원     │    │ (변경 없음)   │                    │
│  └───────────────┘    └───────────────┘                    │
└─────────────────────────────────────────────────────────────┘
```

## 변경 사항

### 1. TutorialManager.java

**새로운 필드:**
```java
private static Bundle savedJournalState = null;
```

**새로운 메서드:**
```java
// 튜토리얼 진입 시 호출 - 일지 상태 백업 후 초기화
public static void enterTutorial()

// 튜토리얼 종료 시 호출 - 원본 상태 복원
public static void exitTutorial()

// 일지 격리 상태 확인
public static boolean isJournalIsolated()
```

### 2. Journal.java

`saveGlobal()` 메서드에서 튜토리얼 격리 중일 때 저장 스킵:
```java
public static void saveGlobal(boolean force) {
    // Don't save journal during tutorial
    if (TutorialManager.isJournalIsolated()) {
        return;
    }
    // ... 기존 저장 로직
}
```

### 3. InterlevelScene.java

튜토리얼 진입/종료 시 호출 추가:
```java
if (tutorialLevel) {
    TutorialManager.enterTutorial(); // 추가
    // ... 기존 초기화 로직
}

// 일반 게임 시작 시 복원
TutorialManager.exitTutorial();
```

### 4. TitleScene.java

메인 메뉴 복귀 시 복원:
```java
@Override
public void create() {
    TutorialManager.exitTutorial();
    // ...
}
```

---

## 수정된 파일

| File | Changes |
|------|---------|
| `tutorial/TutorialManager.java` | `enterTutorial()`, `exitTutorial()`, `isJournalIsolated()` 추가 |
| `journal/Journal.java` | `saveGlobal()`에서 튜토리얼 격리 체크 |
| `scenes/InterlevelScene.java` | 튜토리얼 진입/종료 시 호출 |
| `scenes/TitleScene.java` | 메인 메뉴 복귀 시 복원 |

## Behavior

### Before

| 시나리오 | 결과 |
|---------|------|
| 튜토리얼 완료 → 첫 게임 | 일지에 가이드 페이지 7개 이미 열려있음 |
| 게임에서 죽음 → 튜토리얼 | "Dieing" 가이드가 튜토리얼 시작 시 표시됨 |

### After

| 시나리오 | 결과 |
|---------|------|
| 튜토리얼 완료 → 첫 게임 | 일지 비어있음, 직접 발견해야 함 |
| 게임에서 죽음 → 튜토리얼 | 튜토리얼은 깨끗한 상태에서 시작 |
| 튜토리얼 → 게임 복귀 | 게임 일지 진행도 그대로 유지 |

## Design Intent

튜토리얼을 **완전히 독립적인 샌드박스 환경**으로 만들어:

1. **학습 경험 일관성**: 튜토리얼은 항상 처음 하는 것처럼 시작
2. **발견의 재미 보존**: 실제 게임에서 가이드를 직접 발견
3. **반복 튜토리얼 가능**: 조작법을 까먹은 플레이어가 언제든 재시도 가능
4. **일지 진행도 순수성**: 일지는 오직 실제 게임 플레이만 반영

## Testing

1. 튜토리얼 완료 후 새 게임 시작 → 일지가 비어있는지 확인
2. 게임에서 죽은 후 튜토리얼 진입 → "Dieing" 가이드가 표시되지 않는지 확인
3. 튜토리얼에서 메인 메뉴로 복귀 → 게임 일지가 원래대로인지 확인
4. 튜토리얼 중 앱 종료 후 재시작 → 일지가 오염되지 않았는지 확인

---
