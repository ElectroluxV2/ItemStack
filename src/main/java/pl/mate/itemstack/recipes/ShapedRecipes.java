package pl.mate.itemstack.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.yaml.snakeyaml.Yaml;
import pl.mate.itemstack.MainPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static java.util.logging.Level.*;
import static org.bukkit.Bukkit.getServer;
import static pl.mate.itemstack.recipes.ItemStacks.getMaterialOrItemStack;
import static pl.mate.itemstack.recipes.ItemStacks.itemStackHashtable;
import static pl.mate.itemstack.settings.FileManager.shapedRecipes;
import static pl.mate.itemstack.settings.Settings.DebugLevel.EXTREME;
import static pl.mate.itemstack.settings.Settings.DebugLevel.PARTIAL;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class ShapedRecipes {

    public static Map<String, ShapedRecipe> shapedRecipeMap = new HashMap<>();
    private static Integer index = 0;


    public static Boolean load(){

        Boolean errorOccurred = false;

        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(shapedRecipes)) {
            Iterable<Object> itr = yaml.loadAll(in);

            for (Object o : itr) {
                // Read file by objects
                LinkedHashMap map = (LinkedHashMap) o;
                // Parse object
                if(!parseYamlObject(map)) errorOccurred = true;
            }

        } catch (Exception e) {
            debugMessage(PARTIAL, SEVERE, "Can't load shapedRecipes.yml");
            e.printStackTrace();
            return false;
        }

        // Add to server recipes
        for(Map.Entry<String, ShapedRecipe> entry: ShapedRecipes.shapedRecipeMap.entrySet()) {
            getServer().addRecipe(entry.getValue());
            debugMessage(PARTIAL, INFO, "Successfully loaded ShapedRecipe: "+entry.getKey());
        }

        return errorOccurred;
    }

    private static Boolean parseYamlObject(LinkedHashMap map){

        /* Description */
        // Get description
        String description = map.get("description").toString();

        // This option is obligatory
        if(description.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"description\" in shapedRecipes.yml, skipping it.");
            return false;
        }

        /* Shape */
        // Get shape as List
        List<String> shape = new ArrayList<>();
        if(map.get("shape") instanceof List){
            List rawList = (List) map.get("shape");
            for(Object object : rawList) {
                if(object instanceof String){
                    shape.add((String) object);
                } else {
                    debugMessage(PARTIAL, WARNING, "Wrong value for \"shape\" ("+object+") in shapedRecipes.yml, skipping it.");
                    return false;
                }
            }
        } else {
            debugMessage(PARTIAL, WARNING, "Wrong value for \"shape\" in shapedRecipes.yml, skipping it.");
            return false;
        }

        /* Receipt for */
        // Get ItemStack name
        String result = map.get("receipt-for").toString();

        // This option is obligatory
        if(result.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"receipt-for\" in shapedRecipes.yml, skipping it.");
            return false;
        }

        // Check already exists
        if(shapedRecipeMap.get(description)!=null){
            debugMessage(PARTIAL, WARNING, "Found doubled ShapedRecipe: "+description+", skipping it.");
            return false;
        }

        // Create Shaped Recipe
        ShapedRecipe shapedRecipe = receiptFor(result);

        // ItemStack not found
        if(shapedRecipe==null){
            debugMessage(PARTIAL, SEVERE, "ItemStack: "+result+" not found, skipping "+description);
            return false;
        }

        // Assign shape
        shapedRecipe.shape(shape.toArray(new String[0]));

        /* Ingredients */
        // Get as map of List
        if(map.get("ingredients") instanceof List){
            List rawList = (List) map.get("ingredients");
            for(Object object : rawList) {
                // Array list [Character, MaterialName]
                if(object instanceof ArrayList){
                    ArrayList list = (ArrayList) object;
                    // Get Character and Material Name
                    if(list.size()!=2){
                        debugMessage(PARTIAL, WARNING, "Wrong array size in \"ingredients\" ("+ Arrays.toString(list.toArray()) +") in shapedRecipes.yml, skipping it.");
                        return false;
                    }

                    if(!(list.get(0) instanceof String)){
                        debugMessage(PARTIAL, WARNING, "Wrong type in array in \"ingredients\" ("+ list.get(0) +") in shapedRecipes.yml, skipping it.");
                        return false;
                    }

                    Character character =  ((String) list.get(0)).charAt(0);
                    String materialName = (String) list.get(1);

                    // Get as Object
                    Object materialOrItemStack = getMaterialOrItemStack(materialName);

                    // Check type
                    if(materialOrItemStack instanceof Material){

                        debugMessage(EXTREME, INFO, "Use of Material: "+materialName+" in shapedRecipes.yml");

                        // Assign Material
                        shapedRecipe.setIngredient(character, (Material) materialOrItemStack);
                    } else if(materialOrItemStack instanceof ItemStack){

                        debugMessage(EXTREME, INFO, "Use of ItemStack: "+materialName+" in shapedRecipes.yml");


                        // Get Material from item stack
                        ItemStack itemStack = (ItemStack) materialOrItemStack;

                        // Assign Material
                        shapedRecipe.setIngredient(character, itemStack.getType());

                        // Create array from name of item stacks
                        ArrayList<String> itemStackGroup = new ArrayList<>();

                        // Check for already inserted keys
                        if(!shapedRecipe.getGroup().isEmpty()){
                            // Deserialize
                            String[] list1 = shapedRecipe.getGroup().split(",");
                            // Add to array
                            itemStackGroup.addAll(Arrays.asList(list1));
                        }

                        // Add this item stack key
                        itemStackGroup.add(materialName);

                        // Add information about item stack to group
                        shapedRecipe.setGroup(String.join(",", itemStackGroup));
                    } else {
                        // Not found
                        debugMessage(PARTIAL, WARNING, "Found unknown material type: "+materialName+" in shapedRecipes.yml, skipping it.");
                        return false;
                    }
                }

            }
        } else {
            debugMessage(PARTIAL, WARNING, "Wrong value for \"ingredients\" in shapedRecipes.yml, must be array(2), skipping it.");
            return false;
        }

        // Add to List
        shapedRecipeMap.put(description, shapedRecipe);
        return true;
    }


    private static ShapedRecipe receiptFor(String key){

        ItemStack itemStack = itemStackHashtable.get(key);
        if(itemStack==null) return null;

        NamespacedKey namespacedKey = new NamespacedKey(MainPlugin.getInst(), "ShapedRecipe_"+(++index));

        return new ShapedRecipe(namespacedKey, itemStack);
    }
}