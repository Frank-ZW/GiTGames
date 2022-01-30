package net.gtminecraft.gitgames.proxy.connection.handler;

import net.gtminecraft.gitgames.compatability.PacketHandler;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.exception.QuietException;
import net.gtminecraft.gitgames.compatability.handler.PacketBossHandler;
import net.gtminecraft.gitgames.compatability.packet.PacketHandshake;
import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.compatability.wrapper.PacketWrapper;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;

public class HandshakeHandler extends PacketHandler {

	private final CoreProxyPlugin plugin;
	private ChannelWrapper channelWrapper;

	public HandshakeHandler(CoreProxyPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean shouldHandle(PacketWrapper packetWrapper) {
		return !this.channelWrapper.isClosing();
	}

	@Override
	public void connected(ChannelWrapper channelWrapper) {
		this.channelWrapper = channelWrapper;
	}

	@Override
	public void disconnected(ChannelWrapper channelWrapper) {
		this.channelWrapper.close();
	}

	@Override
	public void exception(Throwable t) throws Exception {
		throw new Exception(t);
	}

	@Override
	public void handle(PacketWrapper packetWrapper) {
		if (packetWrapper.getPacket() == null) {
			throw new QuietException("Unexpected packet received during initial handshake process");
		}
	}

	public void handle(PacketHandshake packet) {
		ServerInfo server = this.plugin.getProxy().getServerInfo(packet.getName());
		if (server == null) {
			this.channelWrapper.write(packet);
			return;
		}

		ServerData serverData = null;
		InetSocketAddress remoteAddress = (InetSocketAddress) server.getSocketAddress();
		if (packet.getHost().equals(remoteAddress.getAddress().getHostAddress()) && packet.getPort() == remoteAddress.getPort()) {
			serverData = this.plugin.getServerManager().register(this.channelWrapper, server);
			if (serverData != null) {
				packet = new PacketHandshake(true);
			}
		}

		this.channelWrapper.write(packet);
		if (packet.isConfirmed()) {
			this.channelWrapper.setProtocol(Protocol.PLAY);
			this.channelWrapper.getChannel().pipeline().get(PacketBossHandler.class).setPacketHandler(new UpstreamBridge(this.plugin, server, serverData, this.channelWrapper));
		}
	}
}
