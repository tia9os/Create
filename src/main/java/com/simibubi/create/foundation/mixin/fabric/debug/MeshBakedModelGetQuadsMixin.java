package com.simibubi.create.foundation.mixin.fabric.debug;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.fabricators_of_create.porting_lib.models.MeshBakedModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = MeshBakedModel.class, remap = false)
public abstract class MeshBakedModelGetQuadsMixin {
	@Shadow
	protected Mesh mesh;

	@Inject(method = "getQuads", at = @At("HEAD"), cancellable = true, require = 0)
	private void create$useModelHelperQuadsNamed(BlockState state, Direction side, RandomSource random, CallbackInfoReturnable<List<BakedQuad>> cir) {
		cir.setReturnValue(ModelHelper.toQuadLists(mesh)[ModelHelper.toFaceIndex(side)]);
	}

	@Inject(method = "method_4707", at = @At("HEAD"), cancellable = true, require = 0)
	private void create$useModelHelperQuadsIntermediary(BlockState state, Direction side, RandomSource random, CallbackInfoReturnable<List<BakedQuad>> cir) {
		cir.setReturnValue(ModelHelper.toQuadLists(mesh)[ModelHelper.toFaceIndex(side)]);
	}
}
