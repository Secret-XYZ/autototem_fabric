package com.artem.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        ItemStack offhand = client.player.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
            // Offhand already has a totem, nothing to do
            return;
        }

        int totemSlot = findTotemInInventory(client);
        if (totemSlot == -1) {
            // No totem available in inventory
            return;
        }

        // Slot 45 is the offhand slot in the player screen handler
        int offhandSlotId = 45;

        client.interactionManager.clickSlot(
                client.player.playerScreenHandler.syncId,
                totemSlot,
                0,
                SlotActionType.PICKUP,
                client.player
        );
        client.interactionManager.clickSlot(
                client.player.playerScreenHandler.syncId,
                offhandSlotId,
                0,
                SlotActionType.PICKUP,
                client.player
        );
        // If the cursor still holds something (e.g. swapped an item out of offhand),
        // put it back where the totem was.
        if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
            client.interactionManager.clickSlot(
                    client.player.playerScreenHandler.syncId,
                    totemSlot,
                    0,
                    SlotActionType.PICKUP,
                    client.player
            );
        }
    }

    private int findTotemInInventory(MinecraftClient client) {
        var inventory = client.player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                // Inventory slot index -> screen handler slot id (main inventory + hotbar)
                // Fabric's PlayerInventory index maps directly to slots 9-35 (main) and 0-8 (hotbar)
                if (i < 9) {
                    return i + 36; // hotbar slots are 36-44 in the screen handler
                } else {
                    return i; // main inventory slots are 9-35
                }
            }
        }
        return -1;
    }
}
