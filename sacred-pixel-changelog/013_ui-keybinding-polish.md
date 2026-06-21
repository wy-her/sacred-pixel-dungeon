# 013. UI 폴리시, 조작키 설정 개편, 키바인딩 변경, 버그 수정

**날짜**: 2026-03-24

## 개요

설정 UI(슬라이더, 조작키 설정), 타이틀 화면, 키 바인딩 배치, 아시아 언어 텍스트 간격, 브라우저 언어 감지, 번역 오류, 임프 퀘스트 버그 등 다수의 UI/UX 개선 및 버그 수정 적용.

---

## 변경 사항

### 1. OptionSlider (설정 바) 레이아웃 개선

### 1-1. 제목 텍스트 1줄 강제 + 폰트 축소
**파일:** `core/.../ui/OptionSlider.java`

제목이 슬라이더 너비를 초과하여 2줄로 넘어가는 경우, 폰트 크기를 6→5→4로 단계적 축소하여 1줄에 맞춤. `RenderedTextBlock`은 `size` 변경 메서드가 없으므로 새 객체로 교체하는 방식.

### 1-2. min/max 텍스트 위치 변경
**파일:** `core/.../ui/OptionSlider.java`

min/max 값 텍스트를 제목 바로 아래(기존 2째줄 위치)에 고정 배치. 슬라이더 바/눈금은 컴포넌트 하단에 고정(`y + height() - 7`).

### 1-3. SLIDER_HEIGHT 28px
**파일:** `core/.../windows/WndSettings.java`

슬라이더 높이를 기존 21px에서 28px로 증가하여 제목(1줄) + min/max + 슬라이더가 겹치지 않도록 공간 확보.

---

## 2. 타이틀 화면 버튼 너비 확장

### 2-1. Fireball 기준 버튼 너비
**파일:** `core/.../scenes/TitleScene.java`

버튼 영역 너비를 기존 `buttonAreaWidth`와 fireball 좌/우 끝 너비(`fireballTotalWidth`)의 **중간값**으로 설정. 로고 양쪽 불꽃 장식과 버튼이 시각적으로 조화.

---

## 3. 조작키 설정 (WndKeyBindings) 전면 개편

### 3-1. 창 너비 및 열 비율
**파일:** `core/.../windows/WndKeyBindings.java`

- WIDTH: 240 → **168** (30% 축소)
- 행동 열: **46%**, 키 1/2/3열: 각 **18%** (나머지 54%를 3등분)
- `colSepX()` 정적 메서드로 헤더와 행의 구분선 위치를 동일한 정수 픽셀로 통일

### 3-2. 폰트 크기 통일 (size 5)
**파일:** `core/.../windows/WndKeyBindings.java`

헤더(행동, 1번째 키, 2번째 키, 3번째 키)와 BindingItem(행동명, 키 이름) 모두 **size 5**로 통일. 기존의 zoom 기반 축소 메커니즘은 예상과 다른 동작(확대)을 유발하여 완전 제거.

### 3-3. 구분선 정렬 통일
**파일:** `core/.../windows/WndKeyBindings.java`

헤더와 BindingItem 모두 `colSepX(n)` 메서드를 공유하여 정확히 동일한 x 좌표에 구분선 배치. 이전에는 헤더가 `(int)(WIDTH*frac)`, 행이 `width*frac`(float)로 0.5px 차이 발생.

### 3-4. 세로 정렬 보정
**파일:** `core/.../windows/WndKeyBindings.java`

- 1줄 텍스트: `(height() - textHeight) / 2f`로 완벽한 세로 중앙 정렬
- 2줄 텍스트: RenderedTextBlock의 `bottomPad` (2줄일 때 size*0.5 = 2.5px)를 보정하여 시각적 중앙 정렬. `actionPadCompensation = bottomPad / 2` 적용

### 3-5. 버튼 오른쪽 끝 정렬
**파일:** `core/.../windows/WndKeyBindings.java`

WIDTH=홀수에서 `WIDTH/2` 정수 나눗셈 절삭으로 "기본 조작키" 버튼과 "취소" 버튼의 오른쪽 끝이 1px 어긋남. `halfR = WIDTH - halfL`로 나머지를 보정.

### 3-6. onClick 열 판정 통일
**파일:** `core/.../windows/WndKeyBindings.java`

키 클릭 영역 판정도 `colSepX()`를 사용하여 헤더/구분선과 정확히 일치.

---

## 4. 키 바인딩 대폭 변경

### 4-1. 기본 조작키 변경
**파일:** `core/.../SPDAction.java`

| 행동 | 이전 키 | 변경 후 키 |
|------|---------|-----------|
| 뒤로 | ESC, Backspc | ESC, **Numpad .**, Backspc |
| 인벤토리 | B | **7** |
| 1번째 가방 | N | **8** |
| 2번째 가방 | M | **9** |
| 3번째 가방 | , | **0** |
| 4번째 가방 | . | **-** |
| 5번째 가방 | / | **=** |
| 배낭 선택 | [ | **N** |
| 퀵슬롯 선택 | ] | **M** |
| 화면 확대 | +, = | **]** |
| 화면 축소 | - | **[** |
| 아이템 집기 | Y → B | **V** |
| 행동 계속하기 | Space | **B** |
| 다음 타겟/탭 | V | **Space**, **Numpad 0** |
| 1턴 대기 | (없음) | **T** |
| 휴식 | T | **Y** |
| 카메라 북 | Num9 | **O** |
| 카메라 서 | I | **K** |
| 카메라 남 | L | **.** |
| 카메라 동 | P | **;** |
| 카메라 북서 | Num8 | **I** |
| 카메라 북동 | Num0 | **P** |
| 카메라 남서 | K | **,** |
| 카메라 남동 | ; | **/** |
| 카메라 영웅 | O | **L** |

### 4-2. Numpad Enter 지원
**파일:** `core/.../SPDAction.java`, `core/.../scenes/PixelScene.java`

- `NUMPAD_ENTER`를 하드 바인딩으로 등록 (`SPDAction.NONE`)
- `PixelScene`의 Alt+Enter 풀스크린 토글에서 `NUMPAD_ENTER`도 동일하게 처리
- (기존에 Window.java, CellSelector.java, WndBag.java에서는 이미 처리됨)

### 4-3. "Numpad ." 키 이름
**파일:** `SPD-classes/.../input/KeyBindings.java`

`NUMPAD_DOT` 키의 표시 이름을 "Num ." → **"Numpad ."**으로 변경.

---

## 5. 조작키 항목 순서 변경

### 5-1. static 선언 순서 변경으로 UI 표시 순서 제어
**파일:** `core/.../SPDAction.java`

`GameAction.allActions()`가 static 초기화 순서를 따르므로, 선언 순서를 변경하여 UI 표시 순서 제어.

**변경된 순서:**
1. 이동 (N/W/S/E/NW/NE/SW/SE)
2. 1턴 대기 / 아이템 획득
3. **퀵슬롯 1~6** (기존 하단에서 이동)
4. 인벤토리
5. 가방 1~5
6. 배낭 선택 / 퀵슬롯 선택
7. 조사, 탐색
8. **1턴 대기** (새로 활성화)
9. 휴식
10. 적 공격 / 행동 / **아이템 집기** / **행동 계속하기** / **다음 타겟**
11. 영웅 정보 / 일지
12. 줌 / 카메라

### 5-2. WAIT 행동 활성화
**파일:** `core/.../windows/WndKeyBindings.java`, `core/.../SPDAction.java`

- `WAIT` 행동을 조작키 설정에서 숨김 해제 (기존에는 FOCUS와 함께 숨겨짐)
- "조사, 탐색"과 "휴식" 사이에 배치
- 한국어 번역: "1턴 대기"

---

## 6. 번역 수정

### 6-1. 한국어
**파일:** `core/.../messages/windows/windows_ko.properties`

- `wait`: "대기" → **"1턴 대기"**
- `wait_or_pickup`: "대기 / 아이템 획득" → **"1턴 대기 / 아이템 획득"**

### 6-2. 독일어
**파일:** `core/.../messages/windows/windows_de.properties`

- `bag_5`: "Behälter **6**" → "Behälter **5**" (번호 오타 수정)

### 6-3. 일본어
**파일:** `core/.../messages/windows/windows_ja.properties`

- `se` (남동쪽 이동): "**北東**に行く" (북동=NE, 오류) → "**南東**に行く" (남동=SE, 정상)

### 6-4. 포르투갈어
**파일:** `core/.../messages/windows/windows_pt.properties`

- `quickslot_5`: "slot rápido 5" → "**Slot** rápido 5" (대소문자 통일)
- `quickslot_6`: "slot rápido  6" → "**Slot** rápido 6" (이중 공백 + 대소문자 수정)

---

## 7. 브라우저 언어 자동 감지

### 7-1. navigator.language 기반 Locale 설정
**파일:** `teavm/.../TeaVMLauncher.java`

게임 시작 시 `navigator.language` (JavaScript)를 읽어 `java.util.Locale.setDefault()`로 설정. 이후 `SPDSettings.language()`에서 `Locale.getDefault()`를 참조할 때 브라우저 언어가 반영됨.

이전에는 TeaVM 컴파일 시 JVM 기본 로캘(en_US)이 고정되어, 첫 실행 시 항상 영어로 시작됨.

---

## 8. 아시아 언어 텍스트 간격 최적화

### 8-1. CJK 글자 간 border padding 보정
**파일:** `core/.../ui/RenderedTextBlock.java`

**문제:** 중국어/일본어에서 각 글자가 개별 `RenderedText` 객체로 분리되어 border padding이 글자 간격을 넓힘.

**수정:** word가 길이 1이고 CJK 코드포인트(Hiragana/Katakana/CJK Unified/CJK Symbols)인 경우, `charGap + borderExcess * 0.7f`를 차감하여 시각적 간격 축소.

### 8-2. 한글(Hangul Syllables) 제외
**파일:** `core/.../ui/RenderedTextBlock.java`

**문제:** 한글 1글자 단어("적", "옆" 등)에도 CJK 보정이 적용되어 다음 공백이 압축됨 → "적 공격"이 "적공격"처럼 표시.

**수정:** Hangul Syllables 범위(`\uAC00-\uD7AF`)를 CJK border 보정 대상에서 제외. 한국어는 단어 단위 분리이므로 글자별 보정이 불필요.

### 8-3. 선택 언어와 무관한 글자 기반 판정
**파일:** `core/.../ui/RenderedTextBlock.java`

**문제:** `Messages.lang().isAsian()`은 선택된 언어 기준 → 다른 언어 선택 시 아시아 텍스트 버튼에 보정 미적용.

**수정:** 선택 언어와 무관하게 **실제 글자의 유니코드 범위**로 CJK 보정 여부를 판단. 어떤 언어가 선택되어 있든 CJK 글자에 동일한 보정 적용.

---

## 9. 오디오 에러 메시지 억제

### 9-1. howler.js bufferSource 에러 필터링
**파일:** `teavm/webapp/index.html`

**문제:** howler.js에서 `AudioContext`가 초기화되지 않은 상태에서 `bufferSource` 설정 시 에러 발생 → `window.onerror`가 화면에 빨간 텍스트로 표시.

**수정:** `window.onerror`에서 `bufferSource`, `audiocontext`, `decodeaudiodata` 관련 에러를 필터링하여 콘솔 경고로만 남기고 화면에 표시하지 않음.

---

## 10. 임프 퀘스트 반지 레벨 버그 수정

### 10-1. 보상 반지 레벨 초기화
**파일:** `core/.../windows/WndImpReward.java`

**문제:** `Generator.random(RING)`이 `Ring.random()`을 통해 이미 +0/+1/+2 중 하나를 부여한 상태에서, `ring.upgrade(선택레벨)`이 **추가**로 더함. +2 선택 시 최대 +4, +1 선택 시 최대 +3 발생 가능.

**수정:** `ring.level(0)` 호출로 레벨을 0으로 리셋한 후 `ring.upgrade(upgradeLevel)` 실행. 선택한 레벨이 정확히 반영됨.

---

---

## 수정된 파일

| 파일 | 변경 내용 |
|------|----------|
| `core/.../ui/OptionSlider.java` | 제목 1줄 강제, min/max 위치 변경 |
| `core/.../windows/WndSettings.java` | SLIDER_HEIGHT 28px |
| `core/.../scenes/TitleScene.java` | 버튼 너비 fireball 중간값 |
| `core/.../windows/WndKeyBindings.java` | WIDTH 168, 열비율 46/18/18/18, 폰트 size 5, 구분선 통일, 세로 정렬 보정, 버튼 정렬 |
| `core/.../SPDAction.java` | 키바인딩 전면 변경, WAIT 활성화, 항목 순서 변경, NUMPAD_ENTER 하드바인딩 |
| `core/.../scenes/PixelScene.java` | NUMPAD_ENTER 풀스크린 토글 |
| `SPD-classes/.../input/KeyBindings.java` | NUMPAD_DOT 이름 "Numpad ." |
| `core/.../ui/RenderedTextBlock.java` | CJK 글자 간격 보정 (한글 제외), 선택 언어 무관 판정 |
| `core/.../windows/WndImpReward.java` | ring.level(0) 리셋 |
| `teavm/.../TeaVMLauncher.java` | getBrowserLanguage(), Locale 설정 |
| `teavm/webapp/index.html` | 오디오 에러 필터링 |
| `messages/windows/windows_ko.properties` | 1턴 대기 번역 |
| `messages/windows/windows_de.properties` | bag_5 번호 수정 |
| `messages/windows/windows_ja.properties` | SE 방향 수정 |
| `messages/windows/windows_pt.properties` | quickslot 대소문자/공백 수정 |
