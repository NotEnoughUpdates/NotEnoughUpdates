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
