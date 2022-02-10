package net.gtminecraft.gitgames.server.minigame.states.impl;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.gtminecraft.gitgames.compatability.packet.PacketPlayerDataUpdate;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.states.GameState;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

// State representing transient period between the first player joining and the minigame already having been
// initialized in memory and the last player joining and the countdown beginning
// The minigame will have been created by now
public class QueuingState extends GameState {

	public QueuingState() {
		super(GameStateUtils.QUEUEING_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {}

	@Override
	public void onDisable() {}

	@Override
	public AbstractGameState nextState() {
		return new PreparationState();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (player.isDead()) {
			player.spigot().respawn();
		}

		if (this.minigame.isPlayer(player.getUniqueId())) {
			player.setGameMode(GameMode.ADVENTURE);
			player.teleportAsync(this.minigame.getLobby()).thenAccept(result -> {
				if (!result) {
					player.sendMessage(Component.text(ChatColor.RED + "An error occurred while teleporting you to the minigame lobby. You have been sent back to the main lobby."));
					this.minigameManager.connectToProxyLobby(player);
					return;
				}

				player.getInventory().clear();
				player.setFireTicks(0);
				if (this.minigame.getNumPlayers() >= this.minigameManager.getMinPlayers() && this.minigameManager.isInState(QueuingState.class)) {
					this.minigameManager.nextState();
				}
			});
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (!this.minigame.getPlayers().remove(player.getUniqueId())) {
			return;
		}

		this.plugin.getConnectionManager().write(new PacketPlayerDataUpdate(PlayerStatus.INACTIVE, player.getUniqueId()));
		if (this.minigame.getNumPlayers() == 0) {
			this.minigameManager.setState(new FinishedState());
		}
	}
}
