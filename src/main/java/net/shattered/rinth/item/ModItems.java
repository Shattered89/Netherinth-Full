package net.shattered.rinth.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.shattered.rinth.Netherinth;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shattered.rinth.entity.CustomTridentEntity;
import net.shattered.rinth.item.custom.ChiselItem;
import net.shattered.rinth.item.custom.CustomTridentItem;
import net.shattered.rinth.item.custom.HammerItem;

import static net.shattered.rinth.Netherinth.MOD_ID;

public class ModItems {
    public static final Item PINK_GARNET = registerItem("pink_garnet", new Item(new Item.Settings()));
    public static final Item RAW_PINK_GARNET = registerItem("raw_pink_garnet", new Item(new Item.Settings()));

    public static final Item CHISEL = registerItem("chisel", new ChiselItem(new Item.Settings().maxDamage(32)));

    public static final Item LAVA_NUGGET = registerItem("lava_nugget", new Item(new Item.Settings().food(ModFoodComponents.LAVANUGGET)));

    public static final Item STARLIGHT_ASHES = registerItem("starlight_ashes", new Item(new Item.Settings()));

    public static final Item PINK_GARNET_SWORD = registerItem("pink_garnet_sword",
            new SwordItem(ModToolMaterials.PINK_GARNET, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.PINK_GARNET, 3, -2.4F))));
    public static final Item PINK_GARNET_PICKAXE = registerItem("pink_garnet_pickaxe",
            new PickaxeItem(ModToolMaterials.PINK_GARNET, new Item.Settings()
                    .attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.PINK_GARNET, 3, -2.8F))));
    public static final Item PINK_GARNET_SHOVEL = registerItem("pink_garnet_shovel",
            new ShovelItem(ModToolMaterials.PINK_GARNET, new Item.Settings()
                    .attributeModifiers(ShovelItem.createAttributeModifiers(ModToolMaterials.PINK_GARNET, 1.5f, -3.0F))));
    public static final Item PINK_GARNET_AXE = registerItem("pink_garnet_axe",
            new AxeItem(ModToolMaterials.PINK_GARNET, new Item.Settings()
                    .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.PINK_GARNET, 6, -3.2F))));
    public static final Item PINK_GARNET_HOE = registerItem("pink_garnet_hoe",
            new HoeItem(ModToolMaterials.PINK_GARNET, new Item.Settings()
                    .attributeModifiers(HoeItem.createAttributeModifiers(ModToolMaterials.PINK_GARNET, 0, -3F))));

    public static final Item HELLFIRE_SWORD = registerItem("hellfire_sword",
            new SwordItem(ModToolMaterials.HELLFIRE, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.HELLFIRE, 3, -2.4F))));
    public static final Item HELLFIRE_AXE = registerItem("hellfire_axe", new AxeItem(ModToolMaterials.HELLFIRE, new Item.Settings()
            .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.HELLFIRE, 6, -3.2F))));

    public static final Item QUEEN_SWORD = registerItem("queen_sword",
            new SwordItem(ModToolMaterials.HELLFIRE, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.HELLFIRE, 3, -2.4F))));
    public static final Item QUEEN_AXE = registerItem("queen_axe", new AxeItem(ModToolMaterials.HELLFIRE, new Item.Settings()
            .attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.HELLFIRE, 6, -3.2F))));

    public static final Item PINK_GARNET_HAMMER = registerItem("pink_garnet_hammer",
            new HammerItem(ModToolMaterials.PINK_GARNET, new Item.Settings()
                    .attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.PINK_GARNET, 7, -3.4F))));

    public static final Item PITCHFORK = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "pitch_fork"),
            new CustomTridentItem(new Item.Settings()
                    .maxCount(1)
                    .maxDamage(250)
                    .attributeModifiers(TridentItem.createAttributeModifiers())
                    .component(DataComponentTypes.TOOL, TridentItem.createToolComponent())
                    .fireproof()));

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries ->
                fabricItemGroupEntries.addAfter(Items.TRIDENT, PITCHFORK)
        );
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    public static void registerModItems() {
        Netherinth.LOGGER.info("Registering mod items" + MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(PINK_GARNET);
            entries.add(RAW_PINK_GARNET);
        });
    }
}
