#!/usr/bin/env python3
"""
폰트 서브셋팅 스크립트 (앱인토스 빌드 전용)

번역 파일에서 실제 사용되는 문자만 추출하여 CJK 폰트 크기를 축소합니다.
- 원본: 27.5MB → 서브셋: ~2MB

중요: webapp/fonts/의 원본 폰트는 건드리지 않고,
      build/dist/webapp/fonts/에 서브셋 폰트를 직접 생성합니다.

사용법:
    python subset_fonts.py [--output-dir <path>]

사전 요구사항:
    pip install fonttools brotli
"""

import os
import sys
import re
import argparse
from pathlib import Path
from collections import defaultdict

try:
    from fontTools.subset import main as subset_main
    from fontTools.ttLib import TTFont
except ImportError:
    print("Error: fonttools가 설치되어 있지 않습니다.")
    print("설치: pip install fonttools brotli")
    sys.exit(1)

# 프로젝트 경로 설정
SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent.parent
MESSAGES_DIR = PROJECT_ROOT / "core" / "src" / "main" / "assets" / "messages"
FONTS_DIR = SCRIPT_DIR.parent / "webapp" / "fonts"
DEFAULT_OUTPUT_DIR = SCRIPT_DIR.parent / "build" / "dist" / "webapp" / "fonts"

# 폰트 매핑 (언어 코드 → 폰트 파일)
FONT_MAPPING = {
    "ja": "noto-sans-jp.woff2",
    "ko": "noto-sans-kr.woff2",
    "zh": "noto-sans-sc.woff2",
    "zh-hant": "noto-sans-tc.woff2",
}

# 기본 ASCII + 라틴 확장 문자 (모든 폰트에 포함)
BASE_CHARS = set()
for i in range(0x0020, 0x007F):  # Basic Latin
    BASE_CHARS.add(chr(i))
for i in range(0x00A0, 0x0100):  # Latin-1 Supplement
    BASE_CHARS.add(chr(i))


def extract_text_from_properties(file_path: Path) -> str:
    """properties 파일에서 텍스트 추출 (값만 추출, 키는 무시)"""
    text = ""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                if '=' in line:
                    # 키=값 형식에서 값만 추출
                    _, _, value = line.partition('=')
                    text += value + "\n"
    except Exception as e:
        print(f"  Warning: {file_path.name} 읽기 실패: {e}")
    return text


def get_language_from_filename(filename: str) -> str:
    """파일명에서 언어 코드 추출"""
    name = Path(filename).stem

    if '_zh-hant' in name:
        return 'zh-hant'
    elif '_zh' in name:
        return 'zh'
    elif '_ja' in name:
        return 'ja'
    elif '_ko' in name:
        return 'ko'
    else:
        return None


def collect_chars_by_language() -> dict:
    """언어별로 사용되는 문자 수집"""
    chars_by_lang = defaultdict(set)

    print(f"번역 파일 스캔: {MESSAGES_DIR}")

    if not MESSAGES_DIR.exists():
        print(f"Error: 메시지 디렉토리가 존재하지 않습니다: {MESSAGES_DIR}")
        sys.exit(1)

    for props_file in MESSAGES_DIR.rglob("*.properties"):
        lang = get_language_from_filename(props_file.name)
        if lang is None:
            continue

        text = extract_text_from_properties(props_file)
        chars_by_lang[lang].update(text)

    for lang in chars_by_lang:
        chars_by_lang[lang].update(BASE_CHARS)

    return chars_by_lang


def filter_chars_for_font(chars: set, lang: str) -> set:
    """폰트에 맞는 문자만 필터링"""
    filtered = set()

    for char in chars:
        code = ord(char)

        if code < 0x0100:
            filtered.add(char)
            continue

        if lang == 'ja':
            if (0x3040 <= code <= 0x309F or
                0x30A0 <= code <= 0x30FF or
                0x31F0 <= code <= 0x31FF or
                0x4E00 <= code <= 0x9FFF or
                0x3400 <= code <= 0x4DBF or
                0xFF00 <= code <= 0xFFEF or
                0x3000 <= code <= 0x303F):
                filtered.add(char)

        elif lang == 'ko':
            if (0xAC00 <= code <= 0xD7AF or
                0x1100 <= code <= 0x11FF or
                0x3130 <= code <= 0x318F or
                0x4E00 <= code <= 0x9FFF or
                0x3000 <= code <= 0x303F or
                0xFF00 <= code <= 0xFFEF):
                filtered.add(char)

        elif lang in ('zh', 'zh-hant'):
            if (0x4E00 <= code <= 0x9FFF or
                0x3400 <= code <= 0x4DBF or
                0x20000 <= code <= 0x2A6DF or
                0x2A700 <= code <= 0x2B73F or
                0x2B740 <= code <= 0x2B81F or
                0xF900 <= code <= 0xFAFF or
                0x3000 <= code <= 0x303F or
                0xFF00 <= code <= 0xFFEF or
                0x2000 <= code <= 0x206F):
                filtered.add(char)

    return filtered


def create_unicodes_file(chars: set, output_path: Path):
    """유니코드 포인트 파일 생성"""
    with open(output_path, 'w', encoding='utf-8') as f:
        for char in sorted(chars):
            code = ord(char)
            if code >= 0x20:
                f.write(f"U+{code:04X}\n")


def subset_font(input_font: Path, output_font: Path, unicodes_file: Path):
    """폰트 서브셋 생성"""
    args = [
        str(input_font),
        f"--unicodes-file={unicodes_file}",
        f"--output-file={output_font}",
        "--flavor=woff2",
        "--no-hinting",
        "--desubroutinize",
    ]

    try:
        subset_main(args)
        return True
    except Exception as e:
        print(f"  Error: 서브셋 생성 실패: {e}")
        return False


def get_file_size_mb(path: Path) -> float:
    if path.exists():
        return path.stat().st_size / (1024 * 1024)
    return 0


def main():
    parser = argparse.ArgumentParser(description='폰트 서브셋팅 (앱인토스 빌드 전용)')
    parser.add_argument('--output-dir', type=str, default=None,
                        help='서브셋 폰트 출력 디렉토리 (기본: build/dist/webapp/fonts)')
    args = parser.parse_args()

    output_dir = Path(args.output_dir) if args.output_dir else DEFAULT_OUTPUT_DIR

    print("=" * 60)
    print("폰트 서브셋팅 시작 (앱인토스 빌드 전용)")
    print("=" * 60)
    print(f"원본 폰트: {FONTS_DIR}")
    print(f"출력 디렉토리: {output_dir}")

    # 출력 디렉토리 생성
    output_dir.mkdir(parents=True, exist_ok=True)

    # 1. 언어별 문자 수집
    print("\n[1/3] 번역 파일에서 문자 추출...")
    chars_by_lang = collect_chars_by_language()

    for lang, chars in chars_by_lang.items():
        print(f"  {lang}: {len(chars)} 문자")

    # 2. 비-CJK 폰트 복사 (서브셋팅 불필요)
    print("\n[2/3] 비-CJK 폰트 복사...")
    import shutil
    for font_file in ['inter-full.woff2', 'noto-sans-v42-latin-regular.woff2', 'OFL.txt']:
        src = FONTS_DIR / font_file
        dst = output_dir / font_file
        if src.exists():
            shutil.copy2(src, dst)
            print(f"  복사: {font_file}")

    # 3. 서브셋 폰트 생성
    print("\n[3/3] 서브셋 폰트 생성...")

    temp_dir = SCRIPT_DIR / "temp"
    temp_dir.mkdir(exist_ok=True)

    results = []

    for lang, font_file in FONT_MAPPING.items():
        if lang not in chars_by_lang:
            print(f"  {lang}: 번역 파일 없음, 스킵")
            continue

        original_font = FONTS_DIR / font_file
        output_font = output_dir / font_file
        unicodes_file = temp_dir / f"{lang}_unicodes.txt"

        if not original_font.exists():
            print(f"  {font_file}: 폰트 파일 없음, 스킵")
            continue

        # 문자 필터링
        filtered_chars = filter_chars_for_font(chars_by_lang[lang], lang)
        print(f"  {lang}: {len(filtered_chars)} 문자 (필터링 후)")

        # 유니코드 파일 생성
        create_unicodes_file(filtered_chars, unicodes_file)

        # 원본 크기
        original_size = get_file_size_mb(original_font)

        # 서브셋 생성
        print(f"  {font_file} 서브셋 생성 중...")
        if subset_font(original_font, output_font, unicodes_file):
            subset_size = get_file_size_mb(output_font)
            saved = original_size - subset_size
            results.append((font_file, original_size, subset_size, saved))
            print(f"    완료: {original_size:.1f}MB -> {subset_size:.1f}MB (절약: {saved:.1f}MB)")
        else:
            # 실패 시 원본 복사
            shutil.copy2(original_font, output_font)
            print(f"    실패, 원본 복사")

    # 임시 파일 정리
    if temp_dir.exists():
        shutil.rmtree(temp_dir)

    # 결과 요약
    print("\n" + "=" * 60)
    print("결과 요약")
    print("-" * 60)

    total_original = sum(r[1] for r in results)
    total_subset = sum(r[2] for r in results)
    total_saved = sum(r[3] for r in results)

    print(f"{'폰트':<25} {'원본':>10} {'서브셋':>10} {'절약':>10}")
    print("-" * 60)
    for font_file, original, subset, saved in results:
        print(f"{font_file:<25} {original:>9.1f}MB {subset:>9.1f}MB {saved:>9.1f}MB")
    print("-" * 60)
    print(f"{'합계':<25} {total_original:>9.1f}MB {total_subset:>9.1f}MB {total_saved:>9.1f}MB")

    print("\n" + "=" * 60)
    print("폰트 서브셋팅 완료!")
    print(f"출력: {output_dir}")
    print("=" * 60)

    return 0


if __name__ == "__main__":
    sys.exit(main())
