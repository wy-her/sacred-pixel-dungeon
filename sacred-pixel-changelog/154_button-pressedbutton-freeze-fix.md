# 154. Button pressedButton 영구 고착 버그 수정

**날짜**: 2026-08-09

## 배경

HTML5 빌드에서 드물게 발생하는 "커맨드 프리즈" 버그가 보고되었다:
- **증상**: 화면 터치/클릭으로 이동은 가능하지만, 모든 UI 버튼(퀵슬롯, 대기, 탐색, 인벤토리, 메뉴 등)이 영구적으로 작동하지 않음
- **발생 시점**: Scorpio(전갈) 공격 직후
- **특징**: 기다려도 자동 복구되지 않음, 게임 재시작 필요

---

## 원인 진단

### 분석 방법
코드 경로 분석 및 교차 검증을 통해 원인 확정

### 핵심 발견

**Button 클래스의 `pressedButton` 정적 변수 관리 결함**

1. `Button.pressedButton`은 **정적 변수**로 모든 버튼 인스턴스가 공유
2. 버튼 DOWN 이벤트 발생 시 `pressedButton = this`로 설정
3. UP 이벤트 발생 시 `pressedButton = null`로 해제
4. **문제**: 버튼이 DOWN 상태에서 파괴되면 (Window 닫힘 등), UP 이벤트 없이 `destroy()` 호출
5. `destroy()`에서 `pressedButton` 정리 코드가 **누락**되어 있음
6. 결과: `pressedButton`이 파괴된 버튼을 영구 참조
7. 이후 모든 버튼 클릭 시 `pressedButton != null` 조건으로 `clickReady` 설정 안 됨
8. **CellSelector는 `pressedButton`을 사용하지 않음** → 이동은 정상 작동

### 버그 발생 경로

```
1. Scorpio 공격 중 퀵슬롯/인벤토리 버튼 클릭 시도
2. Window 열림 + 버튼 DOWN 이벤트 발생 (pressedButton = 해당 버튼)
3. Scorpio 공격 완료 또는 상태 변화로 Window 닫힘
4. Button.destroy() 호출되지만 pressedButton 정리 안 됨
5. pressedButton이 파괴된 버튼을 영구 참조
6. 이후 모든 버튼: clickReady 설정 안 됨 → 클릭 무시
7. CellSelector: pressedButton 미사용 → 이동 정상
```

### 확신도: 95%

---

## 수정 방안 검토

### 검토된 수정안

| Fix | 내용 | 평가 |
|-----|------|------|
| **Fix 1** | `Button.destroy()`에 pressedButton 정리 추가 | **SAFE (채택)** |
| Fix 2 | `PixelScene.destroy()`에 clearPressedButton 호출 | CAUTION (보조) |
| Fix 3 | `Button.update()`에 방어적 체크 추가 | SAFE (선택) |

### Fix 1 채택 이유

1. **근본 원인 직접 해결**: 버튼 파괴 시점에 pressedButton 정리
2. **100% 커버리지**: 모든 버튼 파괴 경로 (Window 닫힘, 씬 전환 등)에서 작동
3. **부작용 없음**: 단순 참조 비교와 null 할당
4. **최소 코드 변경**: 3줄 추가
5. **롤백 용이**: 추가된 코드만 제거하면 됨

---

## 최종 수정안

### 변경 사항

```diff
// Button.java
@Override
public synchronized void destroy () {
+   // Fix: Clear pressedButton if this button is being destroyed while pressed
+   // This prevents permanent button freeze when a button is destroyed mid-click
+   if (pressedButton == this) {
+       pressedButton = null;
+   }
    super.destroy();
    KeyEvent.removeKeyListener( keyListener );
    killTooltip();
}
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../ui/Button.java` | `destroy()` 메서드에 pressedButton 정리 코드 추가 (3줄) |

---

## 영향

- 버튼이 DOWN 상태에서 파괴되어도 다른 버튼 클릭이 정상 작동
- 기존 게임플레이에 영향 없음
- 성능 영향 없음 (참조 비교 1회)

---

## 관련 코드

- `Button.java:307-316` - 수정된 코드 위치
