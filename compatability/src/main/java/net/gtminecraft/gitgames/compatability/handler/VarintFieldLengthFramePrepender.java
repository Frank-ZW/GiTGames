package net.gtminecraft.gitgames.compatability.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

public class VarintFieldLengthFramePrepender extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
		int readable = msg.readableBytes();
		int header = VarintFieldLengthFramePrepender.variableIntSize(readable);
		out.ensureWritable(header + readable);
		DefinedPacket.writeVarInt(out, readable);
		out.writeBytes(msg);
	}

	private static int variableIntSize(int paramInt) {
		if ((paramInt & 0xFFFFFF80) == 0) {
			return 1;
		}

		if ((paramInt & 0xFFFFC000) == 0) {
			return 2;
		}

		if ((paramInt & 0xFFE00000) == 0) {
			return 3;
		}

		if ((paramInt & 0xF0000000) == 0) {
			return 4;
		}

		return 5;
	}
}
