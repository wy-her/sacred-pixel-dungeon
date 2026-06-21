/**
 * Firebase configuration for Sacred Pixel Dungeon cloud save.
 *
 * Firebase 설정은 환경변수(.env)에서 읽어옵니다.
 * .env.example 파일을 참고하여 .env 파일을 생성하세요.
 */

import { initializeApp } from 'firebase/app';
import { getFirestore, doc, getDoc, setDoc } from 'firebase/firestore';

// Firebase 설정 (환경변수에서 읽기)
const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "",
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "",
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "",
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "",
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "",
    appId: import.meta.env.VITE_FIREBASE_APP_ID || ""
};

// Firebase 초기화
let app: ReturnType<typeof initializeApp> | null = null;
let db: ReturnType<typeof getFirestore> | null = null;

export function initFirebase(): boolean {
    try {
        // 설정이 완료되지 않은 경우 초기화하지 않음
        if (!firebaseConfig.apiKey || firebaseConfig.apiKey === "your_api_key_here") {
            console.warn('[Firebase] Config not set - cloud save disabled. Check .env file.');
            return false;
        }

        app = initializeApp(firebaseConfig);
        db = getFirestore(app);
        console.log('[Firebase] Initialized successfully');
        return true;
    } catch (e) {
        console.error('[Firebase] Initialization failed:', e);
        return false;
    }
}

/**
 * 클라우드에서 게임 데이터 불러오기
 * @param userHash getUserKeyForGame()에서 받은 유저 식별자
 * @returns 저장된 데이터 또는 null
 */
export async function loadCloudData(userHash: string): Promise<CloudSaveData | null> {
    if (!db) {
        console.warn('[Firebase] DB not initialized');
        return null;
    }

    try {
        const docRef = doc(db, 'saves', userHash);
        const docSnap = await getDoc(docRef);

        if (docSnap.exists()) {
            const data = docSnap.data() as CloudSaveData;
            console.log('[Firebase] Loaded cloud data for user:', userHash.substring(0, 8) + '...');
            return data;
        } else {
            console.log('[Firebase] No cloud data found for user:', userHash.substring(0, 8) + '...');
            return null;
        }
    } catch (e) {
        console.error('[Firebase] Load failed:', e);
        return null;
    }
}

/**
 * 게임 데이터를 클라우드에 저장
 * @param userHash getUserKeyForGame()에서 받은 유저 식별자
 * @param data 저장할 데이터
 * @returns 성공 여부
 */
export async function saveCloudData(userHash: string, data: CloudSaveData): Promise<boolean> {
    if (!db) {
        console.warn('[Firebase] DB not initialized');
        return false;
    }

    try {
        const docRef = doc(db, 'saves', userHash);
        await setDoc(docRef, {
            ...data,
            updatedAt: Date.now()
        });
        console.log('[Firebase] Saved cloud data for user:', userHash.substring(0, 8) + '...');
        return true;
    } catch (e) {
        console.error('[Firebase] Save failed:', e);
        return false;
    }
}

/**
 * 클라우드 세이브 데이터 구조
 * (Cloudflare 버전의 '데이터 내보내기'와 동일한 데이터)
 */
export interface CloudSaveData {
    // 버전 정보
    version: number;

    // 업적 (Badges) - 해금된 업적 목록
    badges: string[];

    // 랭킹 기록
    rankings: RankingRecord[];

    // 도감 - 발견한 아이템
    catalog: Record<string, string[]>;

    // 도감 - 발견한 몬스터
    bestiary: Record<string, string[]>;

    // 문서 - Lore, Guide, Alchemy
    documents: Record<string, string[]>;

    // 메타데이터
    updatedAt?: number;
}

export interface RankingRecord {
    score: number;
    heroClass: string;
    armorTier: number;
    herolevel: number;
    depth: number;
    win: boolean;
    challenges: number;
    seed: string;
}
