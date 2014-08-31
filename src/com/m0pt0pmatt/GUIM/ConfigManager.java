package com.m0pt0pmatt.GUIM;

/*
* Copyright (C) 2012
*
* Permission is hereby granted, free of charge, to any person obtaining a copy 
* of this software and associated documentation files (the "Software"), to deal 
* in the Software without restriction, including without limitation the rights 
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
* copies of the Software, and to permit persons to whom the Software is 
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE 
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
* SOFTWARE.
*/

/*
 * Original class, ConfigAccessor, has been renamed and modified
 */
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Acts as an intrface for saving to and reading from YAML configuration files
 * @author Matthew
 *
 */
public class ConfigManager {

	/**
	 * Config filename
	 */
    private final String fileName;
    
    /**
     * Plugin. Required
     */
    private final JavaPlugin plugin;
    
    /**
     * Actual file
     */
    private File configFile;
    
    /**
     * Configuration
     */
    private FileConfiguration fileConfiguration;

    /**
     * Defualt constructor
     * @param plugin
     * @param fileName
     */
    public ConfigManager(JavaPlugin plugin, String fileName) {
        
        //set the plugin and the filename
        this.plugin = plugin;
        this.fileName = fileName;
    }

    /**
     * reloads the configuration from file
     */
    public void reloadConfig() {
    	
    	//if the file hasn't already been loaded, get it
        if (configFile == null) {
            File dataFolder = plugin.getDataFolder();
            if (dataFolder == null)
                throw new IllegalStateException();
            configFile = new File(dataFolder, fileName);
        }
        
        //get the configuration
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

    }

    /**
     * returns the FileConfiguration of the config file
     * @return the FileConfiguration of the config file
     */
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }
        return fileConfiguration;
    }

    /**
     * Saves the config to file
     */
    public void saveConfig() {
        if (fileConfiguration == null || configFile == null) {
            return;
        } else {
            try {
                getConfig().save(configFile);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
            }
        }
    }
    
    public void saveDefaultConfig() {
        if (!configFile.exists()) {            
            this.plugin.saveResource(fileName, false);
        }
    }
    
    public File getFile(){
    	return configFile;
    }

}