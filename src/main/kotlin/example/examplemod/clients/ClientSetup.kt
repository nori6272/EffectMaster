package example.examplemod.clients

import example.examplemod.ExampleMod
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent


@Mod.EventBusSubscriber(modid = ExampleMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object ClientSetup {
    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            MenuScreens.register(ExampleMod.EFFECT_CONFIG_MENU.get()) { menu, inventory, title ->
                EffectConfigScreen(menu, inventory, title)
            }
        }
    }
}