package dev.engine_room.flywheel.lib.model.baked;

import net.minecraft.client.renderer.rendertype.RenderType;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.material.Material;

public interface BlockMaterialFunction {
	@Nullable
	Material apply(RenderType chunkRenderType, boolean shaded, boolean ambientOcclusion);
}
