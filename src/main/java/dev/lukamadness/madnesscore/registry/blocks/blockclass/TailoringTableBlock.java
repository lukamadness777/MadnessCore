package dev.lukamadness.madnesscore.registry.blocks.blockclass;

import com.mojang.serialization.MapCodec;
import dev.lukamadness.madnesscore.network.TailoringRecipeListPayload;
import dev.lukamadness.madnesscore.registry.screenhandler.screenhandlerclass.TailoringTableScreenHandler;
import dev.lukamadness.madnesscore.tailoring.TailoringRecipeManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TailoringTableBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public TailoringTableBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(TailoringTableBlock::new);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TailoringTableBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state,
                                 World world,
                                 BlockPos pos,
                                 PlayerEntity player,
                                 BlockHitResult hit) {

        if (!world.isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, p) -> new TailoringTableScreenHandler(syncId, playerInventory),
                    Text.translatable("container.madnesscore.tailoring_table")
            ));

            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer,
                        TailoringRecipeListPayload.of(TailoringRecipeManager.getInstance().getRecipes()));
            }
        }

        return ActionResult.SUCCESS;
    }
}