package net.gtminecraft.gitgames.proxy.data.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;
import net.gtminecraft.gitgames.compatability.mechanics.GameStatus;
import net.gtminecraft.gitgames.compatability.packet.PacketCreateGame;
import net.gtminecraft.gitgames.compatability.packet.PacketServerAction;
import net.gtminecraft.gitgames.proxy.data.MinigameServerData;
import net.gtminecraft.gitgames.proxy.data.ServerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class MinigameServerManager {

	private final ServerManager serverManager;
	private final Random random;
	private final List<MinigameServerData> minigames = new ArrayList<>();
	private final List<MinigameServerData> inactives = new ArrayList<>();
	private final BiMap<Integer, MinigameServerData> actives = HashBiMap.create();
	private final AtomicInteger gameKeyGenerator = new AtomicInteger(0);

	public void requeue(MinigameServerData serverData) {
		serverData.setMaxPlayers(0);
		serverData.setGameKey(Integer.MIN_VALUE);
		this.actives.values().remove(serverData);
		this.inactives.add(serverData);
	}

	public void register(MinigameServerData serverData) {
		this.minigames.add(serverData);
		this.inactives.add(serverData);
	}

	public void unregister(MinigameServerData serverData) {
		this.minigames.remove(serverData);
		this.inactives.remove(serverData);
	}

	public ServerData randomlySelectServer(AbstractGameClassifier type, int maxPlayers) {
		int i = 0;
		MinigameServerData serverData = null;
		for (MinigameServerData filter : this.minigames) {
			if (filter.getMaxPlayers() != maxPlayers || filter.getGameType().getId() != type.getId() || filter.getServer().getPlayers().size() >= maxPlayers) {
				continue;
			}

			i++;
			if (serverData == null) {
				serverData = filter;
				continue;
			}

			int j = this.random.nextInt(i + 1);
			if (j < 1) {
				serverData = filter;
			}
		}

		if (serverData == null) {
			if (this.inactives.isEmpty()) {
				return null;
			}

			int gameKey = this.gameKeyGenerator.getAndIncrement();
			serverData = this.inactives.remove(0);
			serverData.write(new PacketCreateGame(type.getId(), gameKey, maxPlayers));
			serverData.setGameKey(gameKey);
			serverData.setGameStatus(GameStatus.WAITING);
			serverData.setGameType(type);
			serverData.setMaxPlayers(maxPlayers);
		}

		if (serverData.getServer().getPlayers().size() + 1 >= maxPlayers) {
			this.actives.put(serverData.getGameKey(), serverData);
		}

		return serverData;
	}

	public boolean forceEnd(int gameKey) {
		MinigameServerData serverData = this.actives.remove(gameKey);
		if (serverData == null) {
			return false;
		}

		this.forceEnd(serverData);
		return true;
	}

	public boolean forceEnd(String name) {
		ServerData serverData = this.serverManager.getServerData(name);
		if (serverData == null) {
			return false;
		}

		this.forceEnd(serverData);
		return true;
	}

	private void forceEnd(ServerData serverData) {
		serverData.write(new PacketServerAction(PacketServerAction.ServerAction.FORCE_END));
	}
}
