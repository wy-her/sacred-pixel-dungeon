#!/bin/bash

echo "=== CHECKING FOR ENCODING/TYPO ISSUES ==="
echo ""

# Check for common Polish character patterns
echo "[Polish Special Characters Check]"
echo ""

echo "Files with proper Polish diacritics (ą, ć, ę, ł, ń, ó, ś, ź, ż):"
for file in */*_pl.properties; do
    if grep -q '[ąćęłńóśźż]' "$file"; then
        count=$(grep -o '[ąćęłńóśźż]' "$file" | wc -l)
        echo "  $file: $count characters"
    fi
done

echo ""
echo "[Empty value check - keys with no translation]:"
for file in */*_pl.properties; do
    empty=$(grep '=$' "$file" | wc -l)
    if [ $empty -gt 0 ]; then
        echo "  $file: $empty empty values"
        grep '=$' "$file" | head -3
    fi
done

echo ""
echo "[Newline handling check - multiline translations]:"
grep '\n' actors/actors_pl.properties | head -3
echo ""
grep '\n' windows/windows_pl.properties | head -3

