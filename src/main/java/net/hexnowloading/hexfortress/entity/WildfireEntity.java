package net.hexnowloading.hexfortress.entity;

import net.hexnowloading.hexfortress.entity.projectile.WildfireFireball;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class WildfireEntity extends Monster {
    private float allowedHeightOffset = 0.5F;
    private int nextHeightOffsetChangeTick;
    private static final EntityDataAccessor<Boolean> DATA_FLAGS_ID = SynchedEntityData.defineId(WildfireEntity.class, EntityDataSerializers.BOOLEAN);
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState attackRevertAnimationState = new AnimationState();

    public WildfireEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
        this.xpReward = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new WildfireAttackGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33034_) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public void aiStep() {
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }
        if (this.level().isClientSide) {
            if (this.random.nextInt(24) == 0 && !this.isSilent()) {
                this.level().playLocalSound(this.getX() + 0.5D, this.getY() + 0.5D, this.getZ() + 0.5D, SoundEvents.BLAZE_BURN, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }

            for(int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
        }
        super.aiStep();
    }

    @Override
    protected void customServerAiStep() {
        --this.nextHeightOffsetChangeTick;
        if (this.nextHeightOffsetChangeTick <= 0) {
            this.nextHeightOffsetChangeTick = 100;
            this.allowedHeightOffset = (float)this.random.triangle(0.5D, 6.891D);
        }

        LivingEntity livingentity = this.getTarget();
        if (livingentity != null && livingentity.getEyeY() > this.getEyeY() + (double)this.allowedHeightOffset && this.canAttack(livingentity)) {
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, ((double)0.3F - vec3.y) * (double)0.3F, 0.0D));
            this.hasImpulse = true;
        }
        super.customServerAiStep();
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPersistenceRequired() { return false; }

    @Override
    public boolean causeFallDamage(float p_149683_, float p_149684_, DamageSource p_149685_) {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return this.isCharged();
    }

    public boolean isCharged() { return this.entityData.get(DATA_FLAGS_ID); }

    void setCharged(boolean flag) {
        this.entityData.set(DATA_FLAGS_ID, flag);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            if (isCharged()) {
                this.attackRevertAnimationState.stop();
                this.attackAnimationState.startIfStopped(this.tickCount);
            } else {
                this.attackAnimationState.stop();
                this.attackRevertAnimationState.startIfStopped(this.tickCount);
            }
        }
        super.tick();
    }

    static class WildfireAttackGoal extends Goal {
        private final WildfireEntity wildfire;
        private static final int FIREBALL_SHOT_AT_ONCE = 3;
        private static final int SHOOTING_ATTEMPTS = 10;
        private static final int ATTACK_COOLDOWN = 60;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        public WildfireAttackGoal(WildfireEntity entity) {
            this.wildfire = entity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.wildfire.getTarget();
            return livingEntity != null && livingEntity.isAlive() && this.wildfire.canAttack(livingEntity);
        }

        @Override
        public void start() { this.attackStep = 0; }

        @Override
        public void stop() {
            this.wildfire.setCharged(false);
            this.lastSeen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }

        private double getFollowDistance() {
            return this.wildfire.getAttributeValue(Attributes.FOLLOW_RANGE);
        }

        @Override
        public void tick() {
            --this.attackTime;
            LivingEntity livingEntity = this.wildfire.getTarget();
            if (livingEntity != null) {
                boolean flag = this.wildfire.getSensing().hasLineOfSight(livingEntity);
                if (flag) {
                    this.lastSeen = 0;
                } else {
                    this.lastSeen++;
                }

                double targetDistance = this.wildfire.distanceToSqr(livingEntity);
                if (targetDistance < 4.0D) {
                    if (!flag) {
                        return;
                    }

                    if (this.attackTime <= 0) {
                        this.attackTime = 20;
                        this.wildfire.doHurtTarget(livingEntity);
                    }

                    this.wildfire.getMoveControl().setWantedPosition(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 2.0D);
                } else if (targetDistance < this.getFollowDistance() * this.getFollowDistance() && flag) {
                    double d1 = livingEntity.getX() - this.wildfire.getX();
                    double d2 = livingEntity.getY(0.5D) - this.wildfire.getY(0.5D);
                    double d3 = livingEntity.getZ() - this.wildfire.getZ();
                    if (this.attackTime <= 0) {
                        ++this.attackStep;
                        if (this.attackStep == 1) {
                            this.attackTime = ATTACK_COOLDOWN;
                            this.wildfire.setCharged(true);
                        } else if (this.attackStep <= SHOOTING_ATTEMPTS + 1) {
                            this.attackTime = 6;
                        } else {
                            this.attackTime = 100;
                            this.attackStep = 0;
                            this.wildfire.setCharged(false);
                        }

                        if (this.attackStep > 1) {
                            double d4 = Math.sqrt(Math.sqrt(targetDistance)) * 0.5D;
                            if (!this.wildfire.isSilent()) {
                                this.wildfire.level().levelEvent((Player)null, 1018, this.wildfire.blockPosition(), 0);
                            }

                            for (int i = 0; i < FIREBALL_SHOT_AT_ONCE; ++i) {
                                WildfireFireball wildfireFireball = new WildfireFireball(this.wildfire.level(), this.wildfire, this.wildfire.getRandom().triangle(d1, 5.297D * d4), d2, this.wildfire.getRandom().triangle(d3, 5.297D * d4));
                                wildfireFireball.setPos(wildfireFireball.getX(), this.wildfire.getY(0.5D) + 0.5D, wildfireFireball.getZ());
                                wildfireFireball.setFireballDamage(10);
                                //SmallFireball smallFireball = new SmallFireball(this.wildfire.level(), this.wildfire, this.wildfire.getRandom().triangle(d1, 2.297D * d4), d2, this.wildfire.getRandom().triangle(d3, 2.297D * d4));
                                //smallFireball.setPos(smallFireball.getX(), this.wildfire.getY(0.5D) + 0.5D, smallFireball.getZ());
                                this.wildfire.level().addFreshEntity(wildfireFireball);
                            }
                        }
                    }
                    this.wildfire.getLookControl().setLookAt(livingEntity, 10.0F, 10.0F);
                } else if (this.lastSeen < 5) {
                    this.wildfire.getMoveControl().setWantedPosition(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 1.0D);
                }
                super.tick();
            }
        }
    }
}
