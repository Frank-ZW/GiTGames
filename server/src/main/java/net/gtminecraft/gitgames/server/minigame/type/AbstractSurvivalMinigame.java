package net.gtminecraft.gitgames.server.minigame.type;

import lombok.Setter;
import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import net.gtminecraft.gitgames.server.util.PlayerUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public abstract class AbstractSurvivalMinigame extends AbstractMinigame {

	protected Map<World.Environment, World> worlds = new HashMap<>();
	protected Map<UUID, List<Advancement>> advancements = new HashMap<>();
	protected WorldType worldType;
	@Setter
	protected long seed;
	protected boolean allowNether;
	protected boolean allowEnd;

	public AbstractSurvivalMinigame(String name, Location lobby, int gameKey) {
		this(gameKey, name, lobby, true, true);
	}

	public AbstractSurvivalMinigame(int gameKey, String name, Location lobby, boolean allowNether, boolean allowEnd) {
		super(name, lobby, gameKey);
		this.worldType = WorldType.NORMAL;
		this.seed = this.random.nextLong();
		this.allowNether = allowNether;
		this.allowEnd = allowEnd;
	}
	public void addAwardedAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
		this.advancements.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>()).add(advancement);
	}

	public void clearAwardedAdvancements(@NotNull Player player) {
		this.advancements.computeIfPresent(player.getUniqueId(), (uniqueId, advancements) -> {
			PlayerUtil.clearAdvancements(player);
			return advancements;
		});
	}

	@Override
	public void deleteWorlds() {
		for (World world : this.worlds.values()) {
			if (world.getPlayerCount() != 0) {
				for (Player player : world.getPlayers()) {
					player.teleport(this.lobby);
					player.sendMessage(ChatColor.RED + "You were unexpectedly in the world " + world.getName() + " as it was being deleted. You have been teleported back to the lobby.");
				}
			}

			Bukkit.unloadWorld(world, true);
			try {
				FileUtils.deleteDirectory(world.getWorldFolder());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onPlayerStartTeleport(@NotNull Player player, @NotNull Location to) {
		player.teleportAsync(to).thenAccept(result -> {
			if (result) {
				player.sendMessage(this.startMessage(player.getUniqueId()));
			} else {
				player.sendMessage(ChatColor.RED + "Failed to teleport you to the " + this.getName() + " world(s). Contact an administrator if this occurs.");
			}
		});

		return true;
	}

	@Override
	public void onPlayerEndTeleport(@NotNull Player player) {
		if (this.isSpectator(player.getUniqueId())) {
			PlayerUtil.unsetSpectator(player);
			this.showSpectator(player);
		} else {
			this.clearAwardedAdvancements(player);
			PlayerUtil.resetAttributes(player);
		}

		player.teleport(this.lobby);
	}

	@Override
	public void startTeleport() {
		World overworld = this.getOverworld();
		overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		int radius = Math.max(8, 3 * this.players.size());
		float theta = -90.0F;
		float delta = 360.0F / this.players.size();
		for (UUID uniqueId : this.players) {
			Player player = Bukkit.getPlayer(uniqueId);
			if (player == null || !player.isOnline()) {
				continue;
			}

			if (player.isDead()) {
				player.spigot().respawn();
			}

			PlayerUtil.clearAdvancements(player);
			PlayerUtil.resetAttributes(player);

			int x = (int) (overworld.getSpawnLocation().getX() + radius * Math.cos(Math.toRadians(theta)));
			int z = (int) (overworld.getSpawnLocation().getZ() + radius * Math.sin(Math.toRadians(theta)));
			int y = overworld.getHighestBlockYAt(x, z) + 1;
			Location location = new Location(overworld, x, y, z);
			if (this.onPlayerStartTeleport(player, location)) {
				theta += delta;
			}

			this.startTimestamp = System.currentTimeMillis();
		}
	}

	@Override
	public void endTeleport() {
		for (UUID uniqueId : this.players) {
			Player player = Bukkit.getPlayer(uniqueId);
			if (player == null || !player.isOnline()) {
				continue;
			}

			if (player.isDead()) {
				player.spigot().respawn();
			}

			this.onPlayerEndTeleport(player);
		}
	}

	@Override
	public void cancelCountdown() {
		super.cancelCountdown();
		this.advancements.clear();
	}

	@Override
	public boolean createWorlds() {

		if (this.worlds.size() == 3) {
			return true;
		}

		for (int i = 0; i < 3; i++) {
			World.Environment environment = World.Environment.values()[i];
			World world = Bukkit.createWorld(new WorldCreator(this.getWorldName(environment)).environment(environment).type(this.worldType).seed(this.seed));
			if (world != null) {
				world.setKeepSpawnInMemory(false);
				this.worlds.put(environment, world);
			}
		}

		return this.worlds.size() == 3;
	}

	@Override
	public void handlePlayerEvent(@NotNull Event event) {
		if (event instanceof PlayerPortalEvent e) {
			Player player = e.getPlayer();
			World fromWorld = e.getFrom().getWorld();
			if (e.getTo().getWorld() == null || e.getFrom().getWorld() == null || e.isCancelled()) {
				return;
			}

			switch (e.getCause()) {
				case NETHER_PORTAL:
					switch (fromWorld.getEnvironment()) {
						case NORMAL -> {
							e.getTo().setWorld(this.getNether());
							if (this.players.contains(player.getUniqueId()) && !this.spectators.contains(player.getUniqueId())) {
								this.plugin.grantNetherAdvancement(player);
							}
						}
						case NETHER -> e.setTo(new Location(this.getOverworld(), e.getFrom().getX() * 8.0D, e.getFrom().getY(), e.getFrom().getZ() * 8.0D));
					}

					break;
				case END_PORTAL:
					switch (fromWorld.getEnvironment()) {
						case NORMAL -> {
							e.getTo().setWorld(this.getEnd());
							if (this.players.contains(player.getUniqueId()) && !this.spectators.contains(player.getUniqueId())) {
								this.plugin.grantEndAdvancement(player);
							}
						}
						case THE_END -> e.setTo(player.getBedSpawnLocation() == null ? this.getOverworld().getSpawnLocation() : player.getBedSpawnLocation());
					}

					break;
				default:
			}
		} else if (event instanceof PortalCreateEvent e) {
			switch (e.getReason()) {
				case FIRE:
				case NETHER_PAIR:
					if (!this.allowNether) {
						e.setCancelled(true);
					}

					break;
				default:
					if (!this.allowEnd) {
						e.setCancelled(true);
					}
			}
		} else if (event instanceof PlayerRespawnEvent e) {
			if (this.getOverworld() == null) {
				this.endMinigame(new GeneralErrorWrapper(ChatColor.RED + "The " + this.getName() + " overworld failed to generate properly... Contact an administrator if this occurs."), true);
				return;
			}

			Player player = e.getPlayer();
			e.setRespawnLocation(player.getBedSpawnLocation() == null ? this.getOverworld().getSpawnLocation() : player.getBedSpawnLocation());
		}
	}

	@Override
	public boolean worldsLoaded() {
		return !this.worlds.isEmpty();
	}

	public World getWorld(World.Environment environment) {
		return this.worlds.get(environment);
	}

	public World getOverworld() {
		return this.getWorld(World.Environment.NORMAL);
	}

	public World getNether() {
		return this.getWorld(World.Environment.NETHER);
	}

	public World getEnd() {
		return this.getWorld(World.Environment.THE_END);
	}

	/**
	 * Clears all internal game data for players and spectators as well as cancelling all ongoing tasks originally
	 * started by the minigame and setting all timestamps and game keys to their minimum value.
	 * <p>
	 * Any derived methods should always call the super class's method or else data will not be unloaded properly
	 */
	@Override
	public void unload() {
		super.unload();
		this.worlds.clear();
		this.advancements.clear();
	}

	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent e) {
		this.handleEvent(e, e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPortalCreate(PortalCreateEvent e) {
		this.handleEvent(e, e.getEntity() == null ? null : e.getEntity().getUniqueId(), false);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		this.handleEvent(e, e.getPlayer().getUniqueId());
	}
}
