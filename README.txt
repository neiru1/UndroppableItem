
Adds the ability to disable specified items to be dropped, moved away, or removed from the player's inventory. Originally created to support my RPG-based modpack and its class-specific items systems.

You can list the items in config\udi-common.toml in your modpack/minecraft directory.

The idea and the source code originally belonged to theabdel572. The mod was converted to support Forge 47,4,0 and Minecraft 1.20.1 while retaining the original structure. Full credits belong to theabdel572. I only updated and converted the code with additional AI help. I have little to no coding experience especially with Java so potentially updating the mod for other Minecraft/Loader versions may not happen, as well with additional mod support. Free of use, you can take the source code and modify/change as much as you like.

Current issues:
- Unable to use "/udi" in-game commands
- Not thoroughly tested. Potential external inventory issues (such as crafting table, anvil, etc).
- May not recognize modded inventories. Undroppable effect may not function.
