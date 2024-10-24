package effect_master.network

import effect_master.clients.EffectConfigScreen
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class OpenConfigScreenPacket {
    fun encode(buf: FriendlyByteBuf) {
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): OpenConfigScreenPacket {
            return OpenConfigScreenPacket()
        }
    }

    fun handle(ctx: Supplier<NetworkEvent.Context>): Boolean {
        ctx.get().enqueueWork {
            // クライアント側で設定画面を開く
            if (ctx.get().direction.receptionSide.isClient) {
                val minecraft = net.minecraft.client.Minecraft.getInstance()
                minecraft.execute {
                    val currentScreen = minecraft.screen
                    val configScreen = EffectConfigScreen.create(currentScreen)
                    minecraft.setScreen(configScreen)
                }
            }
        }
        return true
    }
}