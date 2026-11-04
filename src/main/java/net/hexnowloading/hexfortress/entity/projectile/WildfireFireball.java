package net.hexnowloading.hexfortress.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import static net.minecraft.world.entity.EntityType.SMALL_FIREBALL;

public class WildfireFireball extends Fireball {

    float fireballDamage = 10.0F;

    public WildfireFireball(Level level, LivingEntity shooter, double accelerationX, double accelerationY, double accelerationZ) {
        super(SMALL_FIREBALL, shooter, accelerationX, accelerationY, accelerationZ, level);
    }

    public void setFireballDamage(float damage) {
        this.fireballDamage = damage;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!this.level().isClientSide) {
            Entity target = hitResult.getEntity();
            Entity shooter = this.getOwner();
            int i = target.getRemainingFireTicks();
            boolean flag = true;
            if (target instanceof LivingEntity) {
                if (target.fireImmune()) {
                    flag = false;
                }
            }
            if (flag) {
                target.setSecondsOnFire(5);
                if (!target.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity) shooter), fireballDamage)) {
                    target.setRemainingFireTicks(i);
                    if (target instanceof Player && ((Player) target).isBlocking()) {
                        RandomSource randomSource = this.level().getRandom();
                        if (randomSource.nextFloat() > 0.5) {
                            ((Player) target).disableShield(true);
                        }
                    }
                } else if (shooter instanceof LivingEntity) {
                    this.doEnchantDamageEffects((LivingEntity) shooter, target);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!this.level().isClientSide) {
            Entity shooter = this.getOwner();
            if (!(shooter instanceof Mob) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
                BlockPos blockpos = hitResult.getBlockPos().relative(hitResult.getDirection());
                if (this.level().isEmptyBlock(blockpos)) {
                    this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
                }
            }

        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.discard();
        }

    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource p_37381_, float p_37382_) {
        return false;
    }
}