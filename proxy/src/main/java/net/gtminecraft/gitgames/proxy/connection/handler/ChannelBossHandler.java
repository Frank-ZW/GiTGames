package net.gtminecraft.gitgames.proxy.connection.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;

@RequiredArgsConstructor
public class ChannelBossHandler extends ChannelInboundHandlerAdapter {

	private final CoreProxyPlugin plugin;

	@Override
	public void handlerRemoved(ChannelHandlerContext context) {
		this.plugin.getServerManager().unregister(context.channel());
	}
}
