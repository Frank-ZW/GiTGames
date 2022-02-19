package net.gtminecraft.gitgames.server.map;

import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;

@RequiredArgsConstructor
public abstract class AbstractGameMap implements GameMapInterface {

	protected final CorePlugin plugin;
	protected final AbstractGame game;
}
