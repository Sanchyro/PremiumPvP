package net.miraclepvp.kitpvp.commands.subcommands.guild;

import net.miraclepvp.kitpvp.data.Config;
import net.miraclepvp.kitpvp.data.Data;
import net.miraclepvp.kitpvp.data.guild.Guild;
import net.miraclepvp.kitpvp.data.user.User;
import net.miraclepvp.kitpvp.objects.PermissionType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

import static net.miraclepvp.kitpvp.bukkit.Text.color;

public class FriendlyFireGuild implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 1){
            sender.sendMessage(color("&cPlease use /guild friendlyfire"));
            return true;
        }
        UUID uuid = ((Player) sender).getUniqueId();

        User user = Data.getUser(((Player)sender));

        if(user.getGuild() == null){
            sender.sendMessage(color("&4You are not in a guild."));
            return true;
        }
        Guild guild = Data.getGuild(user.getGuild());

        ArrayList<PermissionType> userPerms = new ArrayList<>();

        if(guild.getMembers().contains(uuid))
            userPerms = guild.getMemberPerms();
        if(guild.getOfficers().contains(uuid))
            userPerms = guild.getOfficerPerms();

        if(!userPerms.contains(PermissionType.FRIENDLYFIRE) && !guild.getMaster().equals(uuid)){
            sender.sendMessage(color("&4You don't have enough permissions to do this."));
            return true;
        }

        if(!Config.getToggleFriendlyFire()){
            sender.sendMessage(color("&4This feature has been disabled by the server."));
            return true;
        }

        guild.setFriendlyfire(!guild.getFriendlyfire());
        guild.sendBroadcast(sender.getName() + " has turned friendlyfire " + (guild.getFriendlyfire() ? "on" : "off") + ".");
        return true;
    }
}
