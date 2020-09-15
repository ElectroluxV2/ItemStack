package pl.mate.itemstack.recipes;

import org.bukkit.Material;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import static java.util.logging.Level.*;
import static pl.mate.itemstack.settings.FileManager.fuels;
import static pl.mate.itemstack.settings.Settings.DebugLevel.PARTIAL;
import static pl.mate.itemstack.settings.Settings.debugMessage;

public class Fuels {
    public static Hashtable<Material, Integer> burnable = new Hashtable<>();

    public static Boolean load(){

        Boolean errorOccurred = false;

        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(fuels)) {
            Iterable<Object> itr = yaml.loadAll(in);

            for (Object o : itr) {
                // Read file by objects
                LinkedHashMap map = (LinkedHashMap) o;
                // Parse object
                if(!parseYamlObject(map)) errorOccurred = true;
            }

        } catch (Exception e) {

            debugMessage(PARTIAL, SEVERE, "Can't load fuels.yml");
            e.printStackTrace();
            return false;
        }

        return errorOccurred;
    }

    private static Boolean parseYamlObject(LinkedHashMap map){

        /* Material */
        // Get name of material
        String materialName = map.get("material").toString();

        // This option is obligatory
        if(materialName.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"material\" in fuels.yml, skipping it.");
            return false;
        }

        // Match material
        Material material = Material.matchMaterial(materialName);

        // If match fail, output debug message
        if(material == null){
            debugMessage(PARTIAL, WARNING, "Found unknown material type: \"+materialName+\" in fuels.yml, skipping it.");

            return false;
        }

        /* burnTime */
        // As string to parse
        String burnTimeToParse = map.get("burn-time").toString();

        // This option is obligatory
        if(burnTimeToParse.isEmpty()){
            debugMessage(PARTIAL, WARNING, "Missing value for \"burn-time\" in fuels.yml, skipping it.");
            return false;
        }

        // Try to parse
        Integer burnTime;
        try
        {
            burnTime = Integer.parseInt(burnTimeToParse);
        }
        catch (Exception e)
        {
            debugMessage(PARTIAL, WARNING, "Error occurred while parsing value for \"burn-time="+burnTimeToParse+"\" in fuels.yml, skipping it.");
            return false;
        }

        if(burnable.get(material)!=null){
            debugMessage(PARTIAL, WARNING, "Found doubled fuel: "+material+" in fuels.yml, skipping it.");
        }

        // Add to list
        burnable.put(material, burnTime);
        debugMessage(PARTIAL, INFO, "Successfully loaded fuel: "+material);
        return true;
    }
}
