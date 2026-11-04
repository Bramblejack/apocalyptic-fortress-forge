package net.hexnowloading.hexfortress.block.entity;

import net.hexnowloading.hexfortress.entity.WildfireEntity;
import net.hexnowloading.hexfortress.registry.HFBlockEntities;
import net.hexnowloading.hexfortress.registry.HFProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResistanceCancelerBlockEntity extends BlockEntity {
    public ResistanceCancelerBlockEntity(BlockPos pos, BlockState state) {
        super(HFBlockEntities.RESISTANCE_CANCELER.get(), pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
    }

    public static void tick(@NotNull Level level, BlockPos pos, BlockState state, ResistanceCancelerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (state.getValue(HFProperties.ACTIVATED)) {
                if (level.getGameTime() % 20L == 0L) {
                    applyEffects(level, pos, state, blockEntity);
                }
            }
        }
    }

    private static void applyEffects(@NotNull Level level, BlockPos pos, BlockState state, ResistanceCancelerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            double d0 = (double)state.getValue(HFProperties.RANGE);
            AABB aabb = (new AABB(pos)).inflate(d0).setMinY(0.0).setMaxY(level.getHeight());
            List<Player> list = level.getEntitiesOfClass(Player.class, aabb);
            List<AbstractSkeleton> skeletonList = level.getEntitiesOfClass(AbstractSkeleton.class, aabb);
            List<Blaze> blazeList = level.getEntitiesOfClass(Blaze.class, aabb);
            List<WildfireEntity> wildfireList = level.getEntitiesOfClass(WildfireEntity.class, aabb);

            for (Player player : list) {
                if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    player.removeEffect(MobEffects.FIRE_RESISTANCE);
                    playSound(player.level(), player.blockPosition(), SoundEvents.BEACON_POWER_SELECT);
                }
            }
            for (AbstractSkeleton skeleton : skeletonList) {
                if (level.getDifficulty() == Difficulty.NORMAL) {
                    skeleton.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1));
                } else if (level.getDifficulty() == Difficulty.HARD) {
                    skeleton.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 2));
                }
            }
            for (Blaze blaze : blazeList) {
                if (level.getDifficulty() == Difficulty.NORMAL) {
                    blaze.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1));
                } else if (level.getDifficulty() == Difficulty.HARD) {
                    blaze.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 2));
                }
            }
            for (WildfireEntity wildfire : wildfireList) {
                if (level.getDifficulty() == Difficulty.NORMAL) {
                    wildfire.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1));
                } else if (level.getDifficulty() == Difficulty.HARD) {
                    wildfire.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 2));
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    public static void playSound(Level level, BlockPos pos, SoundEvent soundEvent) {
        level.playSound((Player)null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
