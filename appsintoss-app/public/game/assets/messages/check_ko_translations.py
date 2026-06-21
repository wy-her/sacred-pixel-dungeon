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
    """Extract format placeholders like %s, %d, %1$s, etc."""
    placeholders = re.findall(r'%(?:\d+\$)?[dfsxo]', text)
    return sorted(placeholders)

def get_text_length_in_em(text):
    """Rough estimation of button text length."""
    # This is simplified - just counting characters
    # In reality would need font metrics
    return len(text)

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

all_issues = {}

for en_file, ko_file, category in language_pairs:
    if not os.path.exists(en_file) or not os.path.exists(ko_file):
        print(f"Skipping {category}: files not found")
        continue
    
    print(f"\n=== {category} ===")
    issues = []
    
    en_props = parse_properties_file(en_file)
    ko_props = parse_properties_file(ko_file)
    
    # Check for missing keys
    missing_keys = set(en_props.keys()) - set(ko_props.keys())
    if missing_keys:
        issues.append(f"Missing {len(missing_keys)} keys in Korean file")
        for key in sorted(missing_keys)[:10]:  # Show first 10
            issues.append(f"  - {key}")
        if len(missing_keys) > 10:
            issues.append(f"  ... and {len(missing_keys) - 10} more")
    
    # Check for extra keys
    extra_keys = set(ko_props.keys()) - set(en_props.keys())
    if extra_keys:
        issues.append(f"Extra {len(extra_keys)} keys in Korean file (not in English)")
        for key in sorted(extra_keys)[:5]:
            issues.append(f"  - {key}")
    
    # Check format string mismatches
    format_issues = []
    for key in en_props.keys():
        if key in ko_props:
            en_fmt = extract_format_placeholders(en_props[key])
            ko_fmt = extract_format_placeholders(ko_props[key])
            if en_fmt != ko_fmt:
                format_issues.append((key, en_fmt, ko_fmt))
    
    if format_issues:
        issues.append(f"\nFormat string mismatches ({len(format_issues)} keys):")
        for key, en_fmt, ko_fmt in format_issues[:10]:
            issues.append(f"  - {key}")
            issues.append(f"    EN: {en_fmt}")
            issues.append(f"    KO: {ko_fmt}")
        if len(format_issues) > 10:
            issues.append(f"  ... and {len(format_issues) - 10} more")
    
    # Check length issues (very rough - button text longer than 150%)
    length_issues = []
    for key in en_props.keys():
        if key in ko_props:
            en_len = len(en_props[key])
            ko_len = len(ko_props[key])
            # Very rough heuristic: if Korean is >1.5x English length
            # (Korean chars can be wider, but this is a simple check)
            if en_len > 10 and ko_len > en_len * 1.5:  # Arbitrary thresholds
                length_issues.append((key, en_len, ko_len))
    
    if length_issues:
        issues.append(f"\nPotential length issues ({len(length_issues)} keys):")
        for key, en_len, ko_len in length_issues[:10]:
            issues.append(f"  - {key}: EN({en_len}) vs KO({ko_len})")
        if len(length_issues) > 10:
            issues.append(f"  ... and {len(length_issues) - 10} more")
    
    if issues:
        all_issues[category] = issues
        for issue in issues:
            print(issue)
    else:
        print("No issues found")

print("\n" + "="*60)
print("SUMMARY")
print("="*60)
for category in all_issues:
    print(f"{category}: Issues found")

