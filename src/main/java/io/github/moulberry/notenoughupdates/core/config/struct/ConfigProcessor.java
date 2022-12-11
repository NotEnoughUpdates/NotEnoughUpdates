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

package io.github.moulberry.notenoughupdates.core.config.struct;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.Config;
import io.github.moulberry.notenoughupdates.core.config.annotations.Category;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigAccordionId;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorAccordion;
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
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditor;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorAccordion;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorButton;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorColour;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorDraggableList;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorFSR;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorKeybind;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiOptionEditorText;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;

public class ConfigProcessor {
	public static class ProcessedCategory {
		public final String name;
		public final String desc;
		public final LinkedHashMap<String, ProcessedOption> options = new LinkedHashMap<>();

		public ProcessedCategory(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}
	}

	public static class ProcessedOption {
		public final String name;
		public final String desc;
		public final int subcategoryId;
		public GuiOptionEditor editor;

		public int accordionId = -1;
		public final String[] searchTags;

		private final Field field;
		private final Object container;

		public ProcessedOption(String name, String desc, int subcategoryId, Field field, Object container, String[] searchTags) {
			this.name = name;
			this.desc = desc;
			this.subcategoryId = subcategoryId;

			this.field = field;
			this.container = container;
			this.searchTags = searchTags;
		}

		public Object get() {
			try {
				return field.get(container);
			} catch (Exception e) {
				return null;
			}
		}

		public boolean set(Object value) {
			try {
				if (field.getType() == int.class && value instanceof Number) {
					field.set(container, ((Number) value).intValue());
				} else {
					field.set(container, value);
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public static LinkedHashMap<String, ProcessedCategory> create(Config config) {
		LinkedHashMap<String, ProcessedCategory> processedConfig = new LinkedHashMap<>();
		for (Field categoryField : config.getClass().getDeclaredFields()) {
			boolean exposePresent = categoryField.isAnnotationPresent(Expose.class);
			boolean categoryPresent = categoryField.isAnnotationPresent(Category.class);

			if (exposePresent && categoryPresent) {
				Object categoryObj;
				try {
					categoryObj = categoryField.get(config);
				} catch (Exception e) {
					//System.err.printf("Failed to load config category %s. Field was not accessible.\n", categoryField.getName());
					continue;
				}

				Category categoryAnnotation = categoryField.getAnnotation(Category.class);
				ProcessedCategory cat = new ProcessedCategory(
					categoryAnnotation.name(),
					categoryAnnotation.desc()
				);
				processedConfig.put(categoryField.getName(), cat);

				for (Field optionField : categoryObj.getClass().getDeclaredFields()) {
					boolean optionPresent = optionField.isAnnotationPresent(ConfigOption.class);

					if (optionPresent) {
						ConfigOption optionAnnotation = optionField.getAnnotation(ConfigOption.class);
						ProcessedOption option = new ProcessedOption(
							optionAnnotation.name(),
							optionAnnotation.desc(),
							optionAnnotation.subcategoryId(),
							optionField,
							categoryObj,
							optionAnnotation.searchTags()
						);
						if (optionField.isAnnotationPresent(ConfigAccordionId.class)) {
							ConfigAccordionId annotation = optionField.getAnnotation(ConfigAccordionId.class);
							option.accordionId = annotation.id();
						}

						GuiOptionEditor editor = null;
						Class<?> optionType = optionField.getType();
						if (optionType.isAssignableFrom(int.class) &&
							optionField.isAnnotationPresent(ConfigEditorKeybind.class)) {
							ConfigEditorKeybind configEditorAnnotation = optionField.getAnnotation(ConfigEditorKeybind.class);
							editor = new GuiOptionEditorKeybind(option, (int) option.get(), configEditorAnnotation.defaultKey());
						}
						if (optionField.isAnnotationPresent(ConfigEditorButton.class)) {
							ConfigEditorButton configEditorAnnotation = optionField.getAnnotation(ConfigEditorButton.class);
							editor = new GuiOptionEditorButton(
								option,
								configEditorAnnotation.runnableId(),
								configEditorAnnotation.buttonText(),
								config
							);
						}
						if (optionField.isAnnotationPresent(ConfigEditorFSR.class)) {
							ConfigEditorFSR configEditorAnnotation = optionField.getAnnotation(ConfigEditorFSR.class);
							editor = new GuiOptionEditorFSR(
								option,
								configEditorAnnotation.runnableId(),
								configEditorAnnotation.buttonText(),
								config
							);
						}
						if (optionType.isAssignableFrom(boolean.class) &&
							optionField.isAnnotationPresent(ConfigEditorBoolean.class)) {
							ConfigEditorBoolean configEditorAnnotation = optionField.getAnnotation(ConfigEditorBoolean.class);
							editor = new GuiOptionEditorBoolean(option, configEditorAnnotation.runnableId(), config);
						}
						if (optionType.isAssignableFrom(boolean.class) &&
							optionField.isAnnotationPresent(ConfigEditorAccordion.class)) {
							ConfigEditorAccordion configEditorAnnotation = optionField.getAnnotation(ConfigEditorAccordion.class);
							editor = new GuiOptionEditorAccordion(option, configEditorAnnotation.id());
						}
						if (optionType.isAssignableFrom(int.class)) {
							if (optionField.isAnnotationPresent(ConfigEditorDropdown.class)) {
								ConfigEditorDropdown configEditorAnnotation = optionField.getAnnotation(ConfigEditorDropdown.class);
								editor = new GuiOptionEditorDropdown(option, configEditorAnnotation.values(), (int) option.get(), true);
							}
						}
						if (optionType.isAssignableFrom(List.class)) {
							if (optionField.isAnnotationPresent(ConfigEditorDraggableList.class)) {
								ConfigEditorDraggableList configEditorAnnotation =
									optionField.getAnnotation(ConfigEditorDraggableList.class);
								editor = new GuiOptionEditorDraggableList(
									option,
									configEditorAnnotation.exampleText(),
									configEditorAnnotation.allowDeleting()
								);
							}
						}
						if (optionType.isAssignableFrom(String.class)) {
							if (optionField.isAnnotationPresent(ConfigEditorDropdown.class)) {
								ConfigEditorDropdown configEditorAnnotation = optionField.getAnnotation(ConfigEditorDropdown.class);
								editor = new GuiOptionEditorDropdown(option, configEditorAnnotation.values(),
									configEditorAnnotation.initialIndex(), false
								);
							} else if (optionField.isAnnotationPresent(ConfigEditorColour.class)) {
								editor = new GuiOptionEditorColour(option);
							} else if (optionField.isAnnotationPresent(ConfigEditorText.class)) {
								editor = new GuiOptionEditorText(option);
							}
						}
						if (optionType.isAssignableFrom(int.class) ||
							optionType.isAssignableFrom(float.class) ||
							optionType.isAssignableFrom(double.class)) {
							if (optionField.isAnnotationPresent(ConfigEditorSlider.class)) {
								ConfigEditorSlider configEditorAnnotation = optionField.getAnnotation(ConfigEditorSlider.class);
								editor = new GuiOptionEditorSlider(option, configEditorAnnotation.minValue(),
									configEditorAnnotation.maxValue(), configEditorAnnotation.minStep()
								);
							}
						}
						if (optionType.isAssignableFrom(String.class)) {
							if (optionField.isAnnotationPresent(ConfigEditorDropdown.class)) {
								ConfigEditorDropdown configEditorAnnotation = optionField.getAnnotation(ConfigEditorDropdown.class);
								editor = new GuiOptionEditorDropdown(option, configEditorAnnotation.values(), 0, false);
							}
						}
						if (editor == null) {
							//System.err.printf("Failed to load config option %s. Could not find suitable editor.\n", optionField.getName());
							continue;
						}
						option.editor = editor;
						cat.options.put(optionField.getName(), option);
					}
				}
			} else if (exposePresent || categoryPresent) {
				//System.err.printf("Failed to load config category %s. Both @Expose and @Category must be present.\n", categoryField.getName());
			}
		}
		return processedConfig;
	}
}
