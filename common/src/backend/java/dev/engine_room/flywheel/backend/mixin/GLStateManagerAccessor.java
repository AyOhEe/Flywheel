package dev.engine_room.flywheel.backend.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GlStateManager.class, remap = false)
public interface GLStateManagerAccessor {
	// Accessor mixin bodies are ignored on static methods
	@Accessor("activeTexture")
	static int flywheel$_getActiveTexture() {
		return 0;
	}
}
