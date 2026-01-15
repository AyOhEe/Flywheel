package dev.engine_room.flywheel.backend.gl;

import dev.engine_room.flywheel.backend.mixin.GLStateManagerAccessor;

import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class GlTexture extends GlObject {
	private final int textureType;

	public GlTexture(int textureType) {
		this.textureType = textureType;
		handle(GL20.glGenTextures());
	}

	@Override
	protected void deleteInternal(int handle) {
		GL20.glDeleteTextures(handle);
	}

	public void bind() {
		GL20.glBindTexture(textureType, handle());
	}

	public void unbind() {
		GL20.glBindTexture(textureType, 0);
	}

	public static int getActiveTexture() {
		return GLStateManagerAccessor.flywheel$_getActiveTexture() + GL_TEXTURE0;
	}
}
