# 012. GWT → TeaVM HTML5 백엔드 포팅

**날짜**: 2026-03-21

## 개요

HTML5 빌드 시스템을 GWT 2.10.0에서 **TeaVM 0.13.1** (gdx-teavm 1.5.4)로 마이그레이션. GWT는 사실상 유지보수 중단 상태이며, TeaVM은 더 작은 JS 출력(5.2MB vs GWT 10MB+), 빠른 컴파일(~1분 vs ~3분), Java 21+ 지원을 제공한다.

---

## 변경 사항

## 새로 생성된 모듈: `teavm/`

### 디렉토리 구조

```
teavm/
├── build.gradle
├── webapp/
│   └── index.html
└── src/main/java/
    ├── com/sacredpixel/sacredpixeldungeon/teavm/
    │   ├── TeaVMBuilder.java          ← 빌드 진입점 (TeaCompiler)
    │   ├── TeaVMLauncher.java         ← 런타임 진입점 (WebApplication)
    │   ├── TeaVMPlatformSupport.java  ← 플랫폼 지원
    │   └── TeaVMClassRegistry.java    ← 클래스 레지스트리 (1465개)
    ├── com/watabou/utils/
    │   ├── FileUtils.java             ← localStorage (@JSBody)
    │   ├── Reflection.java            ← TeaVMClassRegistry 참조
    │   ├── DateCompat.java            ← JS Date (@JSBody)
    │   ├── Compat.java                ← superclass chain walking
    │   ├── StringCompat.java          ← String.format() 에뮬레이션
    │   ├── ThreadCompat.java          ← 싱글스레드 no-op
    │   ├── DeviceCompat.java          ← isHTML5()=true
    │   └── GZIPCompat.java            ← GZIP 미지원
    ├── com/watabou/input/
    │   └── ControllerHandler.java     ← 컨트롤러 no-op 스텁
    ├── com/badlogic/gdx/graphics/g2d/freetype/
    │   └── FreeTypeFontGenerator.java ← Canvas2D 폰트 렌더링
    └── org/json/
        ├── JSONObject.java            ← LibGDX JsonValue 래퍼
        ├── JSONArray.java
        ├── JSONTokener.java
        └── JSONException.java
```

---

## 핵심 변경사항

### 1. 빌드 시스템 (`teavm/build.gradle`)

| 항목 | GWT (기존) | TeaVM (신규) |
|------|-----------|-------------|
| 의존성 | `gdx-backend-gwt:1.14.0` + GWT SDK 2.10.0 | `gdx-teavm:backend-web:1.5.4` |
| 컴파일 방식 | GWT Compiler (소스코드 → JS) | TeaCompiler (바이트코드 → JS) |
| 소스 JAR | 필수 (`:sources`) | 불필요 |
| 모듈 설정 | `GdxDefinition.gwt.xml` | Java 코드 (`TeaVMBuilder.java`) |
| 출력 파일 | `html.nocache.js` + permutations | `app.js` (단일 파일, 5.2MB) |
| 개발 서버 | GWT SuperDev | 내장 Jetty 또는 python HTTP |

**주요 Gradle 태스크:**
- `buildRelease` — 프로덕션 빌드 (ADVANCED 최적화, 난독화)
- `buildDebug` — 디버그 빌드 (SIMPLE 최적화, 소스맵)
- `runRelease` / `runDebug` — Jetty 서버로 실행

### 2. 런처 (`TeaVMLauncher.java`)

GWT의 `GwtApplication` 상속 → TeaVM의 `WebApplication` 직접 생성:

```java
// GWT 방식:
public class HtmlLauncher extends GwtApplication { ... }

// TeaVM 방식:
public class TeaVMLauncher {
    public static void main(String[] args) {
        WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
        config.width = 0;  // 전체 화면
        config.height = 0;
        new WebApplication(wrappedGame, config);
    }
}
```

에러 복구 래퍼(최대 10회 render() 재시도)를 그대로 유지.

### 3. JSNI → @JSBody 변환 (10개 메서드)

GWT의 JSNI 문법(`/*-{ ... }-*/`)을 TeaVM의 `@JSBody` 어노테이션으로 변환:

```java
// GWT JSNI:
private static native String localStorageGetItem(String key) /*-{
    return $wnd.localStorage.getItem(key);
}-*/;

// TeaVM @JSBody:
@JSBody(params = {"key"}, script = "return window.localStorage.getItem(key);")
private static native String localStorageGetItem(String key);
```

**변환 목록:**

| 파일 | 메서드 수 | 변환 내용 |
|------|----------|----------|
| `FileUtils.java` | 5 | `$wnd.localStorage.*` → `window.localStorage.*` |
| `TeaVMLauncher.java` | 4 | `$wnd.*` → `window.*`, `$doc.*` → `document.*` |
| `DateCompat.java` | 1 | UTC 날짜 포맷 |
| `FreeTypeFontGenerator.java` | 5 | Canvas2D 폰트 렌더링 + 메트릭 측정 |

### 4. 폰트 렌더링 (`FreeTypeFontGenerator.java`)

GWT 버전은 `pixmap.@Pixmap::getContext()()` JSNI 구문으로 Pixmap 내부 캔버스에 직접 접근했으나, TeaVM에서는 Pixmap이 WASM 힙 기반이라 직접 접근이 불가능.

**해결: Canvas → PNG data URL → Pixmap 방식**

1. 오프스크린 Canvas에 글리프 렌더링
2. 알파 이진화 (anti-aliasing 제거)
3. `canvas.toDataURL('image/png')` → base64 PNG 문자열 반환
4. Java에서 base64 디코딩 → `new Pixmap(byte[], 0, len)` 으로 PNG 로드

```java
String dataUrl = jsRenderGlyphsToDataUrl(chars, srcXs, srcYs, ...);
byte[] pngBytes = decodeBase64(dataUrl.substring(dataUrl.indexOf(',') + 1));
Pixmap pixmap = new Pixmap(pngBytes, 0, pngBytes.length);
```

커스텀 base64 디코더를 포함하여 TeaVM classlib 호환성 확보.

### 5. 클래스 레지스트리 (`TeaVMClassRegistry.java`)

`GwtClassRegistry` → `TeaVMClassRegistry`로 리네임 (패키지 변경).
1,465개 클래스 등록은 동일하게 유지. TeaVM도 런타임 리플렉션(`Class.forName()`)을 지원하지 않으므로 수동 등록 방식 필요.

`Reflection.java`의 참조도 `TeaVMClassRegistry`로 업데이트.

### 6. Super-source → 일반 소스 전환

GWT의 super-source(패키지 경로 리매핑) 메커니즘은 TeaVM에 없음.
대신 `teavm/src/main/java/`의 동일 패키지 클래스가 SPD-classes JAR보다 클래스패스에서 우선.

| 파일 | 변환 유형 |
|------|----------|
| `java/lang/Thread.java` | **삭제** (TeaVM 자체 구현) |
| `FileUtils.java` | JSNI 5개 → @JSBody |
| `DateCompat.java` | JSNI 1개 → @JSBody |
| `FreeTypeFontGenerator.java` | 전면 재작성 (PNG data URL 방식) |
| `Reflection.java` | TeaVMClassRegistry 참조 변경 |
| 나머지 9개 | 코드 변경 없이 이동 |

### 7. 에셋 프리로딩 (`preload.txt`)

gdx-teavm은 에셋 매니페스트 파일 `preload.txt`를 필요로 함.
포맷: `fileType:assetType:path:size:preloadFlag` (콜론 구분 5필드)

```
i:b:sprites/warrior.png:12345:1    ← Internal, Binary, 경로, 크기, preload
i:d:sprites:0:1                     ← Internal, Directory, 경로, 0, preload
```

빌드 후 Python 스크립트로 자동 생성 (493개 항목).

### 8. 웹 리소스 (`webapp/index.html`)

gdx-teavm이 생성하는 기본 HTML을 커스텀 버전으로 교체:
- NeoDunggeunmoPro 폰트 `@font-face` 선언 + FontFace API 프리로드
- `100dvh` 모바일 동적 뷰포트
- WebGL context loss/restore 핸들러
- 글로벌 에러 핸들러 (빨간색 에러 메시지 표시)
- `app.js` 로드 후 `main()` 호출

---

## 프로젝트 설정 변경

### `settings.gradle`

```diff
+include ':teavm'
```

### `build.gradle` (루트)

```diff
 repositories {
     maven { url 'https://central.sonatype.com/repository/maven-snapshots/' }
     maven { url 'https://jitpack.io' }
+    maven {
+        url 'https://teavm.org/maven/repository/'
+        allowInsecureProtocol = true
+    }
 }
```

### `core/src/main/assets/`

모든 `desktop.ini` 파일 제거 (Windows 시스템 파일이 gdx-teavm 에셋 복사 시 오류 유발).

---

## 의존성 버전 매트릭스

| 컴포넌트 | GWT (기존 html/) | TeaVM (신규 teavm/) |
|---------|-----------------|-------------------|
| LibGDX | 1.14.0 | 1.14.0 (동일) |
| Web 백엔드 | gdx-backend-gwt 1.14.0 | gdx-teavm backend-web 1.5.4 |
| 컴파일러 | GWT SDK 2.10.0 | TeaVM 0.13.1 (transitive) |
| JS 출력 크기 | ~10MB+ | **5.2MB** |
| 컴파일 시간 | ~3분 | **~1분** |
| 소스 JAR 필요 | 예 | **아니오** |

---

## 빌드 및 실행 방법

```bash
# 프로덕션 빌드
./gradlew :teavm:buildRelease

# 빌드 후 에셋 매니페스트 생성 (빌드 출력 디렉토리에서)
cd teavm/build/dist/webapp/assets
python3 generate_preload.py  # 또는 수동으로 preload.txt 생성

# 로컬 테스트 (PC)
cd teavm/build/dist/webapp
python -m http.server 8080

# 모바일 테스트 (같은 WiFi)
python -m http.server 8080 --bind 0.0.0.0
# 모바일에서 http://<PC_IP>:8080 접속
```

---

## 기존 html/ 모듈 상태

`html/` 모듈은 그대로 유지. GWT 빌드가 여전히 작동하며, TeaVM 포팅이 완전히 안정화된 후 제거 가능.
두 모듈은 독립적으로 빌드 가능 (`gradlew :html:compileGwt` / `gradlew :teavm:buildRelease`).

---

## 알려진 제한사항

1. **preload.txt 수동 생성** — 빌드 후 에셋 매니페스트를 별도로 생성해야 함. 자동화 스크립트 추가 예정.
2. **폰트 렌더링 성능** — PNG data URL 방식은 GWT의 직접 캔버스 접근보다 약간 느림. 첫 로딩 시 글리프 아틀라스 재빌드에 추가 시간 소요.
3. **html/ 모듈 병존** — 현재 두 모듈이 공존. 안정화 후 html/ 제거 계획.
