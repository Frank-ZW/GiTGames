package net.gtminecraft.gitgames.compatability;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.gtminecraft.gitgames.compatability.handler.PacketBossHandler;

public class PipelineUtils {

	public static final PipelineUtils.Base BASE = new PipelineUtils.Base();
	public static final String TIMEOUT_HANDLER = "timeout";
	public static final String BOSS_HANDLER = "boss_handler";
	public static final String PACKET_ENCODER = "packet_encoder";
	public static final String PACKET_DECODER = "packet_decoder";
	public static final String CHANNEL_HANDLER = "channel_handler";

	public static final class Base extends ChannelInitializer<Channel> {

		@Override
		public void initChannel(Channel channel) {
			channel.config().setOption(ChannelOption.TCP_NODELAY, true);
			channel.pipeline()
					.addLast("timeout", new ReadTimeoutHandler(30))
					.addLast("boss_handler", new PacketBossHandler());
		}
	}
}
