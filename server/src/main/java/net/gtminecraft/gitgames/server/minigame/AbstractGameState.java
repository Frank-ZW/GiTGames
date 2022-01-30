package net.gtminecraft.gitgames.server.minigame;

import net.gtminecraft.gitgames.compatability.packet.PacketGameUpdate;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import org.bukkit.event.Listener;

public abstract class AbstractGameState implements Listener {

	protected final CorePlugin plugin;
	protected final MinigameManager minigameManager;
	protected final int priority;

	public AbstractGameState(MinigameManager minigameManager, int priority) {
		this.plugin = minigameManager.getPlugin();
		this.minigameManager = minigameManager;
		this.priority = priority;
	}

	public void onEnable() {
		this.writeUpdate();
	}

	public abstract void onDisable();
	public abstract AbstractGameState nextState();

	public void writeUpdate() {
		this.plugin.getConnectionManager().write(new PacketGameUpdate(this.priority, null));
	}
}
