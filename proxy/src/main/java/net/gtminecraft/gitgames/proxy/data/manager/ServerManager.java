package net.gtminecraft.gitgames.proxy.data.manager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import net.gtminecraft.gitgames.compatability.DefinedPacket;
import net.gtminecraft.gitgames.compatability.mechanics.*;
import net.gtminecraft.gitgames.compatability.packet.PacketPlayerQueue;
import net.gtminecraft.gitgames.compatability.packet.PacketServerDisconnect;
import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.connection.PipelineBase;
import net.gtminecraft.gitgames.proxy.data.MinigameServerData;
import net.gtminecraft.gitgames.proxy.data.PlayerData;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ServerManager {

	private final CoreProxyPlugin plugin;
	private final TaskScheduler scheduler;
	private final EventLoopGroup bossEventLoopGroup;
	private final ChannelGroup channels;
	private final Random random = new Random();
	private final EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
	private final Map<String, ServerData> servers = new CaseInsensitiveMap<>();
	private final AtomicInteger serverIdGenerator = new AtomicInteger(0);
	@Getter
	private final MinigameServerManager minigamesManager;
	private final Phaser phaser;
	@Getter
	private Channel channel;

	public ServerManager(CoreProxyPlugin plugin) {
		this.plugin = plugin;
		this.minigamesManager = new MinigameServerManager(this, this.random);
		this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		this.phaser = new Phaser(0);
		this.scheduler = plugin.getProxy().getScheduler();
		this.bossEventLoopGroup = new NioEventLoopGroup(1);
		this.scheduler.runAsync(plugin, () -> {
			try {
				this.channel = new ServerBootstrap()
						.group(this.bossEventLoopGroup, this.workerEventLoopGroup)
						.channel(NioServerSocketChannel.class)
						.localAddress(new InetSocketAddress(this.plugin.getSettings().getPort()))
						.childHandler(new PipelineBase(plugin))
						.bind()
						.sync()
						.channel();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

	public void disable() {
		try {
			if (this.channels.isEmpty()) {
				this.plugin.getLogger().info(ChatColor.GREEN + "No servers connected to the network... skipping straight to Event Loop Group shutdown.");
				this.phaser.register();
				this.phaser.arriveAndDeregister();
			} else {
				DefinedPacket packet = new PacketServerDisconnect(PacketServerDisconnect.DisconnectAction.PROMPT_SHUTDOWN);
				this.servers.values().forEach(serverData -> serverData.write(packet));
			}

			this.phaser.arriveAndAwaitAdvance();
			this.bossEventLoopGroup.shutdownGracefully().sync();
			this.workerEventLoopGroup.shutdownGracefully().sync();
			this.channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.scheduler.cancel(this.plugin);
		}
	}

	// Check if server is minigames and remove from inactives
	public synchronized void unregister(Channel channel) {
		this.channels.remove(channel);
		Iterator<ServerData> iterator = this.servers.values().iterator();
		while (iterator.hasNext()) {
			ServerData serverData = iterator.next();
			if (serverData.getChannelWrapper().getChannel().equals(channel)) {
				serverData.disconnect();
				if (serverData instanceof MinigameServerData minigameServerData) {
					this.minigamesManager.unregister(minigameServerData);
				}

				this.phaser.arriveAndDeregister();
				this.plugin.getProxy().getConsole().sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + serverData.getServer().getName() + ChatColor.YELLOW + " has been unregistered from " + ChatColor.LIGHT_PURPLE + "GiTGames" + ChatColor.YELLOW + "."));
				iterator.remove();
			}
		}
	}

	public ServerData register(ChannelWrapper channelWrapper, @NotNull ServerInfo serverInfo) {
		if (this.channels.add(channelWrapper.getChannel())) {
			ServerData serverData;
			ServerType serverType = this.plugin.getSettings().getServerType(serverInfo.getName());
			if (serverType == ServerType.MINIGAME) {
				serverData = new MinigameServerData(this.plugin, serverInfo, channelWrapper, this.serverIdGenerator.getAndIncrement());
				this.minigamesManager.register((MinigameServerData) serverData);
				this.plugin.getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "The minigame server " + ChatColor.LIGHT_PURPLE + serverData.getServer().getName() + ChatColor.YELLOW + " has been registered to the network."));
			} else {
				serverData = new ServerData(this.plugin, serverInfo, channelWrapper, this.serverIdGenerator.getAndIncrement(), serverType);
				this.plugin.getProxy().getConsole().sendMessage(new TextComponent(ChatColor.YELLOW + "The server " + ChatColor.LIGHT_PURPLE + serverData.getServer().getName() + ChatColor.YELLOW + " has been registered to the network."));
			}

			this.servers.put(serverInfo.getName(), serverData);
			this.phaser.register();
			return serverData;
		}

		return null;
	}

	@Nullable
	public ServerData getRandomServer(ServerType serverType) {
		List<ServerData> serverTypes = this.servers.values().stream().filter(filter -> filter.getServerType() == serverType).toList();
		return serverTypes.isEmpty() ? null : serverTypes.get(this.random.nextInt(serverTypes.size()));
	}

	public AbstractGameClassifier retrieveMinigame(@NotNull ProxiedPlayer player, String name, int maxPlayers) {
		boolean valid = false;
		AbstractGameClassifier type = switch (name.toLowerCase()) {
			case "manhunt":
				valid = maxPlayers == 1 ? player.hasPermission(String.format(StringUtil.SOLO_MINIGAME_COMMAND, "manhunt")) : maxPlayers >= 2 && maxPlayers <= 5;
				yield GameClassifiers.MANHUNT;
			case "spleef":
				valid = maxPlayers > 1;
				yield GameClassifiers.SPLEEF;
			default:
				yield GameClassifiers.INACTIVE;
		};

		if (!valid) {
			player.sendMessage(new TextComponent(ChatColor.RED + type.getName() + "s with " + maxPlayers + " player are currently unsupported. To help the developer, consider making a small donation through our online web-store."));
		}

		return type;
	}

	public void queuePlayer(@NotNull PlayerData playerData, String name, int maxPlayers) {
		ProxiedPlayer player = playerData.getPlayer();
		AbstractGameClassifier type = this.retrieveMinigame(player, name, maxPlayers);
		if (GameClassifiers.INACTIVE.equals(type)) {
			return;
		}

		ServerData serverData = this.minigamesManager.randomlySelectServer(type, maxPlayers);
		if (serverData == null) {
			player.sendMessage(new TextComponent(ChatColor.RED + "There are currently no available servers to host a " + type.getName() + " on. Please try again later."));
			return;
		}

		playerData.setStatus(PlayerStatus.QUEUING);
		serverData.write(new PacketPlayerQueue(player.getUniqueId(), null, PacketPlayerQueue.QueueType.QUEUE));
		player.connect(serverData.getServer());
	}

	public boolean isConnected(String name) {
		ServerData serverData = this.getServerData(name);
		return serverData != null && serverData.getChannelWrapper().getChannel().isActive();
	}

	@Nullable
	public ServerData getServerData(String name) {
		return this.servers.get(name);
	}

	@Nullable
	public ServerData getServerData(@Nullable ServerInfo server) {
		return server == null ? null : this.getServerData(server.getName());
	}

	public void acceptToAll(Consumer<ServerData> consumer) {
		this.servers.values().forEach(consumer);
	}
}
