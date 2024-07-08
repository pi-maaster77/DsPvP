package my.app.pimaaster77;

import org.bukkit.plugin.java.JavaPlugin;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;


import java.io.File;

public class Bot extends JavaPlugin {
    private GatewayDiscordClient discordClient;

    @Override
    public void onEnable() {
        getLogger().info("TuPlugin ha sido habilitado");
        loadConfig();

        String token = getConfig().getString("token");  // Reemplaza con tu token de Discord

        if (token == null || token.isEmpty()) {
            getLogger().severe("No token found in config.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DiscordClient client = DiscordClient.create(token);
        discordClient = client.login().block();

        discordClient.getEventDispatcher().on(MessageCreateEvent.class)
            .subscribe(event -> {
                Message message = event.getMessage();
                if (message.getContent().equalsIgnoreCase("!ping")) {
                    MessageChannel channel = message.getChannel().block();
                    channel.createMessage("Pong!").block();
                }
            });

        getLogger().info("Bot de Discord habilitado");
    }

    @Override
    public void onDisable() {
        if (discordClient != null) {
            discordClient.logout().block();
        }
        getLogger().info("TuPlugin ha sido deshabilitado");
    }

    private void loadConfig() {
        // Comprueba si el archivo de configuración ya existe
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            // Crea el archivo de configuración predeterminado si no existe
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }
    }
}
