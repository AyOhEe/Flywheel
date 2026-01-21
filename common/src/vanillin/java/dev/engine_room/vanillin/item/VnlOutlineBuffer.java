package dev.engine_room.vanillin.item;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.model.Model;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class VnlOutlineBuffer extends OutlineBufferSource {
	// They're both private in the superclass, but rendering code might not be safe to mix into
	protected final VnlBufferSource vnlOutlineBuffer = VnlBufferSource.immediate(new ByteBufferBuilder(1536));
	protected int vnlOutlineColor = -1;

	// Basically identical, but has to be reimplemented using the new references and record type
	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		if (renderType.isOutline()) {
			VertexConsumer delegate = vnlOutlineBuffer.getBuffer(renderType);
			return new EntityOutlineGenerator(delegate, vnlOutlineColor);
		}

		Optional<RenderType> outlineOptional = renderType.outline();
		if (outlineOptional.isPresent()) {
			VertexConsumer delegate = vnlOutlineBuffer.getBuffer(outlineOptional.get());
			return new EntityOutlineGenerator(delegate, vnlOutlineColor);
		}

		throw new IllegalStateException("Can't render an outline for this rendertype!");
	}

	// Nah, gonna do my own thing
	public void setColor(int color) {
		vnlOutlineColor = color;
	}

	public void endOutlineBatch() {
		vnlOutlineBuffer.endBatch();
	}

	public List<Model.ConfiguredMesh> popAllMeshes() {
		return vnlOutlineBuffer.popAllMeshes();
	}

	// Private in OutlineBufferSource, had to reimplement
	@Environment(EnvType.CLIENT)
	record EntityOutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer {
		public VertexConsumer addVertex(float x, float y, float z) {
			this.delegate.addVertex(x, y, z).setColor(this.color);
			return this;
		}

		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			return this;
		}

		public VertexConsumer setColor(int color) {
			return this;
		}

		public VertexConsumer setUv(float u, float v) {
			this.delegate.setUv(u, v);
			return this;
		}

		public VertexConsumer setUv1(int u, int v) {
			return this;
		}

		public VertexConsumer setUv2(int u, int v) {
			return this;
		}

		public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
			return this;
		}

		public VertexConsumer setLineWidth(float f) {
			return this;
		}
	}
}
