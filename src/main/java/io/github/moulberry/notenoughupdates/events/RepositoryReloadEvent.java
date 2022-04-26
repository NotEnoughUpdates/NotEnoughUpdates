package io.github.moulberry.notenoughupdates.events;

import java.io.File;

public class RepositoryReloadEvent extends NEUEvent {
	private final File baseFile;

	public RepositoryReloadEvent(File baseFile) {
		this.baseFile = baseFile;
	}

	public File getRepositoryRoot() {
		return baseFile;
	}
}
