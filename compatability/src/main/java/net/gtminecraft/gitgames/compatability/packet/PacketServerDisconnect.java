package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

@NoArgsConstructor
@AllArgsConstructor
public class PacketServerDisconnect extends DefinedPacket {

	@Getter
	private DisconnectAction action;

	@Override
	public void read(ByteBuf buf) {
		this.action = DisconnectAction.values()[DefinedPacket.readVarInt(buf)];
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeByte(this.action.ordinal());
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}

	public enum DisconnectAction {
		PROMPT_SHUTDOWN,
		REQUEST_SHUTDOWN,
		CONFIRM_SHUTDOWN
	}
}
