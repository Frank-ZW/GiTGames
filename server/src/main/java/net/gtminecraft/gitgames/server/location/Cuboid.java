package net.gtminecraft.gitgames.server.location;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.gtminecraft.gitgames.server.CorePlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@EqualsAndHashCode
public class Cuboid {

	private final CorePlugin plugin = CorePlugin.getInstance();
	@Getter
	private double minX;
	@Getter
	private double minY;
	@Getter
	private double minZ;
	@Getter
	private double maxX;
	@Getter
	private double maxY;
	@Getter
	private double maxZ;

	public Cuboid(double x, double y, double z) {
		this(x, y, z, x, y, z);
	}

	public Location getInternalLocation(@NotNull World world, boolean random) {
		return this.getInternalLocation(world, 0.0F, 0.0F, random);
	}

	public Location getInternalLocation(@NotNull World world, float yaw, float pitch, boolean random) {
		double x = random ? this.plugin.getRandom().nextDouble(this.minX, this.maxX) : (this.minX + this.maxX) / 2;
		double y = random ? this.plugin.getRandom().nextDouble(this.minY, this.maxY) : (this.minY + this.maxY) / 2;
		double z = random ? this.plugin.getRandom().nextDouble(this.minZ, this.maxZ) : (this.minZ + this.maxZ) / 2;
		return new Location(world, x, y, z, yaw, pitch);
	}
}
