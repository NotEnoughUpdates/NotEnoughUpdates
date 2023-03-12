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

package io.github.moulberry.notenoughupdates.miscgui;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.events.SignSubmitEvent;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiEditSign;
import io.github.moulberry.notenoughupdates.util.Calculator;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Objects;

@NEUAutoSubscribe
public class SignCalculator {

	String lastSource = null;
	BigDecimal lastResult = null;
	Calculator.CalculatorException lastException = null;

	private boolean isEnabled() {
		return NotEnoughUpdates.INSTANCE.config.misc.calculationMode != 0;
	}

	@SubscribeEvent
	public void onSignDrawn(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!(event.gui instanceof GuiEditSign))
			return;
		if (!isEnabled()) return;
		GuiEditSign guiEditSign = (GuiEditSign) event.gui;
		TileEntitySign tileSign = ((AccessorGuiEditSign) guiEditSign).getTileSign();
		if (!tileSign.signText[1].getUnformattedText().equals("^^^^^^^^^^^^^^^") &&
			!tileSign.signText[1].getUnformattedText().equals("^^^^^^")) return;
		String source = tileSign.signText[0].getUnformattedText();
		refresh(source);

		int calculationMode = NotEnoughUpdates.INSTANCE.config.misc.calculationMode;
		if ((calculationMode == 1 && !source.startsWith("!"))) return;

		Utils.drawStringCentered(getRenderedString(), guiEditSign.width / 2F, 58, false, 0x808080FF);
	}

	@SubscribeEvent
	public void onSignSubmitted(SignSubmitEvent event) {
		if (!isEnabled()) return;
		if (Objects.equals(event.lines[1], "^^^^^^^^^^^^^^^") || Objects.equals(event.lines[1], "^^^^^^")) {
			refresh(event.lines[0]);
			if (lastResult != null) {
				event.lines[0] = lastResult.toPlainString();
			}
		}
	}

	public String getRenderedString() {
		if (lastResult != null) {
			DecimalFormat formatter = new DecimalFormat("#,##0.##");
			String lr = formatter.format(lastResult);
			if (Minecraft.getMinecraft().fontRendererObj.getStringWidth(lr) > 90) {
				return EnumChatFormatting.WHITE + lastSource + " " + EnumChatFormatting.YELLOW + "= " + EnumChatFormatting.RED +
					"Result too long";
			}
			return EnumChatFormatting.WHITE + lastSource + " " + EnumChatFormatting.YELLOW + "= " + EnumChatFormatting.GREEN +
				lr;
		} else if (lastException != null) {
			return EnumChatFormatting.RED + lastException.getMessage();
		}
		return EnumChatFormatting.RED + "No calculation has been done.";
	}

	private void refresh(String source) {
		if (Objects.equals(source, lastSource)) return;
		lastSource = source;
		int calculationMode = NotEnoughUpdates.INSTANCE.config.misc.calculationMode;
		if (source.isEmpty() || calculationMode == 0 || (calculationMode == 1 && !source.startsWith("!"))) {
			lastResult = null;
			lastException = null;
			return;
		}
		try {
			lastResult = Calculator.calculate(calculationMode == 1 ? source.substring(1) : source);
			lastException = null;
		} catch (Calculator.CalculatorException ex) {
			lastException = ex;
			lastResult = null;
		}
	}
}
