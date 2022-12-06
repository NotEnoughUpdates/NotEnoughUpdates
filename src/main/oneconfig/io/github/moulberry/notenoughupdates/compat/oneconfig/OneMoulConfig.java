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

package io.github.moulberry.notenoughupdates.compat.oneconfig;

import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigButton;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigColorElement;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigDropdown;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigInfo;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigKeyBind;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigSlider;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigSwitch;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigTextBox;
import io.github.moulberry.notenoughupdates.core.config.Config;
import io.github.moulberry.notenoughupdates.core.config.annotations.Category;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorButton;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorColour;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDraggableList;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorFSR;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorKeybind;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorText;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;
import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class OneMoulConfig extends cc.polyfrost.oneconfig.config.Config {

	final Config moulConfig;
	private final Runnable saveCallback;

	public OneMoulConfig(Mod modData, Config moulConfig, Runnable saveCallback) {
		super(modData, "_SHOULD_NOT_BE_WRITTEN.json");
		this.moulConfig = moulConfig;
		this.saveCallback = saveCallback;
		initialize();
	}

	@Override
	public void initialize() {
		if (moulConfig != null) {
			mod.config = this;
			OneConfigCompat.getModList().add(mod);

			try {
				processMoulConfig();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private void processMoulConfig() throws IllegalAccessException {
		for (Field categoryField : moulConfig.getClass().getDeclaredFields()) {
			Category annotation = categoryField.getAnnotation(Category.class);
			if (annotation == null) continue;
			Object categoryInstance = categoryField.get(moulConfig);
			OptionSubcategory subCategory = ConfigUtils.getSubCategory(mod.defaultPage, annotation.name(), "");
			createPageForCategory(subCategory, categoryInstance);
		}
	}

	private void createPageForCategory(OptionSubcategory category, Object categoryInstance) {
		for (Field optionField : categoryInstance.getClass().getDeclaredFields()) {
			ConfigOption annotation = optionField.getAnnotation(ConfigOption.class);
			if (annotation == null) continue;
			String cat = category.getName();
			String subcategory = "";
			ConfigEditorBoolean configEditorBoolean = optionField.getAnnotation(ConfigEditorBoolean.class);
			if (configEditorBoolean != null) {
				category.options.add(new ConfigSwitch(
					optionField,
					categoryInstance,
					annotation.name(),
					annotation.desc(),
					cat, subcategory, 1
				));
			}
			ConfigEditorText configEditorText = optionField.getAnnotation(ConfigEditorText.class);
			if (configEditorText != null) {
				category.options.add(new ConfigTextBox(
					optionField,
					categoryInstance,
					annotation.name(),
					annotation.desc(),
					cat, subcategory, 1,
					annotation.name(), false, false
				));
			}
			ConfigEditorKeybind configEditorKeybind = optionField.getAnnotation(ConfigEditorKeybind.class);
			if (configEditorKeybind != null) {
				category.options.add(new OneFancyKeybind(
					optionField,
					categoryInstance,
					annotation.name(),
					annotation.desc(),
					cat, subcategory, 1
				));
			}
			ConfigEditorColour configEditorColour = optionField.getAnnotation(ConfigEditorColour.class);
			if (configEditorColour != null) {
				category.options.add(new OneFancyColor(
					optionField,
					categoryInstance,
					annotation.name(),
					annotation.desc(),
					cat, subcategory, 1, true
				));
			}
			ConfigEditorSlider configEditorSlider = optionField.getAnnotation(ConfigEditorSlider.class);
			if (configEditorSlider != null) {
				category.options.add(new ConfigSlider(
					optionField,
					categoryInstance,
					annotation.name(),
					annotation.desc(),
					cat,
					subcategory,
					configEditorSlider.minValue(),
					configEditorSlider.maxValue(),
					(int) configEditorSlider.minStep()
				));
			}
			ConfigEditorButton configEditorButton = optionField.getAnnotation(ConfigEditorButton.class);
			if (configEditorButton != null) {
				category.options.add(new ConfigButton(
					() -> moulConfig.executeRunnable(configEditorButton.runnableId()),
					categoryInstance,
					annotation.name(),
					annotation.desc(),
					cat,
					subcategory,
					1, configEditorButton.buttonText()
				));
			}
			ConfigEditorDropdown configEditorDropdown = optionField.getAnnotation(ConfigEditorDropdown.class);
			if (configEditorDropdown != null) {
				category.options.add(new ConfigDropdown(
					optionField,
					categoryInstance,
					annotation.name(),
					annotation.desc(),
					cat, subcategory,
					1, configEditorDropdown.values()
				));
			}
			ConfigEditorDraggableList configEditorDraggableList = optionField.getAnnotation(ConfigEditorDraggableList.class);
			if (configEditorDraggableList != null) {
				category.options.add(new ConfigInfo(
					optionField, categoryInstance,
					"This option (" + annotation.name() + ") is not available via the oneconfig gui",
					cat, subcategory, 1, InfoType.ERROR
				));
			}
			ConfigEditorFSR configEditorFSR = optionField.getAnnotation(ConfigEditorFSR.class);
			if (configEditorFSR != null) {
				category.options.add(new ConfigInfo(
					optionField, categoryInstance,
					annotation.desc(), cat, subcategory, 1, InfoType.WARNING
				));
			}
		}
	}

	@Override
	public void save() {
		saveCallback.run();
	}

	@Override
	public void load() {
	}

	@Override
	public boolean supportsProfiles() {
		return false;
	}

}
