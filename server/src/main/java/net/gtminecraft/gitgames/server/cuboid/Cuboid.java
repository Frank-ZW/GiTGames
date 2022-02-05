package net.gtminecraft.gitgames.server.cuboid;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Cuboid {

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
}
