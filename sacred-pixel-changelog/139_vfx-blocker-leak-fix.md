# 139. VFX Blocker 누수 및 HTML5 영구 정지 버그 수정

**날짜**: 2026-07-25

## 배경

VFX 애니메이션(MagicMissile, Chains 등)의 callback에서 예외가 발생하면 `vfxBlockers` 카운터가 감소하지 않아 게임 턴 진행이 중단되는 버그.

- **Desktop**: 3초 후 강제 해제로 복구 가능
- **HTML5**: timeout이 없어 **영구 정지**

---

## 원인 분석

### 1. callback 예외 시 blocker 누수

`MagicMissile.java`와 `Chains.java`에서 callback 호출 후 `removeVfxBlocker()`를 호출하는데, callback이 예외를 던지면 해당 라인이 실행되지 않음.

```java
// MagicMissile.java (문제 코드)
if (callback != null) callback.call();  // ← 예외 발생 시
if (wasBlocking) Actor.removeVfxBlocker();  // ← 실행 안 됨!
```

### 2. HTML5에 timeout 없음

`Actor.process()`에서:
- Desktop: `System.currentTimeMillis()`로 3초 timeout 구현
- HTML5: `current = null; break;`만 하고 timeout 없음

---

## 변경 사항

### 1. MagicMissile.java - try-catch 추가

```diff
 if ((time -= d) <= 0) {
     on = false;
     boolean wasBlocking = blockingVfx;
     blockingVfx = false;
-    if (callback != null ) callback.call();
+    if (callback != null) {
+        try {
+            callback.call();
+        } catch (Exception e) {
+            Game.reportException(e);
+        }
+    }
     if (wasBlocking) Actor.removeVfxBlocker();
 }
```

### 2. Chains.java - try-catch 추가

```diff
 boolean wasBlocking = blockingVfx;
 blockingVfx = false;
 killAndErase();
 if (callback != null) {
-    callback.call();
+    try {
+        callback.call();
+    } catch (Exception e) {
+        Game.reportException(e);
+    }
 }
 if (wasBlocking) Actor.removeVfxBlocker();
```

### 3. Actor.java - HTML5 timeout 추가

```diff
 // 새로운 변수 추가
+private static int vfxStallFrames = 0;
+private static final int MAX_VFX_STALL_FRAMES = 180; // ~3 seconds at 60fps

 // process() 내부
 if (vfxBlockers > 0) {
     if (ThreadCompat.currentThread() == null) {
+        // HTML5: safety timeout
+        vfxStallFrames++;
+        if (vfxStallFrames > MAX_VFX_STALL_FRAMES) {
+            vfxBlockers = 0;
+            vfxStallFrames = 0;
+        }
         current = null;
         break;
     } else {
         // Desktop: 기존 3초 timeout 유지
     }
+} else {
+    vfxStallFrames = 0;  // 정상 시 리셋
 }
```

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `effects/MagicMissile.java` | callback에 try-catch 추가 |
| `effects/Chains.java` | callback에 try-catch 추가 |
| `actors/Actor.java` | HTML5 vfxBlockers timeout 추가 (~3초) |
| 〃 | `vfxStallFrames` 변수 및 상수 추가 |
| 〃 | `clear()`에서 `vfxStallFrames` 리셋 |

---

## 플랫폼별 동작

| 시나리오 | Desktop | HTML5 (수정 전) | HTML5 (수정 후) |
|----------|---------|----------------|----------------|
| callback 예외 | 3초 후 복구 | **영구 정지** | 즉시 복구 |
| blocker 누수 | 3초 후 복구 | **영구 정지** | 3초 후 복구 |

---

## 영향

- HTML5에서 VFX 관련 영구 정지 버그 해결
- callback 예외 발생 시에도 게임 진행 가능
- 예외는 `Game.reportException()`으로 로깅되어 디버깅 가능
- `Pushing.java`, `MissileSprite.java`와 일관된 패턴 적용

---

## 관련 문서

- `Actor.java:181-183` - vfxBlockers 변수 정의
- `Actor.java:314-340` - process() 내 VFX 블로킹 처리
- `Pushing.java:170-175` - 기존 try-catch 패턴 참고
- `MissileSprite.java:205-210` - 기존 try-catch 패턴 참고
