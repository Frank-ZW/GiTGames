package net.gtminecraft.gitgames.compatability;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.gtminecraft.gitgames.compatability.handler.PacketBossHandler;
import net.gtminecraft.gitgames.compatability.handler.VarintFieldLengthFramePrepender;
import net.gtminecraft.gitgames.compatability.handler.VarintFrameDecoder;

public class PipelineUtils {

	public static final PipelineUtils.Base BASE = new PipelineUtils.Base();
	public static final String TIMEOUT_HANDLER = "timeout";
	public static final String BOSS_HANDLER = "boss_handler";
	public static final String PACKET_ENCODER = "packet_encoder";
	public static final String PACKET_DECODER = "packet_decoder";
	public static final String CHANNEL_HANDLER = "channel_handler";
	public static final String FRAME_DECODER = "frame_decoder";
	public static final String FRAME_PREPENDER = "frame_prepender";

	public static final class Base extends ChannelInitializer<Channel> {

		@Override
		public void initChannel(Channel channel) {
			channel.config().setOption(ChannelOption.TCP_NODELAY, true);
			channel.pipeline()
					.addLast(PipelineUtils.FRAME_DECODER, new VarintFrameDecoder())
					.addLast(PipelineUtils.TIMEOUT_HANDLER, new ReadTimeoutHandler(30))
					.addLast(PipelineUtils.FRAME_PREPENDER, new VarintFieldLengthFramePrepender())
					.addLast(PipelineUtils.BOSS_HANDLER, new PacketBossHandler());
		}
	}
}
