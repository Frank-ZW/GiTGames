package net.gtminecraft.gitgames.server.minigame.impl.manhunt;

import net.gtminecraft.gitgames.server.map.MapDataContainer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class VanillaManhunt extends AbstractManhunt {

	public VanillaManhunt(MapDataContainer container, int gameKey) {
		super(container, gameKey);
	}

	@Override
	protected void handlePlayerEvent(@NotNull Event event) {
		super.handlePlayerEvent(event);
		if (event instanceof PlayerEvent) {
			Player player = ((PlayerEvent) event).getPlayer();
			if (event instanceof PlayerInteractEvent e) {
				ItemStack item = e.getItem();
				if (this.isHunter(player.getUniqueId()) && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_AIR) && item != null && this.isPlayerTracker(item)) {
					this.updatePlayerTracker(player, this.getSpeedrunnerAsPlayer(), item);
				}
			} else if (event instanceof PlayerDropItemEvent e) {
				if (this.isHunter(player.getUniqueId())) {
					ItemStack drop = e.getItemDrop().getItemStack();
					if (this.isPlayerTracker(drop)) {
						e.setCancelled(true);
						player.sendMessage(Component.text(ChatColor.RED + "You cannot drop your Player Tracker!"));
					}
				}
			} else if (event instanceof PlayerRespawnEvent) {
				if (this.isHunter(player.getUniqueId())) {
					player.getInventory().setItem(8, this.createPlayerTracker());
				}
			} else if (event instanceof PlayerAdvancementDoneEvent e) {
				this.addAwardedAdvancement(e.getPlayer(), e.getAdvancement());
			}
		} else if (event instanceof PlayerDeathEvent e) {
			if (!(this.winner instanceof EmptyWinner)) {
				e.setCancelled(true);
			} else if (this.isSpeedrunner(e.getEntity().getUniqueId())) {
				this.endMinigame(new HunterWinner(), false);
			} else {
				e.getDrops().removeIf(this::isPlayerTracker);
			}
		} else if (event instanceof EntityDamageByEntityEvent e) {
			if (!e.isCancelled() && e.getEntity() instanceof Player && e.getDamager() instanceof Player && TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.startTimestamp) <= 3) {
				e.setCancelled(true);
			}
		} else if (event instanceof EnderDragonChangePhaseEvent e) {
			if (e.getNewPhase() == EnderDragon.Phase.DYING) {
				this.endMinigame(new SpeedrunnerWinner(Bukkit.getOfflinePlayer(this.speedrunner).getName()), false);
			}
		} else if (event instanceof EntityDamageEvent e) {
			if (this.isPlayer(e.getEntity().getUniqueId()) && !(this.winner instanceof EmptyWinner)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		this.handleEvent(e, e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		this.handleEvent(e, e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		this.handleEvent(e, e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		this.handleEvent(e, e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e) {
		this.handleEvent(e, e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		this.handleEvent(e, e.getEntity().getUniqueId(), true);
		this.handleEvent(e, e.getDamager().getUniqueId(), true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		this.handleEvent(e, e.getEntity().getUniqueId(), true);
	}

	@EventHandler
	public void onEnderdragonChangePhase(EnderDragonChangePhaseEvent e) {
		this.handleEvent(e, null, false);
	}
}
