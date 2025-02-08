package net.jrdemiurge.skyarena.util;

import net.jrdemiurge.skyarena.SkyArena;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ModTags {

    public static final TagKey<Structure> EYE_OF_SKY_LOCATED = registerStructureTag("eye_of_sky_located");

    public static final TagKey<Structure> EYE_OF_ICE_LOCATED = registerStructureTag("eye_of_ice_located");

    private static TagKey<Structure> registerStructureTag(String name) {
        return TagKey.create(Registries.STRUCTURE, new ResourceLocation(SkyArena.MOD_ID, name));
    }
}
