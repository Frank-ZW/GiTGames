package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.server.minigame.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.GameState;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

// State representing transient period between the first player joining and the minigame already having been
// initialized in memory and the last player joining and the countdown beginning
// The minigame will have been created by now
public class QueuingState extends GameState {

	public QueuingState(MinigameManager minigameManager) {
		super(minigameManager, GameStateUtils.QUEUEING_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {}

	@Override
	public void onDisable() {}

	@Override
	public AbstractGameState nextState() {
		return new PreparationState(this.minigameManager);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (player.isDead()) {
			player.spigot().respawn();
		}

		if (this.minigame.isPlayer(player.getUniqueId())) {
			player.setGameMode(GameMode.SURVIVAL);
			player.teleportAsync(this.minigame.getLobby()).thenAccept(result -> {
				if (!result) {
					player.sendMessage(Component.text(ChatColor.RED + "An error occurred while teleporting you to the minigame lobby. You have been sent back to the main lobby."));
					this.minigameManager.sendToProxyLobby(player);
					return;
				}

				player.getInventory().clear();
				player.setFireTicks(0);
				player.sendMessage(Component.text(String.format("%s: " + ChatColor.GREEN + "The minigame currently has %s out of %s players.", this.getClass().getSimpleName(), this.minigame.getNumPlayers(), this.minigameManager.getMaxPlayers())));
				if (this.minigame.getNumPlayers() >= this.minigameManager.getMaxPlayers()) {
					this.minigameManager.nextState();
				}
			});
		}
	}
}
