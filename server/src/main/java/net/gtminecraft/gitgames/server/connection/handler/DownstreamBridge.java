package net.gtminecraft.gitgames.server.connection.handler;

import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.compatability.PacketHandler;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.packet.*;
import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.compatability.wrapper.PacketWrapper;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.runnable.ShutdownRunnable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

@RequiredArgsConstructor
public class DownstreamBridge extends PacketHandler {

	private final CorePlugin plugin;
	private ChannelWrapper channelWrapper;
	private ShutdownRunnable shutdown;

	@Override
	public void connected(ChannelWrapper wrapper) {
		this.channelWrapper = wrapper;
	}

	@Override
	public void disconnected(ChannelWrapper wrapper) {
		this.channelWrapper.close();
	}

	@Override
	public void exception(Throwable t) throws Exception {
		throw new Exception(t);
	}

	@Override
	public void handle(PacketWrapper wrapper) {}

	@Override
	public void handle(PacketHandshake packet) {
		if (packet.isConfirmed()) {
			this.channelWrapper.setProtocol(Protocol.PLAY);
			Bukkit.getLogger().info(ChatColor.GREEN + "Established connection for " + this.plugin.getSettings().getName() + " on " + this.plugin.getSettings().getHost() + ":" + this.plugin.getSettings().getPort() + ". This server can now communicate with the proxy.");
		} else {
			Bukkit.getLogger().warning("One or more of the settings entered in the config.yml do not match up with the settings entered in the proxy. Before restarting the plugin, double check to make sure the name, IP address, and port of the server match that of Bungee's.");
			this.channelWrapper.close();
		}
	}

	@Override
	public void handle(PacketKeepAlive packet) {
		this.channelWrapper.write(packet);
	}

	@Override
	public void handle(PacketShutdown packet) {
		switch (packet.getAction()) {
			case START -> {
				if (this.shutdown != null && !this.shutdown.isCancelled()) {
					this.shutdown.cancel();
					Bukkit.broadcast(Component.text(ChatColor.YELLOW + "The server shutdown has been rescheduled to " + ChatColor.LIGHT_PURPLE + packet.getAttribute() + " second" + (packet.getAttribute() == 1 ? "" : "s") + ChatColor.YELLOW + "."));
				} else {
					Bukkit.broadcast(Component.text(ChatColor.YELLOW + "The server has been scheduled to shutdown in " + ChatColor.LIGHT_PURPLE + packet.getAttribute() + " second" + (packet.getAttribute() == 1 ? "" : "s") + ChatColor.YELLOW + "."));
				}

				this.shutdown = new ShutdownRunnable(this.plugin, packet.getAttribute());
				this.shutdown.runTaskTimerAsynchronously(this.plugin, 20L, 20L);
			}

			case CANCEL -> {
				if (this.shutdown != null && !this.shutdown.isCancelled()) {
					this.shutdown.cancel();
					Bukkit.broadcast(Component.text(ChatColor.YELLOW + "The server shutdown has been cancelled by " + ChatColor.LIGHT_PURPLE + packet.getWho() + ChatColor.YELLOW + "."));
				}
			}

			default -> this.channelWrapper.write(new PacketShutdown(packet.getWho(), this.shutdown != null && !this.shutdown.isCancelled() ? this.shutdown.getCountdown() : -1, PacketShutdown.ShutdownAction.STATUS));
		}
	}

	@Override
	public void handle(PacketServerDisconnect packet) {
		switch (packet.getAction()) {
			case PROMPT_SHUTDOWN -> Bukkit.getScheduler().callSyncMethod(this.plugin, () -> {
				Bukkit.getServer().shutdown();
				return null;
			});

			case CONFIRM_SHUTDOWN -> this.plugin.getConnectionManager().countdown();
			default -> Bukkit.getLogger().warning("Received unknown action enum for " + packet.getClass().getSimpleName() + ", got " + packet.getAction());
		}
	}

	@Override
	public void handle(PacketPlayerQueue packet) {
		this.plugin.getMinigameManager().queuePlayer(packet.getPlayer(), packet.getTarget());
	}

	@Override
	public void handle(PacketServerAction packet) {
		switch (packet.getAction()) {
			case FORCE_END -> Bukkit.getScheduler().callSyncMethod(this.plugin, () -> {
				this.plugin.getMinigameManager().forceEnd();
				return null;
			});

			default -> {}
		}
	}

	@Override
	public void handle(PacketCreateGame packet) {
		this.plugin.getMinigameManager().createMinigame(packet.getGameId(), packet.getGameKey(), packet.getMaxPlayers());
	}
}
