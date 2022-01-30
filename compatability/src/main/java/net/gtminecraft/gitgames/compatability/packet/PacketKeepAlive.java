package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

@NoArgsConstructor
@AllArgsConstructor
public class PacketKeepAlive extends DefinedPacket {

	@Getter
	private long id;

	@Override
	public void read(ByteBuf buf) {
		this.id = buf.readLong();
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeLong(this.id);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
