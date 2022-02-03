package net.gtminecraft.gitgames.server.minigame.type;

import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import org.bukkit.Location;

public abstract class AbstractMapMinigame extends AbstractMinigame {

	public AbstractMapMinigame(String name, Location lobby, int gameKey) {
		super(name, lobby, gameKey);
	}
}
