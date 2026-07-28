package swapper.swappermod.menu.screen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import swapper.swappermod.menu.ModMenuTypes;
import swapper.swappermod.menu.screen.block.SwapperBlockScreen;

public class ModScreens implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.SWAPPER_BLOCK, SwapperBlockScreen::new);
    }
}
