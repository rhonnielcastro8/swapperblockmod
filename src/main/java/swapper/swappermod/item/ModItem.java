package swapper.swappermod.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
//import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
//import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
//import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import swapper.swappermod.SwapperMod;

import java.util.function.Function;

public class ModItem {
    public static final Item SWAPPER = registerItem("swapper", Item::new);

    private static Item registerItem(String name,  Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(SwapperMod.MOD_ID, name), function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SwapperMod.MOD_ID, name)))));
    }

    public static void registerModItems() {
        SwapperMod.LOGGER.info("Register Mod Items for"+ SwapperMod.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            output.accept(SWAPPER);
        });
    }
}
