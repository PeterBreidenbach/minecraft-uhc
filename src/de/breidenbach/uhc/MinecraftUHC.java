package de.breidenbach.uhc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftUHC extends JavaPlugin {

    public static final String VERSION = "1.0";
    private Game game;

    @Override
    public void onEnable(){
        game = new Game(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(game), this);
        System.out.println("UHC " + VERSION + " enabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(label.equalsIgnoreCase("uhc")){
            if(sender.isOp()) {
                if (args.length > 0) {
                    switch (args[0]) {
                        case "init":
                            sender.sendMessage("Initializing match...");
                            game.init();
                            sender.sendMessage("Done.");
                            return true;
                        case "start":
                            if(game.active) {
                                if(!game.countdownStarted && !game.started) {
                                    if (game.alivePlayers.size() > 1) {
                                        sender.sendMessage("Starting countdown...");
                                        game.start();
                                    } else {
                                        sender.sendMessage("At least 2 players are needed to start!");
                                    }
                                }else{
                                    sender.sendMessage("Game already started!");
                                }
                            }else{
                                sender.sendMessage("Please run \"/uhc init\" first!");
                            }
                            return true;
                        default:
                            return false;
                    }
                } else {
                    sender.sendMessage("UHC " + VERSION + " loaded.");
                    return true;
                }
            }else{
                sender.sendMessage(ChatColor.RED + "UHC-Settings can only be accessed by an operator!");
                return true;
            }
        }
        return false;
    }
}
