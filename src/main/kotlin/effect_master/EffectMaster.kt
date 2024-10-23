package effect_master

import effect_master.clients.EffectConfigMenu
import effect_master.commands.ListEffectsCommand
import effect_master.utils.EffectOptions
import effect_master.utils.EffectToggleState
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.living.MobEffectEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.registries.DeferredRegister
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist


/**
 * Main mod class. Should be an `object` declaration annotated with `@Mod`.
 * The modid should be declared in this object and should match the modId entry
 * in mods.toml.
 *
 * An example for blocks is in the `blocks` package of this mod.
 */
@Mod(EffectMaster.ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
object EffectMaster {
    const val ID = "effect_master"

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(ID)


    private val MENU_TYPES = DeferredRegister.create(Registries.MENU, ID)

    val EFFECT_CONFIG_MENU = MENU_TYPES.register("effect_config") {
        MenuType(::EffectConfigMenu, FeatureFlags.VANILLA_SET)
    }

    init {
        val modEventBus = MOD_BUS
        MENU_TYPES.register(modEventBus)
        EffectToggleState.loadConfig()

//        val obj = runForDist(
//            clientTarget = {
//                MOD_BUS.addListener(this::onClientSetup)
//                Minecraft.getInstance()
//            },
//            serverTarget = {
//                MOD_BUS.addListener(this::onServerSetup)
//            })
//
//        println(obj)
    }

    @SubscribeEvent
    fun onServerStarting(event: RegisterCommandsEvent) {
        ListEffectsCommand.register(event.dispatcher)
    }

    @Mod.EventBusSubscriber(modid = ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    object EventHandler {
        @SubscribeEvent (priority = EventPriority.HIGH)
        fun onEffectApplicable(event: MobEffectEvent.Applicable) {
            println("start onEffectApplicable")
            val entity = event.entity
            if (entity.level().isClientSide) return

            if (entity is ServerPlayer) {
                val effect = event.effectInstance.effect
                if (EffectToggleState.isEffectDisabled(effect)) {
                    event.result = Event.Result.DENY
                    println("Prevented application of disabled effect: ${effect.displayName} to player: ${entity.name}")
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
                    if (EffectToggleState.isEffectDisabled(effect.effect)) {
                        effectsToRemove.add(effect.effect)
                        println("Removed disabled effect: ${effect.effect.displayName} from player: ${player.name}")
                    }
                }
                effectsToRemove.forEach { effect: MobEffect ->
                    player.removeEffect(effect)
                    println("Removed disabled effect: ${BuiltInRegistries.MOB_EFFECT.getKey(effect)} from player: ${player.name} during tick")
                }
            }
        }

        @SubscribeEvent
        fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent){
            val player = event.entity
            if (player is ServerPlayer) {
                val effectsSettings = EffectToggleState.getAllEffectSettings()
                effectsSettings.forEach { effectSetting ->
                    if (effectSetting.value === EffectOptions.PERSISTENT){
                        val effect = BuiltInRegistries.MOB_EFFECT.get(effectSetting.key)
                        if(effect != null){
                            player.addEffect(MobEffectInstance(effect, Int.MAX_VALUE, 0, false, false))
                        }
                    }
                }
            }
        }
    }
}