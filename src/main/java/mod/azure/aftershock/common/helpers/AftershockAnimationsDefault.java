package mod.azure.aftershock.common.helpers;

import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.RawAnimation;

public class AftershockAnimationsDefault {

	public static final RawAnimation BIRTH = RawAnimation.begin().then("change_skin", LoopType.PLAY_ONCE);
	public static final RawAnimation LOOK = RawAnimation.begin().then("look", LoopType.PLAY_ONCE);
	public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	public static final RawAnimation HURT = RawAnimation.begin().thenPlayAndHold("hurt");
	public static final RawAnimation GRAB = RawAnimation.begin().then("grab", LoopType.PLAY_ONCE);
	public static final RawAnimation PUKE = RawAnimation.begin().thenPlayXTimes("grab", 1).thenPlayXTimes("puke", 3);
	public static final RawAnimation SCREAM = RawAnimation.begin().then("aggro", LoopType.PLAY_ONCE);
	public static final RawAnimation MOLT = RawAnimation.begin().thenPlayAndHold("statis_start");
	public static final RawAnimation SPAWN = RawAnimation.begin().thenPlayAndHold("statis_idle");
	public static final RawAnimation PASSOUT = RawAnimation.begin().thenPlayAndHold("pass_out");
	public static final RawAnimation WAKEUP = RawAnimation.begin().then("wake", LoopType.PLAY_ONCE);
	public static final RawAnimation RUN = RawAnimation.begin().thenLoop("running");
	public static final RawAnimation RUNSCREAM = RawAnimation.begin().thenLoop("running");
	public static final RawAnimation WALK = RawAnimation.begin().thenLoop("moving");
	public static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
	public static final RawAnimation BLOW_TORCH = RawAnimation.begin().then("blow_torch", LoopType.PLAY_ONCE);
	public static final RawAnimation LAY = RawAnimation.begin().thenPlayAndHold("laying_egg");
	public static final RawAnimation UNLAY = RawAnimation.begin().then("unlaying_egg", LoopType.PLAY_ONCE);
	public static final RawAnimation TAKE_OFF = RawAnimation.begin().then("take_off", LoopType.PLAY_ONCE);
	public static final RawAnimation GLIDING = RawAnimation.begin().thenLoop("gliding");
	public static final RawAnimation GLIDING_LOOK = RawAnimation.begin().then("gliding_look", LoopType.PLAY_ONCE);
	public static final RawAnimation DRAGGING = RawAnimation.begin().then("dragging", LoopType.PLAY_ONCE);
	public static final RawAnimation GRAB1 = RawAnimation.begin().then("grab1", LoopType.PLAY_ONCE);
	public static final RawAnimation GRAB2 = RawAnimation.begin().then("grab2", LoopType.PLAY_ONCE);
	public static final RawAnimation ERUPTING = RawAnimation.begin().then("erupting", LoopType.PLAY_ONCE);
	public static final RawAnimation BITE = RawAnimation.begin().then("bite", LoopType.PLAY_ONCE);
	public static final RawAnimation ATTACK = RawAnimation.begin().then("attack", LoopType.PLAY_ONCE);
	public static final RawAnimation DIGOUT = RawAnimation.begin().then("digout", LoopType.PLAY_ONCE);
	public static final RawAnimation DIGIN = RawAnimation.begin().then("digin", LoopType.PLAY_ONCE);
}
