package de.breidenbach.uhc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftUHC extends JavaPlugin {

    public static final String VERSION = "INDEV 1.0";

    @Override
    public void onLoad(){

    }

    @Override
    public void onEnable(){
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        System.out.println("UHC " + VERSION + " enabled");
    }

    @Override
    public void onDisable(){

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(label.equalsIgnoreCase("uhc") && sender.isOp()){
            if(args.length > 0){
                switch(args[0]){
                    case "init":
                        Game.init();
                        break;
                    case "start":
                        Game.start();
                        break;
                }
            }else{
                sender.sendMessage("UHC " + VERSION + " loaded.");
            }
            return true;
        }
        return false;
    }
}
