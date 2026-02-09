package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dev.engine_room.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

public class BracketedKineticBlockModel extends ForwardingBakedModel {

	public BracketedKineticBlockModel(BakedModel template) {
		wrapped = template;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
		return Collections.emptyList();
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (blockView instanceof EmptyVirtualBlockGetter) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		BracketedModelData data = new BracketedModelData();
		BracketedBlockEntityBehaviour attachmentBehaviour =
			BlockEntityBehaviour.get(blockView, pos, BracketedBlockEntityBehaviour.TYPE);
		if (attachmentBehaviour != null)
			data.putBracket(attachmentBehaviour.getBracket());

		BakedModel bracket = data.getBracket();
		if (bracket != null)
			bracket.emitBlockQuads(blockView, state, pos, randomSupplier, context);
	}

	private static class BracketedModelData {
		private BakedModel bracket;

		public void putBracket(BlockState state) {
			if (state != null) {
				this.bracket = Minecraft.getInstance()
					.getBlockRenderer()
					.getBlockModel(state);
			}
		}

		public BakedModel getBracket() {
			return bracket;
		}
	}

}
