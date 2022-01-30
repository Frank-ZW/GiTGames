package net.gtminecraft.gitgames.compatability.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gtminecraft.gitgames.compatability.AbstractPacketHandler;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.Protocol;

@NoArgsConstructor
public class PacketHandshake extends DefinedPacket {

	@Getter
	private String name;
	@Getter
	private String host;
	@Getter
	private int port;
	@Getter
	private boolean confirmed;

	public PacketHandshake(boolean confirmed) {
		this(null, null, 0, confirmed);
	}

	public PacketHandshake(String name, String host, int port) {
		this(name, host, port, false);
	}

	public PacketHandshake(String name, String host, int port, boolean confirmed) {
		this.name = name;
		this.host = host == null ? null : host.replace("localhost", "127.0.0.1");
		this.port = port;
		this.confirmed = confirmed;
	}

	@Override
	public void read(ByteBuf buf, Protocol.Direction direction) {
		if (direction == Protocol.Direction.TO_CLIENT) {
			this.confirmed = buf.readBoolean();
		} else {
			// to the server
			this.name = DefinedPacket.readString(buf);
			this.host = DefinedPacket.readString(buf);
			this.port = DefinedPacket.readVarInt(buf);
		}
	}

	@Override
	public void write(ByteBuf buf, Protocol.Direction direction) {
		if (direction == Protocol.Direction.TO_CLIENT) {
			buf.writeBoolean(this.confirmed);
		} else {
			// to the server
			DefinedPacket.writeString(buf, this.name);
			DefinedPacket.writeString(buf, this.host);
			DefinedPacket.writeVarInt(buf, this.port);
		}
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
