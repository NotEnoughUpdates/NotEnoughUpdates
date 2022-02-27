package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorSlider;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class NeuAuctionHouse {
	@Expose
	@ConfigOption(
		name = "Enable NeuAH",
		desc = "Turn on the NEU Auction House. \u00A7cWARNING: May negatively impact performance on low-end machines"
	)
	@ConfigEditorBoolean
	public boolean enableNeuAuctionHouse = false;

	@Expose
	@ConfigOption(
		name = "Disable AH Scroll",
		desc = "Disable scrolling using the scroll wheel inside NeuAH.\n" +
			"This should be used if you want to be able to scroll through tooltips"
	)
	@ConfigEditorBoolean
	public boolean disableAhScroll = false;

	@Expose
	@ConfigOption(
		name = "AH Notification (Mins)",
		desc = "Change the amount of time (in minutes) before the \"Ending Soon\" notification for an auction you have bid on"
	)
	@ConfigEditorSlider(
		minValue = 1f,
		maxValue = 10f,
		minStep = 1f
	)
	public int ahNotification = 5;

	@Expose
	@ConfigOption(
		name = "Price Filtering in NEU AH",
		desc = "The ability to filter the price of items and their respective average BIN values"
	)
	@ConfigEditorBoolean
	public boolean priceFiltering = false;
}
