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
import net.gtminecraft.gitgames.server.minigame.AbstractGame;
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
	private final GameLoaderManager loaderManager;
	@Getter
	private AbstractGameClassifier classifier;
	private AbstractGameState state;
	@Setter
	@Getter
	private AbstractGame game;
	@Getter
	private int maxPlayers;
	@Getter
	private int minPlayers;

	public MinigameManager(CorePlugin plugin) {
		this.plugin = plugin;
		this.loaderManager = new GameLoaderManager(plugin);
		this.setState(new InactiveState());
	}

	public void disable() {
		Bukkit.getScheduler().cancelTasks(this.plugin);
		if (this.game == null) {
			Bukkit.getConsoleSender().sendMessage(Component.text(ChatColor.GREEN + "No active minigame detected... skipping straight to protocol disconnection."));
		} else {
			if (this.isInState(ActiveState.class)) {
				if (this.game.isFinishedTaskRunning()) {
					this.game.runFinishedTaskNow();
				} else {
					this.game.endMinigame(new AbstractGame.GeneralErrorInterruption(ChatColor.GREEN + "The server you were on has disconnected from the network. If you believe the server crashed, contact an administrator."), true);
				}
			} else {
				if (this.isInState(CountdownState.class)) {
					this.game.cancelCountdown();
				}

				this.game.endTeleport();
				if (this.game.worldsLoaded()) {
					this.game.deleteWorlds(true);
				}
			}

			this.game = null;
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
		this.game = null;
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

		this.game.addPlayer(player);
	}

	/**
	 * Forcefully end an active game if one exists. This method guarantees that all players will be connected back to the
	 * lobby and the server re-queued.
	 */
	public void forceEnd() {
		if (this.game == null || this.isInState(InactiveState.class) || this.isInState(FinishedState.class)) {
			return;
		}

		if (this.isInState(ActiveState.class)) {
			if (this.game.isFinishedTaskRunning()) {
				this.game.runFinishedTaskNow();
			} else {
				this.game.endMinigame(new AbstractGame.GeneralErrorInterruption(ChatColor.GREEN + "The " + this.game.getName() + " you were in was forcefully ended."), true);
			}
		} else {
			if (this.isInState(CountdownState.class)) {
				this.game.cancelCountdown();
			}

			if (this.game.getNumPlayers() != 0) {
				this.plugin.getConnectionManager().write(new PacketGameUpdate(GameStateUtils.FINISHED_STATE_PRIORITY, this.game.getPlayers()));
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
		if (this.game != null && !this.isInState(InactiveState.class)) {
			Bukkit.getLogger().warning("Cancelled an attempt to override an active minigame. Contact the developer if this occurs.");
			return;
		}

		this.classifier = GameClassifiers.CLASSIFIERS[gameId];
		GameClassLoaderInterface classLoaderInterface = this.loaderManager.getGameLoader(classifier);
		if (classLoaderInterface == null) {
			this.unload();
			return;
		}

		this.game = classLoaderInterface.load(gameKey);
		this.minPlayers = this.classifier.playerThreshold(maxPlayers);
		this.maxPlayers = maxPlayers;
		Bukkit.getLogger().info(ChatColor.GREEN + "Creating new " + this.game.getName() + " with game key of " + gameKey);
		this.nextState();
	}

	@EventHandler
	public void onAsyncChat(AsyncChatEvent e) {
		Player player = e.getPlayer();
		if (this.game == null || (!this.isInState(ActiveState.class) && !this.isInState(FinishedState.class))) {
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

			if (audience instanceof Player recipient && (this.game.isPlayer(player.getUniqueId()) ? (this.game.isSpectator(player.getUniqueId()) ? !this.game.isSpectator(recipient.getUniqueId()) : !this.game.isPlayer(recipient.getUniqueId())) : !this.game.isPlayer(recipient.getUniqueId()))) {
				iterator.remove();
				continue;
			}

			e.renderer(new GameRenderer(this.game));
		}
	}
}
