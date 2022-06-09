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

package io.github.moulberry.notenoughupdates.mbgui;

import org.lwjgl.util.vector.Vector2f;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class MBGuiGroup extends MBGuiElement {
	public int width;
	public int height;
	protected HashMap<MBGuiElement, Vector2f> childrenPosition = new HashMap<>();

	public MBGuiGroup() {}

	public abstract Collection<MBGuiElement> getChildren();

	public Map<MBGuiElement, Vector2f> getChildrenPosition() {
		return Collections.unmodifiableMap(childrenPosition);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void mouseClick(float x, float y, int mouseX, int mouseY) {
		Map<MBGuiElement, Vector2f> childrenPos = getChildrenPosition();

		for (MBGuiElement child : getChildren()) {
			Vector2f childPos = childrenPos.get(child);
			if (mouseX > x + childPos.x && mouseX < x + childPos.x + child.getWidth()) {
				if (mouseY > y + childPos.y && mouseY < y + childPos.y + child.getHeight()) {
					child.mouseClick(x + childPos.x, y + childPos.y, mouseX, mouseY);
				}
			}
		}
	}

	@Override
	public void mouseClickOutside() {
		for (MBGuiElement child : getChildren()) {
			child.mouseClickOutside();
		}
	}

	@Override
	public void render(float x, float y) {
		Map<MBGuiElement, Vector2f> childrenPos = getChildrenPosition();

		for (MBGuiElement child : getChildren()) {
			Vector2f childPos = childrenPos.get(child);
			child.render(x + childPos.x, y + childPos.y);
		}
	}
}
