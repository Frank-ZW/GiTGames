package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.gtminecraft.gitgames.compatability.mechanics.ServerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
public class PacketPlayerConnect extends DefinedPacket {

	@Getter
	private Collection<UUID> players;
	@Getter
	private ServerType serverType;
	@Getter
	private PlayerStatus playerStatus;

	public PacketPlayerConnect() {
		this.players = new ArrayList<>();
	}

	public PacketPlayerConnect(PlayerStatus playerStatus, ServerType serverType, UUID ... uniqueIds) {
		this.players = Arrays.asList(uniqueIds);
		this.playerStatus = playerStatus;
		this.serverType = serverType;
	}

	@Override
	public void read(ByteBuf buf) {
		this.playerStatus = PlayerStatus.VALUES[buf.readByte()];
		this.serverType = ServerType.VALUES[buf.readByte()];
		int length = DefinedPacket.readVarInt(buf);
		for (int i = 0; i < length; i++) {
			this.players.add(DefinedPacket.readUUID(buf));
		}
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeByte(this.playerStatus.ordinal());
		buf.writeByte(this.serverType.ordinal());
		DefinedPacket.writeVarInt(buf, this.players.size());
		for (UUID uniqueId : this.players) {
			DefinedPacket.writeUUID(buf, uniqueId);
		}
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
