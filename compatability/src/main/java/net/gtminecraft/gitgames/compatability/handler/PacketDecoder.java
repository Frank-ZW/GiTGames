package net.gtminecraft.gitgames.compatability.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.exception.BadPacketException;
import net.gtminecraft.gitgames.compatability.packet.PacketCreateGame;
import net.gtminecraft.gitgames.compatability.wrapper.PacketWrapper;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

	@Setter
	private Protocol protocol;
	private final boolean server;

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf input, List<Object> out) {
		if (context.channel().isActive()) {
			Protocol.DirectionData directionData = this.server ? this.protocol.TO_SERVER : this.protocol.TO_CLIENT;
			ByteBuf copy = input.copy();
			try {
				if (input.readableBytes() != 0 || this.server) {
					int packetId = DefinedPacket.readVarInt(input);
					DefinedPacket packet = directionData.createPacket(packetId);
					if (packet != null) {
						if (packet instanceof PacketCreateGame) {
							System.out.println("! ! ! Got PacketCreateGame with " + Arrays.toString(ByteBufUtil.getBytes(input)));
						}

						packet.read(input, directionData.getDirection());
						if (input.isReadable()) {
							throw new BadPacketException("Did not read all bytes from packet " + packet.getClass().getSimpleName() + " with ID " + packetId);
						}
					} else {
						input.skipBytes(input.readableBytes());
					}

					out.add(new PacketWrapper(packet, copy));
					copy = null;
				}
			} catch (IndexOutOfBoundsException | BadPacketException e) {
				e.printStackTrace();
			} finally {
				if (copy != null) {
					copy.release();
				}
			}
		}
	}
}
