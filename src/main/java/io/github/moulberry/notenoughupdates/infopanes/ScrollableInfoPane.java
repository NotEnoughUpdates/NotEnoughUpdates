package io.github.moulberry.notenoughupdates.infopanes;

import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import org.lwjgl.input.Mouse;

public abstract class ScrollableInfoPane extends InfoPane {
	private static final int SCROLL_AMOUNT = 50;
	protected LerpingInteger scrollHeight = new LerpingInteger(0);

	public ScrollableInfoPane(NEUOverlay overlay, NEUManager manager) {
		super(overlay, manager);
	}

	public void tick() {
		scrollHeight.tick();
		if (scrollHeight.getValue() < 0) scrollHeight.setValue(0);
	}

	@Override
	public void mouseInput(int width, int height, int mouseX, int mouseY, boolean mouseDown) {
		int dWheel = Mouse.getEventDWheel();

		if (dWheel < 0) {
			scrollHeight.setTarget(scrollHeight.getTarget() + SCROLL_AMOUNT);
			scrollHeight.resetTimer();
		} else if (dWheel > 0) {
			scrollHeight.setTarget(scrollHeight.getTarget() - SCROLL_AMOUNT);
			scrollHeight.resetTimer();
		}
	}
}
