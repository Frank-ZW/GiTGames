package net.gtminecraft.gitgames.compatability.mechanics;

public enum GameStatus {
	WAITING,
	COUNTDOWN,
	ACTIVE,
	FINISHED;

	public static final GameStatus[] VALUES = GameStatus.values();

	public boolean isWaiting() {
		return this == GameStatus.WAITING;
	}

	public boolean isCountdown() {
		return this == GameStatus.COUNTDOWN;
	}

	public boolean isActive() {
		return this == GameStatus.ACTIVE;
	}

	public boolean isFinished() {
		return this == GameStatus.FINISHED;
	}
}
