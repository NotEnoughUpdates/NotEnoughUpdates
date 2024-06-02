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

package io.github.moulberry.notenoughupdates.miscgui.itemcustomization;

import io.github.moulberry.notenoughupdates.core.ChromaColour;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemCustomizationUtills {

	public static ItemStack copy(ItemStack stack, GuiItemCustomize instance) {
		ItemStack customStack = stack.copy();
		if (!instance.textFieldCustomItem.getText().isEmpty()) {
			customStack.setItem(ItemCustomizeManager.getCustomItem(stack, instance.textFieldCustomItem.getText().trim()));
			customStack.setItemDamage(ItemCustomizeManager.getCustomItemDamage(stack));
		}
		return customStack;
	}

	public static int getGlintColour(GuiItemCustomize instance) {
		int col = instance.customGlintColour == null
			? ChromaColour.specialToChromaRGB(ItemCustomizeManager.DEFAULT_GLINT_COLOR)
			: ChromaColour.specialToChromaRGB(instance.customGlintColour);
		return 0xff000000 | col;
	}

	public static int getLeatherColour(GuiItemCustomize instance) {
		if (!instance.supportCustomLeatherColour) return 0xff000000;

		String customLeatherColour = instance.customLeatherColour;
		int col = customLeatherColour == null
			? ((ItemArmor) instance.customItemStack.getItem()).getColor(instance.customItemStack)
			: ChromaColour.specialToChromaRGB(customLeatherColour);
		return 0xff000000 | col;
	}
}
