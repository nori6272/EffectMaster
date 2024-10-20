package example.examplemod

import example.examplemod.clients.EffectConfigMenu
import example.examplemod.block.ModBlocks
import example.examplemod.commands.ListEffectsCommand
import example.examplemod.utils.EffectToggleState
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.registries.DeferredRegister
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


/**
 * Main mod class. Should be an `object` declaration annotated with `@Mod`.
 * The modid should be declared in this object and should match the modId entry
 * in mods.toml.
 *
 * An example for blocks is in the `blocks` package of this mod.
 */
@Mod(ExampleMod.ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
object ExampleMod {
    const val ID = "examplemod"

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(ID)


    private val MENU_TYPES = DeferredRegister.create(Registries.MENU, "examplemod")

    val EFFECT_CONFIG_MENU = MENU_TYPES.register("effect_config") {
        MenuType(::EffectConfigMenu, FeatureFlags.VANILLA_SET)
    }

    init {
        LOGGER.log(Level.INFO, "Hello world!")
        val modEventBus = MOD_BUS
        MENU_TYPES.register(modEventBus)

        // Register the KDeferredRegister to the mod-specific event bus
        ModBlocks.REGISTRY.register(MOD_BUS)
        EffectToggleState.loadConfig()

        val obj = runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientSetup)
                Minecraft.getInstance()
            },
            serverTarget = {
                MOD_BUS.addListener(::onServerSetup)
                "test"
            })

        println(obj)
    }

    @SubscribeEvent
    fun onServerStarting(event: RegisterCommandsEvent) {
        ListEffectsCommand.register(event.dispatcher)
    }

    /**
     * This is used for initializing client specific
     * things such as renderers and keymaps
     * Fired on the mod specific event bus.
     */
    private fun onClientSetup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Initializing client...")
    }

    /**
     * Fired on the global Forge bus.
     */
    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
        LOGGER.log(Level.INFO, "Server starting...")
    }
}