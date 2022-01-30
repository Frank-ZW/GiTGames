package net.gtminecraft.gitgames.service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.gtminecraft.gitgames.service.mechanics.AbstractMinigame;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class MinigameEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final AbstractMinigame minigame;
	@Getter
	private final Action action;

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public enum Action {
		START,
		END;

		public boolean isStart() {
			return this == Action.START;
		}

		public boolean isEnd() {
			return this == Action.END;
		}
	}

}
