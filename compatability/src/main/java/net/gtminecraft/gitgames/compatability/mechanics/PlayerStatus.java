package net.gtminecraft.gitgames.compatability.mechanics;

public enum PlayerStatus {
	INACTIVE,
	QUEUING,
	PLAYING,
	SPECTATING;

	public static final PlayerStatus[] VALUES = PlayerStatus.values();

	public boolean isPlaying() {
		return this == PlayerStatus.PLAYING;
	}
}
