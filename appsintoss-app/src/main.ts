import { TossAds, setDeviceOrientation, setScreenAwakeMode, loadFullScreenAd, showFullScreenAd, graniteEvent, setIosSwipeGestureEnabled, SafeAreaInsets, grantPromotionRewardForGame } from '@apps-in-toss/web-framework';
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

// 앱인토스 네비게이션 바 높이 (SafeAreaInsets에 포함되지 않음)
// SafeAreaInsets.top은 상태바/노치 높이만 반환하므로 네비게이션 바 높이를 별도로 추가
const NAVIGATION_BAR_HEIGHT = 56;

// SafeAreaInsets 적용 (네비게이션 바 높이 포함)
function applySafeAreaInsets() {
    const app = document.getElementById('app');
    if (!app) return;

    try {
        const insets = SafeAreaInsets.get();
        const totalTop = insets.top + NAVIGATION_BAR_HEIGHT;
        app.style.setProperty('--safe-area-top', `${totalTop}px`);
        console.log('SafeAreaInsets applied:', insets, 'Total top:', totalTop);
    } catch (e) {
        // fallback: 네비게이션 바 높이만 적용
        app.style.setProperty('--safe-area-top', `${NAVIGATION_BAR_HEIGHT}px`);
        console.warn('SafeAreaInsets not available, using fallback:', e);
    }
}

// 초기 적용
applySafeAreaInsets();

// SafeArea 변경 시 동적 업데이트 (화면 회전 등)
try {
    SafeAreaInsets.subscribe({
        onEvent: (insets) => {
            const app = document.getElementById('app');
            if (app) {
                const totalTop = insets.top + NAVIGATION_BAR_HEIGHT;
                app.style.setProperty('--safe-area-top', `${totalTop}px`);
                console.log('SafeAreaInsets updated:', insets, 'Total top:', totalTop);
            }
        }
    });
} catch (e) {
    console.warn('SafeAreaInsets.subscribe not available:', e);
}

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

const AD_GROUP_ID = 'ait.v2.live.d042aa2a0f2a4b94';

// 전면 광고 가용 여부 확인
let interstitialAdAvailable = false;

// 광고 프리로드 상태
let adPreloaded = false;
let adPreloading = false;

// SDK에서 전면 광고 API를 제공하는지 확인
// isSupported()가 true를 반환해야만 광고 사용 가능 (더 엄격한 체크)
if (typeof loadFullScreenAd === 'function' && typeof showFullScreenAd === 'function') {
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
 * 전면 광고 미리 로드 (보스 처치 시 호출)
 * 이미 로드 중이거나 로드된 경우 무시
 */
(window as any).__preloadInterstitialAd__ = (): void => {
    if (!interstitialAdAvailable) {
        console.log('Interstitial ad preload: not available');
        return;
    }

    if (adPreloaded) {
        console.log('Interstitial ad preload: already loaded');
        return;
    }

    if (adPreloading) {
        console.log('Interstitial ad preload: already loading');
        return;
    }

    adPreloading = true;
    console.log('Interstitial ad preload: starting...');

    try {
        loadFullScreenAd({
            options: { adGroupId: AD_GROUP_ID },
            onEvent: (event) => {
                console.log('Interstitial ad preload event:', event.type);
                if (event.type === 'loaded') {
                    adPreloaded = true;
                    adPreloading = false;
                    console.log('Interstitial ad preload: SUCCESS');
                }
            },
            onError: (err) => {
                adPreloading = false;
                console.warn('Interstitial ad preload error:', err);
            }
        });
    } catch (e) {
        adPreloading = false;
        console.warn('Interstitial ad preload exception:', e);
    }
};

/**
 * 광고가 프리로드되어 있는지 확인
 */
(window as any).__isAdPreloaded__ = (): boolean => {
    return adPreloaded;
};

/**
 * 프리로드된 전면 광고 표시 (WndRegionComplete에서 호출)
 * - 광고가 프리로드 되어 있으면 즉시 표시
 * - 로드되지 않았으면 로드 후 표시 (fallback)
 */
(window as any).__showPreloadedInterstitialAd__ = (): void => {
    if (!interstitialAdAvailable) {
        console.log('Interstitial ad not available - skipping');
        notifyAdComplete();
        return;
    }

    let completed = false;

    const complete = () => {
        if (!completed) {
            completed = true;
            adPreloaded = false; // 광고 표시 후 상태 리셋
            // Set timestamp for Java-side timeout fallback
            // If JS callback fails, Java can detect ad completion via this timestamp
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

    // 타임아웃 설정 (3초 후 자동 진행 - 프리로드 시 보통 즉시 표시됨)
    // 유저 경험 우선: 3초 내 로드 안 되면 광고 스킵하고 게임 진행
    const timeout = setTimeout(() => {
        console.warn('Interstitial ad timeout (3s) - continuing without ad');
        complete();
    }, 3000);

    // Maximum viewing timeout - safety net if SDK fails to fire close event
    // Bug fix: SDK may show ad but never fire close event (dismissed/closed/completed)
    // This causes the game to hang on black screen indefinitely
    let viewingTimeout: ReturnType<typeof setTimeout> | null = null;
    const MAX_VIEWING_TIME = 60000; // 60 seconds max viewing time

    const clearAndComplete = () => {
        clearTimeout(timeout);
        if (viewingTimeout) clearTimeout(viewingTimeout);
        complete();
    };

    const doShowAd = () => {
        showFullScreenAd({
            options: { adGroupId: AD_GROUP_ID },
            onEvent: (showEvent) => {
                console.log('Interstitial ad show event:', showEvent.type);
                // 광고가 표시되면 로드 타임아웃 취소하고 최대 시청 타임아웃 설정
                if (showEvent.type === 'show' || showEvent.type === 'impression') {
                    clearTimeout(timeout);
                    // Set maximum viewing timeout as safety net
                    viewingTimeout = setTimeout(() => {
                        console.warn('Interstitial ad: max viewing time exceeded (60s) - continuing');
                        complete();
                    }, MAX_VIEWING_TIME);
                    console.log('Interstitial ad: load timeout cleared, max viewing timeout set');
                }
                // 모든 종료 이벤트 처리
                if (showEvent.type === 'dismissed' ||
                    showEvent.type === 'failedToShow' ||
                    showEvent.type === 'closed' ||
                    showEvent.type === 'completed') {
                    if (viewingTimeout) clearTimeout(viewingTimeout);
                    complete();
                }
            },
            onError: (err) => {
                console.warn('Interstitial ad show error:', err);
                clearAndComplete();
            }
        });
    };

    try {
        if (adPreloaded) {
            // 이미 로드된 광고 바로 표시
            console.log('Interstitial ad: showing preloaded ad...');
            doShowAd();
        } else {
            // 로드되지 않은 경우: 로드 후 표시 (fallback)
            console.log('Interstitial ad: not preloaded, loading now...');

            loadFullScreenAd({
                options: { adGroupId: AD_GROUP_ID },
                onEvent: (event) => {
                    console.log('Interstitial ad load event:', event.type);
                    if (event.type === 'loaded') {
                        console.log('Interstitial ad loaded, showing...');
                        doShowAd();
                    }
                },
                onError: (err) => {
                    console.warn('Interstitial ad load error:', err);
                    clearAndComplete();
                }
            });
        }
    } catch (e) {
        console.warn('Interstitial ad exception:', e);
        clearAndComplete();
    }
};

/**
 * 전면 광고 표시 (기존 호환성 유지 - 로드+표시 통합)
 * 게임 iframe은 __onInterstitialAdComplete__ 콜백을 미리 설정해야 함
 */
(window as any).__showInterstitialAd__ = (): void => {
    // 프리로드된 광고 표시 함수로 위임
    (window as any).__showPreloadedInterstitialAd__();
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
    if (bannerContainer && TossAds.attachBanner.isSupported()) {
        TossAds.attachBanner(
            'ait.v2.live.d64a13be44724523',
            bannerContainer,
            {
                // 스타일 옵션
                theme: 'dark',
                tone: 'blackAndWhite',
                variant: 'expanded',
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

// 게임 iframe 높이 조정 (safe-area + 배너 높이 제외)
function adjustGameHeight() {
    const app = document.getElementById('app');
    const banner = document.getElementById('banner-ad');
    const gameContainer = document.getElementById('game-container');

    if (app && banner && gameContainer) {
        const safeAreaTop = parseInt(getComputedStyle(app).paddingTop) || 0;
        const bannerHeight = banner.offsetHeight;
        const windowHeight = window.innerHeight;
        gameContainer.style.height = `${windowHeight - safeAreaTop - bannerHeight}px`;
    }
}

// 초기 조정 및 리사이즈 이벤트 핸들러
adjustGameHeight();
window.addEventListener('resize', adjustGameHeight);

// ========================================
// 종료 확인 모달 처리
// ========================================

/**
 * 종료 확인 모달이 현재 표시 중인지 확인
 */
function isExitModalVisible(): boolean {
    const modal = document.getElementById('exit-modal');
    return modal ? modal.classList.contains('visible') : false;
}

/**
 * 종료 확인 모달 표시
 */
function showExitModal() {
    const modal = document.getElementById('exit-modal');
    if (modal) {
        modal.classList.add('visible');
        console.log('Exit modal shown');
    }
}

/**
 * 종료 확인 모달 숨김
 */
function hideExitModal() {
    const modal = document.getElementById('exit-modal');
    if (modal) {
        modal.classList.remove('visible');
        console.log('Exit modal hidden');
    }
}

/**
 * 뒤로 버튼/X 버튼 처리
 * - 모달이 열려있으면 닫기
 * - 모달이 닫혀있으면 열기
 */
function handleBackEvent() {
    console.log('Back event received');
    if (isExitModalVisible()) {
        hideExitModal();
    } else {
        showExitModal();
    }
}

/**
 * 앱 종료 (closeView 호출)
 */
async function exitApp() {
    try {
        console.log('Closing app...');
        await closeView();
    } catch (e) {
        console.warn('Failed to close app:', e);
    }
}

// 모달 버튼 이벤트 리스너 설정
document.addEventListener('DOMContentLoaded', () => {
    const cancelBtn = document.getElementById('exit-cancel');
    const confirmBtn = document.getElementById('exit-confirm');

    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            hideExitModal();
        });
    }

    if (confirmBtn) {
        confirmBtn.addEventListener('click', () => {
            hideExitModal();
            exitApp();
        });
    }
});

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
            console.log('Back event listener registered (from dynamic import)');
        } catch (e) {
            console.warn('Failed to register back event via dynamic import:', e);
        }
    }
}).catch(() => {
    // 이미 정적 import로 시도했으므로 무시
});

// 게임 iframe에서 종료 모달을 호출할 수 있도록 브릿지 함수 노출
(window as any).__showExitModal__ = showExitModal;
(window as any).__hideExitModal__ = hideExitModal;
