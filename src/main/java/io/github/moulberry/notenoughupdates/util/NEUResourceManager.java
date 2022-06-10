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

package io.github.moulberry.notenoughupdates.util;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class NEUResourceManager implements IResourceManager {
	private final IResourceManager parentResourceManager;

	public NEUResourceManager(IResourceManager parentResourceManager) {
		this.parentResourceManager = parentResourceManager;
	}

	@Override
	public Set<String> getResourceDomains() {
		return parentResourceManager.getResourceDomains();
	}

	@Override
	public IResource getResource(ResourceLocation location) throws IOException {
		return parentResourceManager.getResource(forceNeuRL(location));
	}

	@Override
	public List<IResource> getAllResources(ResourceLocation location) throws IOException {
		return parentResourceManager.getAllResources(forceNeuRL(location));
	}

	private ResourceLocation forceNeuRL(ResourceLocation location) {
		return new ResourceLocation("notenoughupdates", location.getResourcePath());
	}
}
