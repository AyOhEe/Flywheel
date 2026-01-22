package dev.engine_room.vanillin.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;

import net.minecraft.util.ARGB;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;

public class VnlVertexStorage implements VertexConsumer {
	private final LinkedList<PosedQuad> quads = new LinkedList<>();
	private final LinkedList<VertexInformation> vertices = new LinkedList<>();

	private @Nullable VertexInformation partialVertex = null;

	public LinkedList<PosedQuad> allQuads() {
		return quads;
	}

	public int quadCount() {
		return quads.size();
	}

	public LinkedList<VertexInformation> getVertices() {
		return vertices;
	}

	public int vertexCount() {
		return vertices.size();
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		if (partialVertex != null) {
			vertices.add(partialVertex);
		}

		partialVertex = new VertexInformation();
		partialVertex.x = x;
		partialVertex.y = y;
		partialVertex.z = z;

		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		if (partialVertex == null) return this;
		partialVertex.r = red;
		partialVertex.g = green;
		partialVertex.b = blue;
		partialVertex.a = alpha;

		return this;
	}

	@Override
	public VertexConsumer setColor(int color) {
		if (partialVertex == null) return this;
		partialVertex.r = ARGB.red(color);
		partialVertex.g = ARGB.green(color);
		partialVertex.b = ARGB.blue(color);
		partialVertex.a = ARGB.alpha(color);

		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		if (partialVertex == null) return this;
		partialVertex.u = u;
		partialVertex.v = v;

		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		if (partialVertex == null) return this;
		partialVertex.normalX = normalX;
		partialVertex.normalY = normalY;
		partialVertex.normalZ = normalZ;

		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float f) {
		return this;
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		this.putBulkData(pose, quad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, alpha, new int[]{packedLight, packedLight, packedLight, packedLight}, packedOverlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float[] fs, float r, float g, float b, float alpha, int[] light, int overlay) {
		Vector3fc normal = bakedQuad.direction().getUnitVec3f();
		Matrix4f poseMatrix = pose.pose();
		Vector3f posedNormal = pose.transformNormal(normal, new Vector3f());

		quads.add(new PosedQuad(
				new BakedQuad(
					poseMatrix.transformPosition(bakedQuad.position0(), new Vector3f()),
					poseMatrix.transformPosition(bakedQuad.position1(), new Vector3f()),
					poseMatrix.transformPosition(bakedQuad.position2(), new Vector3f()),
					poseMatrix.transformPosition(bakedQuad.position3(), new Vector3f()),
					bakedQuad.packedUV0(),
					bakedQuad.packedUV1(),
					bakedQuad.packedUV2(),
					bakedQuad.packedUV3(),
					bakedQuad.tintIndex(),
					bakedQuad.direction(),
					bakedQuad.sprite(),
					bakedQuad.shade(),
					bakedQuad.lightEmission()
				), posedNormal
		));
	}

	public static class VertexInformation {
		public float x;
		public float y;
		public float z;
		public float r;
		public float g;
		public float b;
		public float a;
		public float u;
		public float v;
		public float normalX;
		public float normalY;
		public float normalZ;
	}

	public record PosedQuad(BakedQuad quad, Vector3f normal) {}
}
