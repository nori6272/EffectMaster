package example.examplemod.network

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.effect.MobEffectInstance
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class EffectTogglePacket(val effectId: Int, val enabled: Boolean) {

    // エンコードメソッド
    fun encode(buf: FriendlyByteBuf) {
        buf.writeInt(effectId)
        buf.writeBoolean(enabled)
    }

    // デコードメソッド（コンパニオンオブジェクト内に静的メソッドとして定義）
    companion object {
        fun decode(buf: FriendlyByteBuf): EffectTogglePacket {
            val effectId = buf.readInt()
            val enabled = buf.readBoolean()
            return EffectTogglePacket(effectId, enabled)
        }
    }

    // ハンドルメソッド
    fun handle(ctx: Supplier<NetworkEvent.Context>): Boolean {
        ctx.get().enqueueWork {
            // サーバー側でのエフェクト適用ロジック
            val player = ctx.get().sender
            if (player != null) {
                val effect = BuiltInRegistries.MOB_EFFECT.byId(effectId)
                if (effect != null) {
                    if (enabled) {
                        player.addEffect(MobEffectInstance(effect, Int.MAX_VALUE, 0, false, false))
                    } else {
                        player.removeEffect(effect)
                    }
                }
            }
        }
        return true
    }
}