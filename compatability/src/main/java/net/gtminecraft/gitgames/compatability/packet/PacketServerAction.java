package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

@NoArgsConstructor
@AllArgsConstructor
public class PacketServerAction extends DefinedPacket {

	@Getter
	@Setter
	private ServerAction action;

	@Override
	public void read(ByteBuf buf) {
		this.action = ServerAction.values()[buf.readByte()];
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeByte(this.action.ordinal());
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}

	public enum ServerAction {
		FORCE_END,
	}
}
