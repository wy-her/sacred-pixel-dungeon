/**
 * Firestore에서 특정 배지 보유 유저를 찾아 요약 리포트를 출력한다.
 *
 * 사용법:
 *   cd appsintoss-app
 *   node scripts/check-badge.mjs                    # 기본: TAKING_THE_MICK (굴욕기)
 *   node scripts/check-badge.mjs VICTORY_ALL_CLASSES
 *   node scripts/check-badge.mjs TAKING_THE_MICK <userHash>   # 특정 유저만
 *
 * .env의 VITE_FIREBASE_* 값을 그대로 사용한다.
 * 출력은 gameData 원문(수십 KB)을 제외한 요약이라 그대로 복사해서 공유해도 된다.
 */

import { readFileSync } from 'fs';
import { dirname, join } from 'path';
import { fileURLToPath } from 'url';
import { initializeApp } from 'firebase/app';
import { getFirestore, collection, query, where, getDocs, doc, getDoc } from 'firebase/firestore';

const __dirname = dirname(fileURLToPath(import.meta.url));

// .env 파싱
const env = {};
for (const line of readFileSync(join(__dirname, '..', '.env'), 'utf-8').split(/\r?\n/)) {
    const m = line.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/);
    if (m) env[m[1]] = m[2].replace(/^["']|["']$/g, '');
}

const app = initializeApp({
    apiKey: env.VITE_FIREBASE_API_KEY,
    authDomain: env.VITE_FIREBASE_AUTH_DOMAIN,
    projectId: env.VITE_FIREBASE_PROJECT_ID,
    storageBucket: env.VITE_FIREBASE_STORAGE_BUCKET,
    messagingSenderId: env.VITE_FIREBASE_MESSAGING_SENDER_ID,
    appId: env.VITE_FIREBASE_APP_ID,
});
const db = getFirestore(app);

const BADGE = process.argv[2] || 'TAKING_THE_MICK';
const USER_HASH = process.argv[3] || null;

function ts(ms) {
    return ms ? new Date(ms).toISOString().replace('T', ' ').slice(0, 19) : '(없음)';
}

function report(id, d) {
    const badges = d.badges || [];
    const hasGlobal = badges.includes(BADGE);

    console.log('');
    console.log('== user: ' + id.slice(0, 12) + '... (전체 ' + id.length + '자)');
    console.log('   updatedAt   : ' + ts(d.updatedAt));
    console.log('   version     : ' + d.version);
    console.log('   badges 총개수: ' + badges.length);
    console.log('   ' + BADGE + ' 전역 보유: ' + (hasGlobal ? 'YES' : 'NO'));

    const runs = d.rankings || [];
    console.log('   rankings 개수: ' + runs.length);

    let evidence = 0;
    runs.forEach((r, i) => {
        let g = null;
        if (r.gameData) { try { g = JSON.parse(r.gameData); } catch (e) { /* 파싱 실패 */ } }
        const localBadges = (g && g.badges && g.badges.badges) || [];
        const has = localBadges.includes(BADGE);
        // Belongings는 hero 번들에 평탄화되어 저장된다 (hero.weapon, hero.armor, ...)
        const w = g && g.hero && g.hero.weapon;
        const wName = w && w.__className ? w.__className.split('.').pop() : '(없음)';
        const wLvl = w && typeof w.level === 'number' ? w.level : '?';

        const mark = has ? ' <-- 이 판에서 획득' : '';
        console.log('     run#' + i
            + ' win=' + r.win
            + ' depth=' + r.depth
            + ' score=' + r.score
            + ' hero=' + r.heroClass
            + ' seed="' + (r.seed || '') + '"'
            + ' date=' + (r.date || '')
            + ' gameID=' + (r.gameID || '')
            + ' | weapon=' + wName + '+' + wLvl
            + ' | gameData=' + (r.gameData ? 'O' : 'X')
            + mark);
        if (has) evidence++;
    });

    console.log('   --> 판정: 전역보유=' + (hasGlobal ? 'Y' : 'N') + ', 해당런증거=' + evidence + '건');
    if (hasGlobal && evidence === 0) {
        console.log('   --> 주의: 배지는 있으나 기록된 런에 증거 없음 (클라우드 복원 또는 top6에서 밀려남)');
    }
    if (!hasGlobal && evidence > 0) {
        console.log('   --> 주의: 런 증거는 있으나 전역 미보유 (커스텀 시드 판일 가능성)');
    }
}

const main = async () => {
    console.log('badge=' + BADGE + ' project=' + env.VITE_FIREBASE_PROJECT_ID);

    if (USER_HASH) {
        const snap = await getDoc(doc(db, 'saves', USER_HASH));
        if (!snap.exists()) { console.log('해당 userHash 문서 없음'); return; }
        report(snap.id, snap.data());
    } else {
        const snap = await getDocs(query(collection(db, 'saves'), where('badges', 'array-contains', BADGE)));
        console.log('해당 배지 보유 문서 수: ' + snap.size);
        snap.forEach(s => report(s.id, s.data()));
    }
};

main().then(() => process.exit(0)).catch(e => { console.error('실패:', e.code || '', e.message); process.exit(1); });
