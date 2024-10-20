package example.examplemod.clients

import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import example.examplemod.network.EffectTogglePacket
import example.examplemod.network.NetworkHandler
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.effect.MobEffect
import java.awt.Color
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.resources.ResourceLocation

@OnlyIn(Dist.CLIENT)
class EffectConfigScreen(parent: Screen?) : Screen(Component.translatable("examplemod.config.title")) {

    private val configBuilder: ConfigBuilder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.translatable("examplemod.config.title"))
        .setSavingRunnable(::saveConfig)

    init {
        val effectsCategory: ConfigCategory = configBuilder.getOrCreateCategory(Component.translatable("examplemod.config.category.effects"))
        val entryBuilder: ConfigEntryBuilder = configBuilder.entryBuilder()

        BuiltInRegistries.MOB_EFFECT.forEach { effect ->
            val effectId = BuiltInRegistries.MOB_EFFECT.getId(effect)
            val effectName = BuiltInRegistries.MOB_EFFECT.getKey(effect)?.toString() ?: "Unknown"

            effectsCategory.addEntry(
                entryBuilder.startBooleanToggle(Component.literal(effectName), false)
                    .setDefaultValue(false)
                    .setTooltip(Component.translatable("examplemod.config.effect.tooltip", effectName))
                    .setSaveConsumer { enabled ->
                        NetworkHandler.sendToServer(EffectTogglePacket(effectId, enabled))
                    }
                    .build()
            )
        }
    }

    override fun init() {
        super.init()
        // Cloth Configのスクリーンを初期化
        val clothConfigScreen = configBuilder.build()
        this.minecraft?.setScreen(clothConfigScreen)
    }

    private fun saveConfig() {
        // 設定の保存処理
        // 必要に応じてForgeの設定システムを使用して設定を保存
    }

    companion object {
        fun create(parent: Screen?): Screen {
            return EffectConfigScreen(parent)
        }
    }
}