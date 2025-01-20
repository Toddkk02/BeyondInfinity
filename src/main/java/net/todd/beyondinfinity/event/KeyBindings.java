package net.todd.beyondinfinity.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String KEY_CATEGORY_BEYOND_INFINITY = "key.category.beyondinfinity.general";
    public static final String KEY_LAUNCH_ROCKET = "key.beyondinfinity.launch_rocket";

    public static final KeyMapping LAUNCH_ROCKET_KEY = new KeyMapping(KEY_LAUNCH_ROCKET,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            KEY_CATEGORY_BEYOND_INFINITY);
}