package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerQueue extends DefinedPacket {

	@Getter
	private UUID player;
	@Getter
	private UUID target;
	@Getter
	private QueueType type;

	@Override
	public void read(ByteBuf buf) {
		this.player = DefinedPacket.readUUID(buf);
		this.type = QueueType.values()[buf.readByte()];
		if (this.type == QueueType.SPECTATE) {
			this.target = DefinedPacket.readUUID(buf);
		}
	}

	@Override
	public void write(ByteBuf buf) {
		DefinedPacket.writeUUID(buf, this.player);
		buf.writeByte(this.type.ordinal());
		if (this.type == QueueType.SPECTATE) {
			DefinedPacket.writeUUID(buf, this.target);
		}
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}

	public enum QueueType {
		QUEUE,
		SPECTATE
	}
}
