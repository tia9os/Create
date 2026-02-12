package com.simibubi.create.foundation.events;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsServerHandler;
import com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class CommonEvents {

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Create.SCHEMATIC_RECEIVER.tick();
			Create.LAGGER.tick();
			ServerSpeedProvider.serverTick(server);
			server.getAllLevels()
				.forEach(level -> {
					Create.RAILWAYS.sided(level)
						.tick(level);
					ControlsServerHandler.tick(level);
				});
		});
		ServerWorldEvents.LOAD.register((server, world) -> Create.RAILWAYS.levelLoaded(world));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Create.RAILWAYS.playerLogin(handler.player));
		ServerPlayConnectionEvents.DISCONNECT
			.register((handler, server) -> Create.RAILWAYS.playerLogout(handler.player));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> Create.SCHEMATIC_RECEIVER.shutdown());
	}

	public static void leftClickEmpty(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof ZapperItem) {
			ZapperInteractionHandler.trySelect(stack, player);
		}
	}
}
