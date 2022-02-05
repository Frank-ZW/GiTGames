package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

import java.util.Arrays;

@NoArgsConstructor
@AllArgsConstructor
public class PacketCreateGame extends DefinedPacket {

	@Getter
	private double gameId;
	@Getter
	private int gameKey;
	@Getter
	private int maxPlayers;

	@Override
	public void read(ByteBuf buf) {
		this.gameId = buf.readDouble();
		this.gameKey = DefinedPacket.readVarInt(buf);
		this.maxPlayers = DefinedPacket.readVarInt(buf);
		System.out.printf("%s --> %s%n", this.getClass().getSimpleName(), Arrays.toString(ByteBufUtil.getBytes(buf)));
	}

	@Override
	public void write(ByteBuf buf) {
		buf.writeDouble(this.gameId);
		DefinedPacket.writeVarInt(buf, this.gameKey);
		DefinedPacket.writeVarInt(buf, this.maxPlayers);
		System.out.printf("%s --> %s%n", this.getClass().getSimpleName(), Arrays.toString(ByteBufUtil.getBytes(buf)));
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
