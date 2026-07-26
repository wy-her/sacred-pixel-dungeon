# 146. TeaVM 세이브 로드 레이스 컨디션 버그 수정

**날짜**: 2026-07-26

## 배경

TeaVM/HTML5 빌드에서 게임 로드 시 처음 1-2회는 실패하고 3회째에 성공하는 버그가 발생했습니다. 또한 화면 모드를 2회 변경하면 로드가 성공하는 현상도 있었습니다.

---

## 문제 분석

### 증상

1. 타이틀 화면에서 "Continue" 클릭
2. 로딩 화면 → 에러 메시지 표시 (1회차)
3. 다시 시도 → 에러 메시지 표시 (2회차)
4. 다시 시도 → 정상 로드 (3회차)

또는:
1. 화면 모드(Fullscreen) 변경 2회
2. 이후 로드 시도 → 정상 작동

### 원인

두 가지 문제가 복합적으로 작용:

#### 1. Static Thread 참조 유지 (TeaVM 특성)

`InterlevelScene`의 `static Thread thread` 변수가 씬 전환 후에도 이전 값을 유지합니다. 네이티브 JVM에서는 GC가 처리하지만, TeaVM의 JavaScript 런타임에서는 static 변수가 명시적으로 초기화되지 않으면 계속 유지됩니다.

```java
// 문제: thread가 null이 아니면 새 스레드가 생성되지 않음
if (thread != null && thread.isAlive()) {
    return; // 스킵됨
}
```

#### 2. Document 초기화 순서 문제

`Document` enum의 static 초기화 블록이 `Game.version` 설정 전에 실행되어 `DeviceCompat.isDebug()`에서 NPE 발생 가능:

```java
// isDebug()가 Game.version이 null일 때 호출되면 NPE
public static boolean isDebug(){
    return Game.version.toUpperCase().contains("INDEV"); // NPE!
}
```

---

## 변경 사항

### 1. InterlevelScene.create() - Static 변수 초기화

씬 생성 시 명시적으로 static 변수를 초기화:

```diff
  @Override
  public void create() {
      super.create();

+     // Reset static thread state to prevent stale references in TeaVM/web build
+     // (Previous thread reference may persist across scene transitions)
+     thread = null;
+     error = null;

      // Detect if an ad was recently shown...
```

### 2. DeviceCompat.isDebug() - Null 안전성 추가

`Game.version`이 null일 때 안전하게 처리:

```diff
  public static boolean isDebug(){
-     return Game.version.toUpperCase().contains("INDEV");
+     return Game.version != null && Game.version.toUpperCase().contains("INDEV");
  }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `scenes/InterlevelScene.java:191-194` | `create()`에서 `thread = null; error = null;` 추가 |
| `SPD-classes/.../DeviceCompat.java:67` | `isDebug()`에 null 체크 추가 |

---

## 플랫폼별 영향

| 플랫폼 | 변경 전 | 변경 후 |
|--------|---------|---------|
| TeaVM/HTML5 | 로드 1-2회 실패 | 첫 시도에 성공 |
| Desktop | 정상 (GC가 처리) | 변화 없음 |
| Android | 정상 | 변화 없음 |

---

## 영향

- TeaVM 빌드에서 첫 로드 시도부터 정상 작동
- Document enum 초기화 시 NPE 방지
- 네이티브 빌드에는 영향 없음 (방어적 코딩)

---

## 관련 코드

- `InterlevelScene.thread` - 레벨 전환 스레드 참조
- `InterlevelScene.error` - 에러 메시지 저장
- `ThreadCompat` - TeaVM 스레드 호환 레이어
- `Document` enum - 저널 문서 정의 (static 초기화)

