package dev.engine_room.flywheel.backend.mixin;

import dev.engine_room.flywheel.backend.engine.uniform.FogUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;

import net.minecraft.client.renderer.fog.environment.FogEnvironment;

import net.minecraft.world.level.material.FogType;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FogRenderer.class, remap = false)
abstract class FogRendererMixin {
	@Shadow
	@Final
	private static List<FogEnvironment> FOG_ENVIRONMENTS;

	@Shadow
	protected abstract FogType getFogType(Camera camera);

	@Inject(method = "setupFog", at = @At("RETURN"))
	private void flywheel$setupFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<Vector4f> cir) {
		FogUniforms.update(camera, renderDistance, deltaTracker, FOG_ENVIRONMENTS, level, getFogType(camera), cir.getReturnValue());
	}
}
