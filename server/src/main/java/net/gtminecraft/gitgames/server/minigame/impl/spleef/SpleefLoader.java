package net.gtminecraft.gitgames.server.minigame.impl.spleef;

import net.gtminecraft.gitgames.compatability.mechanics.GameClassifiers;
import net.gtminecraft.gitgames.server.loader.AbstractGameClassLoader;
import net.gtminecraft.gitgames.server.map.manager.MapLoaderManager;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class SpleefLoader extends AbstractGameClassLoader {

	public SpleefLoader(MapLoaderManager mapLoaderManager) {
		super(GameClassifiers.SPLEEF, mapLoaderManager);
	}

	@Override
	public @NotNull AbstractGame load(int gameKey) {
		return new Spleef(this.mapLoaderManager.getMapContainer(this.classifier), gameKey, EntityDamageEvent.DamageCause.VOID, EntityDamageEvent.DamageCause.LAVA);
	}
}
