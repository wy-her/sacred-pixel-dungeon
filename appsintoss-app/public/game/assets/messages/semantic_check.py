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

# Check for common issues
files_to_check = [
    ('actors/actors.properties', 'actors/actors_ko.properties'),
    ('items/items.properties', 'items/items_ko.properties'),
    ('ui/ui.properties', 'ui/ui_ko.properties'),
    ('windows/windows.properties', 'windows/windows_ko.properties'),
]

print("CHECKING FOR POTENTIAL TRANSLATION ISSUES\n")
print("=" * 80)

for en_file, ko_file in files_to_check:
    try:
        en_props = parse_properties_file(en_file)
        ko_props = parse_properties_file(ko_file)
        
        print(f"\nFile: {ko_file}")
        print("-" * 80)
        
        # Check 1: Keys with only punctuation changes
        potential_untranslated = []
        for key in ko_props:
            if key in en_props:
                en_val = en_props[key]
                ko_val = ko_props[key]
                # Remove spaces and punctuation to see if it's the same
                en_clean = re.sub(r'[^\w]', '', en_val.lower())
                ko_clean = re.sub(r'[^\w]', '', ko_val.lower())
                if en_clean and ko_clean and en_clean == ko_clean:
                    potential_untranslated.append((key, en_val, ko_val))
        
        if potential_untranslated:
            print(f"  [Potential untranslated - identical after cleaning] ({len(potential_untranslated)})")
            for key, en_val, ko_val in potential_untranslated[:3]:
                print(f"    - {key}")
                print(f"      EN: {en_val}")
                print(f"      KO: {ko_val}")
            if len(potential_untranslated) > 3:
                print(f"    ... and {len(potential_untranslated) - 3} more")
        
        # Check 2: Empty translations
        empty_translations = []
        for key in ko_props:
            if key in en_props and ko_props[key].strip() == '':
                empty_translations.append((key, en_props[key]))
        
        if empty_translations:
            print(f"  [Empty translations] ({len(empty_translations)})")
            for key, en_val in empty_translations:
                print(f"    - {key}: '{en_val}'")
        
        # Check 3: Suspicious characters (encoding issues)
        suspicious = []
        for key in ko_props:
            if key in en_props:
                ko_val = ko_props[key]
                # Check for mojibake-like patterns or weird characters
                if '???' in ko_val or '\u0000' in ko_val or '\ufffd' in ko_val:
                    suspicious.append((key, ko_val))
        
        if suspicious:
            print(f"  [Suspicious characters/encoding] ({len(suspicious)})")
            for key, val in suspicious[:3]:
                print(f"    - {key}: {repr(val)}")
        
        print("  ✓ No semantic issues detected" if not (potential_untranslated or empty_translations or suspicious) else "")
        
    except Exception as e:
        print(f"Error processing {ko_file}: {e}")

print("\n" + "=" * 80)

