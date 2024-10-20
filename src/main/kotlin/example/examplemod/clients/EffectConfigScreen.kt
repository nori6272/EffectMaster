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

@OnlyIn(Dist.CLIENT)
class EffectConfigScreen(
    menu: EffectConfigMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<EffectConfigMenu>(menu, playerInventory, title) {

    private var configScreen: Screen? = null

    override fun init() {
        super.init()

        val builder = ConfigBuilder.create()
            .setParentScreen(this)
            .setTitle(title)

        val category = builder.getOrCreateCategory(Component.literal("エフェクト"))

        BuiltInRegistries.MOB_EFFECT.forEach { effect ->
            val effectId = BuiltInRegistries.MOB_EFFECT.getId(effect)
            val name = BuiltInRegistries.MOB_EFFECT.getKey(effect)?.toString() ?: "Unknown"

            category.addEntry(
                builder.entryBuilder()
                    .startBooleanToggle(Component.literal(name), false)
                    .setDefaultValue(false)
                    .setSaveConsumer { enabled ->
                        NetworkHandler.sendToServer(EffectTogglePacket(effectId, enabled))
                    }
                    .build()
            )
        }

        // 次のティックでCloth Configの画面を設定
        minecraft?.tell {
            configScreen = builder.build()
            this@EffectConfigScreen.init() // Cloth Configの画面を初期化
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        configScreen?.render(guiGraphics, mouseX, mouseY, partialTick) ?: run {
            // configScreenがnullの場合、ローディングメッセージを表示
            val loadingText = Component.literal("Loading...")
            guiGraphics.drawCenteredString(font, loadingText, width / 2, height / 2, 0xFFFFFF)
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        // 背景を描画する必要がある場合はここに実装
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return configScreen?.mouseClicked(mouseX, mouseY, button) ?: super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        return configScreen?.mouseDragged(mouseX, mouseY, button, dragX, dragY) ?: super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return configScreen?.mouseReleased(mouseX, mouseY, button) ?: super.mouseReleased(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return configScreen?.keyPressed(keyCode, scanCode, modifiers) ?: super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return configScreen?.keyReleased(keyCode, scanCode, modifiers) ?: super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun removed() {
        super.removed()
        configScreen?.removed()
    }

    override fun onClose() {
        minecraft?.setScreen(null)  // メインメニューに戻る
    }
}
