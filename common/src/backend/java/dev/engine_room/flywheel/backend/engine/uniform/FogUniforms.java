package dev.engine_room.flywheel.backend.engine.uniform;

import com.mojang.blaze3d.systems.RenderSystem;

public final class FogUniforms extends UniformWriter {
	private static final int SIZE = 4 * 7;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);

	public static void update() {
		long ptr = BUFFER.ptr();

		var fog = RenderSystem.getShaderFog();

		ptr = writeFloat(ptr, fog.red());
		ptr = writeFloat(ptr, fog.green());
		ptr = writeFloat(ptr, fog.blue());
		ptr = writeFloat(ptr, fog.alpha());
		ptr = writeFloat(ptr, fog.start());
		ptr = writeFloat(ptr, fog.end());

		var fogShape = fog.shape();
		// Shouldn't ever be null, but we've seen crashes here.
		ptr = writeInt(ptr, (fogShape == null ? FogShape.SPHERE : fogShape).getIndex());

		BUFFER.markDirty();
	}
}
