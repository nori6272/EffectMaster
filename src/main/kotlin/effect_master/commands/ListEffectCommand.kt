package effect_master.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import effect_master.network.NetworkHandler
import effect_master.network.OpenConfigScreenPacket
import net.minecraftforge.fml.loading.FMLEnvironment

object ListEffectsCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("listeffects")
                .requires { it.hasPermission(0) }
                .executes { context -> execute(context) }
        )

        dispatcher.register(
            Commands.literal("effectconfig")
                .requires { it.hasPermission(0) }
                .executes { context ->
                    val player = context.source.player
                    if (player is ServerPlayer) {
                        if (FMLEnvironment.dist.isClient) {
                            // クライアント側でのみ設定画面を開く
                            openConfigScreen(player)
                        } else {
                            player.sendSystemMessage(Component.translatable("effect_master.command.effectconfig.server_message"))
                        }
                    }
                    1
                }
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val effects = BuiltInRegistries.MOB_EFFECT.asIterable().toList()

        source.sendSuccess({ Component.translatable("effect_master.command.listeffects.header") }, false)
        effects.forEachIndexed { index, effect ->
            val name = BuiltInRegistries.MOB_EFFECT.getKey(effect)?.toString() ?: "Unknown"
            source.sendSuccess({ Component.literal("${index + 1}. $name") }, false)
        }

        return effects.size
    }

    private fun openConfigScreen(player: ServerPlayer) {
        // サーバーからクライアントに設定画面を開くよう指示するパケットを送信
        NetworkHandler.sendToClient(OpenConfigScreenPacket(), player)
    }
}