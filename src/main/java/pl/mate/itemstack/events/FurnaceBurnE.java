package pl.mate.itemstack.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import java.util.Map;

import static java.util.logging.Level.INFO;
import static pl.mate.itemstack.recipes.Fuels.burnable;
import static pl.mate.itemstack.settings.Settings.DebugLevel.EXTREME;
import static pl.mate.itemstack.settings.Settings.debugMessage;


public class FurnaceBurnE implements Listener {

    @EventHandler
    public void onFurnaceBurnEvent(FurnaceBurnEvent e) {

        for(Map.Entry<Material, Integer> entry: burnable.entrySet()) {
            if(e.getFuel().getType().equals(entry.getKey())){
                debugMessage(EXTREME, INFO, "Material " + e.getFuel().getType().toString() + " is fuel, set burning time for " + entry.getValue() + " ticks.");
                e.setBurnTime(entry.getValue());
                e.setBurning(true);
                return;
            }
        }

        debugMessage(EXTREME, INFO, "Material " + e.getFuel().getType().toString() + " is not fuel.");
    }
}
