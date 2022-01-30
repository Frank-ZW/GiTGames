package net.gtminecraft.gitgames.service.mechanics;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.mechanics.GameStatus;
import net.gtminecraft.gitgames.service.AbstractCorePlugin;
import net.gtminecraft.gitgames.service.event.MinigameEndEvent;
import net.gtminecraft.gitgames.service.util.ItemUtil;
import net.gtminecraft.gitgames.service.util.PlayerUtil;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
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

public abstract class AbstractMinigame implements Listener {

	protected final AbstractCorePlugin plugin;
	protected final Map<UUID, BukkitTask> disconnections = new HashMap<>();
	protected final List<UUID> players = new ArrayList<>();
	protected final List<UUID> spectators = new ArrayList<>();
	protected final Random random = new Random();
	protected WinnerWrapper winner = new EmptyWinnerWrapper();
	protected BukkitTask countdown;
	@Getter
	@Setter
	protected Difficulty difficulty;
	@Getter
	protected final String name;
	@Getter
	protected final Location lobby;
	@Getter
	protected int gameKey;
	@Getter
	protected GameStatus status = GameStatus.WAITING;
	protected long startTimestamp;

	public AbstractMinigame(String name, Location lobby, int gameKey) {
		this.plugin = AbstractCorePlugin.getInstance();
		this.name = name;
		this.lobby = lobby;
		this.gameKey = gameKey;
	}

	public String getRawName() {
		return this.name.replaceAll("\\s+", "");
	}

	public String getWorldName(World.Environment environment) {
		return StringUtils.lowerCase(this.getRawName() + "_" + this.gameKey + "_" + environment);
	}

	public void addPlayer(UUID uniqueId) {
		if (!this.status.isWaiting() && !this.status.isCountdown()) {
			throw new IllegalStateException("Cannot manually add a player while the game is in-progress!");
		}

		this.players.add(uniqueId);
	}

	public int getNumPlayers() {
		return this.players.size();
	}

	public Collection<UUID> getPlayers() {
		return this.players;
	}

	public boolean isSpectator(UUID uniqueId) {
		return this.spectators.contains(uniqueId);
	}

	public boolean isPlayer(UUID uniqueId) {
		return this.players.contains(uniqueId);
	}

	public void sendTitleWithEffect(Component title, Effect effect) {
		for (UUID uniqueId : this.players) {
			Player player = Bukkit.getPlayer(uniqueId);
			if (player != null) {
				player.showTitle(Title.title(title, Component.empty(), Title.Times.of(Duration.ofMillis(250), Duration.ofMillis(750), Duration.ofMillis(250))));
				player.playEffect(player.getLocation(), effect, effect.getData());
			}
		}
	}

	public void cancelCountdown() {
		if (this.countdown == null || this.countdown.isCancelled()) {
			return;
		}

		this.countdown.cancel();
		this.sendTitleWithEffect(Component.text(ChatColor.RED + "CANCELLED!"), Effect.CLICK2);
	}

	public void unload() {
		this.players.clear();
		this.spectators.clear();
		this.gameKey = Integer.MIN_VALUE;
		this.startTimestamp = Long.MIN_VALUE;
		Bukkit.getScheduler().cancelTasks(this.plugin);
	}

	public void removePlayer(Player player) {
		this.players.remove(player.getUniqueId());
		if (this.spectators.remove(player.getUniqueId())) {
			PlayerUtil.unsetSpectator(player);
			player.teleport(this.lobby);
			this.showSpectator(player);
		}
	}

	public void hideSpectator(@NotNull Player player) {
		for (UUID uniqueId : this.players) {
			Player other = Bukkit.getPlayer(uniqueId);
			if (other == null || this.isSpectator(uniqueId)) {
				continue;
			}

			other.hidePlayer(this.plugin, player);
		}

		this.players.add(player.getUniqueId());
		this.spectators.add(player.getUniqueId());
	}

	public void showSpectator(@NotNull Player player) {
		for (UUID uniqueId : this.players) {
			Player other = Bukkit.getPlayer(uniqueId);
			if (other == null || other.canSee(player)) {
				continue;
			}

			other.showPlayer(this.plugin, player);
		}
	}

	public boolean isSpectatorCompass(@Nullable ItemStack item) {
		if (item == null) {
			return false;
		}

		ItemMeta itemMeta = item.getItemMeta();
		PersistentDataContainer container = itemMeta.getPersistentDataContainer();
		return container.has(this.plugin.getKey(), PersistentDataType.STRING) && "spectator_compass".equals(container.get(this.plugin.getKey(), PersistentDataType.STRING));
	}

	public Component gameDisplayName(@NotNull OfflinePlayer player) {
		return Component.text(ChatColor.GREEN + player.getName());
	}

	public Component playerChatHandler(@NotNull Player player) {
		String prefix = null;
		Chat chat = this.plugin.getChat();
		if (chat != null) {
			prefix = chat.getPlayerPrefix(player);
		}

		return Component.text((prefix == null ? "" : ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.RESET + " ")).append(this.isSpectator(player.getUniqueId()) ? Component.text(ChatColor.BLUE + player.getName()) : this.gameDisplayName(player));
	}

	public void endMinigame(WinnerWrapper winner, boolean urgently) {
		if (this.winner.getClass() != EmptyWinnerWrapper.class) {
			return;
		}

		this.winner = winner;
		Bukkit.broadcast(this.winner.announce());
		Bukkit.getPluginManager().callEvent(new MinigameEndEvent(this.players, urgently));
	}

	public void deleteWorlds(boolean urgently) {
		if (urgently) {
			this.deleteWorlds();
		} else {
			Bukkit.getScheduler().runTaskLater(this.plugin, (Runnable) this::deleteWorlds, (long) (20 * Math.ceil(1.5 * this.getNumPlayers())));
		}
	}

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

	public abstract void handlePlayerEvent(@NotNull Event event);
	public abstract void onPreCountdown();
	public abstract void startTeleport();
	public abstract void endTeleport();
	public abstract boolean createWorlds();
	public abstract boolean worldsLoaded();
	public abstract void deleteWorlds();
	public abstract String startMessage(@NotNull UUID uniqueId);
	public abstract boolean onPlayerStartTeleport(@NotNull Player player, @NotNull Location to);
	public abstract void onPlayerEndTeleport(@NotNull Player player);

	public interface WinnerWrapper {
		@NotNull Component announce();
	}

	public static final class EmptyWinnerWrapper implements WinnerWrapper {

		@Override
		public @NotNull Component announce() {
			return Component.empty();
		}
	}

	public record GeneralErrorWrapper(String message) implements WinnerWrapper {

		@Override
		public @NotNull Component announce() {
			return Component.text(this.message);
		}
	}
}
