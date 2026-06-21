/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * ItemRegistry - Maps item class names to compact IDs for URL export
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping item class names to compact byte IDs.
 * Used in URL export to minimize data size.
 *
 * ID Ranges:
 *   0: Unknown/null
 *   1-49: Melee Weapons
 *   50-69: Missile Weapons
 *   70-89: Armor
 *   90-119: Wands
 *   120-149: Rings
 *   150-179: Artifacts
 *   180-199: Potions
 *   200-219: Scrolls
 *   220-239: Seeds/Plants
 *   240-254: Misc
 *   255: Empty slot
 */
public class ItemRegistry {

    private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();
    private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();

    static {
        // Melee Weapons (1-49)
        register(1, "WornShortsword");
        register(2, "Gloves");
        register(3, "Dagger");
        register(4, "MagesStaff");
        register(5, "Rapier");
        register(6, "Shortsword");
        register(7, "HandAxe");
        register(8, "Spear");
        register(9, "Quarterstaff");
        register(10, "Dirk");
        register(11, "Sword");
        register(12, "Mace");
        register(13, "Scimitar");
        register(14, "RoundShield");
        register(15, "Sai");
        register(16, "Whip");
        register(17, "Longsword");
        register(18, "BattleAxe");
        register(19, "Flail");
        register(20, "RunicBlade");
        register(21, "AssassinsBlade");
        register(22, "Crossbow");
        register(23, "Katana");
        register(24, "Greatsword");
        register(25, "WarHammer");
        register(26, "Glaive");
        register(27, "Greataxe");
        register(28, "Greatshield");
        register(29, "Gauntlet");
        register(30, "WarScythe");

        // Armor (70-89)
        register(70, "ClothArmor");
        register(71, "LeatherArmor");
        register(72, "MailArmor");
        register(73, "ScaleArmor");
        register(74, "PlateArmor");
        register(75, "WarriorArmor");
        register(76, "MageArmor");
        register(77, "RogueArmor");
        register(78, "HuntressArmor");
        register(79, "DuelistArmor");
        register(80, "ClericArmor");

        // Wands (90-119)
        register(90, "WandOfMagicMissile");
        register(91, "WandOfLightning");
        register(92, "WandOfDisintegration");
        register(93, "WandOfFireblast");
        register(94, "WandOfCorrosion");
        register(95, "WandOfBlastWave");
        register(96, "WandOfLivingEarth");
        register(97, "WandOfFrost");
        register(98, "WandOfPrismaticLight");
        register(99, "WandOfWarding");
        register(100, "WandOfTransfusion");
        register(101, "WandOfCorruption");
        register(102, "WandOfRegrowth");

        // Rings (120-149)
        register(120, "RingOfAccuracy");
        register(121, "RingOfArcana");
        register(122, "RingOfElements");
        register(123, "RingOfEnergy");
        register(124, "RingOfEvasion");
        register(125, "RingOfForce");
        register(126, "RingOfFuror");
        register(127, "RingOfHaste");
        register(128, "RingOfMight");
        register(129, "RingOfSharpshooting");
        register(130, "RingOfTenacity");
        register(131, "RingOfWealth");

        // Artifacts (150-179)
        register(150, "AlchemistsToolkit");
        register(151, "CapeOfThorns");
        register(152, "ChaliceOfBlood");
        register(153, "CloakOfShadows");
        register(154, "DriedRose");
        register(155, "EtherealChains");
        register(156, "HornOfPlenty");
        register(157, "MasterThievesArmband");
        register(158, "SandalsOfNature");
        register(159, "TalismanOfForesight");
        register(160, "TimekeepersHourglass");
        register(161, "UnstableSpellbook");
        register(162, "KingsCrown");

        // Potions (180-199)
        register(180, "PotionOfHealing");
        register(181, "PotionOfStrength");
        register(182, "PotionOfLiquidFlame");
        register(183, "PotionOfFrost");
        register(184, "PotionOfToxicGas");
        register(185, "PotionOfParalyticGas");
        register(186, "PotionOfPurity");
        register(187, "PotionOfLevitation");
        register(188, "PotionOfMindVision");
        register(189, "PotionOfInvisibility");
        register(190, "PotionOfExperience");
        register(191, "PotionOfHaste");
        register(192, "PotionOfDivineInspiration");

        // Scrolls (200-219)
        register(200, "ScrollOfIdentify");
        register(201, "ScrollOfUpgrade");
        register(202, "ScrollOfRemoveCurse");
        register(203, "ScrollOfMagicMapping");
        register(204, "ScrollOfTeleportation");
        register(205, "ScrollOfRecharging");
        register(206, "ScrollOfMirrorImage");
        register(207, "ScrollOfTerror");
        register(208, "ScrollOfLullaby");
        register(209, "ScrollOfRage");
        register(210, "ScrollOfRetribution");
        register(211, "ScrollOfTransmutation");
        register(212, "ScrollOfDread");
        register(213, "ScrollOfAntiMagic");

        // Seeds (220-239)
        register(220, "Firebloom");
        register(221, "Icecap");
        register(222, "Sorrowmoss");
        register(223, "Blindweed");
        register(224, "Sungrass");
        register(225, "Earthroot");
        register(226, "Fadeleaf");
        register(227, "Rotberry");
        register(228, "Swiftthistle");
        register(229, "Stormvine");
        register(230, "Dreamfoil");
        register(231, "Starflower");
        register(232, "Mageroyal");

        // Trinkets (240-254)
        register(240, "RatSkull");
        register(241, "ParchmentScrap");
        register(242, "PetrifiedSeed");
        register(243, "ExoticCrystals");
        register(244, "MossyClump");
        register(245, "MimicTooth");
        register(246, "DimensionalSundial");
        register(247, "ThirteenLeafClover");
        register(248, "TrapMechanism");
        register(249, "WondrousResin");
        register(250, "EyeOfNewt");
        register(251, "SaltCube");
        register(252, "ChaosChampionMedal");
        register(253, "SandsOfTime");
    }

    private static void register(int id, String simpleName) {
        NAME_TO_ID.put(simpleName, id);
        ID_TO_NAME.put(id, simpleName);
    }

    /**
     * Gets the ID for an item class name.
     * @param className Full class name or simple name
     * @return ID (0-254), or 0 for unknown
     */
    public static int getId(String className) {
        if (className == null || className.isEmpty()) {
            return 0;
        }

        // Extract simple name from full class name
        String simpleName = className;
        int lastDot = className.lastIndexOf('.');
        if (lastDot >= 0) {
            simpleName = className.substring(lastDot + 1);
        }
        int lastDollar = simpleName.lastIndexOf('$');
        if (lastDollar >= 0) {
            simpleName = simpleName.substring(lastDollar + 1);
        }

        Integer id = NAME_TO_ID.get(simpleName);
        return id != null ? id : 0;
    }

    /**
     * Gets the simple class name for an item ID.
     * @param id Item ID
     * @return Simple class name, or null for unknown
     */
    public static String getName(int id) {
        return ID_TO_NAME.get(id);
    }
}
