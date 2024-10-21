package example.examplemod

import example.examplemod.clients.EffectConfigMenu
import example.examplemod.block.ModBlocks
import example.examplemod.commands.ListEffectsCommand
import example.examplemod.utils.EffectToggleState
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.living.MobEffectEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
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

    @Mod.EventBusSubscriber(modid = ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    object EventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun onEffectApplicable(event: MobEffectEvent.Applicable) {
            val entity = event.entity
            if (entity.level().isClientSide) return

            if (entity is ServerPlayer) {
                val effect = event.effectInstance.effect
                val effectKey = BuiltInRegistries.MOB_EFFECT.getKey(effect)?.toString() ?: ""
                if (entity.persistentData.getBoolean("effect_disabled_$effectKey")) {
                    event.result = Event.Result.DENY
                    entity.removeEffect(effect)
                    println("Prevented application of disabled effect: $effectKey to player: ${entity.name}")
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun onEffectAdded(event: MobEffectEvent.Added) {
            val entity = event.entity
            println("um")
            if (entity is ServerPlayer) {
                val effect = event.effectInstance.effect
                val effectKey = BuiltInRegistries.MOB_EFFECT.getKey(effect)?.toString() ?: ""
                println("effect_disabled_$effectKey")
                if (entity.persistentData.getBoolean("effect_disabled_$effectKey")) {
                    // エフェクトを即座に削除
                    entity.removeEffect(effect)
                    println("Prevented application of disabled effect: $effectKey to player: ${entity.name}")
                }
            }
        }

        @SubscribeEvent
        fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
            if (event.phase != TickEvent.Phase.END) return
            val player = event.player
            if (player.level().isClientSide) return

            if (player is ServerPlayer) {
                val effectsToRemove = mutableListOf<MobEffect>()
                player.activeEffects.forEach { effect ->
                    val effectKey = BuiltInRegistries.MOB_EFFECT.getKey(effect.effect)?.toString() ?: return@forEach
                    if (player.persistentData.getBoolean("effect_disabled_$effectKey")) {
                        effectsToRemove.add(effect.effect)
                        println("Removed disabled effect: $effectKey from player: ${player.name}")
                    }
                }
                effectsToRemove.forEach { effect: MobEffect ->
                    player.removeEffect(effect)
                    println("Removed disabled effect: ${BuiltInRegistries.MOB_EFFECT.getKey(effect)} from player: ${player.name} during tick")
                }
            }
        }

        @SubscribeEvent
        fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
            val player = event.entity
            if (player !is ServerPlayer) return

            val compound = player.persistentData.getCompound(ID)
            println("Loaded effect settings for player: ${player.name}")
            compound.allKeys.forEach { key ->
                if (key.startsWith("effect_disabled_")) {
                    val effectId = key.substringAfter("effect_disabled_")
                    val effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation(effectId))
                    if (effect != null && player.hasEffect(effect)) {
                        player.removeEffect(effect)
                        println("Removed disabled effect: $effectId from player: ${player.name} on login")
                    }
                }
            }
        }

        @SubscribeEvent
        fun onPlayerLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
            val player = event.entity
            if (player is ServerPlayer) {
                // 設定を保存
                val compound = player.persistentData.getCompound(ID)
                player.persistentData.put(ID, compound)
                println("Saved effect settings for player: ${player.name}")
            }
        }
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