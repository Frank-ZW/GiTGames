package net.gtminecraft.gitgames.compatability.wrapper;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.handler.PacketDecoder;
import net.gtminecraft.gitgames.compatability.handler.PacketEncoder;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;

public class ChannelWrapper {

	@Getter
	private final Channel channel;
	@Getter
	@Setter
	private SocketAddress remoteAddress;
	@Getter
	private volatile boolean closing;
	@Getter
	private volatile boolean closed;

	public ChannelWrapper(ChannelHandlerContext context) {
		this.channel = context.channel();
		this.remoteAddress = this.channel.remoteAddress() == null ? this.channel.parent().remoteAddress() : this.channel.remoteAddress();
	}

	public void setProtocol(Protocol protocol) {
		this.channel.pipeline().get(PacketDecoder.class).setProtocol(protocol);
		this.channel.pipeline().get(PacketEncoder.class).setProtocol(protocol);
	}

	public void write(Object packet) {
		if (!this.closed) {
			if (packet instanceof PacketWrapper wrapper) {
				wrapper.setReleased(true);
				this.channel.writeAndFlush(wrapper.getBuf(), this.channel.voidPromise());
			} else {
				this.channel.writeAndFlush(packet, this.channel.voidPromise());
			}
		}
	}

	public void markClosed() {
		this.closing = true;
		this.closed = true;
	}

	public void close() {
		this.close(null);
	}

	public void close(@Nullable Object packet) {
		if (!this.closed) {
			this.closing = true;
			this.closed = true;
			if (packet != null && this.channel.isActive()) {
				this.channel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE).addListener(ChannelFutureListener.CLOSE);
			} else {
				this.channel.flush();
				this.channel.close();
			}
		}
	}

}
