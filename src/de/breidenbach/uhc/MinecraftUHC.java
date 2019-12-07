package de.breidenbach.uhc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftUHC extends JavaPlugin {

    public static final String VERSION = "INDEV 1.0";

    private boolean matchSetup;
    private boolean matchStarted;

    @Override
    public void onLoad(){

    }

    @Override
    public void onEnable(){
        System.out.printf("UHC %s enabled", VERSION);
    }

    @Override
    public void onDisable(){

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        switch(label.toLowerCase()){
            case "setup":
                break;
            case "start":
                break;
            case "stop":
                break;
        }
        return false;
    }
}
