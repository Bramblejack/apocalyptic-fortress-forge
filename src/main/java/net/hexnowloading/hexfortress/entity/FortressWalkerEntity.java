package net.hexnowloading.hexfortress.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FortressWalkerEntity extends Monster {
    private static final EntityDataAccessor<Boolean> COMPACT = SynchedEntityData.defineId(FortressWalkerEntity.class, EntityDataSerializers.BOOLEAN);
    public final AnimationState compactAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState wakeUpAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public FortressWalkerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FortressWalkerMeleeAttackGoal());
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(COMPACT, true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FortressWalkerNavigation(this, level);
    }

    private boolean isCompact() { return this.entityData.get(COMPACT); }

    private void setCompact(boolean flag) { this.entityData.set(COMPACT, flag); }

    private boolean isMovingOnLand() {
        return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D && !this.isInWaterOrBubble();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (this.isCompact()) {
            this.compactAnimationState.stop();
            this.wakeUpAnimationState.startIfStopped(this.tickCount);
            setCompact(false);
        }
        return super.hurt(damageSource, damage);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            if (this.isCompact()) {
                this.compactAnimationState.startIfStopped(this.tickCount);
            } else {
                if (this.isMovingOnLand()) {
                    this.idleAnimationState.stop();
                    this.runAnimationState.startIfStopped(this.tickCount);
                } else {
                    this.runAnimationState.stop();
                    this.idleAnimationState.startIfStopped(this.tickCount);
                }
            }
        }
        super.tick();
    }

    class FortressWalkerMeleeAttackGoal extends MeleeAttackGoal {
        public FortressWalkerMeleeAttackGoal() {
            super(FortressWalkerEntity.this, 1.0D, true);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            float f = FortressWalkerEntity.this.getBbWidth() - 3.1F;
            return (double)(f *2.0F*f*2.0F + livingEntity.getBbWidth());
        }
    }

    static class FortressWalkerNavigation extends GroundPathNavigation {
        public FortressWalkerNavigation(Mob p_33379_, Level p_33380_) {
            super(p_33379_, p_33380_);
        }

        protected PathFinder createPathFinder(int p_33382_) {
            this.nodeEvaluator = new FortressWalkerNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, p_33382_);
        }
    }

    static class FortressWalkerNodeEvaluator extends WalkNodeEvaluator {
        protected BlockPathTypes evaluateBlockPathType(BlockGetter p_33387_, BlockPos p_33390_, BlockPathTypes p_33391_) {
            return p_33391_ == BlockPathTypes.LEAVES ? BlockPathTypes.OPEN : super.evaluateBlockPathType(p_33387_, p_33390_, p_33391_);
        }
    }
}
