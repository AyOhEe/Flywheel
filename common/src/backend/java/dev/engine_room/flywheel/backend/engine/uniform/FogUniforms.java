package dev.engine_room.flywheel.backend.engine.uniform;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

import org.joml.Vector4f;

import java.util.List;

public final class FogUniforms extends UniformWriter {
	private static final int SIZE = 4 * 10;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);

	public static void update(Camera camera, int renderDistance, DeltaTracker deltaTracker, List<FogEnvironment> fogEnvironments, ClientLevel level, FogType fogType, Vector4f fogColour) {
		long ptr = BUFFER.ptr();

		float g = (float)(renderDistance * 16);
		Entity entity = camera.entity();
		FogData fogData = new FogData();

		for(FogEnvironment fogEnvironment : fogEnvironments) {
			if (fogEnvironment.isApplicable(fogType, entity)) {
				fogEnvironment.setupFog(fogData, camera, level, g, deltaTracker);
				break;
			}
		}

		float h = Mth.clamp(g / 10.0F, 4.0F, 64.0F);
		fogData.renderDistanceStart = g - h;
		fogData.renderDistanceEnd = g;

		ptr = writeVec4(ptr, fogColour.x, fogColour.y, fogColour.z, fogColour.w);
		ptr = writeFloat(ptr, fogData.environmentalStart);
		ptr = writeFloat(ptr, fogData.environmentalEnd);
		ptr = writeFloat(ptr, fogData.renderDistanceStart);
		ptr = writeFloat(ptr, fogData.renderDistanceEnd);
		ptr = writeFloat(ptr, fogData.skyEnd);
		ptr = writeFloat(ptr, fogData.cloudEnd);

		BUFFER.markDirty();
	}
}
