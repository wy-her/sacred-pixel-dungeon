/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * DeathCauseRegistry - Maps death cause classes to compact byte IDs for URL export
 * Maximum 256 IDs (0-255) for single byte encoding
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for death cause ID mapping.
 * Used by WebDataExporter/Importer for compact serialization.
 *
 * ID ranges:
 *   0:      Unknown/null
 *   1-99:   Mobs (common)
 *   100-149: Mobs (bosses, special)
 *   150-179: Traps
 *   180-199: Buffs (damage over time)
 *   200-219: Blobs
 *   220-239: Features/Items
 *   240-255: Reserved
 */
public class DeathCauseRegistry {

    private static final Map<String, Integer> classToId = new HashMap<>();
    private static final Map<Integer, String> idToClass = new HashMap<>();

    static {
        // ========== MOBS (1-99, 100-149) ==========

        // Common mobs (1-49)
        register(1, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Rat");
        register(2, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Snake");
        register(3, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Gnoll");
        register(4, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Crab");
        register(5, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Swarm");
        register(6, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Slime");
        register(7, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Skeleton");
        register(8, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Thief");
        register(9, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Bandit");
        register(10, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Shaman");
        register(11, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Bat");
        register(12, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Brute");
        register(13, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Spinner");
        register(14, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Guard");
        register(15, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Warlock");
        register(16, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Monk");
        register(17, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Golem");
        register(18, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Elemental");
        register(19, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Eye");
        register(20, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Scorpio");
        register(21, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Succubus");
        register(22, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Wraith");
        register(23, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Piranha");
        register(24, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Mimic");
        register(25, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Statue");
        register(26, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Bee");
        register(27, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Ghoul");
        register(28, "com.sacredpixel.sacredpixeldungeon.actors.mobs.RipperDemon");
        register(29, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Necromancer");
        register(30, "com.sacredpixel.sacredpixeldungeon.actors.mobs.DemonSpawner");

        // DM series
        register(31, "com.sacredpixel.sacredpixeldungeon.actors.mobs.DM100");
        register(32, "com.sacredpixel.sacredpixeldungeon.actors.mobs.DM200");
        register(33, "com.sacredpixel.sacredpixeldungeon.actors.mobs.DM201");

        // Rare variants (35-49)
        register(35, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Albino");
        register(36, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Acidic");
        register(37, "com.sacredpixel.sacredpixeldungeon.actors.mobs.ArmoredBrute");
        register(38, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Senior");
        register(39, "com.sacredpixel.sacredpixeldungeon.actors.mobs.CausticSlime");
        register(40, "com.sacredpixel.sacredpixeldungeon.actors.mobs.SpectralNecromancer");
        register(41, "com.sacredpixel.sacredpixeldungeon.actors.mobs.PhantomPiranha");
        register(42, "com.sacredpixel.sacredpixeldungeon.actors.mobs.ArmoredStatue");
        register(43, "com.sacredpixel.sacredpixeldungeon.actors.mobs.GoldenMimic");
        register(44, "com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalMimic");
        register(45, "com.sacredpixel.sacredpixeldungeon.actors.mobs.EbonyMimic");
        register(46, "com.sacredpixel.sacredpixeldungeon.actors.mobs.TormentedSpirit");
        register(47, "com.sacredpixel.sacredpixeldungeon.actors.mobs.GreatCrab");
        register(48, "com.sacredpixel.sacredpixeldungeon.actors.mobs.HermitCrab");

        // Quest & special mobs (50-69)
        register(50, "com.sacredpixel.sacredpixeldungeon.actors.mobs.FetidRat");
        register(51, "com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollTrickster");
        register(52, "com.sacredpixel.sacredpixeldungeon.actors.mobs.RotLasher");
        register(53, "com.sacredpixel.sacredpixeldungeon.actors.mobs.RotHeart");
        register(54, "com.sacredpixel.sacredpixeldungeon.actors.mobs.FungalCore");
        register(55, "com.sacredpixel.sacredpixeldungeon.actors.mobs.FungalSpinner");
        register(56, "com.sacredpixel.sacredpixeldungeon.actors.mobs.FungalSentry");
        register(57, "com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalWisp");
        register(58, "com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalGuardian");
        register(59, "com.sacredpixel.sacredpixeldungeon.actors.mobs.CrystalSpire");

        // Gnoll variants (60-69)
        register(60, "com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollExile");
        register(61, "com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollGuard");
        register(62, "com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollSapper");
        register(63, "com.sacredpixel.sacredpixeldungeon.actors.mobs.GnollGeomancer");
        register(64, "com.sacredpixel.sacredpixeldungeon.actors.mobs.DelayedRockFall");

        // Vault mobs (70-79)
        register(70, "com.sacredpixel.sacredpixeldungeon.actors.mobs.VaultMob");
        register(71, "com.sacredpixel.sacredpixeldungeon.actors.mobs.VaultRat");
        register(72, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Pylon");

        // ========== BOSSES (100-149) ==========
        register(100, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Goo");
        register(101, "com.sacredpixel.sacredpixeldungeon.actors.mobs.Tengu");
        register(102, "com.sacredpixel.sacredpixeldungeon.actors.mobs.DM300");
        register(103, "com.sacredpixel.sacredpixeldungeon.actors.mobs.DwarfKing");
        register(104, "com.sacredpixel.sacredpixeldungeon.actors.mobs.YogDzewa");
        register(105, "com.sacredpixel.sacredpixeldungeon.actors.mobs.YogFist");

        // ========== TRAPS (150-179) ==========
        register(150, "com.sacredpixel.sacredpixeldungeon.levels.traps.GrimTrap");
        register(151, "com.sacredpixel.sacredpixeldungeon.levels.traps.ExplosiveTrap");
        register(152, "com.sacredpixel.sacredpixeldungeon.levels.traps.DisintegrationTrap");
        register(153, "com.sacredpixel.sacredpixeldungeon.levels.traps.BlazingTrap");
        register(154, "com.sacredpixel.sacredpixeldungeon.levels.traps.BurningTrap");
        register(155, "com.sacredpixel.sacredpixeldungeon.levels.traps.FrostTrap");
        register(156, "com.sacredpixel.sacredpixeldungeon.levels.traps.ChillingTrap");
        register(157, "com.sacredpixel.sacredpixeldungeon.levels.traps.ShockingTrap");
        register(158, "com.sacredpixel.sacredpixeldungeon.levels.traps.StormTrap");
        register(159, "com.sacredpixel.sacredpixeldungeon.levels.traps.ToxicTrap");
        register(160, "com.sacredpixel.sacredpixeldungeon.levels.traps.CorrosionTrap");
        register(161, "com.sacredpixel.sacredpixeldungeon.levels.traps.PoisonDartTrap");
        register(162, "com.sacredpixel.sacredpixeldungeon.levels.traps.WornDartTrap");
        register(163, "com.sacredpixel.sacredpixeldungeon.levels.traps.TenguDartTrap");
        register(164, "com.sacredpixel.sacredpixeldungeon.levels.traps.PitfallTrap");
        register(165, "com.sacredpixel.sacredpixeldungeon.levels.traps.RockfallTrap");
        register(166, "com.sacredpixel.sacredpixeldungeon.levels.traps.GnollRockfallTrap");
        register(167, "com.sacredpixel.sacredpixeldungeon.levels.traps.GrippingTrap");
        register(168, "com.sacredpixel.sacredpixeldungeon.levels.traps.OozeTrap");
        register(169, "com.sacredpixel.sacredpixeldungeon.levels.traps.GeyserTrap");
        register(170, "com.sacredpixel.sacredpixeldungeon.levels.traps.FlashingTrap");
        register(171, "com.sacredpixel.sacredpixeldungeon.levels.traps.GuardianTrap");
        register(172, "com.sacredpixel.sacredpixeldungeon.levels.traps.SummoningTrap");
        register(173, "com.sacredpixel.sacredpixeldungeon.levels.traps.DistortionTrap");
        register(174, "com.sacredpixel.sacredpixeldungeon.levels.traps.WarpingTrap");
        register(175, "com.sacredpixel.sacredpixeldungeon.levels.traps.TeleportationTrap");
        register(176, "com.sacredpixel.sacredpixeldungeon.levels.traps.WeakeningTrap");
        register(177, "com.sacredpixel.sacredpixeldungeon.levels.traps.CursingTrap");
        register(178, "com.sacredpixel.sacredpixeldungeon.levels.traps.AlarmTrap");
        register(179, "com.sacredpixel.sacredpixeldungeon.levels.traps.ConfusionTrap");

        // ========== BUFFS (180-199) ==========
        register(180, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Burning");
        register(181, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Poison");
        register(182, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Bleeding");
        register(183, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Hunger");
        register(184, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Corrosion");
        register(185, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Ooze");
        register(186, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Chill");
        register(187, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Frost");
        register(188, "com.sacredpixel.sacredpixeldungeon.actors.buffs.Doom");

        // ========== BLOBS (200-219) ==========
        register(200, "com.sacredpixel.sacredpixeldungeon.actors.blobs.ToxicGas");
        register(201, "com.sacredpixel.sacredpixeldungeon.actors.blobs.Electricity");
        register(202, "com.sacredpixel.sacredpixeldungeon.actors.blobs.Fire");
        register(203, "com.sacredpixel.sacredpixeldungeon.actors.blobs.CorrosiveGas");
        register(204, "com.sacredpixel.sacredpixeldungeon.actors.blobs.Inferno");
        register(205, "com.sacredpixel.sacredpixeldungeon.actors.blobs.Blizzard");
        register(206, "com.sacredpixel.sacredpixeldungeon.actors.blobs.Freezing");
        register(207, "com.sacredpixel.sacredpixeldungeon.actors.blobs.ParalyticGas");
        register(208, "com.sacredpixel.sacredpixeldungeon.actors.blobs.StormCloud");
        register(209, "com.sacredpixel.sacredpixeldungeon.actors.blobs.StenchGas");
        register(210, "com.sacredpixel.sacredpixeldungeon.actors.blobs.ConfusionGas");
        register(211, "com.sacredpixel.sacredpixeldungeon.actors.blobs.SacrificialFire");
        register(212, "com.sacredpixel.sacredpixeldungeon.actors.blobs.VaultFlameTraps");

        // ========== FEATURES & ITEMS (220-239) ==========
        register(220, "com.sacredpixel.sacredpixeldungeon.levels.features.Chasm");
        register(221, "com.sacredpixel.sacredpixeldungeon.items.bombs.Bomb");
        register(222, "com.sacredpixel.sacredpixeldungeon.items.weapon.enchantments.Grim");
        register(223, "com.sacredpixel.sacredpixeldungeon.items.armor.glyphs.Viscosity$DeferedDamage");
        register(224, "com.sacredpixel.sacredpixeldungeon.items.weapon.Weapon$Enchantment");
        register(225, "com.sacredpixel.sacredpixeldungeon.items.armor.Armor$Glyph");

        // ========== RESERVED (240-255) ==========
        // Reserved for future use
    }

    private static void register(int id, String className) {
        classToId.put(className, id);
        idToClass.put(id, className);
    }

    /**
     * Gets the compact ID for a death cause class.
     * @param className Full class name (e.g., "com.sacredpixel.sacredpixeldungeon.actors.mobs.Rat")
     * @return ID (1-255) or 0 if not found
     */
    public static int getId(String className) {
        if (className == null) return 0;
        Integer id = classToId.get(className);
        return id != null ? id : 0;
    }

    /**
     * Gets the class name for a compact ID.
     * @param id Compact ID (0-255)
     * @return Full class name or null if not found
     */
    public static String getClassName(int id) {
        return idToClass.get(id);
    }

    /**
     * Checks if a class name is registered.
     */
    public static boolean isRegistered(String className) {
        return classToId.containsKey(className);
    }

    /**
     * Gets total registered death causes.
     */
    public static int size() {
        return classToId.size();
    }
}
