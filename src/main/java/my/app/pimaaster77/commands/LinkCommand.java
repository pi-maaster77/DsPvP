package my.app.pimaaster77.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.security.SecureRandom;

import static my.app.pimaaster77.Bot.verificationCodes;

public class LinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String code = generateVerificationCode();
            verificationCodes.put(code, player.getName());

            // Crear mensaje con click para copiar el código
            TextComponent message = new TextComponent("Usa este código para vincular tu cuenta de Discord: ");
            TextComponent codeComponent = new TextComponent(code);
            codeComponent.setColor(ChatColor.AQUA);
            codeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code));
            message.addExtra(codeComponent);
            player.spigot().sendMessage(message);

            return true;
        }
        return false;
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1000000);
        return String.format("%06d", code);
    }
}
