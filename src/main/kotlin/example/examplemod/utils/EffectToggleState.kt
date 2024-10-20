package example.examplemod.utils

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect

object EffectToggleState {
    private val effectSettings = mutableMapOf<ResourceLocation, EffectOptions>()

    fun getEffectSetting(effect: MobEffect): EffectOptions {
        val key = BuiltInRegistries.MOB_EFFECT.getKey(effect)
        return effectSettings.getOrDefault(key, EffectOptions.DEFAULT)
    }

    fun setEffectSetting(effect: MobEffect, setting: EffectOptions) {
        val key = BuiltInRegistries.MOB_EFFECT.getKey(effect)
        if (key != null) {
            effectSettings[key] = setting
        }
    }

    // 設定の保存と読み込みのメソッドを追加（後で実装）
    fun saveConfig() {
        // TODO: 設定をファイルに保存
    }

    fun loadConfig() {
        // TODO: 設定をファイルから読み込み
    }
}