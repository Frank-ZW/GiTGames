package net.gtminecraft.gitgames.compatability.mechanics;

import static net.gtminecraft.gitgames.compatability.mechanics.GameStatus.*;

public class GameStateUtils {

	public static final int INACTIVE_STATE_PRIORITY = 0;
	public static final int QUEUEING_STATE_PRIORITY = 1;
	public static final int PREPARATION_STATE_PRIORITY = 2;
	public static final int COUNTDOWN_STATE_PRIORITY = 3;
	public static final int ACTIVE_STATE_PRIORITY = 4;
	public static final int FINISHED_STATE_PRIORITY = 5;

	private static final GameStatus[] ORDINALS = new GameStatus[] {
			WAITING,
			null,
			null,
			COUNTDOWN,
			ACTIVE,
			FINISHED
	};

	public static GameStatus gameStatusByPriority(int priority) {
		return GameStateUtils.ORDINALS[priority];
	}
}
