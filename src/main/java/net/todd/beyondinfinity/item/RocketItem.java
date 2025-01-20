package net.todd.beyondinfinity.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RocketItem extends Item {
    public RocketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.onGround()) { // Usa il campo onGround per verificare se il giocatore è a terra
            launchRocket(player);
            itemstack.shrink(1); // Rimuove l'oggetto dopo l'uso
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    private void launchRocket(Player player) {
        // Initial vertical velocity
        double launchVelocity = 2.0D;

        // Set player's vertical motion
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, launchVelocity, motion.z);

        // Prevent fall damage
        player.fallDistance = 0;

        // Start a task to continue upward motion
        new Thread(() -> {
            try {
                while (player.getY() < 320) { // Keep going until y=320 (space)
                    Thread.sleep(50); // Update every 50ms
                    if (!player.isRemoved()) {
                        Vec3 currentMotion = player.getDeltaMovement();
                        player.setDeltaMovement(currentMotion.x, launchVelocity, currentMotion.z);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
