#!/usr/bin/env python3
import re
import os
from pathlib import Path

def parse_properties_file(filepath):
    """Parse properties file and return dict of key-value pairs."""
    props = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.rstrip('\n\r')
            if not line or line.startswith('#'):
                continue
            if '=' not in line:
                continue
            key, value = line.split('=', 1)
            props[key.strip()] = value.strip()
    return props

def extract_format_placeholders(text):
    """Extract format placeholders with their positions."""
    # Match %s, %d, %1$s, %1$d, etc.
    matches = re.findall(r'%(?:(\d+)\$)?([dfsxo])', text)
    # Return normalized list
    return matches

# Process each language file pair
language_pairs = [
    ('actors/actors.properties', 'actors/actors_ko.properties', 'Actors'),
    ('items/items.properties', 'items/items_ko.properties', 'Items'),
    ('journal/journal.properties', 'journal/journal_ko.properties', 'Journal'),
    ('levels/levels.properties', 'levels/levels_ko.properties', 'Levels'),
    ('misc/misc.properties', 'misc/misc_ko.properties', 'Misc'),
    ('plants/plants.properties', 'plants/plants_ko.properties', 'Plants'),
    ('scenes/scenes.properties', 'scenes/scenes_ko.properties', 'Scenes'),
    ('ui/ui.properties', 'ui/ui_ko.properties', 'UI'),
    ('windows/windows.properties', 'windows/windows_ko.properties', 'Windows'),
]

total_issues = {}

for en_file, ko_file, category in language_pairs:
    if not os.path.exists(en_file) or not os.path.exists(ko_file):
        continue
    
    en_props = parse_properties_file(en_file)
    ko_props = parse_properties_file(ko_file)
    
    cat_issues = {}
    
    # 1. Missing keys
    missing_keys = set(en_props.keys()) - set(ko_props.keys())
    if missing_keys:
        cat_issues['missing_keys'] = sorted(list(missing_keys))
    
    # 2. Extra keys
    extra_keys = set(ko_props.keys()) - set(en_props.keys())
    if extra_keys:
        cat_issues['extra_keys'] = sorted(list(extra_keys))
    
    # 3. Format string mismatches
    format_issues = []
    for key in en_props.keys():
        if key in ko_props:
            en_fmt = extract_format_placeholders(en_props[key])
            ko_fmt = extract_format_placeholders(ko_props[key])
            if en_fmt != ko_fmt:
                format_issues.append({
                    'key': key,
                    'en_value': en_props[key],
                    'ko_value': ko_props[key],
                    'en_fmt': en_fmt,
                    'ko_fmt': ko_fmt
                })
    if format_issues:
        cat_issues['format_issues'] = format_issues
    
    # 4. Length ratio check (for UI/button text)
    length_issues = []
    button_keywords = ['button', 'btn', 'label', 'title', 'text', 'msg']
    for key in en_props.keys():
        if key in ko_props and any(kw in key.lower() for kw in button_keywords):
            en_len = len(en_props[key])
            ko_len = len(ko_props[key])
            # If Korean translation is > 150% of English
            if en_len > 5 and ko_len > en_len * 1.5:
                length_issues.append({
                    'key': key,
                    'en_text': en_props[key],
                    'ko_text': ko_props[key],
                    'en_len': en_len,
                    'ko_len': ko_len,
                    'ratio': round(ko_len / en_len, 2)
                })
    if length_issues:
        cat_issues['length_issues'] = length_issues
    
    if cat_issues:
        total_issues[category] = cat_issues

# Print detailed report
print("KOREAN TRANSLATION QUALITY REPORT")
print("=" * 80)

for category in sorted(total_issues.keys()):
    issues = total_issues[category]
    print(f"\n### {category} ###")
    
    if 'missing_keys' in issues:
        print(f"\n[1] MISSING KEYS ({len(issues['missing_keys'])})")
        for key in issues['missing_keys']:
            print(f"    - {key}")
    
    if 'extra_keys' in issues:
        print(f"\n[2] EXTRA KEYS ({len(issues['extra_keys'])})")
        for key in issues['extra_keys']:
            print(f"    - {key}")
    
    if 'format_issues' in issues:
        print(f"\n[3] FORMAT STRING MISMATCHES ({len(issues['format_issues'])})")
        for issue in issues['format_issues']:
            print(f"    - Key: {issue['key']}")
            print(f"      EN: {issue['en_fmt']} in '{issue['en_value']}'")
            print(f"      KO: {issue['ko_fmt']} in '{issue['ko_value']}'")
    
    if 'length_issues' in issues:
        print(f"\n[4] TEXT LENGTH EXCEEDS 150% ({len(issues['length_issues'])})")
        for issue in issues['length_issues']:
            print(f"    - Key: {issue['key']}")
            print(f"      EN: {issue['en_text']} ({issue['en_len']} chars)")
            print(f"      KO: {issue['ko_text']} ({issue['ko_len']} chars, {issue['ratio']}x)")

print("\n" + "=" * 80)
print("SUMMARY")
print("=" * 80)
total_missing = sum(len(issues.get('missing_keys', [])) for issues in total_issues.values())
total_extra = sum(len(issues.get('extra_keys', [])) for issues in total_issues.values())
total_format = sum(len(issues.get('format_issues', [])) for issues in total_issues.values())
total_length = sum(len(issues.get('length_issues', [])) for issues in total_issues.values())

print(f"Categories with issues: {len(total_issues)}")
print(f"Missing keys: {total_missing}")
print(f"Extra keys: {total_extra}")
print(f"Format mismatches: {total_format}")
print(f"Length issues (>150%): {total_length}")

