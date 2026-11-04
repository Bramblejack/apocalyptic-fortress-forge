package net.hexnowloading.hexfortress.block;

import com.google.common.collect.ImmutableList;
import net.hexnowloading.hexfortress.block.entity.PotOfGreedBlockEntity;
import net.hexnowloading.hexfortress.registry.HFItems;
import net.hexnowloading.hexfortress.registry.HFProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PotOfGreedBlock extends BaseEntityBlock {

    public static final IntegerProperty STAGE = HFProperties.STAGE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 15,13);
    private static final ImmutableList<Item> REQUIRED_ITEMS = ImmutableList.of(
            Items.BELL,
            Items.GOLD_BLOCK,
            Items.RAW_GOLD_BLOCK,
            Items.CLOCK,
            Items.GILDED_BLACKSTONE,
            Items.GLISTERING_MELON_SLICE,
            Items.GOLD_INGOT,
            Items.GOLD_ORE,
            Items.GOLDEN_APPLE,
            Items.GOLDEN_AXE,
            Items.GOLDEN_BOOTS,
            Items.GOLDEN_CARROT,
            Items.GOLDEN_CHESTPLATE,
            Items.GOLDEN_HELMET,
            Items.GOLDEN_HOE,
            Items.GOLDEN_HORSE_ARMOR,
            Items.GOLDEN_LEGGINGS,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_SWORD,
            Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Items.NETHER_GOLD_ORE,
            Items.DEEPSLATE_GOLD_ORE,
            Items.RAW_GOLD
    );
    public PotOfGreedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(STAGE, Integer.valueOf(0)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(WATERLOGGED, STAGE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return super.getStateForPlacement(ctx).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter blockGetter, BlockPos pos, PathComputationType pathComputationType) {
        return false;
    }

    public void consumeItem(Level level, BlockState state, BlockPos pos) {
        playSound(level, pos, SoundEvents.ARROW_HIT_PLAYER);
        if (level instanceof ServerLevel) {
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.5D;
            double d2 = (double) pos.getZ() + 0.5D;
            ((ServerLevel) level).sendParticles(ParticleTypes.GLOW, d0, d1, d2, 5, 0.5D, 0.5D, 0.5D, 0.001D);
        }
    }

    public void spawnParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel) {
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.5D;
            double d2 = (double) pos.getZ() + 0.5D;
            ((ServerLevel) level).sendParticles(ParticleTypes.GLOW, d0, d1, d2, 5, 0.5D, 0.5D, 0.5D, 0.001D);
        }
    }

    private void playSound(Level level, BlockPos pos, SoundEvent soundEvent) {
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.5D;
        double d2 = (double)pos.getZ() + 0.5D;

        level.playSound((Player)null, d0, d1, d2, soundEvent, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    public static boolean isGoldenSword(ItemStack itemStack) { return itemStack.is(Items.GOLDEN_SWORD); }
    public static boolean isGoldIngot(ItemStack itemStack) { return itemStack.is(Items.GOLD_INGOT); }

    public void increaseStage(BlockState state, Level level, BlockPos pos) {
        level.setBlock(pos, state.setValue(HFProperties.STAGE, state.getValue(HFProperties.STAGE) + 1), 0);
        playSound(level, pos, SoundEvents.ARROW_HIT_PLAYER);
    }

    public void spawnReward(Level level, BlockState state, BlockPos pos) {
        if (state.getValue(HFProperties.STAGE) == 0) {
            popResource(level, pos.above(), new ItemStack(HFItems.FRAGMENT_OF_GREED.get(), 1));
        } else if (state.getValue(HFProperties.STAGE) == 1) {
            popResource(level, pos.above(), new ItemStack(HFItems.CREST_OF_GREED.get(), 1));
        } else {
            popResource(level, pos.above(), new ItemStack(HFItems.EMBLEM_OF_GREED.get(), 1));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PotOfGreedBlockEntity potOfGreedBlockEntity) {
            ItemStack itemStack = player.getItemInHand(hand);
            Item item = player.getItemInHand(hand).getItem();
            if (state.getValue(HFProperties.STAGE) == 0) {
                if (isGoldenSword(itemStack)) {
                    spawnParticles(level, pos);
                    spawnReward(level, state, pos);
                    increaseStage(state, level, pos);
                }
                return InteractionResult.CONSUME;
            } else if (state.getValue(HFProperties.STAGE) == 1) {
                if (isGoldIngot(itemStack)) {
                    if (potOfGreedBlockEntity.addGoldCounter(player, itemStack)) {
                        spawnReward(level, state, pos);
                        increaseStage(state, level, pos);
                    } else {
                        playSound(level, pos, SoundEvents.ARROW_HIT_PLAYER);
                    }
                    spawnParticles(level, pos);
                }
                return InteractionResult.CONSUME;
            } else if (state.getValue(HFProperties.STAGE) == 2) {
                if (REQUIRED_ITEMS.contains(item)) {
                    if (!potOfGreedBlockEntity.getItems().contains(item)) {
                        if (!player.getAbilities().instabuild) {
                            itemStack.shrink(1);
                        }
                        consumeItem(level, state, pos);
                        potOfGreedBlockEntity.getItems().add(item);
                        if (potOfGreedBlockEntity.getItems().size() == REQUIRED_ITEMS.size()) {
                            spawnReward(level, state, pos);
                            increaseStage(state, level, pos);
                        }
                    } else {
                        playSound(level, pos, SoundEvents.NOTE_BLOCK_BASS.get());
                    }
                    player.displayClientMessage(Component.translatable("warning.hexfortress.pot_of_greed_remaining_items", REQUIRED_ITEMS.size() - potOfGreedBlockEntity.getItems().size()), true);
                } else if (itemStack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    spawnReward(level, state, pos);
                    increaseStage(state, level, pos);
                } else {
                    playSound(level, pos, SoundEvents.NOTE_BLOCK_BASS.get());
                    player.displayClientMessage(Component.translatable("warning.hexfortress.pot_of_greed_remaining_items", REQUIRED_ITEMS.size() - potOfGreedBlockEntity.getItems().size()), true);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotOfGreedBlockEntity(pos, state);
    }
}
