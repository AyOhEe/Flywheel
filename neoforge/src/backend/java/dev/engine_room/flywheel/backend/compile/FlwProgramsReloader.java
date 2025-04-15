package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.backend.NoiseTextures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class FlwProgramsReloader implements ResourceManagerReloadListener {
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Flywheel.ID, "flw_programs_reloader");
	public static final FlwProgramsReloader INSTANCE = new FlwProgramsReloader();

	private FlwProgramsReloader() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);
		NoiseTextures.reload(manager);
	}
}
