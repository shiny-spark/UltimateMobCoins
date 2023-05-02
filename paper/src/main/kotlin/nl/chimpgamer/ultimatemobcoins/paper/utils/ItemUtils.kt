package nl.chimpgamer.ultimatemobcoins.paper.utils

import dev.dejvokep.boostedyaml.block.implementation.Section
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import nl.chimpgamer.ultimatemobcoins.paper.UltimateMobCoinsPlugin
import nl.chimpgamer.ultimatemobcoins.paper.extensions.*
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType

object ItemUtils {

    fun itemSectionToItemStack(itemSection: Section, tagResolver: TagResolver): ItemStack {
        val itemStack = ItemStack(Material.STONE)

        if (itemSection.contains("material")) {
            val material = runCatching { Material.matchMaterial(itemSection.getString("material")) }.getOrNull()
            if (material != null) {
                itemStack.type(material)
            }
        }
        if (itemSection.contains("name")) {
            val name = itemSection.getString("name")
            itemStack.name(name.parse(tagResolver))
        }
        if (itemSection.contains("lore")) {
            val lore = itemSection.getStringList("lore")
            itemStack.lore(lore.map { it.parse(tagResolver) })
        }
        if (itemSection.contains("glow")) {
            val glow = itemSection.getBoolean("glow")
            itemStack.glow(glow)
        }
        if (itemSection.contains("custom_model_data")) {
            val customModelData = itemSection.getInt("custom_model_data", null)
            if (customModelData != null) {
                itemStack.customModelData(customModelData)
            }
        }

        return itemStack
    }

    fun itemDataToItemStack(plugin: UltimateMobCoinsPlugin, itemData: List<String>): ItemStack {
        var itemStack = ItemStack(Material.STONE)
        itemData.forEach { data ->
            val parts = data.split(":", limit = 2)
            val name = parts[0].trim()
            val value = parts[1]

            if (name == "material") {
                val material = kotlin.runCatching { Material.matchMaterial(value) }.getOrNull()
                if (material != null) {
                    itemStack = itemStack.type(material)
                }
            } else if (name == "amount") {
                val amount = value.toIntOrNull()
                if (amount != null) {
                    itemStack = itemStack.amount(amount)
                }
            } else if (name == "name") {
                itemStack = itemStack.name(value)
            } else if (name == "lore") {
                itemStack = if (value.contains(newlineSplitRegex)) {
                    itemStack.lore(value.split(newlineSplitRegex))
                } else {
                    itemStack.lore(value)
                }
            } else if (name == "itemflag") {
                if (value.toBoolean() || value.equals("all", ignoreCase = true)) {
                    itemStack = itemStack.flag(*ItemFlag.values())
                } else {
                    val itemFlags = value.split("#")

                    for (itemFlagStr in itemFlags) {
                        val itemFlag = kotlin.runCatching { ItemFlag.valueOf(itemFlagStr.uppercase()) }.getOrNull()
                        if (itemFlag != null) {
                            itemStack = itemStack.flag(itemFlag)
                        }
                    }
                }
            } else if (name == "glow") {
                val glow = value.toBoolean()
                itemStack.glow(glow)
            } else if (name == "enchantment") {
                val enchantmentParts = value.split("#")
                if (enchantmentParts.size != 2) {
                    return@forEach
                }
                val enchantmentName = enchantmentParts[0].trim()
                val level = enchantmentParts[1].trim().toIntOrNull() ?: -1

                val enchantment =
                    kotlin.runCatching { Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName)) }.getOrNull()
                if (enchantment != null) {
                    itemStack = itemStack.enchantment(enchantment, level)
                }
            } else if (name == "potion") {
                if (itemStack.itemMeta is PotionMeta) {
                    val potionParts = value.split("#")

                    val potionTypeName = potionParts[0].trim()
                    val extended = potionParts[1].trim().toBooleanStrictOrNull() ?: false
                    val upgraded = potionParts[2].trim().toBooleanStrictOrNull() ?: false

                    val potionType =
                        PotionType.values().firstOrNull { potionTypeName.equals(it.name, ignoreCase = true) }
                    if (potionType != null) {
                        itemStack = itemStack.potion(potionType, extended, upgraded)
                    }
                }
            } else if (name == "color") {
                val colorParts = value.split("#")
                if (colorParts.size != 3) {
                    plugin.getLogger().info("Invalid format for colors!")
                    return@forEach
                }

                val red = colorParts[0].trim().toIntOrNull() ?: 0
                val green = colorParts[1].trim().toIntOrNull() ?: 0
                val blue = colorParts[2].trim().toIntOrNull() ?: 0
                val color = Color.fromBGR(red, green, blue)

                itemStack = itemStack.color(color)
            } else if (name == "custommodeldata") {
                val customModelData = value.toIntOrNull()
                if (customModelData == null) {
                    plugin.logger.warning("Invalid custom model data!")
                    return@forEach
                }
                itemStack = itemStack.customModelData(customModelData)
            }
        }
        return itemStack
    }
}