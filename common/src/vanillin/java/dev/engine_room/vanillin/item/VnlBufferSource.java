package dev.engine_room.vanillin.item;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleQuadMesh;
import dev.engine_room.flywheel.lib.vertex.FullVertexView;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.minecraft.client.renderer.texture.OverlayTexture;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

public class VnlBufferSource extends MultiBufferSource.BufferSource {
	private List<Model.ConfiguredMesh> emittedMeshes = new LinkedList<>();
	protected Map<RenderType, VnlVertexStorage> vnlStartedBuilders = new HashMap();

	protected VnlBufferSource(ByteBufferBuilder sharedBuffer, SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers) {
		super(sharedBuffer, fixedBuffers);
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		VnlVertexStorage vertexStorage = vnlStartedBuilders.get(renderType);
		if (vertexStorage != null && !renderType.canConsolidateConsecutiveGeometry()) {
			emitMesh(renderType, vertexStorage);
			vertexStorage = null;
		}

		if (vertexStorage != null) {
			return vertexStorage;
		}

		vertexStorage = new VnlVertexStorage();
		vnlStartedBuilders.put(renderType, vertexStorage);
		return vertexStorage;
	}

	@Override
	public void endBatch(RenderType renderType) {
		VnlVertexStorage vertexStorage = this.vnlStartedBuilders.remove(renderType);
		if (vertexStorage != null) {
			emitMesh(renderType, vertexStorage);
		}
	}

	protected void emitMesh(RenderType renderType, VnlVertexStorage vertexStorage) {
		Material material = ModelUtil.getItemMaterial(renderType);
		if (material == null) {
			material = Materials.TRANSLUCENT_ITEM_ENTITY_ITEM;
		}

		int vertexCount = (vertexStorage.quadCount() * 4) + vertexStorage.vertexCount();
		var memoryBlock = MemoryBlock.mallocTracked(vertexCount * FullVertexView.STRIDE);
		var meshVertices = new FullVertexView();

		meshVertices.nativeMemoryOwner(memoryBlock);
		meshVertices.ptr(memoryBlock.ptr());
		meshVertices.vertexCount(vertexCount);


		Vector3fc position = new Vector3f();
		BakedQuad quad = null;

		int vertex = 0;
		for (VnlVertexStorage.PosedQuad posedQuad : vertexStorage.allQuads()) {
			quad = posedQuad.quad();
			// This is literally the entire reason we poach quads.
			SodiumAnimatedTextureCompat.add(quad.sprite());

			for (int i = 0; i < 4; i++) {
				position = quad.position(i);

				meshVertices.x(vertex, position.x());
				meshVertices.y(vertex, position.y());
				meshVertices.z(vertex, position.z());
				meshVertices.r(vertex, 1.0f);
				meshVertices.g(vertex, 1.0f);
				meshVertices.b(vertex, 1.0f);
				meshVertices.a(vertex, 1.0f);
				meshVertices.u(vertex, UVPair.unpackU(quad.packedUV(i)));
				meshVertices.v(vertex, UVPair.unpackV(quad.packedUV(i)));
				meshVertices.overlay(vertex, OverlayTexture.NO_OVERLAY);
				meshVertices.light(vertex, 0);
				meshVertices.normalX(vertex, posedQuad.normal().x);
				meshVertices.normalY(vertex, posedQuad.normal().y);
				meshVertices.normalZ(vertex, posedQuad.normal().z);

				vertex++;
			}
		}

		// Renderers can do strange things. Better safe than sorry, paying attention to individual vertices too.
		for (VnlVertexStorage.VertexInformation info : vertexStorage.getVertices()) {
			meshVertices.x(vertex, info.x);
			meshVertices.y(vertex, info.y);
			meshVertices.z(vertex, info.z);
			meshVertices.r(vertex, info.r);
			meshVertices.g(vertex, info.g);
			meshVertices.b(vertex, info.b);
			meshVertices.a(vertex, info.a);
			meshVertices.u(vertex, info.u);
			meshVertices.v(vertex, info.v);
			meshVertices.overlay(vertex, OverlayTexture.NO_OVERLAY);
			meshVertices.light(vertex, 0);
			meshVertices.normalX(vertex, info.normalX);
			meshVertices.normalY(vertex, info.normalY);
			meshVertices.normalZ(vertex, info.normalZ);

			vertex++;
		}


		emittedMeshes.add(new Model.ConfiguredMesh(material, new SimpleQuadMesh(meshVertices)));
		if (renderType.equals(this.lastSharedType)) {
			this.lastSharedType = null;
		}
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
