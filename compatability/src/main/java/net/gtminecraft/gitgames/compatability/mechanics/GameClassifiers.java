package net.gtminecraft.gitgames.compatability.mechanics;

import net.gtminecraft.gitgames.compatability.mechanics.type.InactiveClassifier;
import net.gtminecraft.gitgames.compatability.mechanics.type.ManhuntClassifier;
import net.gtminecraft.gitgames.compatability.mechanics.type.SpleefClassifier;

public class GameClassifiers {

	public static final AbstractGameClassifier[] CLASSIFIERS = new AbstractGameClassifier[Byte.MAX_VALUE];
	public static final AbstractGameClassifier INACTIVE = new InactiveClassifier();
	public static final AbstractGameClassifier MANHUNT = new ManhuntClassifier();
	public static final AbstractGameClassifier SPLEEF = new SpleefClassifier();

	static {
		CLASSIFIERS[INACTIVE.getClassifierId()] = INACTIVE;
		CLASSIFIERS[MANHUNT.getClassifierId()] = MANHUNT;
		CLASSIFIERS[SPLEEF.getClassifierId()] = SPLEEF;
	}
}
