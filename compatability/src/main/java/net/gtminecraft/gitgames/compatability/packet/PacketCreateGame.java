package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

@NoArgsConstructor
@AllArgsConstructor
public class PacketCreateGame extends DefinedPacket {

	@Getter
	private int gameId;
	@Getter
	private int gameKey;
	@Getter
	private int maxPlayers;

	@Override
	public void read(ByteBuf buf) {
		this.gameId = DefinedPacket.readVarInt(buf);
		this.gameKey = DefinedPacket.readVarInt(buf);
		this.maxPlayers = DefinedPacket.readVarInt(buf);
	}

	@Override
	public void write(ByteBuf buf) {
		DefinedPacket.writeVarInt(buf, this.gameId);
		DefinedPacket.writeVarInt(buf, this.gameKey);
		DefinedPacket.writeVarInt(buf, this.maxPlayers);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
