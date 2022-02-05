package net.gtminecraft.gitgames.proxy.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.compatability.PipelineUtils;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.handler.PacketBossHandler;
import net.gtminecraft.gitgames.compatability.handler.PacketDecoder;
import net.gtminecraft.gitgames.compatability.handler.PacketEncoder;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.connection.handler.ChannelBossHandler;
import net.gtminecraft.gitgames.proxy.connection.handler.HandshakeHandler;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PipelineBase extends ChannelInitializer<Channel> {

	private final CoreProxyPlugin plugin;

	@Override
	protected void initChannel(@NotNull Channel channel) {
		PipelineUtils.BASE.initChannel(channel);
		channel.pipeline()
				.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new PacketDecoder(Protocol.HANDSHAKE, true))
				.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new PacketEncoder(Protocol.HANDSHAKE, true))
				.addFirst(PipelineUtils.CHANNEL_HANDLER, new ChannelBossHandler(this.plugin));
		channel.pipeline().get(PacketBossHandler.class).setPacketHandler(new HandshakeHandler(this.plugin));
		channel.config().setOption(ChannelOption.TCP_NODELAY, true);
	}
}
