package net.gtminecraft.gitgames.server.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
public class MinigameEndEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	@Getter
	private final Collection<UUID> players;
	@Getter
	private final boolean urgent;

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
