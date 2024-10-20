package example.examplemod.network

import example.examplemod.utils.EffectUtilities
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class EffectTogglePacket(val effectId: Int, val enabled: Boolean) {
    constructor(buf: FriendlyByteBuf) : this(buf.readInt(), buf.readBoolean())

    fun toBytes(buf: FriendlyByteBuf) {
        buf.writeInt(effectId)
        buf.writeBoolean(enabled)
    }

    fun handle(ctx: Supplier<NetworkEvent.Context>): Boolean {
        ctx.get().enqueueWork {
            val player = ctx.get().sender
            if (player is ServerPlayer) {
                val effect = EffectUtilities.getEffectById(effectId)
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