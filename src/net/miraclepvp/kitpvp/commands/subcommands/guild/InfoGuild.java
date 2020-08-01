package net.miraclepvp.kitpvp.commands.subcommands.guild;

import net.miraclepvp.kitpvp.data.Data;
import net.miraclepvp.kitpvp.data.guild.Guild;
import net.miraclepvp.kitpvp.data.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.miraclepvp.kitpvp.bukkit.Text.color;

public class InfoGuild implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player)sender;
        User user = Data.getUser(player);
        if(user.getGuild() == null){
            sender.sendMessage(color("&4You are not in a guild."));
            return true;
        }
        Guild guild = Data.getGuild(user.getGuild());
        player.sendMessage(color("&5&m-----------------------------------------------------"));
        player.sendMessage(color("&f" + guild.getName()));
        player.sendMessage(color(" "));
        player.sendMessage(color("&7Created: " + Bukkit.getOfflinePlayer(guild.getCreator()).getName() + " created this guild at " + guild.getCreated()));
        player.sendMessage(color("&7Master: " + Bukkit.getOfflinePlayer(guild.getMaster()).getName()));
        player.sendMessage(color("&7Members: " + guild.getPlayers().size() + "/8"));
        player.sendMessage(color("&7Description: " + guild.getMotd()));
        player.sendMessage(color(" "));
        player.sendMessage(color("&7Guild Experience: " + guild.getExperience()));
        player.sendMessage(color("&7Guild Level: " + guild.getLevel()));
        player.sendMessage(color("&5&m-----------------------------------------------------"));
        return true;
    }
}
