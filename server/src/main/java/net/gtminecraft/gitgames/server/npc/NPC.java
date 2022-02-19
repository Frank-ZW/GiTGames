package net.gtminecraft.gitgames.server.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface NPC {

	Location getLocation();
	String getName();
	int getId();
	void showTo(@NotNull Player player);
	void hideFrom(@NotNull Player player);
	void delete();
}
