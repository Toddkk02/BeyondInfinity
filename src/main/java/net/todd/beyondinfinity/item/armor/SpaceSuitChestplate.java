package net.todd.beyondinfinity.item.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SpaceSuitChestplate extends ArmorItem {
    private static final int MAX_OXYGEN = 36000; // 30 minuti (20 tick/secondo * 60 secondi * 30 minuti)

    public SpaceSuitChestplate(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public void inventoryTick(ItemStack stack, Level level, LivingEntity entity, int slot, boolean isSelected) {
        // Check if the entity is a player
        if (entity instanceof Player player) {
            if (!level.isClientSide()) {
                if (isInSpace(player)) {
                    // Check if the player has the full space suit
                    if (!hasFullSpaceSuit(player)) {
                        player.hurt(level.damageSources().generic(), 2.0F);
                        return;
                    }

                    // Oxygen management
                    consumeOxygen(stack);

                    // Space effects protection
                    player.setAirSupply(player.getMaxAirSupply());
                    protectFromSpaceEffects(player);
                }
            }
        }
    }
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "beyondinfinity:textures/models/armor/space_suit_layer_" +
                (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
    }
    private boolean isInSpace(Player player) {
        return player.level().dimension().location().toString().equals("beyondinfinity:moon") ||
                player.getY() > 256;
    }

    private boolean hasFullSpaceSuit(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SpaceSuitHelmet &&
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SpaceSuitChestplate &&
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof SpaceSuitLeggings &&
                player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof SpaceSuitBoots;
    }

    public int getOxygen(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Oxygen");
    }
    private void consumeOxygen(ItemStack stack) {
        int oxygen = getOxygen(stack);
        if (oxygen > 0) {
            setOxygen(stack, oxygen - 1);
        }
    }

    private void protectFromSpaceEffects(Player player) {
        // Riduce effetti gravità nello spazio
        player.setDeltaMovement(player.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));
    }
    public void setOxygen(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("Oxygen", Math.max(0, Math.min(amount, MAX_OXYGEN)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int oxygen = getOxygen(stack);
        int minutes = oxygen / 1200; // Converti in minuti (20 tick/secondo * 60 secondi)
        int seconds = (oxygen % 1200) / 20; // Resto in secondi
        tooltip.add(Component.literal(String.format("Oxygen: %02d:%02d", minutes, seconds)));
    }
}
