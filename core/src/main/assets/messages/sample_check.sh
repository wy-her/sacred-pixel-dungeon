#!/bin/bash

echo "=== CHECKING SPECIFIC TRANSLATIONS FOR QUALITY ==="
echo ""

# Check some common terms to see if they're translated consistently
echo "[CONSISTENCY CHECK]"
echo ""

echo "1. 'hero' translations:"
grep "hero" actors/actors_pl.properties | head -5
echo ""

echo "2. 'dungeon' translations:"
grep -i "dungeon" levels/levels_pl.properties | head -5
echo ""

echo "3. Common item terms:"
grep "scroll" items/items_pl.properties | head -3
echo ""

echo "4. Button/action terms:"
grep -E "(button|action|accept|cancel)" ui/ui_pl.properties | head -5

