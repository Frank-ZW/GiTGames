package net.gtminecraft.gitgames.server.connection.manager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.packet.PacketHandshake;
import net.gtminecraft.gitgames.compatability.packet.PacketServerDisconnect;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.connection.PipelineBase;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConnectionManager {

	private final CorePlugin plugin;
	private final CountDownLatch shutdown = new CountDownLatch(1);
	private final EventLoopGroup group;
	@Setter
	private Channel channel;

	public ConnectionManager(CorePlugin plugin) {
		this.plugin = plugin;
		this.group = new NioEventLoopGroup();
		this.attemptConnection();
	}

	public void disable() {
		try {
			if (this.channel.isActive()) {
				this.channel.writeAndFlush(new PacketServerDisconnect(PacketServerDisconnect.DisconnectAction.REQUEST_SHUTDOWN));
				if (!this.shutdown.await(10L, TimeUnit.SECONDS)) {
					Bukkit.getLogger().info(ChatColor.RED + "Did not receive server disconnection confirmation packet with an action ID of one.");
				}
			} else {
				this.shutdown.countDown();
			}

			this.group.shutdownGracefully().sync();
			this.channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void attemptConnection() {
		ChannelFutureListener listener = (future) -> {
			if (future.isSuccess()) {
				future.channel().writeAndFlush(new PacketHandshake(this.plugin.getSettings().getName(), this.plugin.getSettings().getHost(), this.plugin.getSettings().getPort()));
			} else {
				Bukkit.getLogger().info(ChatColor.YELLOW + "Failed to connect with the Proxy... another attempt will be made in 10 seconds. Cause: " + future.cause().getClass().getSimpleName());
				new BootstrapRunnable(this).runTaskLater(this.plugin, 200L);
			}
		};

		this.channel = new Bootstrap()
				.group(this.group)
				.channel(NioSocketChannel.class)
				.remoteAddress(new InetSocketAddress(this.plugin.getSettings().getPluginHost(), this.plugin.getSettings().getPluginPort()))
				.handler(new PipelineBase(this.plugin))
				.connect()
				.addListener(listener)
				.channel();
	}

	public void write(@NotNull DefinedPacket packet) {
		this.channel.writeAndFlush(packet);
	}

	public void countdown() {
		this.shutdown.countDown();
	}

	@RequiredArgsConstructor
	private static final class BootstrapRunnable extends BukkitRunnable {

		private final ConnectionManager connectionManager;

		@Override
		public void run() {
			this.connectionManager.attemptConnection();
		}
	}
}
