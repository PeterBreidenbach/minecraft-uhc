package de.breidenbach.uhc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftUHC extends JavaPlugin {

    public static final String VERSION = "INDEV 1.0";
    private Game game;

    @Override
    public void onLoad(){

    }

    @Override
    public void onEnable(){
        game = new Game(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(game), this);
        System.out.println("UHC " + VERSION + " enabled");
    }

    @Override
    public void onDisable(){

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(label.equalsIgnoreCase("uhc")){
            if(sender.isOp()) {
                if (args.length > 0) {
                    switch (args[0]) {
                        case "init":
                            game.init(sender.getServer());
                            break;
                        case "start":
                            game.start(sender.getServer());
                            break;
                    }
                } else {
                    sender.sendMessage("UHC " + VERSION + " loaded.");
                }
                return true;
            }else{
                sender.sendMessage(ChatColor.RED + "UHC-Settings can only accessed by an operator!");
                return true;
            }
        }
        return false;
    }
}
