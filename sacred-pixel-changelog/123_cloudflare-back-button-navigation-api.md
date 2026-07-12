# 123. Cloudflare 버전 Android 뒤로 버튼 호환성 개선

## 문제
Cloudflare/PWA 버전에서 Android 뒤로 버튼을 ESC 키로 매핑하는 기능이 Edge, Chrome에서는 정상 작동하지만, Samsung Internet Browser와 Firefox에서는 브라우저 주소 뒤로가기가 그대로 작동하는 문제가 있었습니다.

## 원인
Firefox Android의 알려진 버그 ([bugzilla #1813755](https://bugzilla.mozilla.org/show_bug.cgi?id=1813755)):
- `history.pushState()`로 조작된 히스토리를 네이티브 뒤로가기 버튼이 무시함
- `history.back()` 프로그래밍 방식은 정상 작동하나, 물리적 뒤로가기 버튼에서는 `popstate` 이벤트가 제대로 발생하지 않음
- 버그 상태: NEW (3년째 미해결)

Samsung Internet도 유사한 동작을 보임.

## 수정 내용
**Navigation API**를 우선 사용하고, History API를 폴백으로 유지하는 방식으로 변경했습니다.

### Navigation API 지원 현황
- Chrome 102+
- Firefox 147+
- Samsung Internet 19+
- Safari 26.2+
- 전역 지원율: 87.37%

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

    // Primary: Navigation API (Firefox/Samsung Internet에서 안정적으로 작동)
    if (typeof navigation !== 'undefined' && navigation.addEventListener) {
        navigation.addEventListener('navigate', function(e) {
            if (e.navigationType === 'traverse' && e.canIntercept) {
                e.intercept({
                    handler: function() {
                        handleBackButton();
                        return Promise.resolve();
                    }
                });
            }
        });
    }
    // Fallback: History API (구형 브라우저용)
    else {
        history.pushState({ spdGame: true }, '');
        window.addEventListener('popstate', function(e) {
            history.pushState({ spdGame: true }, '');
            handleBackButton();
        });
    }
})();
```

## 기술적 세부사항
- 파일: `teavm/webapp/index.html`
- Navigation API의 `navigate` 이벤트는 `popstate`와 달리 traversal(뒤로/앞으로) 탐색을 안정적으로 인터셉트
- `e.canIntercept` 체크로 인터셉트 가능한 경우에만 처리
- 중복 처리 방지를 위한 200ms 디바운스 적용

## 영향
- Samsung Internet, Firefox에서 뒤로 버튼이 ESC 키로 정상 매핑됨
- 기존 Chrome, Edge 동작 유지
- 구형 브라우저에서는 기존 History API 방식으로 폴백
