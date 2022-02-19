package net.gtminecraft.gitgames.compatability.mechanics.type;

import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;

public class InactiveClassifier extends AbstractGameClassifier {

	public InactiveClassifier() {
		super("Inactive", 0);
	}

	@Override
	public int playerThreshold(int maxPlayers) {
		return -1;
	}

	@Override
	public boolean validate(int maxPlayers) {
		return false;
	}
}
