package mod.azure.aftershock.common.helpers;

import java.util.Map;

public enum AttackType {
	NONE(GenericAttackType.NONE), NORMAL(GenericAttackType.NORMAL), BITE(GenericAttackType.BITE),
	GRAB1(GenericAttackType.GRAB1), GRAB2(GenericAttackType.GRAB2);

	public static final Map<AttackType, String> animationMappings = Map.ofEntries(Map.entry(NORMAL, "attack"),
			Map.entry(BITE, "bite"), Map.entry(GRAB1, "grab1"), Map.entry(GRAB2, "grab2"));

	public final GenericAttackType genericAttackType;

	AttackType(GenericAttackType genericAttackType) {
		this.genericAttackType = genericAttackType;
	}
}
