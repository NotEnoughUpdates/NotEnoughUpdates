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

import com.google.gson.annotations.Expose
import io.github.moulberry.notenoughupdates.util.SBInfo
import io.github.moulberry.notenoughupdates.util.TemplateUtil
import io.github.moulberry.notenoughupdates.util.kotlin.KSerializable

@KSerializable
data class CustomTodo(
    @Expose var label: String,
    @Expose var timer: Int,
    @Expose var trigger: String,
    @Expose var icon: String,
    @Expose var isResetOffset: Boolean,
    @Expose var triggerTarget: TriggerTarget = TriggerTarget.CHAT,
    @Expose var triggerMatcher: TriggerMatcher = TriggerMatcher.CONTAINS,
    @Expose var readyAt: MutableMap<String, Long> = mutableMapOf(),
    @Expose var enabled: MutableMap<String, Boolean> = mutableMapOf(),
) {
    enum class TriggerMatcher {
        REGEX, STARTS_WITH, CONTAINS, EQUALS
    }

    enum class TriggerTarget {
        CHAT, ACTIONBAR, TAB_LIST, SIDEBAR
    }

    fun isValid(): Boolean {
        return timer >= 0 && trigger.isNotBlank()
    }

    fun setDoneNow() {
        val t = System.currentTimeMillis()
        readyAt[SBInfo.getInstance().currentProfile ?: return] =
            if (isResetOffset) {
                t + DAY - t % DAY + timer * 1000L
            } else {
                t + timer * 1000L
            }
    }

    var readyAtOnCurrentProfile: Long?
        get() {
            return readyAt[SBInfo.getInstance().currentProfile ?: return null]
        }
        set(value) {
            readyAt[SBInfo.getInstance().currentProfile ?: return] = value ?: return
        }

    var isEnabledOnCurrentProfile: Boolean
        get() {
            return enabled[SBInfo.getInstance().currentProfile ?: return true] ?: true
        }
        set(value) {
            enabled[SBInfo.getInstance().currentProfile ?: return] = value
        }


    companion object {
        const val templatePrefix = "NEU:CUSTOMTODO/"
        const val DAY = (24 * 60 * 60 * 100)
        fun fromTemplate(data: String): CustomTodo? {
            return TemplateUtil.maybeDecodeTemplate(templatePrefix, data, CustomTodo::class.java)
                ?.also {
                    it.enabled.clear()
                    it.readyAt.clear()
                }
        }
    }

    fun toTemplate(): String {
        return TemplateUtil.encodeTemplate(
            templatePrefix,
            this.copy(enabled = mutableMapOf(), readyAt = mutableMapOf())
        )
    }
}
