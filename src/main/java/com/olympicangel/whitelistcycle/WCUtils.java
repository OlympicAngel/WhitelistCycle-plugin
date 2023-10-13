package com.olympicangel.whitelistcycle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.util.ArrayList;

public final class WCUtils {
    public static String readFile(File file){
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    public static void writeFile(File file,String data){
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(data);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<String> getAllListsName(){
        ArrayList<String> names = new ArrayList();

        //get lists array (list of objects)
        JsonArray lists = main.ref.json.getAsJsonArray("lists");

        if(lists == null)
            return names;

        //for each element(raw object) in array
        for (JsonElement rawList :lists) {
            //convert to object
            JsonObject aList = rawList.getAsJsonObject();
            String listName = aList.get("name").getAsString();
            //add to names list
            names.add(listName);
        }
        return names;
    }

    public static String jsonGetString(String key){
        return jsonGetString(main.ref.json,key);
    }

    public static String jsonGetString(JsonObject json,String key){
        JsonElement value = json.get(key);
        if(value == null)
            return "";
        return value.getAsString();
    }

    public static void sendMessage(CommandSender p, String data){
        p.sendMessage(ChatColor.RESET + "[Whitelist"+ChatColor.YELLOW+"Cycle"+ChatColor.RESET+"]: " + data);
    }

    public static Boolean isPlayerInList(JsonObject list, JsonObject playerRef){
            JsonArray players = list.getAsJsonArray("players");
            if (players.contains(playerRef)) {
               return true;
            }
            return  false;
    }

    public static Boolean isPlayerInList(JsonObject list, String playerName){
        JsonArray players = list.getAsJsonArray("players");
        for (JsonElement player:players) {
            String cName = jsonGetString(player.getAsJsonObject(),"name");
            if(cName.equals(playerName))
                return true;
        }
        return  false;
    }

    public static JsonObject getList(String listName){
        JsonArray lists = main.ref.json.getAsJsonArray("lists");
        for (JsonElement aRawList:lists) {
            JsonObject listObject = aRawList.getAsJsonObject();
            String lName = WCUtils.jsonGetString(listObject,"name");
            if(lName.equals(listName)){
                return listObject;
            }
        }
        return null;
    }

    public static ArrayList<String> getAllListedPlayers(){
        JsonArray playerList = new JsonArray();
        JsonArray lists = main.ref.json.getAsJsonArray("lists");
        for (JsonElement aRawList:lists) {
            JsonObject listObject = aRawList.getAsJsonObject();
            JsonArray listPlayers = listObject.getAsJsonArray("players");
            playerList.addAll(listPlayers);
        }

        ArrayList<String> playerNames = new ArrayList<>();
        for (JsonElement p:playerList) {
            playerNames.add(jsonGetString(p.getAsJsonObject(),"name"));
        }

        return playerNames;
    }
}
