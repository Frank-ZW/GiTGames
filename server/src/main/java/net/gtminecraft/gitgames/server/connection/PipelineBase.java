package net.gtminecraft.gitgames.server.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
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
		PipelineUtils.BASE.initChannel(channel);
		channel.pipeline()
				.addBefore(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.PACKET_DECODER, new PacketDecoder(Protocol.HANDSHAKE, false))
				.addBefore(PipelineUtils.BOSS_HANDLER, PipelineUtils.PACKET_ENCODER, new PacketEncoder(Protocol.HANDSHAKE, false));
		channel.pipeline().get(PacketBossHandler.class).setPacketHandler(new DownstreamBridge(this.plugin));
	}
}
