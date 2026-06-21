# 051. 데이터 동기화 및 설정 화면

**날짜**: 2026-04-20

## 개요

Data sync 기능 버그 수정, SettingsScene 신규 생성, Import/Export UI 개선. JSFunctor 호출 방식 수정, hash fragment asset loader 호환성 개선.

---

## 변경 사항

### Bug Fixes

### JSFunctor 호출 방식 수정 (TeaVMLauncher.java)
- **문제**: `var$1.onSave is not a function` 에러 발생
- **원인**: TeaVM의 JSFunctor는 객체 메서드가 아닌 함수 자체로 호출해야 함
- **수정**:
  - `callback.onSave()` → `callback()`
  - `pauseCallback.call()` → `pauseCallback()`

### pako.js 로컬 파일로 변경 (index.html)
- **문제**: CDN에서 pako.js 로드 실패 시 압축 기능 불가
- **수정**: `webapp/scripts/pako.min.js` 로컬 파일로 변경

### Hash Fragment Asset Loader 호환성 (index.html, WebUrlCodec.java)
- **문제**: URL에 hash fragment(`#CODE`)가 있으면 gdx-teavm asset loader가 깨짐
- **원인**: asset URL 계산 시 hash가 포함되어 404 발생
- **수정**:
  - 페이지 로드 시 hash를 sessionStorage에 저장 후 URL에서 제거
  - 게임 로드 완료 후 sessionStorage에서 hash 읽어서 import 처리
  - hashchange 이벤트 감지하여 게임 실행 중 import URL 붙여넣기 지원

### Settings 버튼 작동 안함 (TitleScene.java)
- **문제**: 타이틀 화면 설정 버튼 클릭해도 반응 없음
- **수정**: `super.onClick()` 호출 추가

### SettingsScene 뒤로가기/ESC 문제 (WndSettings.java)
- **문제**: SettingsScene에서 뒤로가기/ESC 누르면 TitleScene 대신 SettingsScene 재시작
- **원인**: WndSettings.hide()가 seamlessResetScene() 호출
- **수정**: SettingsScene일 때 seamlessResetScene() 스킵

### Import된 랭킹 상세 데이터 표시 오류 (Rankings.java, WebDataImporter.java)
- **문제**: Import된 랭킹 레코드 클릭 시 상세 데이터 표시 안됨
- **원인**:
  - hasScoreBreakdown()이 점수가 0이면 false 반환
  - customSeed 미초기화로 NPE 발생 가능
- **수정**:
  - scoreBreakdownKnown 플래그 추가
  - WebDataImporter에서 customSeed, daily, version, gameData 초기화

---

### New Features

### SettingsScene 신규 생성
- 타이틀 화면 설정 버튼 클릭 시 WndSettings 대신 SettingsScene으로 이동
- JournalScene과 동일한 배경 (ColorBlock, 검정색) + ExitButton + WndSettings

### Import 후 자동 새로고침
- Import(병합/덮어쓰기) 완료 시 페이지 자동 새로고침
- 게임 데이터(Badges, Rankings 등) 즉시 반영

---

### UI Improvements

### Import 다이얼로그 버튼 스타일 통일
- 병합, 덮어쓰기, 취소 버튼 모두 `Chrome.Type.RED_BUTTON` 스타일로 변경

### Export URL 표시 길이 조절
- 가로모드: 최대 50자 (앞 25자...뒤 17자)
- 세로모드: 최대 30자 (앞 15자...뒤 10자)
- 패널 너비 초과 방지

---

---

## 수정된 파일

| File | Changes |
|------|---------|
| `core/.../scenes/SettingsScene.java` | NEW - ColorBlock 배경 설정 화면 |
| `teavm/webapp/scripts/pako.min.js` | NEW - 로컬 압축 라이브러리 |
| `teavm/.../TeaVMLauncher.java` | JSFunctor 호출 수정 |
| `teavm/webapp/index.html` | hash handling, pako 로컬 파일 |
| `teavm/.../web/WebUrlCodec.java` | sessionStorage hash, reloadPage() |
| `teavm/.../web/WebDataServiceImpl.java` | reloadPage() 구현 |
| `teavm/.../web/WebDataImporter.java` | customSeed, daily 초기화 |
| `core/.../scenes/DataScene.java` | 버튼 스타일, URL 길이 |
| `core/.../scenes/TitleScene.java` | SettingsScene 이동 |
| `core/.../windows/WndSettings.java` | seamlessResetScene 스킵 |
| `core/.../Rankings.java` | scoreBreakdownKnown 플래그 |

---
