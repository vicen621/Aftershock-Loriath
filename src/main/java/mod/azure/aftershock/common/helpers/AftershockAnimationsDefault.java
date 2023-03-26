package mod.azure.aftershock.common.helpers;

import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.RawAnimation;

public class AftershockAnimationsDefault {

	public static final RawAnimation BIRTH = RawAnimation.begin().then("change_skin", LoopType.PLAY_ONCE);
	public static final RawAnimation LOOK = RawAnimation.begin().thenLoop("look");
	public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	public static final RawAnimation HURT = RawAnimation.begin().thenPlayAndHold("hurt");
	public static final RawAnimation GRAB = RawAnimation.begin().thenLoop("grab");
	public static final RawAnimation PUKE = RawAnimation.begin().thenLoop("puke");
	public static final RawAnimation SCREAM = RawAnimation.begin().thenLoop("aggro");
	public static final RawAnimation MOLT = RawAnimation.begin().thenPlayAndHold("statis_start");
	public static final RawAnimation SPAWN = RawAnimation.begin().thenPlayAndHold("statis_idle");
	public static final RawAnimation RUN = RawAnimation.begin().thenLoop("running");
	public static final RawAnimation RUNSCREAM = RawAnimation.begin().thenLoop("running");
	public static final RawAnimation WALK = RawAnimation.begin().thenLoop("moving");
	public static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
	public static final RawAnimation BLOW_TORCH = RawAnimation.begin().thenLoop("blow_torch");
}
