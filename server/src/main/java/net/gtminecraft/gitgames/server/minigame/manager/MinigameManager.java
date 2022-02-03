package net.gtminecraft.gitgames.server.minigame.manager;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.gtminecraft.gitgames.compatability.mechanics.ServerType;
import net.gtminecraft.gitgames.compatability.packet.PacketGameUpdate;
import net.gtminecraft.gitgames.compatability.packet.PacketPlayerConnect;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.loader.manager.GameLoaderManager;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.states.impl.ActiveState;
import net.gtminecraft.gitgames.server.minigame.states.impl.CountdownState;
import net.gtminecraft.gitgames.server.minigame.states.impl.FinishedState;
import net.gtminecraft.gitgames.server.minigame.states.impl.InactiveState;
import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import net.gtminecraft.gitgames.server.util.PlayerUtil;
import net.gtminecraft.gitgames.server.renderer.GameRenderer;
import net.gtminecraft.gitgames.server.renderer.LobbyRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MinigameManager implements Listener {

	@Getter
	private final CorePlugin plugin;
	@Getter
	private final Map<UUID, UUID> spectatorQueue = new HashMap<>();
	private final GameLoaderManager loaderManager = new GameLoaderManager();
	private AbstractGameState state;
	@Setter
	@Getter
	private AbstractMinigame minigame;
	@Setter
	@Getter
	private int maxPlayers;

	public MinigameManager(CorePlugin plugin) {
		this.plugin = plugin;
		this.setState(new InactiveState(this));
	}

	public void disable() {
		Bukkit.getScheduler().cancelTasks(this.plugin);
		if (this.minigame == null) {
			Bukkit.getConsoleSender().sendMessage(Component.text(ChatColor.GREEN + "No active minigame detected... skipping straight to protocol disconnection."));
		} else {
			if (this.isInState(ActiveState.class)) {
				this.minigame.endMinigame(new AbstractMinigame.GeneralErrorWrapper(ChatColor.GREEN + "The server you were on has disconnected from the network. If you believe the server crashed, contact an administrator."), true);
			} else if (this.isInState(FinishedState.class)) {
				this.minigame.endTeleport();
				this.minigame.deleteWorlds(true);
			} else {
				this.minigame.deleteWorlds(true);
			}

			this.minigame = null;
		}
	}

	public boolean isInState(Class<? extends AbstractGameState> clazz) {
		return this.state != null && this.state.getClass() == clazz;
	}

	public void nextState() {
		this.setState(this.state.nextState());
	}

	public void setState(AbstractGameState state) {
		if (this.state != null) {
			this.state.onDisable();
			HandlerList.unregisterAll(this.state);
		}

		this.state = state;
		Bukkit.getPluginManager().registerEvents(this.state, this.plugin);
		this.state.onEnable();
	}

	public void clearSpectatorQueue() {
		this.spectatorQueue.clear();
	}

	public void sendToProxyLobby(Player player) {
		this.plugin.getConnectionManager().write(new PacketPlayerConnect(PlayerStatus.INACTIVE, ServerType.LOBBY, player.getUniqueId()));
	}

	public void handleQueue(UUID player, @Nullable UUID target) {
		if (target != null) {
			this.spectatorQueue.put(player, target);
			return;
		}

		this.minigame.addPlayer(player);
	}

	public void handleForceEnd() {
		if (this.minigame == null) {
			return;
		}

		if (this.isInState(ActiveState.class) || this.isInState(FinishedState.class)) {
			if (this.isInState(ActiveState.class)) {
				this.minigame.endMinigame(new AbstractMinigame.GeneralErrorWrapper(ChatColor.GREEN + "The " + this.minigame.getName() + " you were in was forcefully ended."), true);
			}

			this.minigame.endTeleport();
			this.minigame.deleteWorlds();
		} else {
			if (this.isInState(CountdownState.class)) {
				this.minigame.cancelCountdown();
			}

			if (this.minigame.getNumPlayers() == 0) {
				return;
			}

			this.plugin.getConnectionManager().write(new PacketGameUpdate(GameStateUtils.FINISHED_STATE_PRIORITY, this.minigame.getPlayers()));
		}
	}

	public void createMinigame(int gameId, int gameKey, int maxPlayers) {
		if (this.minigame != null && !this.isInState(InactiveState.class)) {
			Bukkit.getLogger().warning("Cancelled an attempt to override an active minigame. Contact the developer if this occurs.");
			return;
		}

		GameClassLoaderInterface loaderInterface = this.loaderManager.getGameLoader(gameId);
		if (loaderInterface == null) {
			this.maxPlayers = 0;
			return;
		}

		this.minigame = loaderInterface.loadGame(this.plugin.getSettings().getLobby(), gameKey);
		Bukkit.getLogger().info(ChatColor.GREEN + "Creating new " + this.minigame.getName() + " with game key of " + gameKey);
		this.maxPlayers = maxPlayers;
		this.nextState();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (this.isInState(ActiveState.class) || this.isInState(FinishedState.class)) {
			this.spectatorQueue.computeIfPresent(player.getUniqueId(), (k, v) -> {
				Player target = Bukkit.getPlayer(v);
				if (target == null) {
					this.sendToProxyLobby(player);
					return null;
				}

				player.teleportAsync(target.getLocation()).thenAccept(result -> {
					if (result) {
						this.minigame.hideSpectator(player);
						PlayerUtil.setSpectator(player);
						player.sendMessage(ChatColor.GREEN + "You are now spectating " + target.getName() + ".");
					} else {
						player.sendMessage(ChatColor.RED + "Failed to teleport you to " + target.getName() + ". You have been connected back to the lobby.");
						this.sendToProxyLobby(player);
					}
				});

				return null;
			});
		}
	}

	@EventHandler
	public void onAsyncChat(AsyncChatEvent e) {
		Player player = e.getPlayer();
		if (this.minigame == null || (!this.isInState(ActiveState.class) && !this.isInState(FinishedState.class))) {
			e.renderer(new LobbyRenderer());
			return;
		}

		Iterator<Audience> iterator = e.viewers().iterator();
		while (iterator.hasNext()) {
			Audience audience = iterator.next();
			if (!(audience instanceof Player) && !(audience instanceof ConsoleCommandSender)) {
				iterator.remove();
				continue;
			}

			if (audience instanceof Player recipient && (this.minigame.isPlayer(player.getUniqueId()) ? (this.minigame.isSpectator(player.getUniqueId()) ? !this.minigame.isSpectator(recipient.getUniqueId()) : !this.minigame.isPlayer(recipient.getUniqueId())) : !this.minigame.isPlayer(recipient.getUniqueId()))) {
				iterator.remove();
				continue;
			}

			e.renderer(new GameRenderer(this.minigame));
		}
	}
}
