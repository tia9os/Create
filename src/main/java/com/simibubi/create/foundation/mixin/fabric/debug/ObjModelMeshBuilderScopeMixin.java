package com.simibubi.create.foundation.mixin.fabric.debug;

import com.simibubi.create.foundation.render.fabric.ObjModelMeshBuilderScope;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;

@Mixin(targets = "io.github.fabricators_of_create.porting_lib.models.obj.ObjModel$ModelMesh", remap = false)
public class ObjModelMeshBuilderScopeMixin {
	@Inject(method = "addQuads", at = @At("HEAD"), require = 0)
	private void create$pushScopedBuilderForAddQuads(CallbackInfo ci) {
		MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
		ObjModelMeshBuilderScope.push(meshBuilder);
	}

	@Inject(method = "addQuads", at = @At("RETURN"), require = 0)
	private void create$popScopedBuilderForAddQuads(CallbackInfo ci) {
		ObjModelMeshBuilderScope.pop();
	}

	@Inject(method = "bake", at = @At("HEAD"), require = 0)
	private void create$pushScopedBuilderForBake(CallbackInfo ci) {
		MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
		ObjModelMeshBuilderScope.push(meshBuilder);
	}

	@Inject(method = "bake", at = @At("RETURN"), require = 0)
	private void create$popScopedBuilderForBake(CallbackInfo ci) {
		ObjModelMeshBuilderScope.pop();
	}
}
