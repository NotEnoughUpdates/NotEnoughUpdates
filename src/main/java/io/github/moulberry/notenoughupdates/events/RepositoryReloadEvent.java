package io.github.moulberry.notenoughupdates.events;

import java.io.File;

public class RepositoryReloadEvent extends NEUEvent {
	private final File baseFile;
	private boolean isFirstLoad;

	public RepositoryReloadEvent(File baseFile, boolean isFirstLoad) {
		this.baseFile = baseFile;
		this.isFirstLoad = isFirstLoad;
	}

	public boolean isFirstLoad() {
		return isFirstLoad;
	}

	public File getRepositoryRoot() {
		return baseFile;
	}
}
