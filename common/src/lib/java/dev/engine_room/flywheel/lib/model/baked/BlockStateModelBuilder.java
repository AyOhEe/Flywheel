package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public final class BlockStateModelBuilder {
	final BlockStateModel blockStateModel;
	@Nullable
	BlockAndTintGetter level;
	@Nullable
	BlockPos pos;
	@Nullable
	PoseStack poseStack;
	@Nullable
	BiFunction<ChunkSectionLayer, Boolean, Material> materialFunc;

	public BlockStateModelBuilder(BlockStateModel blockStateModel) {
		this.blockStateModel = blockStateModel;
	}

	public BlockStateModelBuilder level(@Nullable BlockAndTintGetter level) {
		this.level = level;
		return this;
	}

	public BlockStateModelBuilder pos(@Nullable BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public BlockStateModelBuilder poseStack(@Nullable PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BlockStateModelBuilder materialFunc(@Nullable BiFunction<ChunkSectionLayer, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public SimpleModel build() {
		if (level == null) {
			level = EmptyVirtualBlockGetter.FULL_DARK;
		}
		if (pos == null) {
			pos = BlockPos.ZERO;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getMaterial;
		}

		return FlwLibXplat.INSTANCE.buildBakedModelBuilder(this);
	}
}
