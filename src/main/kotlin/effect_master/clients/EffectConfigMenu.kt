package effect_master.clients

import effect_master.EffectMaster
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class EffectConfigMenu(containerId: Int, playerInventory: Inventory) : AbstractContainerMenu(EffectMaster.EFFECT_CONFIG_MENU.get(), containerId) {
    constructor(containerId: Int, playerInventory: Inventory, extraData: FriendlyByteBuf) : this(containerId, playerInventory)

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean = true
}