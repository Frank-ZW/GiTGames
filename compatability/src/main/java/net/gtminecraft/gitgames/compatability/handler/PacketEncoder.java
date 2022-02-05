package net.gtminecraft.gitgames.compatability.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.Protocol;

public class PacketEncoder extends MessageToByteEncoder<DefinedPacket> {

	@Setter
	private Protocol protocol;
	private final boolean server;

	public PacketEncoder(Protocol protocol, boolean server) {
		this.protocol = protocol;
		this.server = server;
	}

	@Override
	protected void encode(ChannelHandlerContext context, DefinedPacket packet, ByteBuf out) {
		Protocol.DirectionData directionData = this.server ? this.protocol.TO_CLIENT : this.protocol.TO_SERVER;
		DefinedPacket.writeVarInt(out, directionData.getId(packet.getClass()));
		packet.write(out, directionData.getDirection());
	}
}
