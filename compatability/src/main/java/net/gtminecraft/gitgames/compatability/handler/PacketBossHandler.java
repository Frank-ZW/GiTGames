package net.gtminecraft.gitgames.compatability.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.PacketHandler;
import net.gtminecraft.gitgames.compatability.exception.KeepAliveTimeoutException;
import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.compatability.wrapper.PacketWrapper;
import org.jetbrains.annotations.NotNull;

public class PacketBossHandler extends ChannelInboundHandlerAdapter {

	@Getter
	protected ChannelWrapper channelWrapper;
	@Setter
	private PacketHandler packetHandler;

	@Override
	public void channelActive(@NotNull ChannelHandlerContext context) throws Exception {
		this.channelWrapper = new ChannelWrapper(context);
		if (this.packetHandler != null) {
			this.packetHandler.connected(this.channelWrapper);
		}
	}

	@Override
	public void channelInactive(@NotNull ChannelHandlerContext context) throws Exception {
		if (this.packetHandler != null) {
			this.channelWrapper.markClosed();
			this.packetHandler.disconnected(this.channelWrapper);
		}
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object msg) throws Exception {
		if (this.packetHandler != null) {
			PacketWrapper wrapper = (PacketWrapper) msg;
			boolean shouldHandle = this.packetHandler.shouldHandle(wrapper);
			try {
				if (shouldHandle && wrapper.getPacket() != null) {
					wrapper.getPacket().handle(this.packetHandler);
				}

				if (shouldHandle) {
					this.packetHandler.handle(wrapper);
				}
			} finally {
				wrapper.trySingleRelease();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
		cause.printStackTrace();
		if (cause instanceof KeepAliveTimeoutException) {
			this.channelWrapper.close();
		}
	}
}
