package dev.engine_room.flywheel.backend.engine;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;

import dev.engine_room.flywheel.backend.Samplers;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class TextureBinder {
	public static void bind(ResourceLocation resourceLocation) {
		GlStateManager._bindTexture(byName(resourceLocation));
	}

	public static void bindLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		// TODO - Review
		Samplers.OVERLAY.makeActive();
		gameRenderer.overlayTexture()
				.setupOverlayColor();
		GlTexture glTexture1 = (GlTexture) RenderSystem.getShaderTexture(1).texture();
		GlStateManager._bindTexture(glTexture1.glId());

		Samplers.LIGHT.makeActive();
		gameRenderer.lightTexture()
				.turnOnLightLayer();
		GlTexture glTexture2 = (GlTexture) RenderSystem.getShaderTexture(2).texture();
		GlStateManager._bindTexture(glTexture2.glId());
	}

	public static void resetLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;

		gameRenderer.overlayTexture()
				.teardownOverlayColor();
		gameRenderer.lightTexture()
				.turnOffLightLayer();
	}

	/**
	 * Get a built-in texture by its resource location.
	 *
	 * @param texture The texture's resource location.
	 * @return The texture.
	 */
	public static int byName(ResourceLocation texture) {
		GpuTexture gpuTexture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(texture)
				.getTexture();
		return ((GlTexture) gpuTexture).glId();
	}
}
