package effect_master.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraftforge.fml.loading.FMLPaths
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

object EffectToggleState {
    private val effectSettings = mutableMapOf<ResourceLocation, EffectOptions>()
    private val configFile = File(FMLPaths.CONFIGDIR.get().toFile(), "effect_master_effects.json")
    private val gson = GsonBuilder()
        .registerTypeAdapter(ResourceLocation::class.java, ResourceLocationAdapter())
        .registerTypeAdapter(EffectOptions::class.java, EffectOptionsAdapter())
        .create()

    fun getAllEffectSettings(): Map<ResourceLocation, EffectOptions> {
        loadConfig()
        return effectSettings.toMap()  // 変更不可能なコピーを返す
    }

    fun setEffectSetting(effect: MobEffect, setting: EffectOptions) {
        val key = BuiltInRegistries.MOB_EFFECT.getKey(effect)
        if (key != null) {
            effectSettings[key] = setting
        }
    }

    fun isEffectDisabled(effect: MobEffect): Boolean {
        val key = BuiltInRegistries.MOB_EFFECT.getKey(effect)
        return effectSettings[key] === EffectOptions.DISABLED
    }

    fun saveConfig() {
        try {
            FileWriter(configFile).use { writer ->
                gson.toJson(effectSettings, writer)
            }
        } catch (e: Exception) {
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
                println("Failed to load config: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("Config file does not exist")
        }
    }
}

class ResourceLocationAdapter : JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
    override fun serialize(src: ResourceLocation, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ResourceLocation {
        val parts = json.asString.split(":")
        return ResourceLocation(parts[0], parts[1])
    }
}

class EffectOptionsAdapter : JsonSerializer<EffectOptions>, JsonDeserializer<EffectOptions> {
    override fun serialize(src: EffectOptions, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.name)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EffectOptions {
        return try {
            EffectOptions.valueOf(json.asString)
        } catch (e: IllegalArgumentException) {
            EffectOptions.DEFAULT
        }
    }
}