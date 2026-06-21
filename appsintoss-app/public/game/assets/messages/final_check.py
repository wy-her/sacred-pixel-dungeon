#!/usr/bin/env python3
import re

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

# Comprehensive check of all files
files_to_check = [
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

all_findings = {}

for en_file, ko_file, category in files_to_check:
    try:
        en_props = parse_properties_file(en_file)
        ko_props = parse_properties_file(ko_file)
        
        findings = {}
        
        # 1. Missing keys (highest priority)
        missing = sorted(list(set(en_props.keys()) - set(ko_props.keys())))
        if missing:
            findings['missing'] = missing
        
        # 2. Empty values
        empty = []
        for key in ko_props:
            if not ko_props[key].strip():
                empty.append(key)
        if empty:
            findings['empty'] = empty
        
        # 3. Format mismatch
        format_mismatch = []
        for key in en_props:
            if key in ko_props:
                en_val = en_props[key]
                ko_val = ko_props[key]
                # Count format specs
                en_specs = len(re.findall(r'%(?:\d+\$)?[dfsxo]', en_val))
                ko_specs = len(re.findall(r'%(?:\d+\$)?[dfsxo]', ko_val))
                if en_specs != ko_specs:
                    format_mismatch.append((key, en_specs, ko_specs, en_val, ko_val))
        if format_mismatch:
            findings['format_mismatch'] = format_mismatch
        
        if findings:
            all_findings[category] = findings
    
    except Exception as e:
        print(f"Error with {category}: {e}")

# Print report
print("\nKOREAN TRANSLATION ISSUE REPORT")
print("=" * 100)

for category in sorted(all_findings.keys()):
    findings = all_findings[category]
    print(f"\n[{category}]")
    
    if 'missing' in findings:
        print(f"  Issue 1: MISSING KEYS ({len(findings['missing'])})")
        for key in findings['missing'][:10]:
            print(f"    • {key}")
        if len(findings['missing']) > 10:
            print(f"    ... and {len(findings['missing']) - 10} more")
    
    if 'empty' in findings:
        print(f"  Issue 2: EMPTY TRANSLATIONS ({len(findings['empty'])})")
        for key in findings['empty'][:10]:
            print(f"    • {key}")
        if len(findings['empty']) > 10:
            print(f"    ... and {len(findings['empty']) - 10} more")
    
    if 'format_mismatch' in findings:
        print(f"  Issue 3: FORMAT STRING MISMATCHES ({len(findings['format_mismatch'])})")
        for key, en_specs, ko_specs, en_val, ko_val in findings['format_mismatch'][:5]:
            print(f"    • {key}")
            print(f"      EN ({en_specs} specs): {en_val}")
            print(f"      KO ({ko_specs} specs): {ko_val}")
        if len(findings['format_mismatch']) > 5:
            print(f"    ... and {len(findings['format_mismatch']) - 5} more")

print("\n" + "=" * 100)
print("TOTAL ISSUES FOUND:")
for category in sorted(all_findings.keys()):
    findings = all_findings[category]
    count = len(findings.get('missing', [])) + len(findings.get('empty', [])) + len(findings.get('format_mismatch', []))
    print(f"  {category}: {count} issues")

