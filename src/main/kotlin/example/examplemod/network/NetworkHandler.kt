package example.examplemod.network

import example.examplemod.network.EffectSettingPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import example.examplemod.ExampleMod

object NetworkHandler {
    private val CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation(ExampleMod.ID, "main"),
        { "1.0" },
        { it == "1.0" },
        { it == "1.0" }
    )

    fun init() {
        var id = 0
        CHANNEL.messageBuilder(EffectSettingPacket::class.java, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(EffectSettingPacket::encode)
            .decoder { buf -> EffectSettingPacket.decode(buf) }
            .consumerMainThread { msg, ctx -> msg.handle(ctx) }
            .add()

        CHANNEL.messageBuilder(OpenConfigScreenPacket::class.java, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(OpenConfigScreenPacket::encode)
            .decoder(OpenConfigScreenPacket::decode)
            .consumerMainThread(OpenConfigScreenPacket::handle)
            .add()
    }

    fun sendToServer(msg: EffectSettingPacket) {
        CHANNEL.sendToServer(msg)
    }

    fun sendToClient(msg: OpenConfigScreenPacket, player: ServerPlayer) {
        CHANNEL.send(PacketDistributor.PLAYER.with { player }, msg)
    }
}