package com.simibubi.create.foundation.events;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ClientEvents {

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> CreateClient.checkGraphicsFanciness());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CreateClient.RAILWAYS.cleanUp());
		ClientTickEvents.END_CLIENT_TICK.register(client -> ServerSpeedProvider.clientTick());
	}
}
