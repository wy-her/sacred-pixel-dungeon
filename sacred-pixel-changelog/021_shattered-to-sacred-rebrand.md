# 021. Shattered → Sacred 리브랜딩

**날짜**: 2026-03-25

## 개요

프로젝트 전체에서 "Shattered"를 "Sacred"로 리브랜딩. GPLv3 라이선스에 의한 원작 크레딧/어트리뷰션은 모두 유지.

---

## 변경 사항

### 1. 저작권 헤더 수정 (~1,299개 Java 파일)

기존 (잘못됨):
```java
 * Shattered Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
```

수정 후:
```java
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Sacred Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
```

- Evan Debenham 원저작권 원복 (2014-2026)
- Sacred Pixel Dungeon 크레딧 추가

### 2. 패키지명 변경

- `com.shatteredpixel.shatteredpixeldungeon` → `com.sacredpixel.sacredpixeldungeon`
- 7개 모듈의 디렉토리 구조 이동 (core, teavm, services, debugNews, sacredNews, debugUpdates, githubUpdates)
- 모든 Java 파일의 `package` 선언 (~1,200개) 및 `import` 문 (~12,400개) 일괄 치환

### 3. 모듈/파일명 변경

| 변경 전 | 변경 후 |
|---------|---------|
| `services/news/shatteredNews/` | `services/news/sacredNews/` |
| `ShatteredPixelDungeon.java` | `SacredPixelDungeon.java` |
| `ShatteredNews.java` | `SacredNews.java` |
| `ShatteredPD.gwt.xml` | `SacredPD.gwt.xml` |

### 4. 클래스명/상수명 변경

| 변경 전 | 변경 후 |
|---------|---------|
| `ShatteredPixelDungeon` | `SacredPixelDungeon` |
| `ShatteredNews` | `SacredNews` |
| `SHATTEREDPD_BIRTHDAY` | `SACREDPD_BIRTHDAY` |
| `SHATTERED_CAKE` | `SACRED_CAKE` |

### 5. 빌드 설정

- `settings.gradle`: `include ':services:news:shatteredNews'` → `include ':services:news:sacredNews'`
- `teavm/build.gradle`: `mainClassName` 패키지 경로 업데이트
- `gradle.properties`: 코멘트 내 "Shattered" → "Sacred"

### 6. Holiday.java — Sacred's Birthday

- 월 변경: `DateCompat.AUGUST` → `DateCompat.APRIL` (4월 1~7일)
- 코멘트: `//Shattered's Birthday` → `//Sacred's Birthday`

### 7. 로컬라이제이션 (23개 언어)

변경된 메시지 키:
- `windows.wndsupportprompt.intro` — 게임 이름 브랜딩
- `windows.wndvictorycongrats.thank_you` — 승리 감사 메시지
- `windows.wndranking$statstab.copy_seed_desc` — 시드 복사 설명
- `scenes.welcomescene.save_warning` — 저장 경고 메시지
- `scenes.welcomescene.patch_intro` / `update_intro` / `what_msg` — 업데이트 안내
- `scenes.titlescene$changesbutton.desc` — 변경사항 버튼 설명
- `ui.updatenotification$wndupdate.desc` — 업데이트 알림

### 8. AboutScene.java — 자체 브랜딩 텍스트

- `"ShatteredPD is powered by _libGDX_!"` → `"SacredPD is powered by _libGDX_!"`
- `"ShatteredPD is community-translated..."` → `"SacredPD is community-translated..."`

### 9. 코멘트 업데이트

- `DungeonSeed.java`: "ShatteredPD" → "SacredPD"
- `DeviceCompat.java`: "Shattered" → "Sacred"
- `PlatformSupport.java`: "Shattered's" → "Sacred's"

### 10. 문서 파일 업데이트

- `CLAUDE.md`: "Shattered Pixel Dungeon" → "Sacred Pixel Dungeon"
- `docs/recommended-changes.md`: 패키지 경로 및 브랜딩 업데이트
- `Changelog/*.md`: 코드 참조 및 브랜딩 텍스트 업데이트

### 11. Favicon 추가

- `teavm/webapp/index.html`에 `<link rel="icon" href="assets/interfaces/sacred-logo.png">` 추가

---

## 유지된 항목 (변경 금지)

### GPLv3 필수 어트리뷰션
- 저작권 헤더 내 "Shattered Pixel Dungeon" + "Evan Debenham" 라인
- `AboutScene.java` 원작 크레딧 (Evan Debenham, Watabou, 기여자 전체)
- `SupporterScene.java` Evan의 Patreon 링크, UTM 파라미터, "- Evan" 서명

### 역사적 기록
- 체인지로그 (v0_1_X ~ v3_X_Changes.java) 내 모든 "Shattered" 언급
- `levels.citylevel.upcoming_quest_intro_body` (Evan 서명 포함 인게임 메시지)
- `supporterscene.patreon_msg` (Evan의 후원 메시지, 22개 언어)

### 게임 메카닉
- `ShatteredPot` 클래스명 및 관련 로직 (깨진 꿀단지)
- `AC_SHATTER` 상수 (깨뜨리기 액션)
- `items.honeypot$shatteredpot.*` 메시지 키
- `"When shattered, this brew..."` 포션 설명
- `items.food.pasty.shattered_name/desc` 메시지 키

### 내부 식별자
- `SHPX_COLOR`, `Icons.SHPX` 상수/열거값
- 번역자 닉네임 `"ShatteredFlameBlast"`

### 외부 URL
- `shatteredpixel.com` 관련 URL (뉴스피드, GitHub API, Transifex, Patreon)

---

## 수정된 파일

| File | Changes |
|------|---------|
| `*.java` (~1,299개) | 저작권 헤더 수정 |
| `com.shatteredpixel` → `com.sacredpixel` | 패키지명 변경 (7개 모듈) |
| `ShatteredPixelDungeon.java` | `SacredPixelDungeon.java`로 이름 변경 |
| `settings.gradle`, `build.gradle` | 빌드 설정 업데이트 |
| `Holiday.java` | Birthday 날짜 변경 |
| `messages/*.properties` (23개 언어) | 브랜딩 텍스트 변경 |
| `AboutScene.java` | 자체 브랜딩 텍스트 변경 |
| `CLAUDE.md`, `docs/*.md` | 문서 업데이트 |
| `teavm/webapp/index.html` | Favicon 추가 |
