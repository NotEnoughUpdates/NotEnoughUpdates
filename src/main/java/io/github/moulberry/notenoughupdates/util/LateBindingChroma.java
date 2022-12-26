/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
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

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.var;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class LateBindingChroma {
	private static final Pattern FORMATTING_CODE = Pattern.compile("§(.)");

	public static LateBindingChroma of(String raw) {
		if (!raw.contains("§z"))
			return new LateBindingChroma.WithoutChroma(raw);
		var matcher = FORMATTING_CODE.matcher(raw);
		var chunks = new ArrayList<Chunk>();
		var sb = new StringBuffer();
		var color = "";
		var extraFormatting = "";
		while (matcher.find()) {
			sb.setLength(0);
			matcher.appendReplacement(sb, "");
			chunks.add(new Chunk(color, extraFormatting, sb.toString()));
			var c = matcher.group(1);
			if ("lnomk".contains(c)) {
				extraFormatting += "§" + c;
			} else {
				color = c;
				extraFormatting = "";
			}
		}
		sb.setLength(0);
		matcher.appendTail(sb);
		chunks.add(new Chunk(color, extraFormatting, sb.toString()));
		chunks.removeIf(it -> it.text.isEmpty());
		return new LateBindingChroma.WithChroma(chunks);
	}

	public abstract String render(float chromaOffset);

	@Value
	@EqualsAndHashCode(callSuper = false)
	static class WithoutChroma extends LateBindingChroma {
		String content;

		@Override
		public String render(float chromaOffset) {
			return content;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	static class WithChroma extends LateBindingChroma {
		List<Chunk> chunks;

		@Override
		public String render(float offset) {
			var sb = new StringBuilder();
			for (Chunk chunk : chunks) {
				String text;
				if (chunk.color.equals("z")) {
					text = Utils.chromaString(chunk.text, offset, chunk.extraFormatting);
				} else {
					text = (chunk.color.isEmpty() ? "" : ("§" + chunk.color)) + chunk.extraFormatting + chunk.text;
				}
				sb.append(text);
				offset += Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
			}
			return sb.toString();
		}
	}

	@Value
	public static class Chunk {
		String color;
		String extraFormatting;
		String text;
	}

}
