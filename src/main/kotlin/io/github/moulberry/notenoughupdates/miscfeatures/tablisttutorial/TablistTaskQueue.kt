/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures.tablisttutorial

import io.github.moulberry.notenoughupdates.util.NotificationHandler

object TablistTaskQueue {
    val queue = mutableListOf<TablistTutorial.EnableTask>()

    fun addToQueue(task: TablistTutorial.EnableTask) {
        if (!queueContainsElements()) {
            NotificationHandler.displayNotification(
                listOf(
                    "§l§4Widget missing",
                    "§cOne or more tab list widgets, which are required for NEU to function properly, are missing",
                    "§cOpen the Tablist Widgets settings using",
                    "§b/tab",
                    "§cto get some assistance in fixing this problem."
                ),
                false
            )
        }
        if (task !in queue) {
            // see todo in MiningOverlay.java:377
//            Utils.addChatMessage("Adding $task")
            queue.add(task)
        }
    }

    fun removeFromQueue(task: TablistTutorial.EnableTask) {
        queue.remove(task)
    }

    fun queueContainsElements(): Boolean {
        return queue.isNotEmpty()
    }

    fun getNextQueueItem(): TablistTutorial.EnableTask? {
        return if (!queueContainsElements()) {
            null
        } else {
            queue.removeLast()
        }
    }
}
