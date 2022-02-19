package net.gtminecraft.gitgames.server.minigame.states.impl;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.compatability.packet.PacketGameUpdate;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.states.GameState;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class PreparationState extends GameState {

	private boolean success = false;

	public PreparationState() {
		super(GameStateUtils.PREPARATION_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		this.success = this.game.createWorlds();
		if (this.success) {
			this.minigameManager.nextState();
		} else {
			Bukkit.broadcast(Component.text(ChatColor.RED + "An error occurred while loading up the world for " + this.game.getName() + ". You have been sent back to the lobby."));
			this.plugin.getConnectionManager().write(new PacketGameUpdate(GameStateUtils.FINISHED_STATE_PRIORITY, this.game.getPlayers()));
			this.minigameManager.setState(new InactiveState());
		}
	}

	@Override
	public void onDisable() {
		if (!this.success) {
			this.game.deleteWorlds();
			this.game.unload();
		}
	}

	@Override
	public AbstractGameState nextState() {
		return new CountdownState();
	}
}
