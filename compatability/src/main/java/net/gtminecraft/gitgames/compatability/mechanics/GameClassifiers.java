package net.gtminecraft.gitgames.compatability.mechanics;

import net.gtminecraft.gitgames.compatability.mechanics.type.InactiveClassifier;
import net.gtminecraft.gitgames.compatability.mechanics.type.ManhuntClassifier;
import net.gtminecraft.gitgames.compatability.mechanics.type.SpleefClassifier;

public class GameClassifiers {

	public static final AbstractGameClassifier INACTIVE_CLASSIFIER = new InactiveClassifier();
	public static final AbstractGameClassifier VANILLA_MANHUNT_CLASSIFIER = new ManhuntClassifier();
	public static final AbstractGameClassifier SPLEEF_CLASSIFIER = new SpleefClassifier();
}
