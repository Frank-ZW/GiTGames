package net.gtminecraft.gitgames.manhunt;

import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.manhunt.functional.PlayerTrackerHandler;
import net.gtminecraft.gitgames.service.mechanics.AbstractSurvivalMinigame;
import net.gtminecraft.gitgames.service.mechanics.functional.IPlayerTracker;
import net.gtminecraft.gitgames.service.util.ItemUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractManhunt extends AbstractSurvivalMinigame implements IPlayerTracker {

	protected UUID speedrunner;
	protected final List<UUID> hunters = new ArrayList<>();
	private final IPlayerTracker trackerHandler = new PlayerTrackerHandler(this.plugin, ItemUtil.DEFAULT_PLAYER_TRACKER, "player_tracker");

	public AbstractManhunt(Location lobby, int gameKey) {
		super("Manhunt", lobby, gameKey);
	}

	public boolean isSpeedrunner(UUID uniqueId) {
		return this.speedrunner.equals(uniqueId);
	}

	public boolean isHunter(UUID uniqueId) {
		return this.hunters.contains(uniqueId);
	}

	@Nullable
	public Player getSpeedrunnerAsPlayer() {
		return Bukkit.getPlayer(this.speedrunner);
	}

	@Override
	public Component gameDisplayName(@NotNull OfflinePlayer player) {
		return Component.text((this.isSpeedrunner(player.getUniqueId()) ? ChatColor.GREEN : ChatColor.RED) + player.getName());
	}

	@Override
	public void removePlayer(Player player) {
		super.removePlayer(player);
		if (this.isSpeedrunner(player.getUniqueId())) {
			this.endMinigame(new HunterWrapper(), false);
			return;
		}

		if (this.hunters.remove(player.getUniqueId()) && this.hunters.isEmpty()) {
			this.endMinigame(new SpeedrunnerWrapper(Bukkit.getOfflinePlayer(this.speedrunner).getName()), false);
		}
	}

	@Override
	public boolean onPlayerStartTeleport(@NotNull Player player, @NotNull Location to) {
		if (this.isSpeedrunner(player.getUniqueId())) {
			to = this.getOverworld().getSpawnLocation();
			return !super.onPlayerStartTeleport(player, to);
		}

		player.getInventory().setItem(8, this.createPlayerTracker());
		return super.onPlayerStartTeleport(player, to);
	}

	@Override
	public void startTeleport() {
		Player player = this.getSpeedrunnerAsPlayer();
		if (player == null) {
			this.endMinigame(new GeneralErrorWrapper(ChatColor.RED + "An error occurred while selecting the speedrunner. Contact an administrator if this occurs."), true);
			return;
		}

		super.startTeleport();
	}

	@Override
	public String startMessage(@NotNull UUID uniqueId) {
		return this.isSpeedrunner(uniqueId) ? ChatColor.GREEN + "You are the speedrunner. You must kill the Enderdragon before the hunters kill you." : ChatColor.GREEN + "You are " + (this.hunters.size() == 1 ? "the" : "a") + " hunter. You must use your Player Tracker to relentlessly hunt and kill the speedrunner.";
	}

	@Override
	public void startCountdown(@NotNull BukkitRunnable runnable) {
		this.speedrunner = this.players.get(this.random.nextInt(this.players.size()));
		this.hunters.addAll(this.players);
		this.hunters.remove(this.speedrunner);
		super.startCountdown(runnable);
	}

	@Override
	public void cancelCountdown() {
		super.cancelCountdown();
		this.speedrunner = null;
		this.hunters.clear();
	}

	@Override
	public void unload() {
		super.unload();
		this.speedrunner = null;
		this.hunters.clear();
	}

	@Override
	public void updatePlayerTracker(@NotNull Player player, @Nullable Player target, @NotNull ItemStack tracker) {
		this.trackerHandler.updatePlayerTracker(player, target, tracker);
	}

	@Override
	public boolean isPlayerTracker(@NotNull ItemStack item) {
		return this.trackerHandler.isPlayerTracker(item);
	}

	@Override
	public @NotNull ItemStack createPlayerTracker() {
		return this.trackerHandler.createPlayerTracker();
	}

	@RequiredArgsConstructor
	public static final class SpeedrunnerWrapper implements WinnerWrapper {

		private final String name;

		@Override
		public @NotNull Component announce() {
			return Component.text(ChatColor.GREEN + this.name + " has won the Manhunt.");
		}
	}
	public final class HunterWrapper implements WinnerWrapper {

		@Override
		public @NotNull Component announce() {
			return Component.text(ChatColor.GREEN + "The hunter" + (hunters.size() == 1 ? " has" : "s have") + " won the Manhunt.");
		}
	}
}
