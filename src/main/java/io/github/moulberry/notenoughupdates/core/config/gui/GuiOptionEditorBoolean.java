package io.github.moulberry.notenoughupdates.core.config.gui;

import io.github.moulberry.notenoughupdates.core.GuiElementBoolean;
import io.github.moulberry.notenoughupdates.core.config.Config;
import io.github.moulberry.notenoughupdates.core.config.struct.ConfigProcessor;

public class GuiOptionEditorBoolean extends GuiOptionEditor {

	private final GuiElementBoolean bool;
	private final Config config;
	private final int runnableId;

	public GuiOptionEditorBoolean(
		ConfigProcessor.ProcessedOption option,
		int runnableId,
		Config config
	) {
		super(option);
		this.config = config;
		this.runnableId = runnableId;
		bool = new GuiElementBoolean(0, 0, (boolean) option.get(),10,	(value) -> onUpdate(option, value));
	}

	@Override
	public void render(int x, int y, int width) {
		super.render(x, y, width);
		int height = getHeight();

		bool.x = x + width / 6 - 24;
		bool.y = y + height - 7 - 14;
		bool.render();
	}

	@Override
	public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
		int height = getHeight();
		bool.x = x + width / 6 - 24;
		bool.y = y + height - 7 - 14;
		return bool.mouseInput(mouseX, mouseY);
	}

	@Override
	public boolean keyboardInput() {
		return false;
	}

	private void onUpdate(ConfigProcessor.ProcessedOption option, boolean value) {
		if (option.set(value)) {
			config.executeRunnable(runnableId);
		}
	}
}
