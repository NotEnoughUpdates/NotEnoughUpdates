/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.loader;

import lombok.SneakyThrows;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Tweaker used to load library JARs conditionally. See the subclasses for concrete implementations.
 */
public abstract class JARLoadingTweaker implements ITweaker {

	/**
	 * @return a path pointing to a folder filled with JAR files
	 */
	protected abstract Path getFilesToLoad();

	protected abstract String getTestClass();

	@SneakyThrows
	protected @Nullable Path getShadowedElement(String path) {
		URI uri = Objects.requireNonNull(getClass().getResource(path)).toURI();
		if ("jar".equals(uri.getScheme())) {
			FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
			closeFileSystemLater(fs);
			return fs.getPath(path);
		} else {
			return Paths.get(uri);
		}
	}

	private List<FileSystem> toClose = new ArrayList<>();

	protected void closeFileSystemLater(FileSystem fileSystem) {
		toClose.add(fileSystem);
	}

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

	}

	protected void performLoading(LaunchClassLoader classLoader) {
		try {
			if (Launch.blackboard.get("fml.deobfuscatedEnvironment") == Boolean.TRUE) {
				System.out.println("Skipping JAR loading in development environment.");
				return;
			}

			Path p = getFilesToLoad();

			System.out.println("Loading a JAR from " + p.toAbsolutePath());
			Path tempDirectory = Files.createTempDirectory("notenoughupdates-extracted-" + getClass().getSimpleName());
			System.out.println("Using temporary directory " + tempDirectory + " to store extracted jars.");
			tempDirectory.toFile().deleteOnExit();
			try (Stream<Path> libraries = Files.walk(p, 1)) {
				libraries.filter(it -> it.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
								 .forEach(it -> {
									 try {
										 Path extractedPath = tempDirectory.resolve(it.getFileName().toString());
										 extractedPath.toFile().deleteOnExit();
										 Files.copy(it, extractedPath);
										 ClassLoaderExtUtil.addClassSourceTwice(classLoader, extractedPath.toUri().toURL());
									 } catch (Exception e) {
										 throw new RuntimeException(e);
									 }
								 });
			}
			classLoader.loadClass(getTestClass());
			System.out.println("Could successfully load a class from loaded library.");
		} catch (Throwable e) {
			System.err.println("Failed to load a JAR into NEU. This is most likely a bad thing.");
			e.printStackTrace();
		} finally {
			Iterator<FileSystem> iterator = toClose.iterator();
			while (iterator.hasNext())
				try {
					iterator.next().close();
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					iterator.remove();
				}
		}

	}

	@Override
	public String getLaunchTarget() {
		return null;
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}
}
