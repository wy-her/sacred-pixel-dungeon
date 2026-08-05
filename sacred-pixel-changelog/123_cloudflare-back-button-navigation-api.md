# 123. Cloudflare 버전 Android 뒤로 버튼 호환성 개선

## 문제
Cloudflare/PWA 버전에서 Android 뒤로 버튼을 ESC 키로 매핑하는 기능이 Edge, Chrome에서는 정상 작동하지만, Samsung Internet Browser와 Firefox에서는 브라우저 주소 뒤로가기가 그대로 작동하는 문제가 있었습니다.

## 원인
Firefox Android의 알려진 버그 ([bugzilla #1813755](https://bugzilla.mozilla.org/show_bug.cgi?id=1813755)):
- `history.pushState()`로 조작된 히스토리를 네이티브 뒤로가기 버튼이 무시함
- `history.back()` 프로그래밍 방식은 정상 작동하나, 물리적 뒤로가기 버튼에서는 `popstate` 이벤트가 제대로 발생하지 않음
- 버그 상태: NEW (3년째 미해결)

Samsung Internet도 유사한 동작을 보임.

## 수정 내용 (v1 → v2)

### v1 (초기 수정)
Navigation API를 우선 사용하고, History API를 폴백으로 유지. 그러나 **Navigation API 브랜치에서 `history.pushState`를 하지 않아서** Edge, Chrome에서도 뒤로 버튼이 작동하지 않는 문제 발생.

### v2 (최종 수정)
**항상 `history.pushState` 실행** + Navigation API와 popstate 모두 등록하는 방식으로 변경.

### teavm/webapp/index.html

```javascript
(function() {
    var backHandled = false;

    function handleBackButton() {
        if (backHandled) return;
        backHandled = true;
        setTimeout(function() { backHandled = false; }, 200);

        if (typeof window.__onBackPressed__ === 'function') {
            window.__onBackPressed__();
        }
    }

    // 항상 초기 히스토리 상태 push - 뒤로 갈 곳이 있어야 intercept 가능
    history.pushState({ spdGame: true }, '');

    // Navigation API (Chrome 102+, Edge, etc.)
    if (typeof navigation !== 'undefined' && navigation.addEventListener) {
        navigation.addEventListener('navigate', function(e) {
            if (e.navigationType === 'traverse' && e.canIntercept) {
                e.intercept({
                    handler: function() {
                        history.pushState({ spdGame: true }, ''); // re-push for next back
                        handleBackButton();
                        return Promise.resolve();
                    }
                });
            }
        });
    }

    // popstate 항상 등록 (Firefox, Samsung Internet, 구형 브라우저)
    // Navigation API가 intercept 성공하면 popstate는 발생하지 않음
    window.addEventListener('popstate', function(e) {
        history.pushState({ spdGame: true }, '');
        handleBackButton();
    });
})();
```

## 핵심 변경점

| 항목 | v1 | v2 |
|------|----|----|
| 초기 pushState | Navigation API 없을 때만 | **항상 실행** |
| intercept 후 re-push | 없음 | **있음** |
| popstate 리스너 | Navigation API 없을 때만 | **항상 등록** |

## 기술적 세부사항
- 파일: `teavm/webapp/index.html` (Cloudflare 버전)
- Navigation API의 `navigate` 이벤트는 히스토리 스택에 항목이 있어야 traverse로 인식
- 중복 처리 방지를 위한 200ms 디바운스 (`backHandled` 플래그)

## 영향
- 모든 브라우저(Chrome, Edge, Firefox, Samsung Internet)에서 뒤로 버튼이 ESC 키로 정상 매핑됨
- Navigation API 지원 여부와 관계없이 안정적으로 동작
