package net.gtminecraft.gitgames.compatability.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.packet.PacketKeepAlive;

@AllArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<DefinedPacket> {

	@Setter
	private Protocol protocol;
	private final boolean server;

	@Override
	protected void encode(ChannelHandlerContext context, DefinedPacket packet, ByteBuf out) {
		Protocol.DirectionData directionData = this.server ? this.protocol.TO_CLIENT : this.protocol.TO_SERVER;
		DefinedPacket.writeVarInt(out, directionData.getId(packet.getClass()));
		if (!(packet instanceof PacketKeepAlive)) {
			System.out.printf("! ! ! %s: Wrote packet ID of %s, %s readable bytes now\n", this.getClass().getSimpleName(), directionData.getId(packet.getClass()), out.readableBytes());
		}

		packet.write(out, directionData.getDirection());
	}
}
