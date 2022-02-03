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
public class PacketPlayerDisconnect extends DefinedPacket {

	@Getter
	private UUID player;

	@Override
	public void read(ByteBuf buf) {
		this.player = DefinedPacket.readUUID(buf);
	}

	@Override
	public void write(ByteBuf buf) {
		DefinedPacket.writeUUID(buf, this.player);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
