# Multiple Whitlist configurations!
This plugin allows you to manage whitlists on the fly!
* Allows creating lists.
* Manage them by adding/removeing players.
* To change current whitlist - use a single command!

## Usage:
`/whitelistcycle` is the command to manage everything!

`/whitelistcycle addList <list Name>` to create new list.

`/whitelistcycle removeList <list Name>` to remove a list.

`/whitelistcycle addPlayer <player Name> <list Name>` to add a player to a list.

`/whitelistcycle removePlayer <player Name> <list Name>` to remove a player from a list.

`/whitelistcycle currentList` to infrom the sender which list is the current Whitelist (and shows the players in the list)

`/whitelistcycle currentList <list Name>` to switch to other list - essentially changing the Whitlist on the fly.


### Notes:
- Each update to player taht in current list via `addPlayer /removePlayer` will directly update the whitlist itself.
- All names (lists/players) must be a single word.
- this plugin keeps the native way of whitelists.json and rewrites it as needed:
  - **By server defaults** any player with OP bypasses whitlist check.
  - Usages of the default `whitelist` command is possible  **IT IS NOT RECOMMANDED THO**.
  - Whitelists.json will get overided when a list is updated from the plugin. 
  - There is no hook into player login etc.. allowing other plugins.
  - Whitelist will get turned ON everytime server gets started.

