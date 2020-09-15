package pl.mate.itemstack.recipes;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;
import pl.mate.itemstack.MainPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static pl.mate.itemstack.settings.FileManager.itemStacks;
import static pl.mate.itemstack.settings.Settings.DebugLevel.PARTIAL;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class ItemStacks {

    public static Hashtable<String, ItemStack> itemStackHashtable = new Hashtable<>();

    public static Boolean load(){

        Boolean errorOccurred = false;

        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(itemStacks)) {
            Iterable<Object> itr = yaml.loadAll(in);

            for (Object o : itr) {
                // Read file by objects
                LinkedHashMap map = (LinkedHashMap) o;
                // Parse object
                if(!parseYamlObject(map)) errorOccurred = true;
            }

        } catch (Exception e) {
            MainPlugin.logger.log(Level.SEVERE, "Can't load itemStacks.yml");
            e.printStackTrace();
            return false;
        }

        return errorOccurred;
    }

    private static Boolean parseYamlObject(LinkedHashMap map) {

        /* Name */
        // Get Name of item stack
        String name = map.get("name").toString();

        /* Material */
        // Get name of material
        String materialName = map.get("material").toString();

        // This option is obligatory
        if(materialName.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"material\" in itemStacks.yml, skipping it.");
            return false;
        }

        // Match material
        Material material = Material.matchMaterial(materialName);

        // If match fail, output debug message
        if(material == null){
            debugMessage(PARTIAL, WARNING, "Found unknown material type: "+materialName+" in itemStacks.yml, skipping it.");
            return false;
        }

        /* Amount */
        // As string to parse
        String amountToParse = map.get("amount").toString();

        // This option is obligatory
        if(amountToParse.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"amount\" in itemStacks.yml, skipping it.");
            return false;
        }

        // Try to parse
        Integer amount;
        try
        {
            amount = Integer.parseInt(amountToParse);
        }
        catch (Exception e)
        {
            debugMessage(PARTIAL, WARNING, "Error occurred while parsing value for \"amount="+amountToParse+"\" in itemStacks.yml, skipping it.");
            return false;
        }

        // Create ItemStack
        MyItemStack itemStack = new MyItemStack(material, amount);

        /* DisplayName */
        // DisplayName is not obligatory
        if(map.get("display-name")!=null){
            // As raw string
            String displayName = map.get("display-name").toString();

            // Cannon't be empty
            if(displayName.isEmpty()){
                debugMessage(PARTIAL, WARNING, "Missing value for \"display-name\" in itemStacks.yml, skipping it. If You don't wont to use display name please delete whole line.");
                return false;
            }

            // Make colorful
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);

            // Assign
            itemStack.setDisplayName(displayName);

        }

        /* Lore */
        // Get as List[String]
        List<String> lore = new ArrayList<>();

        // Lore is not obligatory
        if(map.get("lore")!=null){
            // Load raw lore
            if(map.get("lore") instanceof List){
                List list = (List) map.get("lore");
                // Can be empty
                if(list.size()>0){
                    for(Object line : list){
                        if(line instanceof String){
                            lore.add(ChatColor.translateAlternateColorCodes('&', (String) line));
                        } else {
                            debugMessage(PARTIAL, WARNING, "Wrong value for \"lore\" ("+line+") in itemStacks.yml, skipping it. If You don't wont to use lore please delete whole line.");
                            return false;
                        }
                    }
                    // Assign
                    itemStack.setLore(lore);
                } else {
                    debugMessage(PARTIAL, WARNING, "Wrong value for \"lore\" in itemStacks.yml, skipping it. If You don't wont to use lore please delete whole line.");
                    return false;
                }
            } else {
                debugMessage(PARTIAL, WARNING, "Wrong value for \"lore\" in itemStacks.yml, skipping it. If You don't wont to use lore please delete whole line.");
                return false;
            }
        }

        /* Durability */
        // Durability is not obligatory
        if(map.get("durability")!=null){
            // As string to parse
            String durabilityToParse = map.get("durability").toString();

            // Cannon't be empty
            if(durabilityToParse.isEmpty()){
                debugMessage(PARTIAL, WARNING, "Missing value for \"durability\" in itemStacks.yml, skipping it. If You don't wont to use durability please delete whole line.");
                return false;
            }

            // Try to parse
            Integer durability;
            try
            {
                durability = Integer.parseInt(durabilityToParse);
            }
            catch (Exception e)
            {
                debugMessage(PARTIAL, WARNING, "Error occurred while parsing value for \"durability="+durabilityToParse+"\" in itemStacks.yml, skipping it. If You don't wont to use durability please delete whole line.");
                return false;
            }

            // Assign
            itemStack.setDurability(durability);
        }

        /* Unbreakable */
        // Unbreakable is not obligatory
        if(map.get("unbreakable")!=null){
            // As string to parse
            String durabilityToParse = map.get("unbreakable").toString();

            // Cannon't be empty
            if(durabilityToParse.isEmpty()){
                debugMessage(PARTIAL, WARNING, "Missing value for \"unbreakable\" in itemStacks.yml, skipping it. If You don't wont to use unbreakable please delete whole line.");
                return false;
            }

            // Try to parse
            Boolean unbreakable;
            try
            {
                unbreakable = Boolean.getBoolean(durabilityToParse);
            }
            catch (Exception e)
            {
                debugMessage(PARTIAL, WARNING, "Error occurred while parsing value for \"unbreakable="+durabilityToParse+"\" in itemStacks.yml, skipping it. If You don't wont to use unbreakable please delete whole line.");
                return false;
            }

            // Assign
            itemStack.setUnbreakable(unbreakable);
        }

        /* Enchantments */
        // Enchantments are not obligatory
        if(map.get("enchantments")!=null){
            // As LinkedHashMap[Enchantment, Level]
            LinkedHashMap<Enchantment, Integer> enchantments = new LinkedHashMap<>();

            if(map.get("enchantments") instanceof List){
                List enchantmentsList = (List) map.get("enchantments");
                for(Object element : enchantmentsList){
                    if(element instanceof ArrayList){

                        // Get Single name of Enchantment and it's level
                        List enchantmentList = (List) element;
                        if(enchantmentList.size()!=2){
                            debugMessage(PARTIAL, WARNING, "Wrong array size in \"enchantments\" ("+ Arrays.toString(enchantmentList.toArray()) +") in itemStacks.yml, skipping it. If You don't wont to use enchantments please delete whole line.");
                            return false;
                        }

                        if(!(enchantmentList.get(0) instanceof String)){
                            debugMessage(PARTIAL, WARNING, "Wrong value for \"enchantments\" ("+enchantmentList.get(0)+") in itemStacks.yml, skipping it. If You don't wont to use enchantments please delete whole line.");
                            return false;
                        }

                        if(!(enchantmentList.get(1) instanceof Integer)){
                            debugMessage(PARTIAL, WARNING, "Wrong value for \"enchantments\" ("+enchantmentList.get(1)+") in itemStacks.yml, skipping it. If You don't wont to use enchantments please delete whole line.");
                            return false;
                        }

                        String enchantmentName = (String) enchantmentList.get(0);

                        Integer enchantmentLevel = (Integer) enchantmentList.get(1);

                        // Get vanilla style name
                        NamespacedKey namespacedKey;
                        try
                        {
                            namespacedKey = NamespacedKey.minecraft(enchantmentName);
                        } catch (IllegalArgumentException e)
                        {
                            debugMessage(PARTIAL, WARNING, "Wrong enchantment name ("+enchantmentName+") in itemStacks.yml, skipping it.");
                            return false;
                        }


                        // Get enchantment
                        Enchantment enchantment = Enchantment.getByKey(namespacedKey);

                        if(enchantment==null){
                            debugMessage(PARTIAL, WARNING, "Can't match enchantment name: "+enchantmentName+", skipping it.");
                            continue;
                        }

                        // Add to array
                        enchantments.put(enchantment, enchantmentLevel);

                    } else {
                        debugMessage(PARTIAL, WARNING, "Wrong value for \"enchantments\" ("+element+") in itemStacks.yml, skipping it. If You don't wont to use enchantments please delete whole line.");
                        return false;
                    }
                }

                // Assign
                itemStack.addEnchantments(enchantments);
            } else {
                debugMessage(PARTIAL, WARNING, "Wrong value for \"enchantments\" in itemStacks.yml, skipping it. If You don't wont to use enchantments please delete whole line.");
                return false;
            }
        }

        // Add
        if(itemStackHashtable.get(name)!=null){
            debugMessage(PARTIAL, WARNING, "Found doubled ItemStack: "+name+", skipping it.");
            return false;
        }

        itemStackHashtable.put(name, itemStack);
        debugMessage(PARTIAL, INFO, "Successfully loaded ItemStack: "+name);
        return true;
    }

    static Object getMaterialOrItemStack(String name){

        // Match Material
        Material material = Material.matchMaterial(name);
        if(material!=null){
            return material;
        }

        // Now search in ItemStacks if not found it will return null
        return itemStackHashtable.get(name);
    }

    public static class MyItemStack extends ItemStack {


        MyItemStack(Material material, int amount){
            setType(material);
            setAmount(amount);
            setDurability((short)1);
        }

        void setLore(List<String> lore){
            ItemMeta itemMeta = getItemMeta();
            itemMeta.setLore(lore);
            setItemMeta(itemMeta);
        }

        public void addEnchantment(Enchantment enchantment, int level){
            addUnsafeEnchantment(enchantment, level);
        }

        void setDurability(int durability){
            setDurability((short)durability);
        }

        void setUnbreakable(Boolean unbreakable){
            ItemMeta itemMeta = getItemMeta();
            itemMeta.setUnbreakable(unbreakable);
            setItemMeta(itemMeta);
        }

        void setDisplayName(String displayName){
            ItemMeta itemMeta = getItemMeta();
            itemMeta.setDisplayName(displayName);
            setItemMeta(itemMeta);
        }

    }
}
