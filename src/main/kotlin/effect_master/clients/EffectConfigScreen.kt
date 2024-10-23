package effect_master.clients

import effect_master.network.NetworkHandler
import effect_master.network.EffectSettingPacket
import effect_master.utils.EffectOptions
import effect_master.utils.EffectToggleState
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class EffectConfigScreen(parent: Screen?) : Screen(Component.literal("Effect Master")) {

    private val configBuilder: ConfigBuilder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.literal("Effect Master"))
        .setSavingRunnable(::saveConfig)

    init {
        rebuildConfig()
    }

    private fun rebuildConfig() {
        val effectsCategory: ConfigCategory = configBuilder.getOrCreateCategory(Component.translatable("effect_master.config.category.effects"))
        val entryBuilder: ConfigEntryBuilder = configBuilder.entryBuilder()
        val currentSettings = EffectToggleState.getAllEffectSettings()
        BuiltInRegistries.MOB_EFFECT.forEach { effect ->
            val effectId = BuiltInRegistries.MOB_EFFECT.getId(effect)
            val effectKey = BuiltInRegistries.MOB_EFFECT.getKey(effect)

            val currentSetting = currentSettings[effectKey] ?: EffectOptions.DEFAULT
            effectsCategory.addEntry(
                entryBuilder.startEnumSelector(
                    effect.displayName,
                    EffectOptions::class.java,
                    currentSetting
                )
                    .setTooltip(Component.literal(effectKey.toString()))
                    .setSaveConsumer { newValue ->
                        EffectToggleState.setEffectSetting(effect, newValue)
                        NetworkHandler.sendToServer(EffectSettingPacket(effectId, newValue))
                    }
                    .build()
            )
        }
    }

    override fun init() {
        super.init()
        val clothConfigScreen = configBuilder.build()
        this.minecraft?.setScreen(clothConfigScreen)
    }

    private fun saveConfig() {
        EffectToggleState.saveConfig()
    }

    companion object {
        fun create(parent: Screen?): Screen {
            return EffectConfigScreen(parent)
        }
    }
}