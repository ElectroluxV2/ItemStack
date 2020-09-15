package pl.mate.itemstack;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import pl.mate.itemstack.commands.Reload;
import pl.mate.itemstack.events.FurnaceBurnE;
import pl.mate.itemstack.events.InventoryClickE;
import pl.mate.itemstack.events.PrepareItemCraftE;
import pl.mate.itemstack.logger.CustomLogger;
import pl.mate.itemstack.recipes.*;
import pl.mate.itemstack.settings.FileManager;
import pl.mate.itemstack.settings.Settings;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static pl.mate.itemstack.settings.Settings.DebugLevel.EXTREME;
import static pl.mate.itemstack.settings.Settings.DebugLevel.FULL;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class MainPlugin extends JavaPlugin {

    // Instance
    private static MainPlugin instance;

    // Logger
    public static Logger logger;


    public void onEnable(){

        // Instance
        instance = this;

        // Logger
        logger = new CustomLogger(this);

        // Settings
        FileManager.checkFiles();
        Settings.load();

        // Listeners
        Bukkit.getPluginManager().registerEvents(new PrepareItemCraftE(), this);
        Bukkit.getPluginManager().registerEvents(new FurnaceBurnE(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickE(), this);

        // Burnable
        if(Settings.moreFuelsEnable){
            debugMessage(FULL, INFO, "Loading fuels.yml");
            Fuels.load();
        } else {
            debugMessage(FULL, INFO, "Ignoring fuels.yml");
        }

        // Load ItemStacks
        debugMessage(FULL, INFO, "Loading itemStacks.yml");
        ItemStacks.load();

        // Recipes
        if(Settings.shapedRecipesEnable){
            debugMessage(FULL, INFO, "Loading shapedRecipes.yml");
            ShapedRecipes.load();
        } else {
            debugMessage(FULL, INFO, "Ignoring shapedRecipes.yml");
        }

        if(Settings.shapelessRecipesEnable){
            debugMessage(FULL, INFO, "Loading shapelessRecipes.yml");
            ShapelessRecipes.load();
        } else {
            debugMessage(FULL, INFO, "Ignoring shapelessRecipes.yml");
        }


        if(Settings.furnaceRecipesEnable){
            debugMessage(FULL, INFO, "Loading furnaceRecipes.yml");
            FurnaceRecipes.load();
        } else {
            debugMessage(FULL, INFO, "Ignoring furnaceRecipes.yml");
        }

        debugMessage(EXTREME, INFO, "Registering commands");
        /* Register commands */
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            debugMessage(EXTREME, INFO, "Register of command \"isr\"");
            commandMap.register("isr", new Reload());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDisable(){}

    public static MainPlugin getInst() {
        return instance;
    }
}
