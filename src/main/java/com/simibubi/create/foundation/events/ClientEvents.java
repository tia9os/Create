package com.simibubi.create.foundation.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.track.CurvedTrackInteraction;
import com.simibubi.create.content.trains.track.TrackBlockItem;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackPlacement;
import com.simibubi.create.content.trains.track.TrackTargetingClient;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class ClientEvents {

	private static void onTick(Minecraft client) {
		if (client.level == null || client.player == null)
			return;

		CreateClient.GLUE_HANDLER.tick();
		ServerSpeedProvider.clientTick();
		TrackTargetingClient.clientTick();
		TrackPlacement.clientTick();
		CurvedTrackInteraction.clientTick();
	}

	private static void onRenderWorld(WorldRenderContext event) {
		if (event.matrixStack() == null)
			return;

		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		Vec3 camera = Minecraft.getInstance()
			.gameRenderer
			.getMainCamera()
			.getPosition();

		event.matrixStack()
			.pushPose();
		TrackBlockOutline.drawCurveSelection(event.matrixStack(), buffer, camera);
		TrackTargetingClient.render(event.matrixStack(), buffer, camera);
		buffer.draw();
		RenderSystem.enableCull();
		event.matrixStack()
			.popPose();
	}

	private static boolean onBlockOutline(WorldRenderContext context, WorldRenderContext.BlockOutlineContext blockOutlineContext) {
		HitResult hitResult = Minecraft.getInstance().hitResult;
		if (hitResult == null)
			return true;

		if (context.matrixStack() == null || context.consumers() == null)
			return true;

		float partialTicks = context.tickCounter()
			.getGameTimeDeltaPartialTick(false);
		return !TrackBlockOutline.drawCustomBlockSelection(context.worldRenderer(), context.camera(), hitResult,
			partialTicks, context.matrixStack(), context.consumers());
	}

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> CreateClient.checkGraphicsFanciness());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CreateClient.RAILWAYS.cleanUp());
		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onTick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ClientEvents::onRenderWorld);
		WorldRenderEvents.BLOCK_OUTLINE.register(ClientEvents::onBlockOutline);
		UseBlockCallback.EVENT.register(TrackBlockItem::sendExtenderPacket);
	}
}
