package dev.engine_room.flywheel.lib.internal;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockStateModelBuilder;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;

public interface FlwLibXplat {
	FlwLibXplat INSTANCE = DependencyInjection.load(FlwLibXplat.class, "dev.engine_room.flywheel.impl.FlwLibXplatImpl");

	@UnknownNullability
	BlockStateModel getBlockstateModel(ModelManager modelManager, ResourceLocation location);

	SimpleModel buildBakedModelBuilder(BlockStateModelBuilder builder);

	SimpleModel buildBlockModelBuilder(BlockModelBuilder builder);
}
