/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.util;

public class DiscordMarkdownBuilder {
	private final StringBuilder builder;

	public DiscordMarkdownBuilder() {
		this.builder = new StringBuilder();
		this.builder.append("```md\n");
	}

	public DiscordMarkdownBuilder category(String name) {
		builder.append("# ").append(name).append("\n");
		return this;
	}

	public DiscordMarkdownBuilder append(String key, Object value) {
		if (!key.isEmpty()) {
			builder.append("[").append(key).append("]");
		}
		builder.append("[").append(value).append("]").append("\n");
		return this;
	}

	@Override
	public String toString() {
		return builder.append("```").toString();
	}
}
