package pl.mate.itemstack.settings;

import java.io.File;

import static pl.mate.itemstack.MainPlugin.getInst;

public class FileManager {

    static File config;
    public static File itemStacks;
    public static File shapedRecipes;
    public static File shapelessRecipes;
    public static File furnaceRecipes;
    public static File fuels;

    public static void checkFiles(){

        // Main folder
        if(!getInst().getDataFolder().exists()){
            getInst().getDataFolder().mkdir();
        }

        // Config file
        config = new File(getInst().getDataFolder(), "config.yml");

        // Config file
        itemStacks = new File(getInst().getDataFolder(), "itemStacks.yml");

        // Config file
        shapedRecipes = new File(getInst().getDataFolder(), "shapedRecipes.yml");

        // Config file
        shapelessRecipes = new File(getInst().getDataFolder(), "shapelessRecipes.yml");

        // Config file
        fuels = new File(getInst().getDataFolder(), "fuels.yml");

        // Config file
        furnaceRecipes = new File(getInst().getDataFolder(), "furnaceRecipes.yml");


        // Massing!
        if(!config.exists()){
            getInst().saveDefaultConfig();
        }

        if(!shapedRecipes.exists()){
            getInst().saveResource("shapedRecipes.yml", true);
        }

        if(!itemStacks.exists()){
            getInst().saveResource("itemStacks.yml", true);
        }

        if(!shapelessRecipes.exists()){
            getInst().saveResource("shapelessRecipes.yml", true);
        }

        if(!fuels.exists()){
            getInst().saveResource("fuels.yml", true);
        }

        if(!furnaceRecipes.exists()){
            getInst().saveResource("furnaceRecipes.yml", true);
        }
    }
}
