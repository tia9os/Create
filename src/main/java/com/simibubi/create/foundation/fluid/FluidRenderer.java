package com.simibubi.create.foundation.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.BasicFluidRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FluidRenderer extends BasicFluidRenderer {

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, MultiBufferSource buffer, PoseStack ms, int light) {
		renderFluidStream(fluidStack, direction, radius, progress, inbound, getFluidBuilder(buffer), ms, light);
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, VertexConsumer builder, PoseStack ms, int light) {
		FluidVariant fluidVariant = variantOf(fluidStack.getFluid(), fluidStack.getComponentsPatch());
		FluidVariant fallbackVariant = TransferUtil.fluidVariantOf(fluidStack.getFluid());
		TextureAtlasSprite[] sprites = getSpritesSafe(fluidVariant);
		TextureAtlasSprite[] fallbackSprites = getSpritesSafe(fallbackVariant);
		TextureAtlasSprite missing = getMissingSprite();

		TextureAtlasSprite fallbackStill = pickSprite(fallbackSprites, 0, null, missing);
		TextureAtlasSprite stillTexture = pickSprite(sprites, 0, fallbackStill, missing);
		TextureAtlasSprite fallbackFlow = pickSprite(fallbackSprites, 1, stillTexture, missing);
		TextureAtlasSprite flowTexture = pickSprite(sprites, 1, fallbackFlow, missing);

		if (stillTexture == null || flowTexture == null) {
			return;
		}

		int color = FluidVariantRendering.getColor(fluidVariant);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, FluidVariantAttributes.getLuminance(fluidVariant));
		light = (light & 0xF00000) | luminosity << 4;

		if (inbound)
			direction = direction.getOpposite();

		var msr = TransformStack.of(ms);
		ms.pushPose();
		msr.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(direction))
			.rotateXDegrees(direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)
			.uncenter();
		ms.translate(.5, 0, .5);

		float h = radius;
		float hMin = -radius;
		float hMax = radius;
		float y = inbound ? 1 : .5f;
		float yMin = y - Mth.clamp(progress * .5f, 0, 1);
		float yMax = y;

		for (int i = 0; i < 4; i++) {
			ms.pushPose();
			renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, hMax, yMax, h, builder, ms, light, color, flowTexture);
			ms.popPose();
			msr.rotateYDegrees(90);
		}

		if (progress != 1)
			renderStillTiledFace(Direction.DOWN, hMin, hMin, hMax, hMax, yMin, builder, ms, light, color, stillTexture);

		ms.popPose();
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom,
		boolean invertGasses) {
		renderFluidBox(fluid, amount, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light,
			renderBottom, invertGasses, DataComponentPatch.EMPTY);
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom,
		boolean invertGasses) {
		renderFluidBox(fluid, amount, xMin, yMin, zMin, xMax, yMax, zMax, builder, ms, light, renderBottom,
			invertGasses, DataComponentPatch.EMPTY);
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom,
		boolean invertGasses, @Nullable DataComponentPatch fluidData) {
		renderFluidBox(fluid, amount, xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light,
			renderBottom, invertGasses, fluidData);
	}

	public static void renderFluidBox(Fluid fluid, long amount, float xMin, float yMin, float zMin, float xMax,
		float yMax, float zMax, VertexConsumer builder, PoseStack ms, int light, boolean renderBottom,
		boolean invertGasses, @Nullable DataComponentPatch fluidData) {
		FluidVariant fluidVariant = variantOf(fluid, fluidData);
		FluidVariant fallbackVariant = TransferUtil.fluidVariantOf(fluid);
		TextureAtlasSprite missing = getMissingSprite();
		TextureAtlasSprite fluidTexture = getStillSpriteSafe(fluidVariant);
		if (fluidTexture == null || fluidTexture == missing) {
			fluidTexture = getStillSpriteSafe(fallbackVariant);
		}
		if (fluidTexture == null || fluidTexture == missing) {
			return;
		}

		int color = FluidVariantRendering.getColor(fluidVariant);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, FluidVariantAttributes.getLuminance(fluidVariant));
		light = (light & 0xF00000) | luminosity << 4;

		Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
		ms.pushPose();
		if (invertGasses && FluidVariantAttributes.isLighterThanAir(fluidVariant)) {
			ms.translate(center.x, center.y, center.z);
			ms.mulPose(Axis.XP.rotationDegrees(180));
			ms.translate(-center.x, -center.y, -center.z);
		}

		for (Direction side : Iterate.directions) {
			if (side == Direction.DOWN && !renderBottom)
				continue;

			boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
			if (side.getAxis().isHorizontal()) {
				if (side.getAxis() == Direction.Axis.X) {
					renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin,
						builder, ms, light, color, fluidTexture);
				} else {
					renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin,
						builder, ms, light, color, fluidTexture);
				}
			} else {
				renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin,
					builder, ms, light, color, fluidTexture);
			}
		}

		ms.popPose();
	}

	private static FluidVariant variantOf(Fluid fluid, @Nullable DataComponentPatch fluidData) {
		if (fluidData == null || fluidData.isEmpty()) {
			return TransferUtil.fluidVariantOf(fluid);
		}
		return TransferUtil.fluidVariantOf(fluid, fluidData);
	}

	private static TextureAtlasSprite getMissingSprite() {
		return Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(MissingTextureAtlasSprite.getLocation());
	}

	@Nullable
	private static TextureAtlasSprite[] getSpritesSafe(FluidVariant fluidVariant) {
		try {
			return FluidVariantRendering.getSprites(fluidVariant);
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	@Nullable
	private static TextureAtlasSprite getStillSpriteSafe(FluidVariant fluidVariant) {
		try {
			return FluidVariantRendering.getSprite(fluidVariant);
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	@Nullable
	private static TextureAtlasSprite pickSprite(TextureAtlasSprite[] sprites, int index, @Nullable TextureAtlasSprite fallback,
		TextureAtlasSprite missing) {
		TextureAtlasSprite selected = sprites != null && index < sprites.length ? sprites[index] : null;
		if (selected == null || selected == missing) {
			selected = fallback;
		}
		if (selected == missing) {
			return null;
		}
		return selected;
	}

	public static void renderFlowingTiledFace(Direction dir, float left, float down, float right, float up,
		float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 0.5f);
	}

}
