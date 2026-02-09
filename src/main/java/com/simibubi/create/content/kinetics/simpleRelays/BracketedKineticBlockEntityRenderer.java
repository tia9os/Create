package com.simibubi.create.content.kinetics.simpleRelays;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;

public class BracketedKineticBlockEntityRenderer extends KineticBlockEntityRenderer<BracketedKineticBlockEntity> {

	public BracketedKineticBlockEntityRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(BracketedKineticBlockEntity be, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {

		renderBracket(be, ms, buffer, light);
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		if (!AllBlocks.LARGE_COGWHEEL.has(be.getBlockState())) {
			super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
			return;
		}

		// Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
		// mesh properly

		VertexConsumer vc = buffer.getBuffer(RenderType.solid());
		Axis axis = getRotationAxisOf(be);
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		renderRotatingBuffer(be,
			CachedBuffers.partialFacingVertical(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, be.getBlockState(), facing),
			ms, vc, light);

		float angle = getAngleForLargeCogShaft(be, axis);
		SuperByteBuffer shaft =
			CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL_SHAFT, be.getBlockState(), facing);
		kineticRotationTransform(shaft, be, axis, angle, light);
		shaft.renderInto(ms, vc);

	}

	private static void renderBracket(BracketedKineticBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
		BracketedBlockEntityBehaviour behaviour = BlockEntityBehaviour.get(be, BracketedBlockEntityBehaviour.TYPE);
		if (behaviour == null)
			return;

		BlockState bracket = behaviour.getBracket();
		if (bracket == null)
			return;

		CachedBuffers.block(bracket)
			.light(light)
			.renderInto(ms, buffer.getBuffer(ItemBlockRenderTypes.getChunkRenderType(bracket)));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(BracketedKineticBlockEntity be, BlockState state) {
		Direction facing = Direction.fromAxisAndDirection(getRotationAxisOf(be), AxisDirection.POSITIVE);
		if (AllBlocks.COGWHEEL.has(state))
			return CachedBuffers.partialFacingVertical(AllPartialModels.COGWHEEL, state, facing);
		return CachedBuffers.partialFacingVertical(AllPartialModels.SHAFT, state, facing);
	}

	public static float getAngleForLargeCogShaft(SimpleKineticBlockEntity be, Axis axis) {
		BlockPos pos = be.getBlockPos();
		float offset = getShaftAngleOffset(axis, pos);
		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		float angle = ((time * be.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
		return angle;
	}

	public static float getShaftAngleOffset(Axis axis, BlockPos pos) {
		if (KineticBlockEntityVisual.shouldOffset(axis, pos)) {
			return 22.5f;
		} else {
			return 0;
		}
	}

}
