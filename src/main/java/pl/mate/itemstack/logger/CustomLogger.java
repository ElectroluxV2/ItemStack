package pl.mate.itemstack.logger;

import org.bukkit.plugin.Plugin;
import pl.mate.itemstack.settings.Settings;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.logging.Level.*;


/**
 * The PluginLogger class is a modified {@link Logger} that prepends all
 * logging calls with the name of the plugin doing the logging. The API for
 * PluginLogger is exactly the same as {@link Logger}.
 *
 * @see Logger
 */
public class CustomLogger extends Logger {

    
    /**
     * Creates a new PluginLogger that have colors
     *
     * @param context A reference to the plugin
     */
    public CustomLogger(Plugin context) {
        super(context.getClass().getCanonicalName(), null);
        setParent(context.getServer().getLogger());
        setLevel(ALL);
    }
    
    @Override
    public void log(LogRecord logRecord) {
        String prefix;
        Level l = logRecord.getLevel();
        
        if(!Settings.consoleColors){
            prefix = "[ItemStack] ";
            
        } else if(l.equals(SEVERE)){
            prefix = ConsoleColors.RED_BOLD_BRIGHT + "[" + ConsoleColors.RED_BOLD + "ItemStack" + ConsoleColors.RED_BOLD_BRIGHT + "] " + ConsoleColors.RED_UNDERLINED;
    
        } else if(l.equals(WARNING)){
            prefix = ConsoleColors.BLUE_BOLD_BRIGHT + "[" + ConsoleColors.YELLOW_BOLD + "ItemStack" + ConsoleColors.BLUE_BOLD_BRIGHT + "] " + ConsoleColors.YELLOW_BOLD_BRIGHT;
    
        } else { // INFO
            prefix = ConsoleColors.BLUE_BOLD_BRIGHT + "[" + ConsoleColors.GREEN_BOLD_BRIGHT + "ItemStack" + ConsoleColors.BLUE_BOLD_BRIGHT + "] " + ConsoleColors.BLUE_BOLD_BRIGHT;
    
        }
        
        String msg = prefix + logRecord.getMessage() + ConsoleColors.RESET;
        
        logRecord.setMessage(msg);
        super.log(logRecord);
    }
    
}