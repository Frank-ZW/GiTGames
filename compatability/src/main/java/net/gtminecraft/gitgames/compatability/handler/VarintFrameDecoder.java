package net.gtminecraft.gitgames.compatability.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

import java.util.List;

public class VarintFrameDecoder extends ByteToMessageDecoder {

	private static boolean WARNING = false;

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf input, List<Object> out) {
		if (!context.channel().isActive()) {
			input.skipBytes(input.readableBytes());
			return;
		}

		input.markReaderIndex();
		final byte[] bytes = new byte[3];
		for (int i = 0; i < bytes.length; i++) {
			if (!input.isReadable()) {
				input.resetReaderIndex();
				return;
			}

			bytes[i] = input.readByte();
			if (bytes[i] >= 0) {
				int length = DefinedPacket.readVarInt(Unpooled.wrappedBuffer(bytes));
				if (length == 0) {
					throw new CorruptedFrameException("Received an empty packet!");
				}

				if (input.readableBytes() < length) {
					input.resetReaderIndex();
					return;
				}

				if (input.hasMemoryAddress()) {
					out.add(input.slice(input.readerIndex(), length).retain());
					input.skipBytes(length);
				} else {
					if (!VarintFrameDecoder.WARNING) {
						VarintFrameDecoder.WARNING = true;
						System.out.println("Netty is not using direct IO buffers.");
					}

					ByteBuf buf = context.alloc().directBuffer(length);
					input.readBytes(buf);
					out.add(buf);
				}

				return;
			}
		}

		throw new CorruptedFrameException("Received packet has a length wider than 21 bits");
	}
}
