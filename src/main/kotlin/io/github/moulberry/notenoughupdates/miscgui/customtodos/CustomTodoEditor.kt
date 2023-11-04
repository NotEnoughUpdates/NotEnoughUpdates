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

package io.github.moulberry.notenoughupdates.miscgui.customtodos

import io.github.moulberry.moulconfig.common.IItemStack
import io.github.moulberry.moulconfig.forge.ForgeItemStack
import io.github.moulberry.moulconfig.internal.ClipboardUtils
import io.github.moulberry.moulconfig.observer.ObservableList
import io.github.moulberry.moulconfig.xml.Bind
import io.github.moulberry.moulconfig.xml.XMLUniverse
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.Utils
import io.github.moulberry.notenoughupdates.util.loadResourceLocation
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

class CustomTodoEditor(
    val from: CustomTodo,
    val todos: ObservableList<CustomTodoEditor>,
    val xmlUniverse: XMLUniverse
) {
    @field:Bind
    var label: String = from.label

    @field:Bind
    var enabled: Boolean = from.isEnabledOnCurrentProfile

    @field:Bind
    var timer: String = from.timer.toString()

    @field:Bind
    var trigger: String = from.trigger

    @field:Bind
    var icon: String = from.icon

    @field:Bind
    var isResetOffset: Boolean = from.isResetOffset

    var target = from.triggerTarget
    var matchMode = from.triggerMatcher
    var lastCustomTodo: CustomTodo? = null

    fun into(): CustomTodo {
        val nextCustomTodo = CustomTodo(
            label,
            timer.toIntOrNull() ?: 0,
            trigger,
            icon,
            isResetOffset,
            target, matchMode,
            from.readyAt,
            from.enabled.toMutableMap().also { it[SBInfo.getInstance().currentProfile ?: return@also] = enabled }
        )
        if (nextCustomTodo != lastCustomTodo) {
            lastCustomTodo = nextCustomTodo
            CustomTodoList(todos, xmlUniverse).save()
        }
        return nextCustomTodo
    }

    @Bind
    fun setChat() {
        target = CustomTodo.TriggerTarget.CHAT
    }

    @Bind
    fun setActionbar() {
        target = CustomTodo.TriggerTarget.ACTIONBAR
    }

    @Bind
    fun setSidebar() {
        target = CustomTodo.TriggerTarget.SIDEBAR
    }

    @Bind
    fun setTablist() {
        target = CustomTodo.TriggerTarget.TAB_LIST
    }

    private fun colorFromBool(b: Boolean): String {
        return if (b) "§a" else "§c"
    }

    @Bind
    fun getChat(): String {
        return colorFromBool(target == CustomTodo.TriggerTarget.CHAT) + "Chat"
    }

    @Bind
    fun getActionbar(): String {
        return colorFromBool(target == CustomTodo.TriggerTarget.ACTIONBAR) + "Actionbar"
    }

    @Bind
    fun getSidebar(): String {
        return colorFromBool(target == CustomTodo.TriggerTarget.SIDEBAR) + "Sidebar"
    }

    @Bind
    fun getTablist(): String {
        return colorFromBool(target == CustomTodo.TriggerTarget.TAB_LIST) + "Tablist"
    }

    @Bind
    fun setRegex() {
        matchMode = CustomTodo.TriggerMatcher.REGEX
    }

    @Bind
    fun setStartsWith() {
        matchMode = CustomTodo.TriggerMatcher.STARTS_WITH
    }

    @Bind
    fun setContains() {
        matchMode = CustomTodo.TriggerMatcher.CONTAINS
    }

    @Bind
    fun setEquals() {
        matchMode = CustomTodo.TriggerMatcher.EQUALS
    }

    @Bind
    fun getRegex(): String {
        return colorFromBool(matchMode == CustomTodo.TriggerMatcher.REGEX) + "Regex"
    }

    @Bind
    fun getStartsWith(): String {
        return colorFromBool(matchMode == CustomTodo.TriggerMatcher.STARTS_WITH) + "Starts With"
    }

    @Bind
    fun getContains(): String {
        return colorFromBool(matchMode == CustomTodo.TriggerMatcher.CONTAINS) + "Contains"
    }

    @Bind
    fun getEquals(): String {
        return colorFromBool(matchMode == CustomTodo.TriggerMatcher.EQUALS) + "Equals"
    }

    @Bind
    fun getItemStack(): IItemStack {
        val item = Item.getByNameOrId(icon) ?: (Items.paper)
        return ForgeItemStack.of(ItemStack(item))
    }

    @Bind
    fun copyTemplate() {
        ClipboardUtils.copyToClipboard(into().toTemplate())
    }

    @Bind
    fun markAsReady() {
        from.readyAtOnCurrentProfile = System.currentTimeMillis()
    }

    @Bind
    fun markAsCompleted() {
        from.setDoneNow()
    }

    @Bind
    fun getFancyTime(): String {
        val tint = timer.toIntOrNull() ?: return "§3Invalid Time"
        val timeFormat = Utils.prettyTime(tint * 1000L)
        if (isResetOffset) {
            return "Reset $timeFormat after 00:00 GMT"
        }
        return "Reset $timeFormat after completion"
    }

    fun changeTimer(value: Int) {
        timer = ((timer.toIntOrNull() ?: 0) + value).coerceAtLeast(0).toString()
    }

    @Bind
    fun plusDay() {
        changeTimer(60 * 60 * 24)
    }

    @Bind
    fun minusDay() {
        changeTimer(-60 * 60 * 24)
    }

    @Bind
    fun minusHour() {
        changeTimer(-60 * 60)
    }

    @Bind
    fun plusHour() {
        changeTimer(60 * 60)
    }

    @Bind
    fun plusMinute() {
        changeTimer(60)
    }

    @Bind
    fun minusMinute() {
        changeTimer(-60)
    }

    @Bind
    fun delete() {
        todos.remove(this)
        CustomTodoList(todos, xmlUniverse).save()
    }

    @Bind
    fun getTitle(): String {
        return "Editing ${into().label}"
    }

    @Bind
    fun close() {
        Minecraft.getMinecraft().displayGuiScreen(
            CustomTodoList(
                todos, xmlUniverse
            ).open()
        )
    }

    @Bind
    fun edit() {
        Minecraft.getMinecraft().displayGuiScreen(
            xmlUniverse.loadResourceLocation(this, ResourceLocation("notenoughupdates:gui/customtodos/edit.xml"))
        )
    }
}
