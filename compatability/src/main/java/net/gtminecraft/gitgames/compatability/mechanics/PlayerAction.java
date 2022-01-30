package net.gtminecraft.gitgames.compatability.mechanics;

import lombok.Getter;

public enum PlayerAction {

	QUEUE(DataType.EMPTY),
	CONNECT(DataType.SERVER_NAME);

	@Getter
	private final DataType dataType;

	PlayerAction(DataType dataType) {
		this.dataType = dataType;
	}

	public enum DataType {
		SERVER_NAME, EMPTY
	}
}
