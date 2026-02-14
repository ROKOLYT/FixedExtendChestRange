//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.john.chestrange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.io.File;
import java.util.logging.Level;

public class ExtendedChestRange extends JavaPlugin {

    private ExtendedChestRangeCommand extendedChestRangeCommand;

    public ExtendedChestRange(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        this.getLogger().at(Level.INFO).log("ExtendedChestRange starting...");

        extendedChestRangeCommand = new ExtendedChestRangeCommand("extendchestrange",
                "Used to extend the range of chests for crafting benches",
                false,
                this.getLogger());

        this.getCommandRegistry().registerCommand(extendedChestRangeCommand);
    }

    protected void start() {
        try {
            loadConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("ExtendedChestConfig.json");

        if (!file.exists()) {
            ExtendedChestConfig chestConfig = new ExtendedChestConfig(14, 6, 100);
            mapper.writeValue(file, chestConfig);
        }

        ExtendedChestConfig chestConfig = mapper.readValue(file, ExtendedChestConfig.class);
        extendedChestRangeCommand.initializeMod(chestConfig);
    }

    protected void shutdown() {
        try {
            extendedChestRangeCommand.saveConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.getLogger().at(Level.INFO).log("ExtendedChestRange shutting down.");
    }
}
