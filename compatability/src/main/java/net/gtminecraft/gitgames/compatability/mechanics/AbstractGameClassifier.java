package net.gtminecraft.gitgames.compatability.mechanics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractGameClassifier {

	@Getter
	protected final String name;
	@Getter
	protected final double id;

}
