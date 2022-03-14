package io.github.moulberry.notenoughupdates.options.customtypes;

public enum NEUDebugFlag {
	// NOTE: Removing enum values causes gson to remove all debugFlags on load if any removed value is present
	METAL,
	WISHING,
	;

	public static final String FLAG_LIST =
		"METAL    - Metal Detector Solver\n" +
		"WISHING  - Wishing Compass Solver";
}
