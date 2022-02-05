package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.compatability.packet.PacketGameUpdate;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import org.bukkit.event.Listener;

public abstract class AbstractGameState implements Listener {

	protected final CorePlugin plugin = CorePlugin.getInstance();
	protected final MinigameManager minigameManager;
	protected final int priority;

	public AbstractGameState(int priority) {
		this.minigameManager = this.plugin.getMinigameManager();
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
