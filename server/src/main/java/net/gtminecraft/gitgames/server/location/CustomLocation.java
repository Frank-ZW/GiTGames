package net.gtminecraft.gitgames.server.location;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class CustomLocation {

	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;

	public static CustomLocation toPlayerLocation(@NotNull Location location) {
		return new CustomLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	public Location toLocation(@NotNull World world) {
		return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
	}
}
