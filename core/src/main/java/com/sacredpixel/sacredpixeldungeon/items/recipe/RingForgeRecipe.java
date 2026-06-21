package com.sacredpixel.sacredpixeldungeon.items.recipe;

import com.sacredpixel.sacredpixeldungeon.items.Item;
import com.sacredpixel.sacredpixeldungeon.items.Recipe;
import com.sacredpixel.sacredpixeldungeon.items.rings.Ring;
import com.watabou.utils.Reflection;
import java.util.ArrayList;

public class RingForgeRecipe extends Recipe {

    @Override
    public boolean testIngredients(ArrayList<Item> ingredients) {
        // Only rings allowed, all must be same type
        if (ingredients.size() < 2 || ingredients.size() > 3) return false;

        Class<?> ringType = null;
        int ringCount = 0;

        for (Item item : ingredients) {
            if (!(item instanceof Ring)) return false;
            if (ringType == null) {
                ringType = item.getClass();
            } else if (item.getClass() != ringType) {
                return false; // Different ring types
            }
            ringCount++;
        }

        return ringCount == 2 || ringCount == 3;
    }

    @Override
    public int cost(ArrayList<Item> ingredients) {
        return 0; // No energy cost
    }

    @Override
    public Item brew(ArrayList<Item> ingredients) {
        Class<?> ringType = ingredients.get(0).getClass();

        int totalLevel = 0;
        for (Item item : ingredients) {
            totalLevel += item.level();
        }
        int resultLevel = totalLevel + 1; // forge bonus

        Ring result = (Ring) Reflection.newInstance(ringType);
        result.cursed = false;
        result.cursedKnown = true;

        for (int i = 0; i < resultLevel; i++) {
            result.upgrade();
        }

        boolean allIdentified = true;
        for (Item item : ingredients) {
            if (!item.isIdentified()) {
                allIdentified = false;
                break;
            }
        }
        if (allIdentified) {
            result.identify();
        }

        // Consume ingredients
        for (Item item : ingredients) {
            item.quantity(item.quantity() - 1);
        }

        return result;
    }

    @Override
    public Item sampleOutput(ArrayList<Item> ingredients) {
        if (!testIngredients(ingredients)) return null;

        Class<?> ringType = ingredients.get(0).getClass();

        int totalLevel = 0;
        for (Item item : ingredients) {
            totalLevel += item.level();
        }
        int resultLevel = totalLevel + 1; // forge bonus

        Ring sample = (Ring) Reflection.newInstance(ringType);
        sample.cursed = false;

        for (int i = 0; i < resultLevel; i++) {
            sample.upgrade();
        }

        boolean allIdentified = true;
        for (Item item : ingredients) {
            if (!item.isIdentified()) {
                allIdentified = false;
                break;
            }
        }
        if (allIdentified) {
            sample.identify();
        }
        return sample;
    }
}
