package net.gtminecraft.gitgames.server.npc;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimpleNPC implements NPC {

	private final CorePlugin plugin;
	private final Set<UUID> viewers = new HashSet<>();
	private final UUID uniqueId = UUID.randomUUID();
	private final String name;
	private final String entityName;
	private final String texture;
	private final String signature;
	private final boolean hideNameTag;

	private ServerPlayer serverPlayer;

	public SimpleNPC(CorePlugin plugin, NPCOptions options) {
		this.plugin = plugin;
		this.name = options.getName();
		this.texture = options.getTexture();
		this.signature = options.getSignature();
		this.hideNameTag = options.isHideNameTag();
		this.entityName = this.hideNameTag ? RandomStringUtils.random(16, true, false) : this.name;
		this.addToWorld(options.getLocation());
	}

	private void addToWorld(Location location) {
		Preconditions.checkNotNull(location.getWorld(), "Location cannot have a null world.");
		MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
		ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
		GameProfile profile = this.createGameProfile();
		this.serverPlayer = new ServerPlayer(minecraftServer, serverLevel, profile);
		this.serverPlayer.teleportTo(serverLevel, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	private GameProfile createGameProfile() {
		GameProfile profile = new GameProfile(this.uniqueId, this.entityName);
		profile.getProperties().put("textures", new Property("textures", this.texture, this.signature));
		return profile;
	}

	@Override
	public Location getLocation() {
		return new Location(this.serverPlayer.getLevel().getWorld(), this.serverPlayer.getX(), this.serverPlayer.getY(), this.serverPlayer.getZ());
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getId() {
		return this.serverPlayer.getId();
	}

	@Override
	public void showTo(@NotNull Player player) {
		this.viewers.add(player.getUniqueId());
		ClientboundPlayerInfoPacket clientboundPlayerInfoPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this.serverPlayer);
	}

	@Override
	public void hideFrom(@NotNull Player player) {

	}

	@Override
	public void delete() {

	}
}
