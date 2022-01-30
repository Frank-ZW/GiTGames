package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.Protocol;

@NoArgsConstructor
@AllArgsConstructor
public class PacketShutdown extends DefinedPacket {

	@Getter
	private String who;
	@Getter
	private int attribute;
	@Getter
	private ShutdownAction action;

	@Override
	public void read(ByteBuf buf, Protocol.Direction direction) {
		this.action = ShutdownAction.values()[buf.readByte()];
		switch (this.action) {
			case START -> {
				this.attribute = DefinedPacket.readVarInt(buf);
			}

			case CANCEL -> {
				this.who = DefinedPacket.readString(buf);
			}

			default -> {
				this.who = DefinedPacket.readString(buf);
				if (direction == Protocol.Direction.TO_SERVER) {
					this.attribute = DefinedPacket.readVarInt(buf);
				}
			}
		}
	}

	@Override
	public void write(ByteBuf buf, Protocol.Direction direction) {
		buf.writeByte(this.action.ordinal());
		switch (this.action) {
			case START -> {
				DefinedPacket.writeVarInt(buf, this.attribute);
			}

			case CANCEL -> {
				DefinedPacket.writeString(buf, this.who);
			}

			default -> {
				DefinedPacket.writeString(buf, this.who);
				if (direction == Protocol.Direction.TO_SERVER) {
					DefinedPacket.writeVarInt(buf, this.attribute);
				}
			}
		}
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}

	public enum ShutdownAction {
		START,		// no who, yes attribute
		CANCEL,		// yes who, no attribute
		STATUS		// yes who, no attribute to server && yes who, yes attribute to proxy
	}
}
