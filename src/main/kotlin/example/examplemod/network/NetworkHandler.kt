package example.examplemod.network

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel

object NetworkHandler {
    private const val PROTOCOL_VERSION = "1"
    private val CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation("examplemod", "main"),
        { PROTOCOL_VERSION },
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    )

    fun init() {
        CHANNEL.messageBuilder(EffectTogglePacket::class.java, 0, NetworkDirection.PLAY_TO_SERVER)
            .decoder(::EffectTogglePacket)
            .encoder(EffectTogglePacket::toBytes)
            .consumerMainThread(EffectTogglePacket::handle)
            .add()
    }

    fun sendToServer(msg: EffectTogglePacket) {
        CHANNEL.sendToServer(msg)
    }
}