package dev.lukamadness.madnesscore.registry.screenhandler.screenhandlerclass;

import dev.lukamadness.madnesscore.MadnessCore;
import dev.lukamadness.madnesscore.registry.screenhandler.ModScreenHandlers;
import dev.lukamadness.madnesscore.tailoring.FabricRequirement;
import dev.lukamadness.madnesscore.tailoring.TailoringRecipe;
import dev.lukamadness.madnesscore.tailoring.TailoringRecipeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TailoringTableScreenHandler extends ScreenHandler {

    public static final TagKey<net.minecraft.item.Item> FABRIC_TAG =
            TagKey.of(RegistryKeys.ITEM, Identifier.of(MadnessCore.MOD_ID, "tailoring_ingredients"));

    private static final int INPUT_SLOT_COUNT = 4;
    private static final int OUTPUT_SLOT_INDEX = INPUT_SLOT_COUNT;

    private final Inventory inputInventory;
    private final Inventory outputInventory;
    private final ScreenHandlerContext context;

    private TailoringRecipe selectedRecipe;

    public TailoringTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public TailoringTableScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    private TailoringTableScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModScreenHandlers.TAILORING_TABLE, syncId);
        this.context = context;

        this.inputInventory = new SimpleInventory(INPUT_SLOT_COUNT) {
            @Override
            public void markDirty() {
                super.markDirty();
                updateOutput();
            }
        };
        this.outputInventory = new SimpleInventory(1);

        this.inputInventory.onOpen(playerInventory.player);
        this.outputInventory.onOpen(playerInventory.player);

        this.addSlot(new FabricInputSlot(inputInventory, 0, 14, 26));
        this.addSlot(new FabricInputSlot(inputInventory, 1, 32, 26));
        this.addSlot(new FabricInputSlot(inputInventory, 2, 14, 44));
        this.addSlot(new FabricInputSlot(inputInventory, 3, 32, 44));

        this.addSlot(new TailoringOutputSlot(this, outputInventory, 0, 145, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        List<TailoringRecipe> recipes = TailoringRecipeManager.getInstance().getRecipes();
        if (id < 0 || id >= recipes.size()) {
            return false;
        }
        this.selectedRecipe = recipes.get(id);
        this.updateOutput();
        return true;
    }

    private void updateOutput() {
        if (this.selectedRecipe != null && this.selectedRecipe.matches(getInputStacks())) {
            this.outputInventory.setStack(0, this.selectedRecipe.output().copy());
        } else {
            this.outputInventory.setStack(0, ItemStack.EMPTY);
        }
    }

    private List<ItemStack> getInputStacks() {
        List<ItemStack> list = new ArrayList<>(INPUT_SLOT_COUNT);
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            list.add(this.inputInventory.getStack(i));
        }
        return list;
    }

    private void onOutputTaken() {
        if (this.selectedRecipe == null) {
            return;
        }

        for (FabricRequirement requirement : this.selectedRecipe.requirements()) {
            int remaining = requirement.count();
            for (int i = 0; i < INPUT_SLOT_COUNT && remaining > 0; i++) {
                ItemStack stack = this.inputInventory.getStack(i);
                if (!stack.isEmpty() && stack.getItem() == requirement.item()) {
                    int taken = Math.min(remaining, stack.getCount());
                    this.inputInventory.removeStack(i, taken);
                    remaining -= taken;
                }
            }
        }

        this.selectedRecipe = null;
        this.updateOutput();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            originalStack = slotStack.copy();

            int playerInvStart = INPUT_SLOT_COUNT + 1;
            int playerInvEnd = this.slots.size();

            if (slotIndex == OUTPUT_SLOT_INDEX) {
                if (!this.insertItem(slotStack, playerInvStart, playerInvEnd, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(slotStack, originalStack);
            } else if (slotIndex >= playerInvStart && slotIndex < playerInvEnd) {
                if (!insertIntoSingleInputSlot(slotStack)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < INPUT_SLOT_COUNT) {
                if (!this.insertItem(slotStack, playerInvStart, playerInvEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return originalStack;
    }

    private boolean insertIntoSingleInputSlot(ItemStack stack) {
        for (int i = 0; i < INPUT_SLOT_COUNT && !stack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack existing = slot.getStack();
            if (!existing.isEmpty() && ItemStack.areItemsAndComponentsEqual(existing, stack)
                    && existing.getCount() < slot.getMaxItemCount()) {
                int space = slot.getMaxItemCount() - existing.getCount();
                int move = Math.min(space, stack.getCount());
                existing.increment(move);
                stack.decrement(move);
                slot.markDirty();
                return true;
            }
        }

        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            Slot slot = this.slots.get(i);
            if (slot.getStack().isEmpty() && slot.canInsert(stack)) {
                int move = Math.min(slot.getMaxItemCount(), stack.getCount());
                ItemStack toInsert = stack.split(move);
                slot.setStack(toInsert);
                slot.markDirty();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.inputInventory);
    }

    private static class FabricInputSlot extends Slot {
        public FabricInputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isIn(FABRIC_TAG);
        }

        @Override
        public int getMaxItemCount() {
            return 16;
        }
    }

    private static class TailoringOutputSlot extends Slot {
        private final TailoringTableScreenHandler handler;

        public TailoringOutputSlot(TailoringTableScreenHandler handler, Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.handler = handler;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            super.onTakeItem(player, stack);
            handler.onOutputTaken();
        }

        @Override
        public void onQuickTransfer(ItemStack newItem, ItemStack original) {
            super.onQuickTransfer(newItem, original);
            handler.onOutputTaken();
        }
    }
}