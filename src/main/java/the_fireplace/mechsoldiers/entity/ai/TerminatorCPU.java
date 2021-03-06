package the_fireplace.mechsoldiers.entity.ai;

import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.mechsoldiers.entity.EntityMechSkeleton;
import the_fireplace.mechsoldiers.util.ICPU;
import the_fireplace.overlord.entity.EntityArmyMember;
import the_fireplace.overlord.entity.ai.*;

public class TerminatorCPU implements ICPU {

	protected int raiseArmTicks;
	protected EntityAIAttackMelee aiAttackOnCollide = null;

	@Override
	public void addAttackAi(EntityMechSkeleton skeleton, byte mode) {
		if (aiAttackOnCollide == null) {
			aiAttackOnCollide = new EntityAIAttackMelee(skeleton, 1.5D, false) {
				@Override
				public void resetTask() {
					super.resetTask();
					skeleton.setSwingingArms(false);
				}

				@Override
				public void startExecuting() {
					super.startExecuting();
					raiseArmTicks = 0;
				}

				@Override
				public void updateTask() {
					if (shouldContinueExecuting()) {
						++raiseArmTicks;

						if (raiseArmTicks >= 5 && this.attackTick < 10) {
							skeleton.setSwingingArms(true);
						} else {
							skeleton.setSwingingArms(false);
						}
						super.updateTask();
					}
				}
			};
		}
		if (skeleton.getMovementMode() > 0)
			skeleton.tasks.addTask(5, aiAttackOnCollide);
	}

	@Override
	public void addTargetAi(EntityMechSkeleton skeleton, byte mode) {
		switch (mode) {
			case 2:
				skeleton.targetTasks.addTask(2, new EntityAIMasterHurtTarget(skeleton));
			case 1:
				skeleton.targetTasks.addTask(2, new EntityAINearestNonTeamTarget(skeleton, EntityPlayer.class, false));
				skeleton.targetTasks.addTask(2, new EntityAINearestNonTeamTarget(skeleton, EntityArmyMember.class, true));
				skeleton.targetTasks.addTask(3, new EntityAINearestNonTeamTarget(skeleton, IMob.class, true));
			case 0:
			default:
				skeleton.targetTasks.addTask(1, new EntityAIMasterHurtByTarget(skeleton));
				skeleton.targetTasks.addTask(1, new EntityAIHurtByNonAllied(skeleton, true));
				skeleton.targetTasks.addTask(4, new EntityAINearestNonTeamTarget(skeleton, IAnimals.class, false));
		}
	}

	@Override
	public void addMovementAi(EntityMechSkeleton skeleton, byte mode) {
		switch (mode) {
			case 1:
				skeleton.setHomePosAndDistance(new BlockPos(skeleton.posX, skeleton.posY, skeleton.posZ), -1);
				if (skeleton.shouldMobAttack(new EntityCreeper(skeleton.world))) {
					skeleton.tasks.addTask(3, new EntityAIAvoidEntity(skeleton, EntityCreeper.class, 10.0F, 1.2D, 1.6D));
				}
				skeleton.tasks.addTask(4, new EntityAIOpenDoor(skeleton, false));
				skeleton.tasks.addTask(6, new EntityAIFollowMaster(skeleton, 1.0D, 20.0F, 1.0F));
				skeleton.tasks.addTask(7, new EntityAIWanderBase(skeleton, 1.0D));
				break;
			case 0:
				skeleton.setHomePosAndDistance(new BlockPos(skeleton.posX, skeleton.posY, skeleton.posZ), 3);
				skeleton.tasks.addTask(7, new EntityAIWanderBase(skeleton, 0.5D));
				break;
			case 2:
			default:
				skeleton.setHomePosAndDistance(new BlockPos(skeleton.posX, skeleton.posY, skeleton.posZ), 20);
				if (skeleton.shouldMobAttack(new EntityCreeper(skeleton.world))) {
					skeleton.tasks.addTask(3, new EntityAIAvoidEntity(skeleton, EntityCreeper.class, 10.0F, 1.2D, 1.6D));
				}
				skeleton.tasks.addTask(4, new EntityAIOpenDoor(skeleton, false));
				skeleton.tasks.addTask(7, new EntityAIWanderBase(skeleton, 1.0D));
		}
	}
}
