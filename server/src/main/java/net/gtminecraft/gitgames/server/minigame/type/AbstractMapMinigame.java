package net.gtminecraft.gitgames.server.minigame.type;

import net.gtminecraft.gitgames.server.map.GameMapInterface;
import net.gtminecraft.gitgames.server.map.LoadableGameMap;
import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import org.bukkit.Location;

public abstract class AbstractMapMinigame extends AbstractMinigame {

	protected final GameMapInterface map;

	public AbstractMapMinigame(String name, String worldName, Location lobby, int gameKey) {
		super(name, lobby, gameKey);
		this.map = new LoadableGameMap(this.plugin.getMapFiles(), worldName, true);
	}
}
