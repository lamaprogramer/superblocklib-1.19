package net.iamaprogrammer.superblocklib.multiblock;

import net.iamaprogrammer.superblocklib.util.MultiblockPositioner;
import net.iamaprogrammer.superblocklib.util.Point;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiFunction;


public class MultiBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty MAIN_BLOCK = BooleanProperty.of("main_block");
    public static final BooleanProperty MODEL_BLOCK = BooleanProperty.of("model_block");


    protected final int WIDTH;
    protected final int HEIGHT;
    protected final int DEPTH;

    protected ArrayList<Point> POINTS;
    protected final int MAX_BLOCKS;

    protected int MODEL_OFFSET_X;
    protected int MODEL_OFFSET_Y;
    protected int MODEL_OFFSET_Z;

    protected int BLOCK_OFFSET_X;
    protected int BLOCK_OFFSET_Y;
    protected int BLOCK_OFFSET_Z;

    private final boolean HOLLOW;
    private final boolean SHAPED;

    public MultiBlock(Settings settings, MultiblockPositioner positioner) {
        super(settings.nonOpaque());

        Vec3i size = positioner.getSize();
        Vec3i modelCenter = positioner.hasCustomModelCenter() ? positioner.getCustomModelCenter() : null;
        Vec3i blockCenter = positioner.hasCustomBlockCenter() ? positioner.getCustomBlockCenter() : null;

        // Dimensions
        WIDTH = size.getX();
        HEIGHT = size.getY();
        DEPTH = size.getZ();

        // Model Offset
        MODEL_OFFSET_X = WIDTH - 1;
        MODEL_OFFSET_X = positioner.isModelCenterX() ? (WIDTH - 1) / 2 : MODEL_OFFSET_X;
        MODEL_OFFSET_X = modelCenter != null ? modelCenter.getX() : MODEL_OFFSET_X;

        MODEL_OFFSET_Y = 0;
        MODEL_OFFSET_Y = positioner.isModelCenterY() ? (HEIGHT - 1) / 2 : MODEL_OFFSET_Y;
        MODEL_OFFSET_Y = modelCenter != null ? modelCenter.getY() : MODEL_OFFSET_Y;

        MODEL_OFFSET_Z = DEPTH;
        MODEL_OFFSET_Z = positioner.isModelCenterZ() ? (DEPTH - 1) / 2 : MODEL_OFFSET_Z;
        MODEL_OFFSET_Z = modelCenter != null ? modelCenter.getZ() : MODEL_OFFSET_Z;

        // Block Offset
        BLOCK_OFFSET_X = (WIDTH - 1) / 2;
        BLOCK_OFFSET_X = positioner.isBlockCenterX() ? (WIDTH - 1) / 2 : BLOCK_OFFSET_X;
        BLOCK_OFFSET_X = blockCenter != null ? blockCenter.getX() : BLOCK_OFFSET_X;

        BLOCK_OFFSET_Y = 0;
        BLOCK_OFFSET_Y = positioner.isBlockCenterY() ? (HEIGHT - 1) / 2 : BLOCK_OFFSET_Y;
        BLOCK_OFFSET_Y = blockCenter != null ? blockCenter.getY() : BLOCK_OFFSET_Y;

        BLOCK_OFFSET_Z = DEPTH;
        BLOCK_OFFSET_Z = positioner.isBlockCenterZ() ? (DEPTH + 1) / 2 : BLOCK_OFFSET_Z;
        BLOCK_OFFSET_Z = blockCenter != null ? blockCenter.getZ() : BLOCK_OFFSET_Z;

        // Attributes
        HOLLOW = positioner.isHollow();
        SHAPED = positioner.isShaped();

        MAX_BLOCKS = SHAPED ? positioner.getPoints().size() : WIDTH * HEIGHT * DEPTH;
        POINTS = SHAPED ? positioner.getPoints() : POINTS;

        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(MAIN_BLOCK, true).with(MODEL_BLOCK, false));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        BlockState state = this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());

        // Check if placeable
        if (pos.getY() < world.getTopY() - 1 && this.preformOnAll(world, state, pos, (worldPos, relativePos) -> world.getBlockState(worldPos).canReplace(ctx))) {
            return state;
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        MultiBlockEntity mainEntity = (MultiBlockEntity) world.getBlockEntity(pos);
        if (mainEntity != null) {
            BlockPos modelPos = new BlockPos(MODEL_OFFSET_X, MODEL_OFFSET_Y, MODEL_OFFSET_Z);

            this.preformOnAll(world, state, pos, (worldPos, relativePos) -> {
                if (!worldPos.equals(pos) || relativePos.equals(modelPos)) {
                    world.setBlockState(worldPos, state.with(MAIN_BLOCK, worldPos.equals(pos))
                            .with(MODEL_BLOCK, relativePos.equals(modelPos))
                    );

                    // Store position of main block in new dummy
                    MultiBlockEntity dummyEntity = (MultiBlockEntity) world.getBlockEntity(worldPos);
                    if (dummyEntity != null) {
                        dummyEntity.setMainBlock(mainEntity.getMainBlock());
                    }
                }
                return true;
            });
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            MultiBlockEntity blockEntity = (MultiBlockEntity) world.getBlockEntity(pos);
            if (blockEntity != null) {
                BlockPos mainPos = blockEntity.getMainBlock();
                this.preformOnAll(world, state, mainPos, (worldPos, relativePos) -> {
                    world.removeBlock(worldPos, false);
                    return true;
                });
            }
        }
        super.onBreak(world, pos, state, player);
    }

    public final boolean preformOnAll(World world, BlockState state, BlockPos pos, BiFunction<BlockPos, Vec3i, Boolean> function) {
        Direction facing = state.get(FACING);
        Direction clockwise = facing.rotateYClockwise();
        BlockPos startPos = pos.offset(facing.getOpposite(), BLOCK_OFFSET_Z).offset(clockwise, BLOCK_OFFSET_X);

        int blocksSet = 0;
        for (int height = 0; height < HEIGHT; height++) {
            BlockPos offsetPos = startPos.up(height + BLOCK_OFFSET_Y);
            for (int width = 0; width < WIDTH; width++) {
                BlockPos rowPos = offsetPos;
                offsetPos = offsetPos.offset(clockwise.getOpposite());
                for (int depth = 0; depth < DEPTH; depth++) {
                    rowPos = rowPos.offset(facing);

                    MultiBlockEntity currBlock = (MultiBlockEntity) world.getBlockEntity(rowPos);
                    if ((currBlock != null && currBlock.getMainBlock().equals(pos)) || world.getBlockState(rowPos).isReplaceable()) {

                        if (SHAPED) {
                            Vec3i relativePos = new Vec3i(width, height, depth);
                            BlockState possibleMain = world.getBlockState(rowPos);
                            for (Point i : POINTS) {
                                if (i.asVec().equals(relativePos) || (possibleMain.isOf(this) && possibleMain.get(MAIN_BLOCK))) {
                                    if (function.apply(rowPos, relativePos)) {
                                        blocksSet++;
                                    }
                                    break;
                                }
                            }
                        } else {
                            if (HOLLOW) {
                                if (width != 0 && depth != 0) {
                                    if (width != WIDTH - 1 && depth != DEPTH - 1) {
                                        blocksSet++;
                                        continue;
                                    }
                                }
                            }
                            if (function.apply(rowPos, new Vec3i(width, height, depth))) {
                                blocksSet++;
                            }
                        }
                    }
                }
            }
        }
        return blocksSet == this.MAX_BLOCKS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return state.get(MultiBlock.MODEL_BLOCK) ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, MAIN_BLOCK, MODEL_BLOCK);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

}