package example.examplemod.network

import example.examplemod.utils.EffectOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.effect.MobEffectInstance
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class EffectSettingPacket(val effectId: Int, val setting: EffectOptions) {
    fun encode(buf: FriendlyByteBuf) {
        buf.writeInt(effectId)
        buf.writeEnum(setting)
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): EffectSettingPacket {
            val effectId = buf.readInt()
            val setting = buf.readEnum(EffectOptions::class.java)
            return EffectSettingPacket(effectId, setting)
        }
    }

    fun handle(ctx: Supplier<NetworkEvent.Context>): Boolean {
        ctx.get().enqueueWork {
            val player = ctx.get().sender
            if (player != null) {
                val effect = BuiltInRegistries.MOB_EFFECT.byId(effectId)
                if (effect != null) {
                    val effectKey = BuiltInRegistries.MOB_EFFECT.getKey(effect)?.toString() ?: return@enqueueWork
                    when (setting) {
                        EffectOptions.DISABLED -> {
                            player.removeEffect(effect)
                            // プレイヤーにこのエフェクトが付与されないようにするフラグを設定
                            player.persistentData.putBoolean("effect_disabled_$effectKey", true)
                            println("Set effect $effectKey disabled for player ${player.name}")
                        }
                        EffectOptions.DEFAULT -> {
                            // デフォルトの場合は、無効化フラグを削除
                            player.persistentData.remove("effect_disabled_$effectKey")
                            println("Set Default flag for effect $effectKey for player ${player.name}")
                        }
                        EffectOptions.PERSISTENT -> {
                            player.persistentData.remove("effect_disabled_$effectKey")
                            player.addEffect(MobEffectInstance(effect, Int.MAX_VALUE, 0, false, false))
                            println("Set effect $effectKey persistent for player ${player.name}")
                        }
                    }
                }
            }
        }
        return true
    }
}