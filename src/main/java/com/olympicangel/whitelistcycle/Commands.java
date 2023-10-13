package com.olympicangel.whitelistcycle;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Utility;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.w3c.dom.css.ViewCSS;

import java.io.File;
import java.util.*;

public class Commands implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender p, Command command, String label, String[] args) {
        if(args.length == 0){
            WCUtils.sendMessage(p,ChatColor.RED + "Usage: /whitelistCycle <action> <name> <group>");
            return true;
        }

        String[] fullArgs = new String[3];
        for (int i = 0; i < args.length; i++)
            fullArgs[i] = args[i];


        switch (fullArgs[0]){
            case "addList":
                 addList(p,fullArgs[1]);
                 break;
            case "removeList":
                 removeList(p,fullArgs[1]);
                break;
            case "addPlayer":
                 addPlayer(p,fullArgs[1],fullArgs[2]);
                break;
            case "removePlayer":
                 removePlayer(p,fullArgs[1],fullArgs[2]);
                break;
            case "currentList":
                 currentList(p,fullArgs[1]);
                break;
            default:
                WCUtils.sendMessage(p,ChatColor.RED + "Usage: /whitelistCycle <action> <name> <group>");
                break;
        }
        return true ;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Provide tab-completion for the first argument.
            completions.add("addList");
            completions.add("removeList");
            completions.add("addPlayer");
            completions.add("removePlayer");
            completions.add("currentList");
        } else if (args.length == 2) {
            if(args[0].equals("removePlayer")){
                completions.addAll(WCUtils.getAllListedPlayers());
            }

            if(args[0].equals("currentList") || args[0].equals("removeList"))
                completions.addAll(WCUtils.getAllListsName());

        } else if (args.length == 3) {
            if( args[0].equals("addPlayer"))
                completions.addAll(WCUtils.getAllListsName());

            if(args[0].equals("removePlayer")){
                JsonArray lists = main.ref.json.getAsJsonArray("lists");
                for (JsonElement aRawList : lists) {
                    JsonObject aList = aRawList.getAsJsonObject();
                    Boolean isInList = WCUtils.isPlayerInList(aList,args[1]);
                    if(isInList)
                        completions.add(WCUtils.jsonGetString(aList,"name"));
                }
            }
        }

        return completions;
    }

    private boolean addList(CommandSender p, String listName){
        if(listName == null || listName.length() == 0){
            WCUtils.sendMessage(p,ChatColor.RED + "Usage: /whitelistCycle addList <list name>");
            return false;
        }

        //check if the name already exists
        ArrayList<String> allListsNames = WCUtils.getAllListsName();
        boolean exists = allListsNames.contains(listName);;
        //if it does - exit
        if(exists){
            WCUtils.sendMessage(p,ChatColor.RED + "That list name already in use! please choose other name.");
            return false;
        }

        //add to json
        JsonObject newList = new JsonObject();
        newList.addProperty("name",listName);
        newList.add("players",new JsonArray());
        main.ref.json.getAsJsonArray("lists").add(newList);

        //user info
        WCUtils.sendMessage(p,ChatColor.GREEN + "Added new whitelist named: " + listName);


        //if there is no other list - set this as active
        if(allListsNames.size() == 0)
            this.currentList(p,listName);
        else
            main.ref.saveJSON();
        return true;
    }
    private boolean removeList(CommandSender p, String listName) {
        if(listName == null || listName.length() == 0){
            WCUtils.sendMessage(p,ChatColor.RED + "Usage: /whitelistCycle removeList <list name>");
            return false;
        }

        //check if the name already exists
        boolean exists = WCUtils.getAllListsName().contains(listName);
        //if doesnt exists output error message.
        if(!exists){
            WCUtils.sendMessage(p,ChatColor.RED + "That whitelist name does not exist!");
            return false;
        }

        //remove list
        JsonArray lists = main.ref.json.getAsJsonArray("lists");
        for (JsonElement aRawList:lists) {
            JsonObject listObject = aRawList.getAsJsonObject();
            String lName = WCUtils.jsonGetString(listObject,"name");
            if(lName.equals(listName)){
                lists.remove(aRawList);
                break;
            }
        }
        WCUtils.sendMessage(p,ChatColor.GREEN + "Removed players & whitelist itself from: " + listName);

        //if the current whitlist is THIS one to remove
        if(main.ref.json.get("currentList") != null &&
                WCUtils.jsonGetString("currentList").equals(listName)) {
            WCUtils.sendMessage(p,ChatColor.DARK_RED + "NOTICE! you removed the current whitelist(" + listName + ")! so currently the whitelist is zeroed and no one an join!\n"
                    + "To set new whitelist as active please use: /whitelistCycle currentList <list name>");
            writeWhitelistJSON(null); //writes empty json as whitelist
            main.ref.json.add("currentList", null);
        }

        //save changes made to json
        main.ref.saveJSON();
        return true;
    }

    private boolean addPlayer(CommandSender p, String playerName, String listName){
        if(playerName == null || playerName.length() < 1 || listName == null || listName.length() < 1){
            WCUtils.sendMessage(p,ChatColor.RED + "Usage: /whitelistCycle addPlayer <player name> <list name>");
            return false;
        }

        if(!WCUtils.getAllListsName().contains(listName)){
            WCUtils.sendMessage(p,ChatColor.RED + "Failed to add player as the whitelist '"+listName+"' does not exists.");
            return false;
        }

        //create json player
        JsonObject newPlayer = new JsonObject();
        newPlayer.addProperty("name",playerName);
        newPlayer.addProperty("uuid",Bukkit.getOfflinePlayer(playerName).getUniqueId().toString() );

        JsonObject listRef = null;
        JsonArray lists = main.ref.json.getAsJsonArray("lists");
        for (JsonElement aRawList:lists) {
            JsonObject listObject = aRawList.getAsJsonObject();
            //get name
            String lName = WCUtils.jsonGetString(listObject,"name");
            //if the current list name is the one to add the player to
            if(lName.equals(listName)) {
                listRef = listObject;
                //if player in the team already
                if(WCUtils.isPlayerInList(listObject,newPlayer))
                {
                    WCUtils.sendMessage(p,ChatColor.RED + "The player '" + playerName + "' is already in whitelist: " + listName);
                    return false;
                }

                //add the player to the list
                listObject.getAsJsonArray("players").add(newPlayer);
                WCUtils.sendMessage(p,"Adding '" + playerName + "' to whitelist: " + listName);
            }
            else{ //if its OTHER list
                //if player in other list
                if(WCUtils.isPlayerInList(listObject,newPlayer)){
                    WCUtils.sendMessage(p,ChatColor.YELLOW + "NOTICE: '" + playerName + "' is also in whitelist: " + listName
                            + ". You might consider removing her/him using the command:\n"
                            + ChatColor.GRAY + "/whitelistCycle removePlayer "+playerName+" " + lName);

                }
            }
        }

        //if added to current list - update Whitelist file
        if(main.ref.json.get("currentList") != null &&
                WCUtils.jsonGetString("currentList").equals(listName))
            writeWhitelistJSON(listRef);

        //save
        main.ref.saveJSON();

        return true;
    }

    private boolean removePlayer(CommandSender p, String playerName, String listName){
        if(playerName ==null || playerName.length() < 1 || listName == null || listName.length() < 1){
            WCUtils.sendMessage(p,ChatColor.RED + "Usage: /whitelistCycle removePlayer <player name> <list name>");
            return false;
        }

        if(!WCUtils.getAllListsName().contains(listName)){
            WCUtils.sendMessage(p,ChatColor.RED + "Failed to remove player as the whitelist '"+listName+"' does not exists.");
            return false;
        }

        //create player to remove obj
        JsonObject playerToRemove = new JsonObject();
        playerToRemove.addProperty("name",playerName);
        playerToRemove.addProperty("uuid",Bukkit.getOfflinePlayer(playerName).getUniqueId().toString() );

        JsonObject listRef = WCUtils.getList(listName);
        if(!WCUtils.isPlayerInList(listRef,playerToRemove))
            WCUtils.sendMessage(p,ChatColor.RED + "The player '" + playerName + "' is NOT in whitelist: " + listName);

        //remove player from list
        listRef.getAsJsonArray("players").remove(playerToRemove);

        WCUtils.sendMessage(p,"Removed '" + playerName + "' from whitelist: " + listName);


        //if added to current list - update Whitelist file
        if(main.ref.json.get("currentList") != null &&
                WCUtils.jsonGetString("currentList").equals(listName))
            writeWhitelistJSON(listRef);

        //save
        main.ref.saveJSON();
        return true;
    }


    private boolean currentList(CommandSender p,String listName){
        if( listName == null || listName.length() == 0){
            if(main.ref.json.get("currentList") != null) {
                String cList = WCUtils.jsonGetString("currentList");
                WCUtils.sendMessage(p, "Current whitelist is: " + cList + ".");
                WCUtils.getList(cList).getAsJsonArray("players");
                StringBuilder players = new StringBuilder();
                players.append("Players: " + ChatColor.GRAY);
                JsonArray listPlayers = WCUtils.getList(cList).getAsJsonArray("players");
                if(listPlayers.size() > 0) {
                    for (JsonElement aRawPlayer : listPlayers) {
                        JsonObject aPlayer = aRawPlayer.getAsJsonObject();
                        String playerName = WCUtils.jsonGetString(aPlayer, "name");
                        players.append(playerName + ", ");
                    }
                    players.setLength(players.length() - 2);
                }
                else
                    players.append("Players: [NO PLAYERS]");
                p.sendMessage(players + ".");
            }
            else
                WCUtils.sendMessage(p,ChatColor.RED + "There is no current active whitelist.");
            WCUtils.sendMessage(p,"To set whitelist as current-list => /whitelistCycle currentList <list name>");
            return true;
        }

        //if list doesnt exists - exit
        if(!WCUtils.getAllListsName().contains(listName)){
            WCUtils.sendMessage(p,ChatColor.RED + "That whitelist name does not exist!");
            return false;
        }
        main.ref.saveJSON();

        //add & save updated json
        main.ref.json.addProperty("currentList",listName);
        main.ref.saveJSON();

        //update whitelist
        writeWhitelistJSON(WCUtils.getList(listName));

        WCUtils.sendMessage(p,"Whitelist activated: " + ChatColor.GREEN + listName);
        return true;
    }


    private void writeWhitelistJSON(JsonObject listJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File whitelistFile = new File(Bukkit.getServer().getWorldContainer(), "whitelist.json");
        String data = "[]";
        if(listJson != null )
            data = gson.toJson(listJson.get("players"));
        WCUtils.writeFile(whitelistFile,data);
        Bukkit.reloadWhitelist();

    }
}