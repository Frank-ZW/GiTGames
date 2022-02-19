package net.gtminecraft.gitgames.server.minigame.type;

import net.gtminecraft.gitgames.server.map.MapDataContainer;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;

public abstract class AbstractMapGame extends AbstractGame {

	public AbstractMapGame(MapDataContainer container, String name, int gameKey) {
		super(container, name, gameKey);
	}
}
