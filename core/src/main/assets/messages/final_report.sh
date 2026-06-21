#!/bin/bash

echo "=== TRANSLATION QUALITY ISSUES ==="
echo ""

echo "1. Empty/untranslated values:"
echo "  - actors/actors_pl.properties: actors.buffs.buff.heromsg (empty)"
echo "  - items/items_pl.properties: items.weapon.melee.meleeweapon.stats_desc (empty)"
echo "  - items/items_pl.properties: items.weapon.missiles.missileweapon.stats_desc (empty)"
echo "  - scenes/scenes_pl.properties: scenes.aboutscene (commented out)"
echo ""

echo "2. Checking for potential mistranslations or inconsistencies:"
echo ""

echo "Sample: checking 'skill' terminology"
grep -i "skill\|umiejętnośc\|zdolnośc" actors/actors_pl.properties | head -5
echo ""

echo "Sample: Check game mechanic terms"
grep "damage\|obrażen" items/items_pl.properties | head -5
echo ""

echo "3. Format string verification (sampling):"
echo ""
echo "EN: Checking items with %d (damage numbers)"
grep -E "items.*%[0-9]*d" items/items.properties | head -2
echo ""
echo "PL: Corresponding Polish"
grep -E "items.*%[0-9]*d" items/items_pl.properties | head -2

