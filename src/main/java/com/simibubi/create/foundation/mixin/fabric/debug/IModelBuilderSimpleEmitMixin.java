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
	private static QuadEmitter create$emitWrapped(QuadEmitter emitter, Operation<QuadEmitter> original, Object... args) {
		QuadEmitter baked = switch (args.length) {
			case 3 -> original.call(emitter, args[0], args[1], args[2]);
			case 1 -> original.call(emitter, args[0]);
			default -> original.call(emitter);
		};
		baked.emit();
		return baked;
	}

	@WrapOperation(
		method = "addCulledFace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;"
		),
		require = 0
	)
	private QuadEmitter create$emitCulledQuad(QuadEmitter emitter, BakedQuad quad, RenderMaterial material, Direction cullFace, Operation<QuadEmitter> original) {
		return create$emitWrapped(emitter, original, quad, material, cullFace);
	}

	@WrapOperation(
		method = "addCulledFace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;fromVanilla(Lnet/minecraft/class_777;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/class_2350;)Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;"
		),
		require = 0
	)
	private QuadEmitter create$emitCulledQuadIntermediary(QuadEmitter emitter, BakedQuad quad, RenderMaterial material, Direction cullFace, Operation<QuadEmitter> original) {
		return create$emitWrapped(emitter, original, quad, material, cullFace);
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
		return create$emitWrapped(emitter, original, quad, material, cullFace);
	}

	@WrapOperation(
		method = "addUnculledFace",
		at = @At(
			value = "INVOKE",
			target = "Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;fromVanilla(Lnet/minecraft/class_777;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/class_2350;)Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;"
		),
		require = 0
	)
	private QuadEmitter create$emitUnculledQuadIntermediary(QuadEmitter emitter, BakedQuad quad, RenderMaterial material, Direction cullFace, Operation<QuadEmitter> original) {
		return create$emitWrapped(emitter, original, quad, material, cullFace);
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
		return create$emitWrapped(emitter, original, quad);
	}
}
