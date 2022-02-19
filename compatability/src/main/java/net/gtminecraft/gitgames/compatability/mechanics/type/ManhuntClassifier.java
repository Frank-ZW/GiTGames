package net.gtminecraft.gitgames.compatability.mechanics.type;

import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;

public class ManhuntClassifier extends AbstractGameClassifier {

	public ManhuntClassifier() {
		super("Manhunt", 1);
	}

	@Override
	public int playerThreshold(int maxPlayers) {
		return switch (maxPlayers) {
			case 1, 2:
				yield maxPlayers;
			case 3, 4:
				yield maxPlayers - 1;
			default:
				yield maxPlayers * 5 / 6;
		};
	}

	@Override
	public boolean validate(int maxPlayers) {
		return maxPlayers >= 2 && maxPlayers <= 5;
	}
}
