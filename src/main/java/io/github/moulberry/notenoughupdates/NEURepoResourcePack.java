package io.github.moulberry.notenoughupdates;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class NEURepoResourcePack implements IResourcePack {

    File repoLocation;
    Set<String> resourceDomains = new HashSet<>();

    public NEURepoResourcePack(File repoLocation, String domain) {
        this.repoLocation = repoLocation;
        resourceDomains.add(domain);
    }

    public boolean loadRepoLocation() {
        if (repoLocation != null) return true;
        NotEnoughUpdates instance = NotEnoughUpdates.INSTANCE;
        if (instance == null) return false;
        NEUManager manager = instance.manager;
        if (manager == null) return false;
        repoLocation = manager.repoLocation;
        return repoLocation != null;
    }

    public File getFileForResource(ResourceLocation loc) {
        if (repoLocation == null) {
            if (!loadRepoLocation())
                return null;
        }
        if (!"neurepo".equals(loc.getResourceDomain())) {
            return null;
        }
        return new File(repoLocation, loc.getResourcePath());
    }

    @Override
    public InputStream getInputStream(ResourceLocation resourceLocation) throws IOException {
        return new BufferedInputStream(new FileInputStream(getFileForResource(resourceLocation)));
    }

    @Override
    public boolean resourceExists(ResourceLocation resourceLocation) {
        File file = getFileForResource(resourceLocation);
        return file != null && file.exists();
    }

    @Override
    public Set<String> getResourceDomains() {
        return resourceDomains;
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer iMetadataSerializer, String s) throws IOException {
        return iMetadataSerializer.parseMetadataSection(s, new JsonObject());
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return "NEU Repo Resources";
    }
}
