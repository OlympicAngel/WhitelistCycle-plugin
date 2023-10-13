package com.olympicangel.whitelistcycle;

import com.google.gson.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;


public final class main extends JavaPlugin {

    public static main ref;
    private Metrics metrics;
    public JsonObject json;
    private File configFile;

    @Override
    public void onEnable() {
        ref = this;
        getServer().setWhitelist(true);

        //load json config
        this.configFile = new File(getDataFolder(), "config.json");
        this.json = loadJSON(this.configFile).getAsJsonObject();

        //load commands
        Commands cmd = new Commands();
        getCommand("whitelistCycle").setExecutor(cmd);
        getCommand("whitelistCycle").setTabCompleter(cmd);

        //load bStat
        int pluginId = 20045;
        metrics = new Metrics(this, pluginId);

    }

    private JsonElement loadJSON(File json_file){
        String rawJSON;
        //create if none
        if (!json_file.exists()) {
            saveResource(json_file.getName(), false);
            rawJSON = "{lists:[]}";
        }else {
            //get file data
            rawJSON = WCUtils.readFile(json_file);
        }
        //convert to generic json element
        JsonParser parser = new JsonParser();
        return parser.parse(rawJSON);
    }

    private void saveJSON(JsonElement a_json, File json_file){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WCUtils.writeFile(json_file,gson.toJson(a_json));
    }

    public void saveJSON(){
        this.saveJSON(this.json,configFile);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.ref = null;
        this.metrics = null;
        this.json = null;
        this.configFile = null;
    }
}
