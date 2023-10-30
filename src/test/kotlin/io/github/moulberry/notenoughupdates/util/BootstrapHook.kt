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

package io.github.moulberry.notenoughupdates.util

import net.minecraft.block.Block
import net.minecraft.block.BlockFire
import net.minecraft.init.Bootstrap
import net.minecraft.item.Item
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class BootstrapHook : BeforeAllCallback, Extension {
    companion object {
        private val LOCK: Lock = ReentrantLock()
        private var bootstrapped = false
    }

    override fun beforeAll(p0: ExtensionContext?) {
        LOCK.lock()
        try {
            if (!bootstrapped) {
                bootstrapped = true

                Bootstrap::class.java.getDeclaredField("alreadyRegistered").also { it.isAccessible = true }
                    .set(null, true)
                Block.registerBlocks()
                BlockFire.init()
                Item.registerItems()
            }
        } finally {
            LOCK.unlock()
        }
    }
}
