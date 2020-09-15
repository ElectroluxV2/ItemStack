package pl.mate.itemstack.events;


import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Hashtable;
import java.util.Map;

import static java.util.logging.Level.*;
import static pl.mate.itemstack.recipes.ItemStacks.itemStackHashtable;
import static pl.mate.itemstack.settings.Settings.DebugLevel.EXTREME;
import static pl.mate.itemstack.settings.Settings.DebugLevel.FULL;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class PrepareItemCraftE implements Listener {

    @EventHandler
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent e) {

        if (e.getInventory().getRecipe() == null) return;

        // Need to get Item Stacks to check from Recipe
        String recipeGroup;

        // Per type get
        if (e.getRecipe() instanceof ShapelessRecipe) {
            // Safe cast
            ShapelessRecipe recipe = (ShapelessRecipe) e.getRecipe();
            // Get group
            recipeGroup = recipe.getGroup();

        } else if (e.getRecipe() instanceof ShapedRecipe) {
            // Safe cast
            ShapedRecipe recipe = (ShapedRecipe) e.getRecipe();
            // Get group
            recipeGroup = recipe.getGroup();

        } else if (e.getRecipe() instanceof FurnaceRecipe) {
            // Safe cast
            FurnaceRecipe recipe = (FurnaceRecipe) e.getRecipe();
            // Get group
            recipeGroup = recipe.getGroup();

        } else {
            debugMessage(EXTREME, INFO, "Not supported recipe type:" + e.getRecipe().getClass().getName());
            // Not supported
            return;
        }

        // Check for item stacks keys
        if (recipeGroup.isEmpty()) {
            debugMessage(EXTREME, INFO, "Skipping empty recipe group.");
            return;
        }

        // Convert to array
        // Deserialize
        String[] keys = recipeGroup.split(",");

        // Create hashtable that will notify if item stacks have successfully valid tests
        Hashtable<ItemStack, Boolean> checkResults = new Hashtable<>();

        // Have to check if this keys are customized item stacks from config
        Boolean foundCustomItemStack = false;
        for (String key : keys) {
            // Get item stack
            ItemStack itemStack = itemStackHashtable.get(key);
            // If its simple material it won't be tested
            if(itemStack==null) continue;
            // Add item stack to results hashtable
            checkResults.put(itemStack, false);
            // Found materials to check
            foundCustomItemStack = true;
            debugMessage(FULL, INFO, "Checks for: "+key);
        }

        // Won't check simple materials
        if(!foundCustomItemStack){
            debugMessage(EXTREME, INFO, "Skipping simple material from check.");
            return;
        }

        // List trough crafting items to prevent gain of item when only material match
        ItemStack[] inCraftingStacks = e.getInventory().getContents();

        // List items
        for(ItemStack itemStack : inCraftingStacks){

            // List all to checks
            for(String key : keys){
                // Cannot be null
                ItemStack validOne = itemStackHashtable.get(key);
                // Check Material
                if(itemStack.getType().equals(validOne.getType())){

                    debugMessage(EXTREME, SEVERE, itemStack.getItemMeta().toString());
                    debugMessage(EXTREME, SEVERE, validOne.getItemMeta().toString());

                    // Now full check
                    if(itemStack.getItemMeta().equals(validOne.getItemMeta())){
                        // Set result for this item stack as true
                        checkResults.put(validOne, true);
                    }
                }
            }
        }

        // Validate results
        for(Map.Entry<ItemStack, Boolean> testResult: checkResults.entrySet()) {
            if(testResult.getValue()){
                debugMessage(FULL, INFO, testResult.getKey().toString() + " test successfully.");
            } else {
                // If anyone of test is false cancel gain of item
                debugMessage(FULL, WARNING, "Gain of " + e.getRecipe().getResult().toString() + " canceled due to " + testResult.getKey().toString() + " test failed");
                // Cancel gain
                ItemStack air = new ItemStack(Material.AIR);
                e.getInventory().setResult(air);
            }
        }
    }
}
