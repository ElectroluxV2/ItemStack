package pl.mate.itemstack.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.yaml.snakeyaml.Yaml;
import pl.mate.itemstack.MainPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static java.util.logging.Level.*;
import static org.bukkit.Bukkit.getServer;
import static pl.mate.itemstack.recipes.ItemStacks.getMaterialOrItemStack;
import static pl.mate.itemstack.recipes.ItemStacks.itemStackHashtable;
import static pl.mate.itemstack.settings.FileManager.shapelessRecipes;
import static pl.mate.itemstack.settings.Settings.DebugLevel.EXTREME;
import static pl.mate.itemstack.settings.Settings.DebugLevel.PARTIAL;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class ShapelessRecipes {

    public static Map<String, ShapelessRecipe> shapelessRecipesHashMap = new HashMap<>();
    private static Integer index = 0;

    public static Boolean load(){

        Boolean errorOccurred = false;

        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(shapelessRecipes)) {
            Iterable<Object> itr = yaml.loadAll(in);

            for (Object o : itr) {
                // Read file by objects
                LinkedHashMap map = (LinkedHashMap) o;
                // Parse object
                if(!parseYamlObject(map)) errorOccurred = true;
            }

        } catch (Exception e) {
            debugMessage(PARTIAL, SEVERE, "Can't load shapeLessRecipes.yml");
            e.printStackTrace();
            return false;
        }

        for(Map.Entry<String, ShapelessRecipe> entry: ShapelessRecipes.shapelessRecipesHashMap.entrySet()) {
            getServer().addRecipe(entry.getValue());
            debugMessage(PARTIAL, INFO, "Successfully loaded ShapelessRecipe: "+entry.getKey());
        }

        return errorOccurred;
    }

    private static Boolean parseYamlObject(LinkedHashMap map){
        /* Description */
        // Get description
        String description = map.get("description").toString();

        // This option is obligatory
        if(description.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"description\" in shapelessRecipes.yml, skipping it.");
            return false;
        }

        /* Receipt for */
        // Get ItemStack name
        String result = map.get("receipt-for").toString();

        // This option is obligatory
        if(result.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"receipt-for\" in shapelessRecipes.yml, skipping it.");
            return false;
        }

        // Create shapeless Recipe
        ShapelessRecipe shapelessRecipe = receiptFor(result);

        if(shapelessRecipe==null){
            debugMessage(PARTIAL, SEVERE, "ItemStack: "+result+" not found in shapelessRecipes.yml, skipping "+description);
            return false;
        }

        /* Ingredients */
        // Get as List
        if(map.get("ingredients") instanceof List){
            List rawList = (List) map.get("ingredients");

            // This cannot be empty
            if(rawList.size()==0){
                debugMessage(PARTIAL, WARNING, "Missing value for \"ingredients\" in shapelessRecipes.yml, skipping it.");
                return false;
            }

            for(Object ingredient : rawList){
                if(ingredient instanceof String){

                    // Get as Object
                    Object materialOrItemStack = getMaterialOrItemStack((String) ingredient);

                    // Get type of ingredient
                    if(materialOrItemStack instanceof Material){

                        debugMessage(EXTREME, INFO, "Use of Material: "+ingredient+" in shapelessRecipes.yml");

                        // Assign Material
                        shapelessRecipe.addIngredient((Material) materialOrItemStack);
                    } else if(materialOrItemStack instanceof ItemStack){

                        debugMessage(EXTREME, INFO, "Use of ItemStack: "+ingredient+" in shapelessRecipes.yml");

                        // Get Material from item stack
                        ItemStack itemStack = (ItemStack) materialOrItemStack;

                        // Assign Material
                        shapelessRecipe.addIngredient(itemStack.getType());

                        // Create array from name of item stacks
                        ArrayList<String> itemStackGroup = new ArrayList<>();

                        // Check for already inserted keys
                        if(!shapelessRecipe.getGroup().isEmpty()){
                            // Deserialize
                            String[] list = shapelessRecipe.getGroup().split(",");
                            // Add to array
                            itemStackGroup.addAll(Arrays.asList(list));
                        }

                        // Add this item stack key
                        itemStackGroup.add((String) ingredient);

                        // Add information about item stack to group
                        shapelessRecipe.setGroup(String.join(",", itemStackGroup));
                    } else {
                        // Not found
                        debugMessage(PARTIAL, WARNING, "Found unknown material type: "+ingredient+", skipping it.");
                        // Try next
                    }
                } else {
                    debugMessage(PARTIAL, WARNING, "Wrong value for \"ingredients\" ("+ingredient+") in shapelessRecipes.yml, skipping it.");
                    return false;
                }
            }

        } else {
            debugMessage(PARTIAL, WARNING, "Wrong value for \"ingredients\" in shapelessRecipes.yml, skipping it.");
            return false;
        }

        if(shapelessRecipesHashMap.get(description)!=null){
            debugMessage(PARTIAL, WARNING, "Found doubled ShapelessRecipe: " + description + ", skipping it.");
            return false;
        }

        shapelessRecipesHashMap.put(description, shapelessRecipe);
        return true;
    }

    private static ShapelessRecipe receiptFor(String key){

        ItemStack itemStack = itemStackHashtable.get(key);
        if(itemStack==null) return null;

        NamespacedKey namespacedKey = new NamespacedKey(MainPlugin.getInst(), "ShapelessRecipe_"+(++index));
        return new ShapelessRecipe(namespacedKey, itemStack);
    }
}
