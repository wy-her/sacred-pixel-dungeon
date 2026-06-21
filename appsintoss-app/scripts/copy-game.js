/**
 * 게임 파일을 public/game/에 복사하는 스크립트
 *
 * 사용법:
 *   node scripts/copy-game.js
 *
 * 사전 요구사항:
 *   ../teavm/build/dist/webapp/ 에 buildAppsintoss 빌드 결과물이 있어야 함
 */

import { cpSync, rmSync, existsSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = join(__dirname, '..');

const source = join(projectRoot, '..', 'teavm', 'build', 'dist', 'appsintoss', 'webapp');
const destination = join(projectRoot, 'public', 'game');

console.log('게임 파일 복사 스크립트');
console.log('========================');
console.log(`소스: ${source}`);
console.log(`대상: ${destination}`);

// 소스 디렉토리 확인
if (!existsSync(source)) {
    console.error(`\n오류: 소스 디렉토리가 존재하지 않습니다.`);
    console.error(`먼저 앱인토스 빌드를 실행하세요:`);
    console.error(`  cd .. && ./gradlew teavm:buildAppsintoss`);
    process.exit(1);
}

// 기존 대상 디렉토리 삭제
if (existsSync(destination)) {
    console.log(`\n기존 게임 파일 삭제 중...`);
    rmSync(destination, { recursive: true, force: true });
}

// 복사
console.log(`\n게임 파일 복사 중...`);
cpSync(source, destination, { recursive: true });

console.log(`\n완료!`);
console.log(`게임 파일이 ${destination}에 복사되었습니다.`);
