package example.examplemod.utils

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffects

object EffectUtilities {
    fun getAllEffects(): List<MobEffect> {
        return BuiltInRegistries.MOB_EFFECT.asIterable().toList()
    }

    fun getEffectById(id: Int): MobEffect? {
        return BuiltInRegistries.MOB_EFFECT.byId(id)
    }
}