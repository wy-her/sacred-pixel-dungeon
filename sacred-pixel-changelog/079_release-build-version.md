# 079. 릴리즈 빌드 버전명 변경

**날짜**: 2026-05-31

## 개요

릴리즈 빌드를 위해 버전명에서 INDEV 접미사를 제거.

---

## 변경 사항

### 1. 버전명 변경

**파일:** `teavm/src/main/java/.../TeaVMLauncher.java`

```java
// 변경 전 (디버그 버전)
Game.version = "3.3.8-HTML5-INDEV";

// 변경 후 (릴리즈 버전)
Game.version = "3.3.8-HTML5";
```

### 2. 영향

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| `isDebug()` | `true` | `false` |
| 개발자 로그 | 출력 | 비출력 |
| 디버그 기능 | 활성화 | 비활성화 |

---

## 수정된 파일

| File | Changes |
|------|---------|
| `teavm/.../TeaVMLauncher.java` | 버전명 설정 변경 |
| `DeviceCompat.java` | `isDebug()` 메서드 (INDEV 포함 여부로 판단) |

---

## 빌드 참고

릴리즈 빌드 시:
- Cloudflare: `buildRelease`
- Capacitor (Android): `buildRelease`
- 로컬호스트 디버그: 버전명에 INDEV 다시 추가 필요

---
