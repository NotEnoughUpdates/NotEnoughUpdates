/*
 * Copyright (C) 2023 Linnea Gräf
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

package io.github.moulberry.notenoughupdates.autosubscribe

import com.google.devtools.ksp.symbol.KSClassDeclaration

internal data class NEUEventSubscriber(
    val invocationKind: InvocationKind,
    val declaration: KSClassDeclaration,
)

internal enum class InvocationKind {
    GET_INSTANCE,
    OBJECT_INSTANCE,
    DEFAULT_CONSTRUCTOR,
    ACCESS_INSTANCE,
}
