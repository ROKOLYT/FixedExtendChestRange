package com.john.chestrange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.CraftingConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

public class ExtendedChestRangeCommand extends AbstractPlayerCommand {

    private final int MAX_RANGE = 500;
    private final int MAX_CHESTS = 1000;
    private final RequiredArg<Integer> horizontalRadius;
    private final RequiredArg<Integer> verticalRadius;
    private final OptionalArg<Integer> chestLimit;
    private final FlagArg flag;
    private Integer horizontalRadiusInt;
    private Integer verticalRadiusInt;
    private Integer chestLimitInt;
    private final HytaleLogger logger;

    public ExtendedChestRangeCommand(@NonNullDecl String name, @NonNullDecl String description, boolean requiresConfirmation, @NonNullDecl HytaleLogger logger) {
        super(name, description, requiresConfirmation);
        this.horizontalRadius = withRequiredArg("horizontal", "Horizontal chest range", ArgTypes.INTEGER);
        this.verticalRadius = withRequiredArg("vertical", "Vertical chest range", ArgTypes.INTEGER);
        this.chestLimit = withOptionalArg("chests", "Maximum number of chests", ArgTypes.INTEGER);
        this.flag = withFlagArg("force", "Forces the setting");
        this.logger = logger;
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        try {
            horizontalRadiusInt = horizontalRadius.get(commandContext);
            verticalRadiusInt = verticalRadius.get(commandContext);
            chestLimitInt = chestLimit.get(commandContext);

            if (chestLimitInt == null) {
                chestLimitInt = 100;
            }

            if (argsHaveBigValuesCheck() && !flag.get(commandContext)) {
                commandContext.sendMessage(Message.raw(String.format(
                        "The requested values are very high and may impact server performance. " +
                                "It is recommended to stay below %d blocks horizontally, %d blocks vertically and %d " +
                                "chests. If you wish to continue run the command again with the --force flag",
                        MAX_RANGE, MAX_RANGE, MAX_CHESTS
                )));
                return;
            }

            modifyCraftingConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        commandContext.sendMessage(Message.raw(String.format("Changed the chest range to %d blocks horizontally " +
                        "and %d blocks vertically with a limit of %d chests.",
                horizontalRadiusInt, verticalRadiusInt, chestLimitInt)));
    }

    private boolean argsHaveBigValuesCheck() {
        return horizontalRadiusInt > MAX_RANGE || verticalRadiusInt > MAX_RANGE || chestLimitInt > MAX_CHESTS;
    }

    private void modifyCraftingConfig() throws Exception {
        DefaultAssetMap<String, GameplayConfig> assetMap = GameplayConfig.getAssetMap();
        if (assetMap == null) {
            logger.at(Level.WARNING).log("GameplayConfig asset map is null, aborting...");
            return;
        }

        GameplayConfig firstConfig = assetMap.getAssetMap().values().iterator().next();
        if (firstConfig == null) {
            logger.at(Level.WARNING).log("First gameplay config is null, aborting...");
            return;
        }

        CraftingConfig sharedCraftingConfig = firstConfig.getCraftingConfig();
        if (sharedCraftingConfig == null) {
            logger.at(Level.WARNING).log("Crafting config is null, aborting...");
            return;
        }

        modifyCraftingConfigInstance(sharedCraftingConfig);
    }

    private void modifyCraftingConfigInstance(CraftingConfig craftingConfig) throws Exception {
        Class<CraftingConfig> clazz = CraftingConfig.class;
        Field horizontalField = clazz.getDeclaredField("benchMaterialHorizontalChestSearchRadius");
        Field verticalField = clazz.getDeclaredField("benchMaterialVerticalChestSearchRadius");
        Field limitField = clazz.getDeclaredField("benchMaterialChestLimit");

        horizontalField.setAccessible(true);
        verticalField.setAccessible(true);
        limitField.setAccessible(true);

        int oldH = horizontalField.getInt(craftingConfig);
        int oldV = verticalField.getInt(craftingConfig);
        int oldLimit = limitField.getInt(craftingConfig);

        logger.at(Level.INFO).log("Config '%s' old values: H=%d, V=%d, Limit=%d", "Global Shared Instance", oldH, oldV, oldLimit);

        horizontalField.setInt(craftingConfig, horizontalRadiusInt);
        verticalField.setInt(craftingConfig, verticalRadiusInt);
        limitField.setInt(craftingConfig, chestLimitInt);

        int newH = horizontalField.getInt(craftingConfig);
        int newV = verticalField.getInt(craftingConfig);
        int newLimit = limitField.getInt(craftingConfig);

        logger.at(Level.INFO).log("Config '%s' new values: H=%d, V=%d, Limit=%d", "Global Shared Instance", newH, newV, newLimit);
    }

    public void initializeMod(ExtendedChestConfig chestConfig) throws Exception {
        horizontalRadiusInt = chestConfig.getHorizontalRadius();
        verticalRadiusInt = chestConfig.getVerticalRadius();
        chestLimitInt = chestConfig.getChestLimit();

        modifyCraftingConfig();
    }

    public void saveConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("ExtendedChestConfig.json");

        ExtendedChestConfig extendedChestConfig = new ExtendedChestConfig(horizontalRadiusInt, verticalRadiusInt, chestLimitInt);
        mapper.writeValue(file, extendedChestConfig);
    }
}

