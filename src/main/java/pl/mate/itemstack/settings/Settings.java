package pl.mate.itemstack.settings;

import org.bukkit.configuration.file.YamlConfiguration;
import pl.mate.itemstack.MainPlugin;

import java.util.logging.Level;

public class Settings {

    public static Boolean shapedRecipesEnable;
    public static Boolean shapelessRecipesEnable;
    public static Boolean furnaceRecipesEnable;
    public static Boolean moreFuelsEnable;
    public static Boolean consoleColors;
    private static DebugLevel debugLevel;

    public static void load(){
        // Get file
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(FileManager.config);

        // Read values
        shapedRecipesEnable = yml.getBoolean("shaped-recipes-enable");
        shapelessRecipesEnable = yml.getBoolean("shapeless-recipes-enable");
        furnaceRecipesEnable = yml.getBoolean("furnace-recipes-enable");
        moreFuelsEnable = yml.getBoolean("more-fuels-enable");
        consoleColors = yml.getBoolean("console-colors");

        // Read debug level
        debugLevel = DebugLevel.valueOf(yml.get("debug-level").toString().toUpperCase());
    }

    public enum DebugLevel{
        NONE(0), PARTIAL(1), FULL(2), EXTREME(3);

        Integer value;

        DebugLevel(Integer val){
            value = val;
        }

    }

    public static void debugMessage(DebugLevel dLevel, Level level, String msg){
        if(debugLevel.value>=dLevel.value){
            MainPlugin.logger.log(level, msg);
        }
    }
}


