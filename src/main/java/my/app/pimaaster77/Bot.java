package my.app.pimaaster77;

import org.bukkit.plugin.java.JavaPlugin;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

import java.io.File;

public class Bot extends JavaPlugin {
    private GatewayDiscordClient discordClient;
    private static NgrokTunnel tunnel = new NgrokTunnel();

    @Override
    public void onEnable() {
        getLogger().info("TuPlugin ha sido habilitado");
        loadConfig();
        String tunnelUrl = tunnel.getNgrokTunnelUrl(getServer().getPort());
        if (tunnelUrl == null || tunnelUrl.isEmpty()) {
            getLogger().severe("No se pudo obtener la URL del túnel de ngrok");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        String token = getConfig().getString("token");
        String anunciosID = getConfig().getString("anunciosID");

        if (token == null || token.isEmpty()) {
            getLogger().severe("No token found in config.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DiscordClient client = DiscordClient.create(token);
        discordClient = client.login().block();

        // Enviar un mensaje al canal de anuncios cuando el bot se habilite
        if (anunciosID != null && !anunciosID.isEmpty()) {
            discordClient.getChannelById(Snowflake.of(anunciosID))
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage("```" + tunnelUrl + "```"))
                .block();
        } else {
            getLogger().severe("No anunciosID found in config.yml!");
        }

        discordClient.getEventDispatcher().on(MessageCreateEvent.class)
            .subscribe(event -> {
                Message message = event.getMessage();
                if (message.getContent().equalsIgnoreCase("!ping")) {
                    MessageChannel channel = message.getChannel().block();
                    if (channel != null) {
                        channel.createMessage("Pong!").block();
                    }
                }
            });

        getLogger().info("Bot de Discord habilitado");
    }

    @Override
    public void onDisable() {
        if (discordClient != null) {
            discordClient.logout().block();
        }
        tunnel.closeNgrokTunnel();
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
