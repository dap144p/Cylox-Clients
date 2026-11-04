package dev.cylox.client;

import dev.cylox.client.gui.GuiCyloxMainMenu;
import dev.cylox.client.module.ModuleManager;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = CyloxClient.MODID, name = CyloxClient.NAME, version = CyloxClient.VERSION,
        acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class CyloxClient {
    public static final String MODID = "cyloxclient";
    public static final String NAME = "Cylox Client";
    public static final String VERSION = "0.1.0-alpha";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModuleManager.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui != null && event.gui.getClass() == GuiMainMenu.class) {
            event.gui = new GuiCyloxMainMenu();
        }
    }
}
