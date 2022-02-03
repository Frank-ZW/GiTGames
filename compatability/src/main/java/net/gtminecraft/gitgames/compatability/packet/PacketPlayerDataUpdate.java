package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
public class PacketPlayerDataUpdate extends DefinedPacket {

	@Getter
	private PlayerStatus status;
	@Getter
	private Collection<UUID> players;

	public PacketPlayerDataUpdate() {
		this.players = new ArrayList<>();
	}

	public PacketPlayerDataUpdate(PlayerStatus status, UUID ... uniqueIds) {
		this.status = status;
		this.players = Arrays.asList(uniqueIds);
	}

	@Override
	public void read(ByteBuf buf) {
		this.status = PlayerStatus.VALUES[buf.readByte()];
		int size = DefinedPacket.readVarInt(buf);
		for (int i = 0; i < size; i++) {
			this.players.add(DefinedPacket.readUUID(buf));
		}
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeByte(this.status.ordinal());
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
