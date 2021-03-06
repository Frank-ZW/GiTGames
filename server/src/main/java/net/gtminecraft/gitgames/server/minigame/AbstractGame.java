package net.gtminecraft.gitgames.server.minigame;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import lombok.Getter;
import net.gtminecraft.gitgames.compatability.packet.PacketPlayerDisconnect;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.map.GameMapInterface;
import net.gtminecraft.gitgames.server.map.MapDataContainer;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.gtminecraft.gitgames.server.runnable.CountdownRunnable;
import net.gtminecraft.gitgames.server.util.ItemUtil;
import net.gtminecraft.gitgames.server.util.MinecraftUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.milkbowl.vault.chat.Chat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class AbstractGame implements Listener {

	protected final CorePlugin plugin;
	protected final Random random;
	@Getter
	protected final Map<UUID, BukkitTask> disconnections = new HashMap<>();
	protected final Set<UUID> players = new HashSet<>();
	protected final Set<UUID> spectators = new HashSet<>();
	protected final GameMapInterface map;
	protected WinnerInterface winner = new EmptyWinner();
	@Getter
	protected CountdownRunnable countdown;
	protected BukkitTask finished;
	@Getter
	protected final String name;
	@Getter
	protected int gameKey;
	protected long startTimestamp;

	public AbstractGame(MapDataContainer container, String name, int gameKey) {
		this.plugin = CorePlugin.getInstance();
		this.random = this.plugin.getRandom();
		this.map = container.toLoadedGameMap(this);
		this.name = name;
		this.gameKey = gameKey;
	}

	/**
	 * Returns the game's friendly display name in lowercase with all spaces replaced with underscores.
	 *
	 * @return	A String representing the raw name of the game.
	 */
	public String getRawName() {
		return StringUtils.lowerCase(this.name.replaceAll("\\s+", "_"));
	}

	/**
	 * Returns the raw name with the game key and environment type appended to the end by underscores.
	 *
	 * @param environment	The environment of the world to be generated.
	 * @return				A String of the world's name.
	 */
	public String getWorldName(World.Environment environment) {
		return StringUtils.lowerCase(this.getRawName() + "_" + this.gameKey + "_" + environment);
	}

	public @NotNull Location getLobby() {
		return this.map.getLobby();
	}

	/**
	 * Directly adds a player to the internal player queue. This method must not be called at any time unless the
	 * server is in the queuing state.
	 *
	 * @param uniqueId	The UUID of the player to be directly added to the game.
	 */
	public void addPlayer(UUID uniqueId) {
		this.players.add(uniqueId);
	}

	/**
	 * @return	The number of players in the minigame.
	 */
	public int getNumPlayers() {
		return this.players.size();
	}

	/**
	 * Returns a collection of all players associated with the game. This method can safely be used during the
	 * queuing, preparation, and countdown stage.
	 *
	 * @return	The collection of player UUIDs backing the game.
	 */
	public Collection<UUID> getPlayers() {
		return this.players;
	}

	/**
	 *
	 * @param uniqueId	The UUID of the player to be checked.
	 * @return			True if the player is spectating the game, false otherwise.
	 */
	public boolean isSpectator(UUID uniqueId) {
		return this.spectators.contains(uniqueId);
	}

	/**
	 * Returns true if the player is in the game, false otherwise.
	 *
	 * @param uniqueId	The UUID of the player to be checked.
	 * @return			True if the player is in the game, false otherwise.
	 */
	public boolean isPlayer(UUID uniqueId) {
		return this.players.contains(uniqueId);
	}

	/**
	 * @param title		The title to be displayed to all players.
	 * @param effect	The sound effect to be played to all players.
	 */
	public void sendTitleWithEffect(@NotNull Component title, @NotNull Effect effect) {
		for (UUID uniqueId : this.players) {
			Player player = Bukkit.getPlayer(uniqueId);
			if (player != null) {
				player.showTitle(Title.title(title, Component.empty(), Title.Times.of(Duration.ofMillis(250), Duration.ofMillis(750), Duration.ofMillis(250))));
				player.playEffect(player.getLocation(), effect, effect.getData());
			}
		}
	}

	/**
	 * Starts the countdown before the initial teleportation begins. Any derived methods should always call the super
	 * class's method or else the countdown will not be run.
	 *
	 * If no countdown is desired, set the runnable to immediately queue the game manager's next game state.
	 *
	 * @param countdown	The runnable representing the countdown
	 */
	public void startCountdown(@NotNull CountdownRunnable countdown) {
		this.countdown = countdown;
		this.countdown.runTaskTimer(this.plugin, 0L, 20L);
	}

	/**
	 * Cancels the countdown if there is one active. Any derived methods should always call the super class's method or
	 * else the countdown will not be cancelled properly.
	 */
	public void cancelCountdown() {
		if (this.countdown == null || this.countdown.isCancelled()) {
			return;
		}

		this.countdown.cancel();
		this.sendTitleWithEffect(Component.text(ChatColor.RED + "CANCELLED!"), Effect.CLICK2);
	}

	/**
	 * @return	True if the delayed finished-minigame task is running, false otherwise.
	 */
	public boolean isFinishedTaskRunning() {
		return this.finished != null && !this.finished.isCancelled();
	}

	/**
	 * Cancels the active finished runnable and forwards the next state of the game {@link MinigameManager#nextState()}.
	 * This method should only be called if the minigame has already ended and an interruption has occurred, so the
	 * {@link #endTeleport()} method has to be called instantaneously.
	 */
	public void runFinishedTaskNow() {
		if (this.isFinishedTaskRunning()) {
			this.finished.cancel();
			this.plugin.getMinigameManager().nextState();
		}
	}

	/**
	 * Clears all internal game data for players and spectators as well as cancelling all ongoing tasks originally
	 * started by the minigame and setting all timestamps and game keys to their minimum value.
	 *
	 * Any derived methods should always call the super class's method or else data will not be unloaded properly
	 */
	public void unload() {
		this.players.clear();
		this.spectators.clear();
		this.gameKey = Integer.MIN_VALUE;
		this.startTimestamp = Long.MIN_VALUE;
		Bukkit.getScheduler().cancelTasks(this.plugin);
	}

	/**
	 * Removes the player from the minigame and checks if the game should end. This method should only be called during
	 * the active state of the game
	 *
	 * @param player	The player to be removed.
	 */
	public void removePlayer(Player player) {
		this.players.remove(player.getUniqueId());
		if (this.spectators.remove(player.getUniqueId())) {
			MinecraftUtil.unsetSpectator(player);
			player.teleport(this.getLobby());
			this.showSpectator(player);
		}
	}

	/**
	 * Hides the specified player from other spectators and in-game players, as well as adds the spectator to the
	 * minigame cache. This method should only be called to add a new spectator to the minigame.
	 *
	 * @param player	The player to be hidden from others.
	 */
	public void hideSpectator(@NotNull Player player) {
		for (UUID uniqueId : this.players) {
			Player other = Bukkit.getPlayer(uniqueId);
			if (other == null || this.isSpectator(uniqueId)) {
				continue;
			}

			other.hidePlayer(this.plugin, player);
		}

		this.spectators.add(player.getUniqueId());
		this.players.add(player.getUniqueId());
	}

	public void showSpectator(@NotNull Player player) {
		for (UUID uniqueId : this.players) {
			Player other = Bukkit.getPlayer(uniqueId);
			if (other == null || other.canSee(player)) {
				continue;
			}

			other.showPlayer(this.plugin, player);
		}

		this.players.remove(player.getUniqueId());
		this.spectators.remove(player.getUniqueId());
	}

	/**
	 * @param item	The nullable item to be checked.
	 * @return		True if the item passed is a spectator compass, false otherwise.
	 */
	public boolean isSpectatorCompass(@Nullable ItemStack item) {
		if (item == null) {
			return false;
		}

		ItemMeta itemMeta = item.getItemMeta();
		PersistentDataContainer container = itemMeta.getPersistentDataContainer();
		return container.has(this.plugin.getKey(), PersistentDataType.STRING) && "spectator_compass".equals(container.get(this.plugin.getKey(), PersistentDataType.STRING));
	}

	/**
	 * Returns a Component of the player's in-game display name in chat. This method can only be called during game.
	 *
	 * @param player	The player typing in chat.
	 * @return			A Component of the player's display name in chat.
	 */
	public Component gameDisplayName(@NotNull OfflinePlayer player) {
		return Component.text(ChatColor.GREEN + player.getName());
	}

	/**
	 * @param player	The player typing in chat.
	 * @return			A Component containing the player's prefix, if they have any, and their game display name.
	 */
	public Component playerChatHandler(@NotNull Player player) {
		String prefix = "";
		Chat chat = this.plugin.getChat();
		if (chat != null) {
			prefix = chat.getPlayerPrefix(player) + " ";
		}

		return Component.text(ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.RESET).append(this.isSpectator(player.getUniqueId()) ? Component.text(ChatColor.BLUE + player.getName()) : this.gameDisplayName(player));
	}

	public void endMinigame(@NotNull AbstractGame.WinnerInterface winner, boolean urgently) {
		if (this.winner.getClass() != EmptyWinner.class) {
			return;
		}

		this.winner = winner;
		Bukkit.broadcast(this.winner.announce());
		this.finished = Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.plugin.getMinigameManager().nextState(), urgently ? 0L : 200L);
	}

	public void deleteWorlds() {
		this.map.unload();
	}

	public void deleteWorlds(boolean urgently) {
		if (urgently) {
			this.deleteWorlds();
		} else {
			Bukkit.getScheduler().runTaskLater(this.plugin, (Runnable) this::deleteWorlds, (long) (20 * Math.ceil(2 * this.getNumPlayers())));
		}
	}

	/**
	 * @param event			The Bukkit event being passed.
	 * @param uniqueId		The target of the event if there is one.
	 * @param checkPlayer	True if the game should check if {@param uniqueId} is a player.
	 */
	public void handleEvent(@NotNull Event event, @Nullable UUID uniqueId, boolean checkPlayer) {
		if (checkPlayer && !this.isPlayer(uniqueId)) {
			return;
		}

		if (this.isSpectator(uniqueId)) {
			this.handleSpectatorEvent(event);
		} else {
			this.handlePlayerEvent(event);
		}
	}

	public void handleEvent(@NotNull Event event, @Nullable UUID uniqueId) {
		this.handleEvent(event, uniqueId, true);
	}

	public void handleSpectatorEvent(@NotNull Event event) {
		if (event instanceof PlayerInteractEvent e) {
			e.setCancelled(true);
			Player player = e.getPlayer();
			Block clicked = e.getClickedBlock();
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				ItemStack item = e.getItem();
				if (this.isSpectatorCompass(item)) {
					Inventory inventory = Bukkit.createInventory(null, 36, ItemUtil.SPECTATOR_GUI);
					for (UUID uniqueId : this.players) {
						if (this.isSpectator(uniqueId)) {
							continue;
						}

						OfflinePlayer offline = Bukkit.getOfflinePlayer(uniqueId);
						ItemStack head = new ItemStack(Material.PLAYER_HEAD);
						SkullMeta meta = (SkullMeta) head.getItemMeta();
						if (meta != null) {
							meta.displayName(this.gameDisplayName(player));
							meta.setOwningPlayer(offline);
							head.setItemMeta(meta);
						}

						inventory.addItem(head);
					}

					player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
					player.openInventory(inventory);
					return;
				}
			}

			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && clicked != null && clicked.getState() instanceof Chest chest) {
				player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
				player.openInventory(chest.getInventory());
			}
		} else if (event instanceof PlayerPickupExperienceEvent) {
			((PlayerPickupExperienceEvent) event).setCancelled(true);
		} else if (event instanceof PlayerPickupArrowEvent) {
			((PlayerPickupArrowEvent) event).setCancelled(true);
		} else if (event instanceof PlayerAdvancementCriterionGrantEvent) {
			((PlayerAdvancementCriterionGrantEvent) event).setCancelled(true);
		} else if (event instanceof EntityPickupItemEvent) {
			((EntityPickupItemEvent) event).setCancelled(true);
		} else if (event instanceof PlayerDeathEvent) {
			((PlayerDeathEvent) event).setCancelled(true);
		} else if (event instanceof PlayerDropItemEvent) {
			((PlayerDropItemEvent) event).setCancelled(true);
		} else if (event instanceof EntityDamageEvent) {
			((EntityDamageEvent) event).setCancelled(true);
		} else if (event instanceof BlockPlaceEvent) {
			((BlockPlaceEvent) event).setCancelled(true);
		} else if (event instanceof BlockBreakEvent) {
			((BlockBreakEvent) event).setCancelled(true);
		} else if (event instanceof InventoryClickEvent e) {
			e.setCancelled(true);
			if (e.getView().title().equals(ItemUtil.SPECTATOR_GUI)) {
				ItemStack clicked = e.getCurrentItem();
				if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
					Player clicker = (Player) e.getWhoClicked();
					Component displayName = clicked.getItemMeta().displayName();
					if (displayName == null) {
						clicker.sendMessage(Component.text(ChatColor.RED + "An error occurred while retrieving the player head clicked. This shouldn't happen but if it does, contact an administrator."));
						return;
					}

					Player spectated = Bukkit.getPlayer(PlainTextComponentSerializer.plainText().serialize(displayName));
					if (spectated == null) {
						return;
					}

					clicker.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
					clicker.teleportAsync(spectated.getLocation()).thenAccept(result -> {
						if (result) {
							clicker.sendMessage(ChatColor.GREEN + "You are now spectating " + spectated.getName());
						} else {
							clicker.sendMessage(ChatColor.RED + "Failed to teleport you to " + spectated.getName());
						}
					});
				}
			}
		} else if (event instanceof VehicleEnterEvent) {
			((VehicleEnterEvent) event).setCancelled(true);
		} else if (event instanceof EntityTargetLivingEntityEvent) {
			((EntityTargetLivingEntityEvent) event).setCancelled(true);
		} else if (event instanceof EntityCombustEvent) {
			((EntityCombustEvent) event).setCancelled(true);
		}
	}

	public void endTeleport() {
		for (UUID uniqueId : this.players) {
			Player player = Bukkit.getPlayer(uniqueId);
			if (player == null || !player.isOnline()) {
				continue;
			}

			if (player.isDead()) {
				player.spigot().respawn();
			}

			this.onPlayerEndTeleport(player);
		}
	}

	protected abstract void handlePlayerEvent(@NotNull Event event);
	public abstract void startTeleport();
	public abstract boolean createWorlds();
	public abstract boolean worldsLoaded();
	public abstract String startMessage(@NotNull UUID uniqueId);
	public abstract boolean onPlayerStartTeleport(@NotNull Player player, @NotNull Location to);
	public abstract void onPlayerEndTeleport(@NotNull Player player);

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (!this.isPlayer(player.getUniqueId())) {
			return;
		}

		if (this.isSpectator(player.getUniqueId())) {
			this.removePlayer(player);
		} else {
			Bukkit.broadcast(this.gameDisplayName(player).append(Component.text(ChatColor.GRAY + " disconnected.")));
			this.disconnections.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
				this.disconnections.remove(player.getUniqueId());
				this.plugin.getConnectionManager().write(new PacketPlayerDisconnect(player.getUniqueId()));
				this.removePlayer(player);
			}, TimeUnit.MINUTES.toSeconds(3) * 20L));
		}
	}

	public void cancelDisconnections() {
		Iterator<BukkitTask> iterator = this.disconnections.values().iterator();
		while (iterator.hasNext()) {
			iterator.next().cancel();
			iterator.remove();
		}
	}

	public interface WinnerInterface {
		@NotNull Component announce();
	}

	public static final class EmptyWinner implements WinnerInterface {

		@Override
		public @NotNull Component announce() {
			return Component.empty();
		}
	}

	public record GeneralErrorInterruption(String message) implements WinnerInterface {

		@Override
		public @NotNull Component announce() {
			return Component.text(this.message);
		}
	}
}
