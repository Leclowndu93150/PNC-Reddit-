package me.desht.pneumaticcraft.common.upgrades;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.world.item.Rarity;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public enum BuiltinUpgrade {
    VOLUME("volume"),
    DISPENSER("dispenser"),
    ITEM_LIFE("item_life"),
    ENTITY_TRACKER("entity_tracker"),
    BLOCK_TRACKER("block_tracker"),
    SPEED("speed"),
    SEARCH("search"),
    COORDINATE_TRACKER("coordinate_tracker"),
    RANGE("range"),
    SECURITY("security"),
    MAGNET("magnet"),
    THAUMCRAFT("thaumcraft", 1, ModIds.THAUMCRAFT), /*Only around when Thaumcraft is */
    CHARGING("charging"),
    ARMOR("armor"),
    JET_BOOTS("jet_boots", 5),
    NIGHT_VISION("night_vision"),
    SCUBA("scuba"),
    CREATIVE("creative"),
    AIR_CONDITIONING("air_conditioning", 1, ModIds.TOUGH_AS_NAILS),
    INVENTORY("inventory"),
    JUMPING("jumping", 4),
    FLIPPERS("flippers"),
    STANDBY("standby"),
    MINIGUN("minigun"),
    RADIATION_SHIELDING("radiation_shielding", 1, ModIds.MEKANISM),
    GILDED("gilded"),
    ENDER_VISOR("ender_visor"),
    STOMP("stomp"),
    ELYTRA("elytra"),
    CHUNKLOADER("chunkloader"),
    MUFFLER("muffler");

    private final String name;
    private final int maxTier;
    private final String[] depModIds;

    BuiltinUpgrade(String name) {
        this(name, 1);
    }

    BuiltinUpgrade(String name, int maxTier, String... depModIds) {
        this.name = name;
        this.maxTier = maxTier;
        this.depModIds = depModIds;
    }

    public String getName() {
        return name;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public Rarity getRarity() {
        return this == CREATIVE ? Rarity.EPIC : Rarity.COMMON;
    }

    public PNCUpgrade registerUpgrade() {
        PNCUpgrade upgrade = PneumaticRegistry.getInstance().getUpgradeRegistry()
                .registerUpgrade(RL(name), maxTier, depModIds);
        return ModUpgrades.registerBuiltin(this, upgrade);
    }
}
