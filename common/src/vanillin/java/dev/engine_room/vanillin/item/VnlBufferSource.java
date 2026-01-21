package dev.engine_room.vanillin.item;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleQuadMesh;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.LinkedList;
import java.util.List;
import java.util.SequencedMap;

public class VnlBufferSource extends MultiBufferSource.BufferSource {
	private List<Model.ConfiguredMesh> emittedMeshes = new LinkedList<>();

	protected VnlBufferSource(ByteBufferBuilder sharedBuffer, SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers) {
		super(sharedBuffer, fixedBuffers);
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return super.getBuffer(renderType);
	}

	@Override
	public void endBatch(RenderType renderType) {
		var material = ModelUtil.getItemMaterial(renderType);

		if (material == null) {
			material = Materials.TRANSLUCENT_ITEM_ENTITY_ITEM;
		}

		BufferBuilder bufferBuilder = this.startedBuilders.remove(renderType);
		if (bufferBuilder != null) {
			emitMesh(material, bufferBuilder);
		}
	}

	protected void emitMesh(Material material, BufferBuilder bufferBuilder)  {
		emittedMeshes.add(new Model.ConfiguredMesh(material, new SimpleQuadMesh()));
	}

	public List<Model.ConfiguredMesh> popAllMeshes() {
		var copy = List.copyOf(emittedMeshes);
		emittedMeshes = new LinkedList<>();
		return copy;
	}


	public static VnlBufferSource immediate(ByteBufferBuilder sharedBuffer) {
		return immediateWithBuffers(Object2ObjectSortedMaps.emptyMap(), sharedBuffer);
	}

	public static VnlBufferSource immediateWithBuffers(SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers, ByteBufferBuilder sharedBuffer) {
		return new VnlBufferSource(sharedBuffer, fixedBuffers);
	}
}
