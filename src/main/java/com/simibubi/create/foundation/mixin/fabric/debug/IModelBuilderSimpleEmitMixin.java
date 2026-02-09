package com.simibubi.create.foundation.mixin.fabric.debug;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

@Mixin(targets = "io.github.fabricators_of_create.porting_lib.models.IModelBuilder$Simple", remap = false)
public abstract class IModelBuilderSimpleEmitMixin {
	@WrapOperation(
		method = "addCulledFace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;"
		),
		require = 0
	)
	private QuadEmitter create$emitCulledQuad(QuadEmitter emitter, BakedQuad quad, RenderMaterial material, Direction cullFace, Operation<QuadEmitter> original) {
		QuadEmitter baked = original.call(emitter, quad, material, cullFace);
		baked.emit();
		return baked;
	}

	@WrapOperation(
		method = "addUnculledFace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;"
		),
		require = 0
	)
	private QuadEmitter create$emitUnculledQuad(QuadEmitter emitter, BakedQuad quad, RenderMaterial material, Direction cullFace, Operation<QuadEmitter> original) {
		QuadEmitter baked = original.call(emitter, quad, material, cullFace);
		baked.emit();
		return baked;
	}

	@WrapOperation(
		method = "addFace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;copyFrom(Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadView;)Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;"
		),
		require = 0
	)
	private QuadEmitter create$emitCopiedQuad(QuadEmitter emitter, QuadView quad, Operation<QuadEmitter> original) {
		QuadEmitter copied = original.call(emitter, quad);
		copied.emit();
		return copied;
	}
}
