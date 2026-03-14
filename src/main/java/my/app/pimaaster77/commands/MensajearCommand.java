package my.app.pimaaster77.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static my.app.pimaaster77.Bot.linkedAccounts;

public class MensajearCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String discordId = linkedAccounts.get(player.getName());
            if (discordId != null) {
                player.sendMessage("Tu cuenta está vinculada con la cuenta de Discord con ID: " + discordId);
            } else {
                player.sendMessage("Tu cuenta no está vinculada con ninguna cuenta de Discord.");
            }
            return true;
        }
        return false;
    }
}
