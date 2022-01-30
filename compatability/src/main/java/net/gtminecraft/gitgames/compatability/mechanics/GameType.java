package net.gtminecraft.gitgames.compatability.mechanics;

import lombok.Getter;

public enum GameType {

	INACTIVE("Inactive"),
	MANHUNT("Manhunt");

	public static final GameType[] VALUES = GameType.values();

	@Getter
	private final String name;

	GameType(String name) {
		this.name = name;
	}
}
