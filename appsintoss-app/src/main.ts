import { TossAds, setDeviceOrientation, setScreenAwakeMode, loadFullScreenAd, showFullScreenAd, graniteEvent, setIosSwipeGestureEnabled, SafeAreaInsets, grantPromotionRewardForGame, requestReview } from '@apps-in-toss/web-framework';
import { closeView } from '@apps-in-toss/web-bridge';
import { initFirebase, loadCloudData, saveCloudData, type CloudSaveData } from './firebase';

// Firebase 초기화
const firebaseReady = initFirebase();
console.log('Firebase ready:', firebaseReady);

// 화면 방향 세로 모드 고정
setDeviceOrientation({ type: 'portrait' });

// 화면 항상 켜짐 (게임 중 화면 꺼지지 않도록)
setScreenAwakeMode({ enabled: true });

// iOS 스와이프 뒤로가기 제스처 비활성화 (앱인토스 요구사항)
setIosSwipeGestureEnabled({ isEnabled: false });

// 네비게이션 바 설정 (방어적 코드 - API 존재 여부 확인)
import('@apps-in-toss/web-framework').then(sdk => {
    // setConfig 존재 확인 후 네비게이션 바 활성화
    if (typeof (sdk as any).setConfig === 'function') {
        (sdk as any).setConfig({
            navigationBar: {
                visible: true,
            }
        });
        console.log('Navigation bar configured');
    } else {
        console.log('setConfig not available - navigation bar may use default settings');
    }
}).catch(e => {
    console.warn('SDK config not available:', e);
});

// SafeAreaInsets 적용
// SDK 2.10.5+에서는 네비게이션 바 높이가 SafeAreaInsets.top에 포함됨
// 하지만 네이티브 네비게이션 바가 이미 표시되면 WebView는 그 아래에서 시작하므로
// 추가 padding이 필요 없음 (중복 방지)
function applySafeAreaInsets() {
    const app = document.getElementById('app');
    if (!app) return;

    try {
        const insets = SafeAreaInsets.get();
        console.log('SafeAreaInsets raw value:', JSON.stringify(insets));

        // 네이티브 네비게이션 바가 표시되는 경우 (granite.config의 navigationBar 설정)
        // WebView는 이미 네비게이션 바 아래에서 시작하므로 top padding 불필요
        // 단, 상태바(status bar) 영역은 WebView가 덮을 수 있으므로 확인 필요
        // SDK 2.10.5+: SafeAreaInsets.top = 상태바 + 네비게이션바
        // 네비게이션바가 네이티브로 표시되면 상태바만큼만 padding 필요
        // 일반적으로 상태바는 24~44px, 네비게이션바는 44~56px
        // 테스트: SafeAreaInsets.top이 너무 크면 (>60px) 네비게이션바 높이를 빼야 할 수 있음

        // 임시 해결: SafeAreaInsets.top을 0으로 설정
        // 네이티브 네비게이션 바가 이미 WebView 영역을 제한하고 있다면 padding 불필요
        const effectiveTop = 0; // 네이티브 헤더가 이미 처리하므로 0
        app.style.setProperty('--safe-area-top', `${effectiveTop}px`);
        console.log('SafeAreaInsets applied: effectiveTop =', effectiveTop, '(raw top =', insets.top, ')');
    } catch (e) {
        // fallback: 0px (SDK가 없으면 safe area 없음)
        app.style.setProperty('--safe-area-top', '0px');
        console.warn('SafeAreaInsets not available, using fallback:', e);
    }
}

// 초기 적용
applySafeAreaInsets();

// SafeArea 변경 구독 제거
// 우리는 항상 effectiveTop = 0을 사용하므로 동적 업데이트가 필요 없음
// SafeAreaInsets.subscribe를 제거하면 네비게이션 바 버튼 클릭 시
// 불필요한 콜백 실행을 방지하여 성능 향상
// (화면 회전 시에도 0을 사용하므로 문제 없음)

// 앱인토스 플랫폼 마커는 index.html의 인라인 스크립트에서 설정됨
// (iframe보다 먼저 실행되어야 하므로)

// Game Center API (동적 로드 - SDK에서 제공하지 않을 수 있음)
// SDK 참조를 저장하여 this 컨텍스트 유지
let gameCenterSdk: any = null;
let leaderboardAvailable = false;
let userKeyAvailable = false;

// SDK에서 Game Center API를 제공하는지 런타임에 확인
import('@apps-in-toss/web-framework').then(sdk => {
    gameCenterSdk = sdk;

    // 리더보드 API
    if (typeof (sdk as any).openGameCenterLeaderboard === 'function') {
        leaderboardAvailable = true;
        console.log('Game Center leaderboard API available');
    }
    if (typeof (sdk as any).submitGameCenterLeaderBoardScore === 'function') {
        console.log('submitGameCenterLeaderBoardScore API available');
    }

    // 유저 식별 API
    if (typeof (sdk as any).getUserKeyForGame === 'function') {
        userKeyAvailable = true;
        console.log('getUserKeyForGame API available');
    }

    // API 가용성을 iframe에 알림
    (window as any).__LEADERBOARD_AVAILABLE__ = leaderboardAvailable;
    (window as any).__USER_KEY_AVAILABLE__ = userKeyAvailable;
}).catch((e) => {
    console.warn('Game Center API not available:', e);
    (window as any).__LEADERBOARD_AVAILABLE__ = false;
    (window as any).__USER_KEY_AVAILABLE__ = false;
});

// 리더보드 브릿지 함수 (iframe에서 호출)
(window as any).__openLeaderboard__ = () => {
    if (gameCenterSdk && typeof gameCenterSdk.openGameCenterLeaderboard === 'function') {
        try {
            gameCenterSdk.openGameCenterLeaderboard();
            console.log('Leaderboard opened');
        } catch (e) {
            console.warn('Failed to open leaderboard:', e);
        }
    } else {
        console.warn('openGameCenterLeaderboard not available');
    }
};

(window as any).__submitScore__ = async (score: number) => {
    if (gameCenterSdk && typeof gameCenterSdk.submitGameCenterLeaderBoardScore === 'function') {
        try {
            // SDK는 score를 문자열로 받음 (예: "12345")
            const scoreString = String(score);
            const result = await gameCenterSdk.submitGameCenterLeaderBoardScore({ score: scoreString });

            if (!result) {
                console.warn('Score submission: unsupported app version');
                return;
            }

            if (result.statusCode === 'SUCCESS') {
                console.log('Score submitted successfully:', score);
            } else {
                console.warn('Score submission failed:', result.statusCode);
            }
        } catch (e) {
            console.warn('Failed to submit score:', e);
        }
    } else {
        console.warn('submitGameCenterLeaderBoardScore not available, sdk:', !!gameCenterSdk);
    }
};

// 유저 키 조회 브릿지 함수 (iframe에서 호출)
// 반환값: { type: 'HASH', hash: string } | 'INVALID_CATEGORY' | 'ERROR' | null
(window as any).__getUserKey__ = async (): Promise<{ type: string; hash: string } | string | null> => {
    if (gameCenterSdk && typeof gameCenterSdk.getUserKeyForGame === 'function') {
        try {
            const result = await gameCenterSdk.getUserKeyForGame();
            if (result && typeof result === 'object' && result.type === 'HASH') {
                console.log('User key retrieved:', result.hash.substring(0, 8) + '...');
                // 유저 해시를 전역에 저장 (클라우드 세이브에서 사용)
                (window as any).__USER_HASH__ = result.hash;
                return result;
            } else if (typeof result === 'string') {
                console.warn('getUserKeyForGame returned:', result);
                return result;
            } else {
                console.warn('getUserKeyForGame returned undefined (app version too old?)');
                return null;
            }
        } catch (e) {
            console.warn('Failed to get user key:', e);
            return 'ERROR';
        }
    } else {
        console.warn('getUserKeyForGame not available');
        return null;
    }
};

// ========================================
// 프로모션 브릿지 함수 (토스 포인트 지급)
// ========================================

// 프로모션 API 가용 여부 (게임 미니앱에서만 사용 가능)
(window as any).__PROMOTION_AVAILABLE__ = true;

/**
 * 프로모션 리워드 지급 (토스 포인트)
 * @param promotionCode 프로모션 코드 (콘솔에서 등록한 코드)
 * @param amount 지급할 토스 포인트 금액
 * @returns 성공 시 { key: string }, 실패 시 { errorCode: string, message: string } 또는 'ERROR' 또는 undefined
 */
(window as any).__grantPromotionReward__ = async (promotionCode: string, amount: number): Promise<any> => {
    console.log('grantPromotionReward called:', promotionCode, amount);
    try {
        const result = await grantPromotionRewardForGame({
            params: {
                promotionCode: promotionCode,
                amount: amount,
            },
        });

        if (!result) {
            console.warn('grantPromotionRewardForGame: unsupported app version');
            return undefined;
        }

        if (result === 'ERROR') {
            console.error('grantPromotionRewardForGame: unknown error');
            return 'ERROR';
        }

        if ('key' in result) {
            console.log('Promotion reward granted successfully:', result.key);
            return result;
        } else if ('errorCode' in result) {
            console.warn('Promotion reward failed:', result.errorCode, result.message);
            return result;
        }

        return result;
    } catch (e) {
        console.error('grantPromotionRewardForGame error:', e);
        return 'ERROR';
    }
};

// ========================================
// 리뷰 요청 브릿지 함수
// ========================================

// 리뷰 API 가용 여부
(window as any).__REVIEW_AVAILABLE__ = true;

/**
 * 앱 리뷰 요청
 * 사용자가 만족을 느낄 만한 시점(튜토리얼 완료 등)에 호출
 * @returns 성공 여부 (UI 표시 여부와 무관하게 요청 완료 시 true)
 */
(window as any).__requestReview__ = async (): Promise<boolean> => {
    console.log('requestReview called');
    try {
        await requestReview();
        console.log('Review request completed');
        return true;
    } catch (e) {
        console.warn('Review request failed:', e);
        return false;
    }
};

// ========================================
// 클라우드 세이브 브릿지 함수 (Firebase)
// ========================================

// 클라우드 세이브 가용 여부
(window as any).__CLOUD_SAVE_AVAILABLE__ = firebaseReady;

/**
 * 클라우드에서 게임 데이터 불러오기
 * @returns JSON 문자열 또는 null
 */
(window as any).__loadCloudSave__ = async (): Promise<string | null> => {
    if (!firebaseReady) {
        console.warn('Cloud save not available - Firebase not initialized');
        return null;
    }

    // 유저 해시 확인
    let userHash = (window as any).__USER_HASH__;
    if (!userHash) {
        // 유저 키 먼저 획득 시도
        const keyResult = await (window as any).__getUserKey__();
        if (keyResult && typeof keyResult === 'object' && keyResult.hash) {
            userHash = keyResult.hash;
        }
    }

    if (!userHash) {
        console.warn('Cloud save load failed - no user hash');
        return null;
    }

    try {
        const data = await loadCloudData(userHash);
        if (data) {
            return JSON.stringify(data);
        }
        return null;
    } catch (e) {
        console.error('Cloud save load error:', e);
        return null;
    }
};

/**
 * 게임 데이터를 클라우드에 저장
 * @param jsonData JSON 문자열
 * @returns 성공 여부
 */
(window as any).__saveCloudSave__ = async (jsonData: string): Promise<boolean> => {
    if (!firebaseReady) {
        console.warn('Cloud save not available - Firebase not initialized');
        return false;
    }

    // 유저 해시 확인
    let userHash = (window as any).__USER_HASH__;
    if (!userHash) {
        // 유저 키 먼저 획득 시도
        const keyResult = await (window as any).__getUserKey__();
        if (keyResult && typeof keyResult === 'object' && keyResult.hash) {
            userHash = keyResult.hash;
        }
    }

    if (!userHash) {
        console.warn('Cloud save failed - no user hash');
        return false;
    }

    try {
        const data: CloudSaveData = JSON.parse(jsonData);
        return await saveCloudData(userHash, data);
    } catch (e) {
        console.error('Cloud save error:', e);
        return false;
    }
};

// ========================================
// 전면 광고 (Interstitial Ad) 브릿지 함수
// ========================================

const AD_GROUP_ID = import.meta.env.VITE_AD_GROUP_ID_INTERSTITIAL || '';

// 전면 광고 가용 여부 확인
let interstitialAdAvailable = false;

// 환경변수가 설정되지 않았으면 광고 비활성화
if (!AD_GROUP_ID) {
    console.log('Interstitial ad: AD_GROUP_ID not configured, disabling ads');
} else if (typeof loadFullScreenAd === 'function' && typeof showFullScreenAd === 'function') {
    // SDK에서 전면 광고 API를 제공하는지 확인
    // isSupported()가 true를 반환해야만 광고 사용 가능 (더 엄격한 체크)
    if (typeof (loadFullScreenAd as any).isSupported === 'function') {
        interstitialAdAvailable = (loadFullScreenAd as any).isSupported();
        console.log('Interstitial ad isSupported():', interstitialAdAvailable);
    } else {
        // isSupported가 없으면 광고 사용 불가로 간주 (dev 환경 등)
        interstitialAdAvailable = false;
        console.log('Interstitial ad: isSupported not available, disabling ads');
    }
} else {
    console.log('Interstitial ad API not available');
}

(window as any).__INTERSTITIAL_AD_AVAILABLE__ = interstitialAdAvailable;

/**
 * 전면 광고 완료 시 게임 iframe에 알림
 * 재시도 로직 포함 - 타이밍 이슈 회피
 */
function notifyAdComplete() {
    console.log('Interstitial ad: notifying game iframe');

    let attempts = 0;
    const maxAttempts = 30; // 최대 30회 재시도 (0.6초)
    const retryInterval = 20; // 20ms 간격 (더 빠른 응답)

    const tryNotify = () => {
        attempts++;
        try {
            const gameFrame = document.getElementById('game-frame') as HTMLIFrameElement;
            if (gameFrame && gameFrame.contentWindow) {
                const callback = (gameFrame.contentWindow as any).__onInterstitialAdComplete__;
                if (typeof callback === 'function') {
                    console.log(`Interstitial ad: callback found on attempt ${attempts}, invoking...`);
                    callback();
                    console.log('Interstitial ad: game iframe notified successfully');
                    return true; // 성공
                }
            }
        } catch (e) {
            console.warn('Interstitial ad: error accessing iframe', e);
        }

        // 재시도
        if (attempts < maxAttempts) {
            console.log(`Interstitial ad: callback not found, retry ${attempts}/${maxAttempts}`);
            setTimeout(tryNotify, retryInterval);
        } else {
            console.error('Interstitial ad: FAILED - callback not found after max attempts');
            // 최후의 수단: postMessage 시도 (same-origin으로 제한)
            try {
                const gameFrame = document.getElementById('game-frame') as HTMLIFrameElement;
                if (gameFrame && gameFrame.contentWindow) {
                    // Use same-origin for security instead of '*'
                    const targetOrigin = window.location.origin;
                    gameFrame.contentWindow.postMessage({ type: 'adComplete' }, targetOrigin);
                    console.log('Interstitial ad: sent postMessage as fallback to', targetOrigin);
                }
            } catch (e) {
                console.warn('Interstitial ad: postMessage fallback failed', e);
            }
        }
        return false;
    };

    tryNotify();
}

/**
 * 전면 광고 표시
 * 광고를 로드하고 표시함
 *
 * 타임아웃 구조 (3 에이전트 합의):
 * - LOAD_TIMEOUT (8초): loadFullScreenAd 호출 후 'loaded' 이벤트까지
 * - SHOW_TIMEOUT (5초): showFullScreenAd 호출 후 'show' 이벤트까지 (핵심 수정)
 * - VIEWING_TIMEOUT (60초): 광고 표시 후 닫힘 이벤트까지 (SDK 버그 대비)
 */
(window as any).__showInterstitialAd__ = (): void => {
    if (!interstitialAdAvailable) {
        console.log('Interstitial ad not available - skipping');
        notifyAdComplete();
        return;
    }

    let completed = false;
    let adShowing = false;  // 광고가 실제로 화면에 표시되었는지 추적

    const complete = () => {
        if (!completed) {
            completed = true;
            // Set timestamp for Java-side timeout fallback
            try {
                const gameFrame = document.getElementById('game-frame') as HTMLIFrameElement;
                if (gameFrame && gameFrame.contentWindow) {
                    (gameFrame.contentWindow as any).__adCompletedTimestamp__ = Date.now();
                    console.log('Interstitial ad: set completion timestamp for Java fallback');
                }
            } catch (e) {
                console.warn('Interstitial ad: failed to set completion timestamp', e);
            }
            notifyAdComplete();
        }
    };

    // 타임아웃 상수
    const LOAD_TIMEOUT = 8000;     // 8초: 로드까지
    const SHOW_TIMEOUT = 5000;     // 5초: show 이벤트까지 (핵심!)
    const VIEWING_TIMEOUT = 60000; // 60초: 최대 시청 시간

    let loadTimeout: ReturnType<typeof setTimeout> | null = null;
    let showTimeout: ReturnType<typeof setTimeout> | null = null;
    let viewingTimeout: ReturnType<typeof setTimeout> | null = null;

    const clearAllTimeouts = () => {
        if (loadTimeout) clearTimeout(loadTimeout);
        if (showTimeout) clearTimeout(showTimeout);
        if (viewingTimeout) clearTimeout(viewingTimeout);
    };

    const doShowAd = () => {
        // show 이벤트 타임아웃 시작 - 광고가 실제로 나타날 때까지 대기
        showTimeout = setTimeout(() => {
            if (!adShowing) {
                console.warn(`Interstitial ad: 'show' event not received within ${SHOW_TIMEOUT/1000}s - skipping ad`);
                clearAllTimeouts();
                complete();
            }
        }, SHOW_TIMEOUT);

        showFullScreenAd({
            options: { adGroupId: AD_GROUP_ID },
            onEvent: (showEvent) => {
                console.log('Interstitial ad show event:', showEvent.type);

                // 광고가 화면에 표시됨
                if (showEvent.type === 'show' || showEvent.type === 'impression') {
                    adShowing = true;

                    // show 타임아웃 취소 - 광고 표시됨
                    if (showTimeout) {
                        clearTimeout(showTimeout);
                        showTimeout = null;
                    }

                    // viewing 타임아웃 설정 (SDK 버그 대비)
                    viewingTimeout = setTimeout(() => {
                        console.warn('Interstitial ad: max viewing time exceeded (60s) - continuing');
                        complete();
                    }, VIEWING_TIMEOUT);

                    console.log('Interstitial ad: now visible, show timeout cleared, viewing timeout set');
                }

                // 광고 종료 이벤트
                if (showEvent.type === 'dismissed' ||
                    showEvent.type === 'failedToShow' ||
                    showEvent.type === 'closed' ||
                    showEvent.type === 'completed') {
                    clearAllTimeouts();
                    complete();
                }
            },
            onError: (err) => {
                console.warn('Interstitial ad show error:', err);
                clearAllTimeouts();
                complete();
            }
        });
    };

    try {
        console.log('Interstitial ad: loading...');

        // 로드 타임아웃 설정
        loadTimeout = setTimeout(() => {
            if (!completed && !adShowing) {
                console.warn(`Interstitial ad: load timeout (${LOAD_TIMEOUT/1000}s) - ad never loaded`);
                clearAllTimeouts();
                complete();
            }
        }, LOAD_TIMEOUT);

        loadFullScreenAd({
            options: { adGroupId: AD_GROUP_ID },
            onEvent: (event) => {
                console.log('Interstitial ad load event:', event.type);
                if (event.type === 'loaded') {
                    // 로드 타임아웃 취소
                    if (loadTimeout) {
                        clearTimeout(loadTimeout);
                        loadTimeout = null;
                    }
                    console.log('Interstitial ad loaded, showing...');
                    doShowAd();
                }
            },
            onError: (err) => {
                console.warn('Interstitial ad load error:', err);
                clearAllTimeouts();
                complete();
            }
        });
    } catch (e) {
        console.warn('Interstitial ad exception:', e);
        clearAllTimeouts();
        complete();
    }
};

// 앱인토스 SDK 초기화 및 배너 광고 로드
TossAds.initialize({
    callbacks: {
        onInitialized: () => {
            console.log('TossAds SDK initialized');
            loadBannerAd();
        },
        onInitializationFailed: (error) => {
            console.error('TossAds SDK initialization failed:', error);
            // 초기화 실패해도 게임은 정상 동작하도록 처리
        },
    },
});

function loadBannerAd() {
    const bannerContainer = document.getElementById('banner-ad');
    const bannerAdGroupId = import.meta.env.VITE_AD_GROUP_ID_BANNER || '';
    if (bannerContainer && bannerAdGroupId && TossAds.attachBanner.isSupported()) {
        TossAds.attachBanner(
            bannerAdGroupId,
            bannerContainer,
            {
                // 스타일 옵션 - dark 테마, grey 배경
                theme: 'dark',
                variant: 'expanded',
                background: 'grey',
                callbacks: {
                    onAdRendered: (payload) => {
                        console.log('Banner ad rendered:', payload);
                    },
                    onAdFailedToRender: (payload) => {
                        console.error('Banner ad failed to render:', payload);
                    },
                },
            }
        );
    }
}

// 게임 iframe 높이 조정은 CSS flexbox가 자동 처리함
// adjustGameHeight() 제거됨 - flexbox (flex: 1)가 더 안정적
// 이전 코드는 배너 광고 렌더링 전에 호출되어 높이 계산 오류 발생

// ========================================
// 종료 확인 모달 처리
// ========================================

/**
 * 뒤로 버튼 처리 - 게임 iframe에 뒤로가기 이벤트 전달
 * 게임 내에서 설정/인벤토리 등이 열려있으면 닫고,
 * 메인 화면이면 무시 (종료는 네비게이션 바 X 버튼으로만)
 */
// backEvent 중복 처리 방지를 위한 디바운스
let lastBackEventTime = 0;
const BACK_EVENT_DEBOUNCE = 300; // 300ms 내 중복 이벤트 무시

function handleBackEvent() {
    // 디바운스: 300ms 내 중복 backEvent 무시 (네비게이션 바 버튼이 여러 이벤트를 발생시킬 수 있음)
    const now = Date.now();
    if (now - lastBackEventTime < BACK_EVENT_DEBOUNCE) {
        console.log('Back event ignored (debounce)');
        return;
    }
    lastBackEventTime = now;

    console.log('Back event received');

    // 게임 iframe에 뒤로가기 이벤트 전달 (requestAnimationFrame으로 비동기 처리)
    // 이렇게 하면 현재 이벤트 핸들러가 빠르게 완료되어 렌더링 블로킹을 방지
    requestAnimationFrame(() => {
        try {
            const gameFrame = document.getElementById('game-frame') as HTMLIFrameElement;
            if (gameFrame && gameFrame.contentWindow) {
                const callback = (gameFrame.contentWindow as any).__onBackPressed__;
                if (typeof callback === 'function') {
                    callback();
                    console.log('Back event forwarded to game iframe');
                } else {
                    console.log('Game iframe __onBackPressed__ not available - ignoring back event');
                }
            }
        } catch (e) {
            console.warn('Failed to forward back event to game iframe:', e);
        }
    });
}

// 뒤로가기/X 버튼 이벤트 처리 (graniteEvent)
// Android 뒤로 버튼 클릭 시 발생 (X 버튼은 별도 처리 필요할 수 있음)
let backEventRegistered = false;

function registerBackEventListener() {
    if (backEventRegistered) {
        console.log('Back event listener already registered');
        return;
    }

    try {
        if (graniteEvent && typeof graniteEvent.addEventListener === 'function') {
            graniteEvent.addEventListener('backEvent', {
                onEvent: () => {
                    console.log('graniteEvent backEvent received');
                    handleBackEvent();
                },
                onError: (error) => {
                    console.warn('Back event error:', error);
                }
            });
            backEventRegistered = true;
            console.log('Back event listener registered successfully');
        } else {
            console.warn('graniteEvent.addEventListener not available, graniteEvent:', graniteEvent);
        }
    } catch (e) {
        console.warn('Failed to register back event listener:', e);
    }
}

// 즉시 등록 시도
registerBackEventListener();

// SDK 동적 import 후에도 재시도 (SDK가 늦게 로드되는 경우 대비)
import('@apps-in-toss/web-framework').then(sdk => {
    if (!backEventRegistered && (sdk as any).graniteEvent) {
        try {
            (sdk as any).graniteEvent.addEventListener('backEvent', {
                onEvent: () => {
                    console.log('graniteEvent backEvent received (dynamic)');
                    handleBackEvent();
                },
                onError: (error: any) => {
                    console.warn('Back event error:', error);
                }
            });
            backEventRegistered = true;
            console.log('Back event listener registered (from dynamic import)');
        } catch (e) {
            console.warn('Failed to register back event via dynamic import:', e);
        }
    }
}).catch(() => {
    // 이미 정적 import로 시도했으므로 무시
});

