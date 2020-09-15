package pl.mate.itemstack.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import static java.util.logging.Level.INFO;
import static pl.mate.itemstack.settings.Settings.DebugLevel.EXTREME;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class InventoryClickE implements Listener {

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        ItemStack cursorItem = e.getCursor();
        ItemStack clickedItem = e.getCurrentItem();
        if (e.getInventory().getType() == InventoryType.FURNACE && e.getClickedInventory() != null && e.getSlot() == 1 && cursorItem != null) {
            if (e.getClickedInventory().equals(e.getWhoClicked().getOpenInventory().getTopInventory()) && cursorItem.getType() != Material.AIR) {
                if (clickedItem != null) {
                    if (clickedItem.getType() == Material.AIR) {
                        e.getWhoClicked().setItemOnCursor(clickedItem);
                        debugMessage(EXTREME, INFO, "Set " + clickedItem.getType().toString() + " at cursor.");
                    }
                }
                e.getClickedInventory().setItem(1, cursorItem);
                debugMessage(EXTREME, INFO, "Set " + cursorItem.getType().toString() + " at 1 position in furnace.");

            }
        }
    }
}
