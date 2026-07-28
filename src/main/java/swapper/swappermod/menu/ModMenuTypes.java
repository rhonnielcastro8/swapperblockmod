package swapper.swappermod.menu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import swapper.swappermod.SwapperMod;
import swapper.swappermod.menu.block.SwapperBlockMenu;

public class ModMenuTypes {
    public static final MenuType<SwapperBlockMenu> SWAPPER_BLOCK = register("swapper", SwapperBlockMenu::new);

    public static <T extends AbstractContainerMenu> MenuType<T> register(
            String name,
            MenuType.MenuSupplier<T> constructor
    ) {
        return Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(SwapperMod.MOD_ID, name), new MenuType<>(constructor, FeatureFlagSet.of()));
    }
    public static void initialize() {
    }
}
