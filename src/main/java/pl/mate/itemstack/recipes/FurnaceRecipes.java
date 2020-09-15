package pl.mate.itemstack.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;
import pl.mate.itemstack.MainPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.bukkit.Bukkit.getServer;
import static pl.mate.itemstack.MainPlugin.logger;
import static pl.mate.itemstack.recipes.ItemStacks.getMaterialOrItemStack;
import static pl.mate.itemstack.recipes.ItemStacks.itemStackHashtable;
import static pl.mate.itemstack.settings.FileManager.furnaceRecipes;
import static pl.mate.itemstack.settings.Settings.DebugLevel.EXTREME;
import static pl.mate.itemstack.settings.Settings.DebugLevel.PARTIAL;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class FurnaceRecipes{

    public static Map<String, FurnaceRecipe> furnaceRecipeHashMap = new HashMap<>();
    private static Integer index = 0;

    public static Boolean load(){

        Boolean errorOccurred = false;

        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(furnaceRecipes)){
            Iterable<Object> itr = yaml.loadAll(in);

            for (Object o : itr){
                // Read file by objects
                LinkedHashMap map = (LinkedHashMap) o;
                // Parse object
                if(!parseYamlObject(map)) errorOccurred = true;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Can't load furnaceRecipes.yml");
            e.printStackTrace();
            return false;
        }

        for(Map.Entry<String, FurnaceRecipe> entry: FurnaceRecipes.furnaceRecipeHashMap.entrySet()) {
            getServer().addRecipe(entry.getValue());
            logger.log(Level.INFO, "Successfully loaded FurnaceRecipe: "+entry.getKey());
        }

        return errorOccurred;
    }

    private static Boolean parseYamlObject(LinkedHashMap map){

        /* Experience */
        // Get experience as String
        String experienceToParse = map.get("experience").toString();

        // This option is obligatory
        if(experienceToParse.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"experience\" in furnaceRecipes.yml, skipping it.");
            return false;
        }

        // Try parse to Float
        Float experience;
        try
        {
            experience = Float.parseFloat(experienceToParse);
        }
        catch (NumberFormatException e)
        {
            debugMessage(PARTIAL, WARNING, "Error occurred while parsing value for \"experience="+experienceToParse+"\" in furnaceRecipes.yml, skipping it.");
            return false;
        }

        /* Cooking time */
        // Get cookingTime as String
        String cookingTimeToParse = map.get("cooking-time").toString();

        // This option is obligatory
        if(cookingTimeToParse.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"cooking-time\" in furnaceRecipes.yml, skipping it.");
            return false;
        }

        // Try parse to Integer
        Integer cookingTime;
        try
        {
            cookingTime = Integer.parseInt(cookingTimeToParse);
        }
        catch (NumberFormatException e)
        {
            debugMessage(PARTIAL, WARNING, "Error occurred while parsing value for \"cooking-time="+experienceToParse+"\" in furnaceRecipes.yml, skipping it.");
            return false;
        }

        /* Receipt for */
        // Get ItemStack name
        String result = map.get("receipt-for").toString();

        // This option is obligatory
        if(result.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"receipt-for\" in furnaceRecipes.yml, skipping it.");
            return false;
        }

        /* Source */
        // Try to get Source Material as String because debug message if material match would fail
        String materialName = map.get("source").toString();

        // This option is obligatory
        if(materialName.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"source\" in furnaceRecipes.yml, skipping it.");
            return false;
        }

        // Match material
        Material source;

        // Recipe
        FurnaceRecipe furnaceRecipe;

        // Get as Object
        Object materialOrItemStack = getMaterialOrItemStack(materialName);

        // Get type of ingredient
        if(materialOrItemStack instanceof Material){

            debugMessage(EXTREME, INFO, "Use of Material: "+materialName+" in furnaceRecipes.yml");

            // Assign Material
            source = (Material) materialOrItemStack;

            // Create Furnace Receipt
            furnaceRecipe = receiptFor(result, source, experience, cookingTime);

            // ItemStack not found
            if(furnaceRecipe==null){
                debugMessage(PARTIAL, WARNING, "ItemStack: "+result+" not found, skipping "+source.getKey().getKey());
                return false;
            }
        } else if(materialOrItemStack instanceof ItemStack){

            debugMessage(EXTREME, INFO, "Use of ItemStack: "+materialName+" in furnaceRecipes.yml");

            // Get Material from item stack
            ItemStack itemStack = (ItemStack) materialOrItemStack;

            // Assign Material
            source = itemStack.getType();

            // Create Furnace Receipt
            furnaceRecipe = receiptFor(result, source, experience, cookingTime);

            // ItemStack not found
            if(furnaceRecipe==null){
                debugMessage(PARTIAL, WARNING, "ItemStack: "+result+" not found, skipping "+source.getKey().getKey());
                return false;
            }

            // No check for already inserted keys because there can't be
            // Add this item stack key
            // Add information about item stack to group
            furnaceRecipe.setGroup(materialName);
        } else {
            // Not found
            debugMessage(PARTIAL, WARNING, "Found unknown material type: "+materialName+" in furnaceRecipes.yml, skipping it.");
            return false;
        }


        // Check already exists
        if(furnaceRecipeHashMap.get(source.getKey().getKey())!=null){
            debugMessage(PARTIAL, WARNING, "Found doubled FurnaceRecipe: "+materialName+", skipping it.");
            return false;
        }

        // Add to List
        furnaceRecipeHashMap.put(source.getKey().getKey(), furnaceRecipe);
        return true;
    }

    private static FurnaceRecipe receiptFor(String key, Material source, Float experience, Integer cookingTime){
        ItemStack itemStack = itemStackHashtable.get(key);
        if(itemStack==null) return null;

        NamespacedKey namespacedKey = new NamespacedKey(MainPlugin.getInst(), "FurnaceRecipe_"+(++index));

        return new FurnaceRecipe(namespacedKey, itemStack, source, experience, cookingTime);
    }
}
