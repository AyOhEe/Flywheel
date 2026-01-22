package dev.engine_room.vanillin.item;

import java.util.List;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.vanillin.Vanillin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemModels {
	public static final TagKey<Item> NO_INSTANCING = TagKey.create(Registries.ITEM, Vanillin.id("no_instancing"));

	private static final Model EMPTY_MODEL = new SimpleModel(List.of());
	//private static final RendererReloadCache<BakedMeshKey, Mesh> MESH_CACHE = new RendererReloadCache<>(key -> bakeMesh(key.model(), key.displayContext()));
	//private static final RendererReloadCache<BakedModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(key -> bakeModel(key.model(), key.displayContext(), key.outlineColor(), key.foil()));

	private static final @Nullable Direction[] DIRECTIONS = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

	private static SubmitNodeStorage SUBMIT_STORAGE = new SubmitNodeStorage();
	private static FeatureRenderDispatcher FEATURE_RENDERER = new FeatureRenderDispatcher(
			SUBMIT_STORAGE,
			Minecraft.getInstance().getBlockRenderer(),
			Buffers.RENDER_BUFFER,
			Minecraft.getInstance().getAtlasManager(),
			Buffers.OUTLINE_BUFFER,
			Buffers.CRUMBLING_BUFFER,
			Minecraft.getInstance().font
	);

	public static boolean isSupported(ItemStack stack) {
		return !stack.is(NO_INSTANCING) && doesNotHaveItemColors(stack.getItem());// && isValidItemState(stack);
	}

	private static boolean doesNotHaveItemColors(Item item) {
		return true;//VanillinXplat.INSTANCE.itemColors(item) == null;
	}

	public static ItemModel getModel(ItemStack stack) {
		return Minecraft.getInstance()
				.getModelManager()
				.getItemModel(stack.get(DataComponents.ITEM_MODEL));
	}

	public static int glowColour(Entity entity) {
		return Minecraft.getInstance().shouldEntityAppearGlowing(entity) ? ARGB.opaque(entity.getTeamColor()) : 0;
	}

	public static Model get(Level level, ItemStack itemStack, ItemDisplayContext displayContext, int outlineColor) {
		// Visuals all hold references to Level so use that as the parameter type for convenience and cast here.
		ClientLevel clientLevel = (level instanceof ClientLevel) ? (ClientLevel) level : null;

		if (itemStack.isEmpty()) {
			return EMPTY_MODEL;
		}

		var model = getModel(itemStack);

		return bakeModel(model, displayContext, outlineColor, itemStack.hasFoil(), itemStack, clientLevel);
	}

	//TODO this SUCKS - doesn't cache
	public static Model bakeModel(ItemModel model, ItemDisplayContext displayContext, int outlineColor, boolean foil, ItemStack stack, ClientLevel level) {
		//var mesh = MESH_CACHE.get(new BakedMeshKey(model, displayContext));


		ItemStackRenderState renderState = new ItemStackRenderState();
		PoseStack poseStack = new PoseStack();

		model.update(renderState, stack, Minecraft.getInstance().getItemModelResolver(), displayContext, level, null, 0);
		renderState.submit(poseStack, SUBMIT_STORAGE, 0, OverlayTexture.NO_OVERLAY, outlineColor);
		FEATURE_RENDERER.renderAllFeatures();

		Buffers.CRUMBLING_BUFFER.popAllMeshes(); // Probably empty. We're working with items. Best to clear it anyway.
		var meshes = Buffers.RENDER_BUFFER.popAllMeshes();
		var outlineMeshes = Buffers.OUTLINE_BUFFER.popAllMeshes();
		List<Model.ConfiguredMesh> allMeshes = Stream.concat(meshes.stream(), outlineMeshes.stream()).toList();

		if (stack.getItem() instanceof BlockItem) {
			allMeshes = allMeshes.stream().map((cMesh) -> {
				var newMaterial = SimpleMaterial.builderOf(cMesh.material())
						.transparency(Transparency.ORDER_INDEPENDENT)
						.build();
				return new Model.ConfiguredMesh(newMaterial, cMesh.mesh());
			}).toList();
		}

		return new SimpleModel(foil ? applyGlintToMeshes(allMeshes) : allMeshes);
	}

	private static List<Model.ConfiguredMesh> applyGlintToMeshes(List<Model.ConfiguredMesh> allMeshes) {
		return allMeshes.stream().mapMulti((Model.ConfiguredMesh mesh, Consumer<Model.ConfiguredMesh> downstream) -> {
			downstream.accept(mesh);
			downstream.accept(new Model.ConfiguredMesh(Materials.GLINT, mesh.mesh()));
		}).toList();
	}


//	public static Mesh bakeMesh(ItemModel model, ItemDisplayContext displayContext) {
//		boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
//
//		var poseStack = new PoseStack();
//		poseStack.translate(-0.5f, -0.5f, -0.5f);
//
//		RandomSource randomSource = RandomSource.create();
//
//		// Write all BakedQuads into a list first to get a count so we can allocate memory ahead of time.
//		List<BakedQuad> allQuads = new ArrayList<>();
//		for (Direction value : DIRECTIONS) {
//			randomSource.setSeed(42L);
//			allQuads.addAll(model.getQuads(null, value, randomSource));
//		}
//
//		int vertexCount = allQuads.size() * 4;
//		var memoryBlock = MemoryBlock.mallocTracked(vertexCount * FullVertexView.STRIDE);
//		var meshVertices = new FullVertexView();
//
//		meshVertices.nativeMemoryOwner(memoryBlock);
//		meshVertices.ptr(memoryBlock.ptr());
//		meshVertices.vertexCount(vertexCount);
//
//		Vector4f position = new Vector4f();
//		Vector3f normal = new Vector3f();
//
//		try (MemoryStack memoryStack = MemoryStack.stackPush();) {
//			int vertex = 0;
//
//			for (BakedQuad quad : allQuads) {
//				SodiumAnimatedTextureCompat.add(quad.sprite());
//
//				var direction = quad.direction();
//
//				normal.set(direction.getStepX(), direction.getStepY(), direction.getStepZ());
//
//				for (int i = 0; i < 4; ++i) {
//					position.set(
//							quad.position(i).x(),
//							quad.position(i).y(),
//							quad.position(i).z(),
//							1.0f
//					);
//
//					// We could probably handle item colors here.
//					meshVertices.x(vertex, position.x());
//					meshVertices.y(vertex, position.y());
//					meshVertices.z(vertex, position.z());
//					meshVertices.r(vertex, 1.0f);
//					meshVertices.g(vertex, 1.0f);
//					meshVertices.b(vertex, 1.0f);
//					meshVertices.a(vertex, 1.0f);
//					meshVertices.u(vertex, UVPair.unpackU(quad.packedUV(i)));
//					meshVertices.v(vertex, UVPair.unpackV(quad.packedUV(i)));
//					meshVertices.overlay(vertex, OverlayTexture.NO_OVERLAY);
//					meshVertices.light(vertex, 0);
//					meshVertices.normalX(vertex, normal.x());
//					meshVertices.normalY(vertex, normal.y());
//					meshVertices.normalZ(vertex, normal.z());
//
//					vertex++;
//				}
//			}
//		}
//
//		return new SimpleQuadMesh(meshVertices);
//	}

	//public record BakedModelKey(ItemModel model, ItemDisplayContext displayContext, int outlineColor, boolean foil) { }
	//public record BakedMeshKey(ItemModel model, ItemDisplayContext displayContext) { }

	private static class Buffers {
		public static VnlBufferSource RENDER_BUFFER = createRenderBuffer();
		public static VnlBufferSource CRUMBLING_BUFFER = createCrumblingBuffer();
		public static VnlOutlineBuffer OUTLINE_BUFFER = createOutlineBuffer();

		private static VnlBufferSource createRenderBuffer() {
			SectionBufferBuilderPack sectionBuffer = new SectionBufferBuilderPack();
			SequencedMap<RenderType, ByteBufferBuilder> sequencedMap = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
				map.put(Sheets.solidBlockSheet(), sectionBuffer.buffer(ChunkSectionLayer.SOLID));
				map.put(Sheets.cutoutBlockSheet(), sectionBuffer.buffer(ChunkSectionLayer.CUTOUT));
				map.put(Sheets.translucentItemSheet(), sectionBuffer.buffer(ChunkSectionLayer.TRANSLUCENT));
				put(map, Sheets.translucentBlockItemSheet());
				put(map, Sheets.shieldSheet());
				put(map, Sheets.bedSheet());
				put(map, Sheets.shulkerBoxSheet());
				put(map, Sheets.signSheet());
				put(map, Sheets.hangingSignSheet());
				map.put(Sheets.chestSheet(), new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE));
				put(map, RenderTypes.armorEntityGlint());
				put(map, RenderTypes.glint());
				put(map, RenderTypes.glintTranslucent());
				put(map, RenderTypes.entityGlint());
				put(map, RenderTypes.waterMask());
			});
			return VnlBufferSource.immediateWithBuffers(sequencedMap, new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE));
		}

		private static VnlBufferSource createCrumblingBuffer() {
			SequencedMap<RenderType, ByteBufferBuilder> sequencedMap2 = Util.make(
					new Object2ObjectLinkedOpenHashMap<>(),
					(map) -> ModelBakery.DESTROY_TYPES.forEach((renderType) -> put(map, renderType)));
			return VnlBufferSource.immediateWithBuffers(sequencedMap2, new ByteBufferBuilder(0));
		}

		private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, RenderType renderType) {
			map.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
		}

		private static VnlOutlineBuffer createOutlineBuffer() {
			return new VnlOutlineBuffer();
		}
	}
}
