package net.gtminecraft.gitgames.proxy.data;

import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.mechanics.ServerType;
import net.gtminecraft.gitgames.compatability.packet.PacketShutdown;
import net.gtminecraft.gitgames.compatability.packet.PacketKeepAlive;
import net.gtminecraft.gitgames.compatability.packet.PacketServerDisconnect;
import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.shutdown.Closeable;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ServerData implements Closeable {

	@Getter
	private final ServerInfo server;
	@Getter
	@Setter
	private ChannelWrapper channelWrapper;
	@Getter
	private final ScheduledTask keepAliveTask;
	@Getter
	private final Set<ProxiedPlayer> pendingDisconnections = new HashSet<>();
	@Getter
	private final int serverId;
	@Getter
	@Setter
	private long id;
	@Getter
	@Setter
	private boolean receivedDisconnectionRequest;
	@Getter
	private final ServerType serverType;

	public ServerData(CoreProxyPlugin plugin, ServerInfo server, ChannelWrapper channelWrapper, int serverId, ServerType serverType) {
		this.server = server;
		this.channelWrapper = channelWrapper;
		this.serverId = serverId;
		this.serverType = serverType;
		this.id = -1L;
		this.keepAliveTask = plugin.getProxy().getScheduler().schedule(plugin, this::sendKeepAlive, 25L, 25L, TimeUnit.SECONDS);
	}

	public void disconnect() {
		this.keepAliveTask.cancel();
		this.channelWrapper.close();
	}

	public void write(DefinedPacket packet) {
		this.channelWrapper.write(packet);
	}

	public void sendKeepAlive() {
		this.id = System.currentTimeMillis();
		this.channelWrapper.write(new PacketKeepAlive(this.id));
	}

	@Override
	public void scheduleShutdown(int duration) {
		this.write(new PacketShutdown("", duration, PacketShutdown.ShutdownAction.START));
	}

	@Override
	public void cancelShutdown(String who) {
		this.write(new PacketShutdown(who, -1, PacketShutdown.ShutdownAction.CANCEL));
	}

	public void handlePendingDisconnection(ProxiedPlayer player) {
		if (this.receivedDisconnectionRequest && this.pendingDisconnections.remove(player) && this.pendingDisconnections.isEmpty()) {
			this.channelWrapper.close(new PacketServerDisconnect(PacketServerDisconnect.DisconnectAction.CONFIRM_SHUTDOWN));
		}
	}

	public void pendingDisconnections(Collection<ProxiedPlayer> players) {
		this.pendingDisconnections.addAll(players);
	}

	@Override
	public int hashCode() {
		return 19 * this.serverId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof ServerData o)) {
			return false;
		}

		return o.getServerId() == this.serverId;
	}
}
