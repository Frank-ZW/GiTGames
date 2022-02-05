package net.gtminecraft.gitgames.server.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import lombok.AllArgsConstructor;
import net.gtminecraft.gitgames.compatability.PipelineUtils;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.handler.PacketBossHandler;
import net.gtminecraft.gitgames.compatability.handler.PacketDecoder;
import net.gtminecraft.gitgames.compatability.handler.PacketEncoder;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.connection.handler.DownstreamBridge;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class PipelineBase extends ChannelInitializer<Channel> {

	private final CorePlugin plugin;

	@Override
	protected void initChannel(@NotNull Channel channel) {
		channel.config().setOption(ChannelOption.TCP_NODELAY, true);
		PipelineUtils.BASE.initChannel(channel);
		channel.pipeline()
				.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new PacketDecoder(Protocol.HANDSHAKE, false))
				.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new PacketEncoder(Protocol.HANDSHAKE, false));
		channel.pipeline().get(PacketBossHandler.class).setPacketHandler(new DownstreamBridge(this.plugin));
	}
}
