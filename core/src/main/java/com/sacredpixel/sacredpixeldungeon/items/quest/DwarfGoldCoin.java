package com.sacredpixel.sacredpixeldungeon.items.quest;

import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.sprites.ItemSpriteSheet;

public class DwarfGoldCoin extends Item {
    {
        image = ItemSpriteSheet.GOLD;
        stackable = true;
        defaultAction = null;
    }

    @Override
    public boolean isUpgradable() { return false; }

    @Override
    public boolean isIdentified() { return true; }

    @Override
    public int value() { return 0; }
}
