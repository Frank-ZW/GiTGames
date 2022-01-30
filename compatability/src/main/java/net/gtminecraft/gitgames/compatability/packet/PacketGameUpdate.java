package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;

import java.util.*;

@AllArgsConstructor
public class PacketGameUpdate extends DefinedPacket {

	@Getter
	private int priority;
	@Getter
	private Collection<UUID> players;

	public PacketGameUpdate() {
		this.players = new ArrayList<>();
	}

	@Override
	public void read(ByteBuf buf) {
		this.priority = buf.readByte();
		if (this.priority == GameStateUtils.ACTIVE_STATE_PRIORITY || this.priority == GameStateUtils.FINISHED_STATE_PRIORITY) {
			int length = DefinedPacket.readVarInt(buf);
			for (int i = 0; i < length; i++) {
				this.players.add(DefinedPacket.readUUID(buf));
			}
		}
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeByte(this.priority);
		if (this.priority == GameStateUtils.ACTIVE_STATE_PRIORITY || this.priority == GameStateUtils.FINISHED_STATE_PRIORITY) {
			DefinedPacket.writeVarInt(buf, this.players.size());
			for (UUID uniqueId : this.players) {
				DefinedPacket.writeUUID(buf, uniqueId);
			}
		}
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
