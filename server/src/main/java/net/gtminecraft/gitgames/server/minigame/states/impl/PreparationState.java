package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.compatability.packet.PacketGameUpdate;
import net.gtminecraft.gitgames.server.minigame.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.GameState;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class PreparationState extends GameState {

	private boolean success = false;

	public PreparationState(MinigameManager minigameManager) {
		super(minigameManager, GameStateUtils.PREPARATION_STATE_PRIORITY, null);
	}

	@Override
	public void onEnable() {
		this.success = this.minigame.createWorlds();
		if (this.success) {
			this.minigameManager.nextState();
		} else {
			Bukkit.broadcast(Component.text(ChatColor.RED + "An error occurred while loading up the world for " + this.minigame.getName() + ". You have been sent back to the lobby."));
			this.plugin.getConnectionManager().write(new PacketGameUpdate(GameStateUtils.FINISHED_STATE_PRIORITY, this.minigame.getPlayers()));
			this.minigameManager.setState(new InactiveState(this.minigameManager));
		}
	}

	@Override
	public void onDisable() {
		if (!this.success) {
			this.minigame.deleteWorlds();
			this.minigame.unload();
		}
	}

	@Override
	public AbstractGameState nextState() {
		return new CountdownState(this.minigameManager);
	}
}
