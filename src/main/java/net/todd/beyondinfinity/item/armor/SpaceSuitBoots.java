package net.todd.beyondinfinity.item.armor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpaceSuitBoots extends ArmorItem {
    public SpaceSuitBoots(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "beyondinfinity:textures/models/armor/space_suit_layer_" +
                (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player && !level.isClientSide()) {
            if (isInSpace(player) && !hasFullSpaceSuit(player)) {
                player.hurt(level.damageSources().generic(), 2.0F);
            }
        }
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
}