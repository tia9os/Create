package com.simibubi.create.content.trains.station;

import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public record OpenStationScreenPacket(BlockPos pos) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, OpenStationScreenPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
		OpenStationScreenPacket::new, OpenStationScreenPacket::pos
	);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		if (player.clientLevel == null)
			return;
		if (!(player.clientLevel.getBlockEntity(pos) instanceof StationBlockEntity be))
			return;

		BlockState blockState = be.getBlockState();
		if (blockState == null)
			return;

		GlobalStation station = be.getStation();
		if (station == null)
			station = new GlobalStation();

		boolean assembling = blockState.getBlock() instanceof StationBlock && blockState.getValue(StationBlock.ASSEMBLING);
		ScreenOpener.open(assembling ? new AssemblyScreen(be, station) : new StationScreen(be, station));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.OPEN_STATION_SCREEN;
	}
}
