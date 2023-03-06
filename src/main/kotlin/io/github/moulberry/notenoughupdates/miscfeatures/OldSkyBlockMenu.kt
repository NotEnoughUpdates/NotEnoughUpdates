/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscfeatures

import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe
import io.github.moulberry.notenoughupdates.events.ReplaceItemEvent
import io.github.moulberry.notenoughupdates.events.SlotClickEvent
import io.github.moulberry.notenoughupdates.util.ItemUtils
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

@NEUAutoSubscribe
object OldSkyBlockMenu {
    private val decimalFormat = DecimalFormat("##,##0", DecimalFormatSymbols(Locale.US))

    val map: Map<Int, SkyBlockButton> by lazy {
        val map = mutableMapOf<Int, SkyBlockButton>()
        for (button in SkyBlockButton.values()) {
            map[button.slot] = button
        }
        map
    }

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!isRightInventory()) return
        if (event.inventory !is ContainerLocalMenu) return

        val skyBlockButton = map[event.slotNumber] ?: return
        val showWarning = skyBlockButton.requiresBoosterCookie && !CookieWarning.hasActiveBoosterCookie()
        val item = if (showWarning) skyBlockButton.itemWithCookieWarning else skyBlockButton.itemWithoutCookieWarning

        if (skyBlockButton == SkyBlockButton.ACCESSORY) {
            val magicalPower = NotEnoughUpdates.INSTANCE.config.profileSpecific?.magicalPower ?: 0

            val lore = ItemUtils.getLore(item)
            lore.add(4, "")
            val format = decimalFormat.format(magicalPower)
            lore.add(5, "§7Magical Power: §6$format")

            val newItem = ItemStack.copyItemStack(item)
            ItemUtils.setLore(newItem, lore)
            event.replaceWith(newItem)
        } else {
            event.replaceWith(item)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onStackClick(event: SlotClickEvent) {
        if (!isRightInventory()) return

        val skyBlockButton = map[event.slotId] ?: return
        event.isCanceled = true

        if (!skyBlockButton.requiresBoosterCookie || CookieWarning.hasActiveBoosterCookie()) {
            NotEnoughUpdates.INSTANCE.sendChatMessage("/" + skyBlockButton.command)
        }
    }

    private fun isRightInventory(): Boolean {
        return NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() &&
                NotEnoughUpdates.INSTANCE.config.misc.oldSkyBlockMenu &&
                Utils.getOpenChestName() == "SkyBlock Menu"
    }

    enum class SkyBlockButton(
        val command: String,
        val slot: Int,
        private val displayName: String,
        private vararg val displayDescription: String,
        private val itemData: ItemData,
        val requiresBoosterCookie: Boolean = true,
    ) {
        TRADES(
            "trades", 40,
            "Trades",
            "View your available trades.",
            "These trades are always",
            "available and accessible through",
            "the SkyBlock Menu.",
            itemData = NormalItemData(Items.emerald),
            requiresBoosterCookie = false
        ),
        ACCESSORY(
            "accessories", 53,
            "Accessory Bag",
            "A special bag which can hold",
            "Talismans, Rings, Artifacts, Relics, and",
            "Orbs within it. All will still",
            "work while in this bag!",
            itemData = SkullItemData(
                "2b73dd76-5fc1-4ac3-8139-6a8992f8ce80",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxYTkxOGMw" +
                        "YzQ5YmE4ZDA1M2U1MjJjYjkxYWJjNzQ2ODkzNjdiNGQ4YWEwNmJmYzFiYTkxNTQ3MzA5ODVmZiJ9fX0="
            )
        ),
        POTION(
            "potionbag", 52,
            "Potion Bag",
            "A handy bag for holding your",
            "Potions in.",
            itemData = SkullItemData(
                "991c4a18-3283-4629-b0fc-bbce23cd658c",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY4Yjg" +
                        "yNDI3YjI2MGQwYTYxZTY0ODNmYzNiMmMzNWE1ODU4NTFlMDhhOWE5ZGYzNzI1NDhiNDE2OGNjODE3YyJ9fX0="
            )
        ),
        QUIVER(
            "quiver", 44,
            "Quiver",
            "A masterfully crafted Quiver",
            "which holds any kind of",
            "projectile you can think of!",
            itemData = SkullItemData(
                "41758912-e6b1-4700-9de5-04f2cfb9c422",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNiM2FjZ" +
                        "GMxMWNhNzQ3YmY3MTBlNTlmNGM4ZTliM2Q5NDlmZGQzNjRjNjg2OTgzMWNhODc4ZjA3NjNkMTc4NyJ9fX0="
            )
        ),
        FISHING(
            "fishingbag", 43,
            "Fishing Bag",
            "A useful bag which can hold all",
            "types of fish, baits, and fishing",
            "loot!",
            itemData = SkullItemData(
                "508c01d6-eabe-430b-9811-874691ee7ee4",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWI4ZT" +
                        "I5N2RmNmI4ZGZmY2YxMzVkYmE4NGVjNzkyZDQyMGFkOGVjYjQ1OGQxNDQyODg1NzJhODQ2MDNiMTYzMSJ9fX0="
            )
        ),
        SACK_OF_SACKS(
            "sacks", 35,
            "Sack of Sacks",
            "A sack which contains other",
            "sacks. Sackception!",
            itemData = SkullItemData(
                "a206a7eb-70fc-4f9f-8316-c3f69d6ba2ca",
                "ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0" +
                        "ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVl" +
                        "LAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5l" +
                        "Y3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0" +
                        "YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0="
            ),
            requiresBoosterCookie = false
        ),
        ;

        val itemWithCookieWarning: ItemStack by lazy { createItem(true) }
        val itemWithoutCookieWarning: ItemStack by lazy { createItem(false) }

        private fun createItem(showCookieWarning: Boolean): ItemStack {
            val lore = mutableListOf<String>()
            for (line in displayDescription) {
                lore.add("§7$line")
            }
            lore.add("")

            if (showCookieWarning) {
                lore.add("§cYou need a booster cookie active")
                lore.add("§cto use this shortcut!")
            } else {
                lore.add("§eClick to execute /${command}")
            }
            val array = lore.toTypedArray()
            val name = "§a${displayName}"
            return when (itemData) {
                is NormalItemData -> Utils.createItemStackArray(itemData.displayIcon, name, array)
                is SkullItemData -> Utils.createSkull(
                    name,
                    itemData.uuid,
                    itemData.value,
                    array
                )
            }
        }
    }

    sealed interface ItemData

    class NormalItemData(val displayIcon: Item) : ItemData

    class SkullItemData(val uuid: String, val value: String) : ItemData
}
