package pl.mate.itemstack.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import pl.mate.itemstack.recipes.*;
import pl.mate.itemstack.settings.Settings;

import java.util.ArrayList;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getServer;
import static pl.mate.itemstack.MainPlugin.logger;

public class Reload extends BukkitCommand {

    public Reload() {
        super("isr");
        this.description = "Reloads whole plugin";
        this.usageMessage = "/isr";
        this.setPermission("isr");
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("isreload");
        this.setAliases(aliases);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(!sender.hasPermission(this.getPermission())) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return false;
        }

        logger.log(Level.INFO, "Reloading ItemStack");
        if(sender instanceof Player) sender.sendMessage(ChatColor.GREEN + "Reloading ItemStack");

        getServer().resetRecipes();

        ItemStacks.itemStackHashtable.clear();
        ShapedRecipes.shapedRecipeMap.clear();
        ShapelessRecipes.shapelessRecipesHashMap.clear();
        FurnaceRecipes.furnaceRecipeHashMap.clear();
        Fuels.burnable.clear();

        Settings.load();

        Boolean errorOccurred = false;

        // Burnable
        if(Settings.moreFuelsEnable){
            if(!Fuels.load()) errorOccurred = true;
        }

        // Load ItemStacks
        if(!ItemStacks.load()) errorOccurred = true;

        // Recipes
        if(Settings.shapedRecipesEnable){
            if(!ShapedRecipes.load()) errorOccurred = true;
        }

        if(Settings.shapelessRecipesEnable){
            if(!ShapelessRecipes.load()) errorOccurred = true;
        }

        if(Settings.furnaceRecipesEnable){
            if(!FurnaceRecipes.load()) errorOccurred = true;
        }

        if(errorOccurred){
            if(sender instanceof Player) sender.sendMessage(ChatColor.RED + "An error occurred while reloading ItemStack. Check console for more information.");
        } else {
            if(sender instanceof Player) sender.sendMessage(ChatColor.GREEN + "Reloading of ItemStack finished without warnings.");
        }

        logger.log(Level.INFO, "Reload finished");
        return errorOccurred;
    }
}
