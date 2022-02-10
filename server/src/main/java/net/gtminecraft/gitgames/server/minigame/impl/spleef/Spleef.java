package net.gtminecraft.gitgames.server.minigame.impl.spleef;

import com.google.common.collect.Iterables;
import net.gtminecraft.gitgames.server.minigame.type.AbstractMapMinigame;
import net.gtminecraft.gitgames.server.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Spleef extends AbstractMapMinigame {

	public Spleef(Location lobby, int gameKey) {
		super("Spleef", "spleef_1", lobby, gameKey);
	}

	@Override
	protected void handlePlayerEvent(@NotNull Event event) {
		if (event instanceof EntityDamageEvent e) {
			e.setCancelled(true);
			if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
				Player player = (Player) e.getEntity();
				this.players.remove(player.getUniqueId());
				this.spectators.add(player.getUniqueId());
				if (this.players.size() == 1) {
					this.endMinigame(new SingleWinner(Bukkit.getOfflinePlayer(Iterables.get(this.players, 0)).getName()), false);
				}
			}
		} else if (event instanceof FoodLevelChangeEvent e) {
			e.setCancelled(true);
		}
	}

	@Override
	public void startTeleport() {
		for (UUID uniqueId : this.players) {
			Player player = Bukkit.getPlayer(uniqueId);
			if (player != null) {
				this.onPlayerStartTeleport(player, this.map.getWorld().getSpawnLocation());
			}
		}
	}

	@Override
	public boolean createWorlds() {
		return true;
	}

	@Override
	public boolean worldsLoaded() {
		return this.map.isLoaded();
	}

	@Override
	public void deleteWorlds() {
		this.map.unload();
	}

	@Override
	public String startMessage(@NotNull UUID uniqueId) {
		return ChatColor.GREEN + "Kill the other players by digging the snow beneath them XDDDDD LOL";
	}

	@Override
	public boolean onPlayerStartTeleport(@NotNull Player player, @NotNull Location to) {
		player.teleportAsync(to).thenAccept(result -> {
			if (result) {
				player.getInventory().setItem(0, this.createShovel());
				player.sendMessage(this.startMessage(player.getUniqueId()));
			} else {
				player.sendMessage(String.format(StringUtil.ERROR_TELEPORTING_TO_MAP, this.getName()));
			}
		});

		return false;	// Return true if you want the players to spawn in a circle?
	}

	@Override
	public void onPlayerEndTeleport(@NotNull Player player) {
		player.teleport(this.lobby);
	}

	@NotNull
	private ItemStack createShovel() {
		ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
		ItemMeta meta = shovel.getItemMeta();
		meta.displayName(Component.text(ChatColor.GREEN + "Nosepicker"));
		meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
		shovel.setItemMeta(meta);
		return shovel;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		this.handleEvent(e, e.getEntity().getUniqueId(), true);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		e.setRespawnLocation(this.map.getWorld().getSpawnLocation());
	}

	@EventHandler
	public void onSaturationChange(FoodLevelChangeEvent e) {
		this.handleEvent(e, e.getEntity().getUniqueId(), true);
	}

	public record SingleWinner(String name) implements WinnerInterface {

		@Override
		public @NotNull Component announce() {
			return Component.text(ChatColor.GREEN + this.name + " has won Spleef.");
		}
	}
}
