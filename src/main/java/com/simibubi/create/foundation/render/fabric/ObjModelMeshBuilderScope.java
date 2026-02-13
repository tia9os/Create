package com.simibubi.create.foundation.render.fabric;

import java.util.ArrayDeque;

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;

public final class ObjModelMeshBuilderScope {
	private static final ThreadLocal<ArrayDeque<MeshBuilder>> SCOPED_BUILDERS =
		ThreadLocal.withInitial(ArrayDeque::new);

	private ObjModelMeshBuilderScope() {}

	public static void push(MeshBuilder meshBuilder) {
		SCOPED_BUILDERS.get()
			.push(meshBuilder);
	}

	public static void pop() {
		ArrayDeque<MeshBuilder> builders = SCOPED_BUILDERS.get();
		if (!builders.isEmpty()) {
			builders.pop();
		}
		if (builders.isEmpty()) {
			SCOPED_BUILDERS.remove();
		}
	}

	public static MeshBuilder peek() {
		ArrayDeque<MeshBuilder> builders = SCOPED_BUILDERS.get();
		return builders.isEmpty() ? null : builders.peek();
	}
}
