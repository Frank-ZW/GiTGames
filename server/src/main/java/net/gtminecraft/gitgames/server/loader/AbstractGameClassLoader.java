package net.gtminecraft.gitgames.server.loader;

import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.map.manager.MapLoaderManager;

@RequiredArgsConstructor
public abstract class AbstractGameClassLoader implements GameClassLoaderInterface {

	protected final CorePlugin plugin = CorePlugin.getInstance();
	protected final AbstractGameClassifier classifier;
	protected final MapLoaderManager mapLoaderManager;

	@Override
	public int getId() {
		return this.classifier.getClassifierId();
	}
}
