package net.gtminecraft.gitgames.server.minigame.manager;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.mechanics.*;
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
	@Getter
	private AbstractGameClassifier classifier;
	private AbstractGameState state;
	@Setter
	@Getter
	private AbstractMinigame minigame;
	@Getter
	private int maxPlayers;
	@Getter
	private int minPlayers;

	public MinigameManager(CorePlugin plugin) {
		this.plugin = plugin;
		this.setState(new InactiveState());
	}

	public void disable() {
		Bukkit.getScheduler().cancelTasks(this.plugin);
		if (this.minigame == null) {
			Bukkit.getConsoleSender().sendMessage(Component.text(ChatColor.GREEN + "No active minigame detected... skipping straight to protocol disconnection."));
		} else {
			if (this.isInState(ActiveState.class)) {
				if (this.minigame.isFinishedTaskRunning()) {
					this.minigame.runFinishedTaskNow();
				} else {
					this.minigame.endMinigame(new AbstractMinigame.GeneralErrorInterruption(ChatColor.GREEN + "The server you were on has disconnected from the network. If you believe the server crashed, contact an administrator."), true);
				}
			} else {
				if (this.isInState(CountdownState.class)) {
					this.minigame.cancelCountdown();
				}

				this.minigame.endTeleport();
				if (this.minigame.worldsLoaded()) {
					this.minigame.deleteWorlds(true);
				}
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

	public void unload() {
		this.minigame = null;
		this.maxPlayers = 0;
		this.minPlayers = 0;
		this.spectatorQueue.clear();
	}

	public void connectToProxyLobby(Player player) {
		this.plugin.getConnectionManager().write(new PacketPlayerConnect(PlayerStatus.INACTIVE, ServerType.LOBBY, player.getUniqueId()));
	}

	public void queuePlayer(UUID player, @Nullable UUID target) {
		if (target != null) {
			this.spectatorQueue.put(player, target);
			return;
		}

		this.minigame.addPlayer(player);
	}

	/**
	 * Forcefully nd an active game if one exists. This method guarantees that all players will be connected back to the
	 * lobby and the server requeued.
	 */
	public void forceEnd() {
		if (this.minigame == null || this.isInState(InactiveState.class) || this.isInState(FinishedState.class)) {
			return;
		}

		if (this.isInState(ActiveState.class)) {
			if (this.minigame.isFinishedTaskRunning()) {
				this.minigame.runFinishedTaskNow();
			} else {
				this.minigame.endMinigame(new AbstractMinigame.GeneralErrorInterruption(ChatColor.GREEN + "The " + this.minigame.getName() + " you were in was forcefully ended."), true);
			}
		} else {
			if (this.isInState(CountdownState.class)) {
				this.minigame.cancelCountdown();
			}

			if (this.minigame.getNumPlayers() != 0) {
				this.plugin.getConnectionManager().write(new PacketGameUpdate(GameStateUtils.FINISHED_STATE_PRIORITY, this.minigame.getPlayers()));
			}

			this.setState(new FinishedState());
		}
	}

	/**
	 * @param gameId		The id of the associated game to create
	 * @param gameKey		The unique game key identifier for the current minigame session
	 * @param maxPlayers	The maximum number of players the minigame can accommodate
	 */
	public void createMinigame(int gameId, int gameKey, int maxPlayers) {
		if (this.minigame != null && !this.isInState(InactiveState.class)) {
			Bukkit.getLogger().warning("Cancelled an attempt to override an active minigame. Contact the developer if this occurs.");
			return;
		}

		this.classifier = GameClassifiers.CLASSIFIERS[gameId];
		GameClassLoaderInterface loaderInterface = this.loaderManager.getGameLoader(gameId);
		if (loaderInterface == null || this.classifier == null) {
			this.unload();
			return;
		}

		this.minigame = loaderInterface.loadGame(this.plugin.getSettings().getLobby(), gameKey);
		this.minPlayers = this.classifier.playerThreshold(maxPlayers);
		this.maxPlayers = maxPlayers;
		Bukkit.getLogger().info(ChatColor.GREEN + "Creating new " + this.minigame.getName() + " with game key of " + gameKey);
		this.nextState();
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
