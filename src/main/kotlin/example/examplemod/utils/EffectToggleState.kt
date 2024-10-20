package example.examplemod.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraftforge.fml.loading.FMLPaths
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object EffectToggleState {
    private val effectSettings = mutableMapOf<ResourceLocation, EffectOptions>()
    private val configFile = File(FMLPaths.CONFIGDIR.get().toFile(), "examplemod_effects.json")
    private val gson = Gson()

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

    fun saveConfig() {
        try {
            FileWriter(configFile).use { writer ->
                gson.toJson(effectSettings, writer)
            }
        } catch (e: Exception) {
            // エラーログを出力
            println("Failed to save config: ${e.message}")
        }
    }

    fun loadConfig() {
        if (configFile.exists()) {
            try {
                FileReader(configFile).use { reader ->
                    val type = object : TypeToken<Map<ResourceLocation, EffectOptions>>() {}.type
                    val loadedSettings: Map<ResourceLocation, EffectOptions> = gson.fromJson(reader, type)
                    effectSettings.clear()
                    effectSettings.putAll(loadedSettings)
                }
            } catch (e: Exception) {
                // エラーログを出力
                println("Failed to load config: ${e.message}")
            }
        }
    }
}