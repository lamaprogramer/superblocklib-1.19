package net.iamaprogrammer.superblocklib;

import net.fabricmc.api.ModInitializer;
import net.iamaprogrammer.superblocklib.registries.BlockEntityRegistry;
import net.iamaprogrammer.superblocklib.registries.BlockRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuperBlockLib implements ModInitializer {
	public static final String MOD_ID = "superblocklib";
	public static final Logger LOGGER = LoggerFactory.getLogger("superblocklib");

	@Override
	public void onInitialize() {
		BlockRegistry.register();
		BlockEntityRegistry.register();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
