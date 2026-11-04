package net.hexnowloading.hexfortress.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TestEntity extends Monster {
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState extendAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();

    public TestEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10D)
                .add(Attributes.ATTACK_DAMAGE, 1.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
    }

    private boolean isMovingOnLand() {
        return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D && !this.isInWaterOrBubble();
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            if (this.isMovingOnLand()) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.tick();
    }

    @Override
    public boolean hurt(DamageSource p_21016_, float p_21017_) {
        this.extendAnimationState.start(this.tickCount);
        return super.hurt(p_21016_, p_21017_);
    }
}
