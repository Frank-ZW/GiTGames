package net.gtminecraft.gitgames.compatability.mechanics.type;

import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;

public class SpleefClassifier extends AbstractGameClassifier {

	public SpleefClassifier() {
		super("Spleef", 2);
	}

	@Override
	public int playerThreshold(int maxPlayers) {
		if (maxPlayers >= 5 && maxPlayers <= 15) {
			return maxPlayers * 5 / 6;
		} else if (maxPlayers > 15) {
			return maxPlayers / 2;
		} else {
			return maxPlayers;
		}
	}
}
