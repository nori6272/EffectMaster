package example.examplemod.commands

import example.examplemod.clients.EffectConfigMenu
import example.examplemod.utils.EffectUtilities
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleMenuProvider

object ListEffectsCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("listeffects")
                .requires { it.hasPermission(0) } // OP権限レベル2以上が必要
                .executes { context -> execute(context) }
        )
        dispatcher.register(
            Commands.literal("effectconfig")
                .requires { it.hasPermission(0) }
                .executes { context ->
                    val player = context.source.player
                    if (player is ServerPlayer) {
                        player.openMenu(SimpleMenuProvider(
                            { windowId, playerInventory, _ ->
                                EffectConfigMenu(windowId, playerInventory)
                            },
                            Component.literal("エフェクト設定")
                        ))
                    }
                    1
                }
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val effects = EffectUtilities.getAllEffects()

        source.sendSuccess({ Component.literal("登録されているエフェクトの一覧:") }, false)
        effects.forEachIndexed { index, effect ->
            val name = BuiltInRegistries.MOB_EFFECT.getKey(effect)?.toString() ?: "Unknown"
            source.sendSuccess({ Component.literal("${index + 1}. $name") }, false)
        }

        return effects.size
    }
}