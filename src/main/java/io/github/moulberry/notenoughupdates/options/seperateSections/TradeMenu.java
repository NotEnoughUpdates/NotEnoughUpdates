package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class TradeMenu {
	@Expose
	@ConfigOption(
		name = "Enable Custom Trade Menu",
		desc = "When trading with other players in skyblock, display a special GUI designed to prevent scamming"
	)
	@ConfigEditorBoolean
	public boolean enableCustomTrade = true;

	@Expose
	@ConfigOption(
		name = "Price Information",
		desc = "Show the price of items in the trade window on both sides"
	)
	@ConfigEditorBoolean
	public boolean customTradePrices = true;

	@Expose
	public boolean customTradePriceStyle = true;
}
