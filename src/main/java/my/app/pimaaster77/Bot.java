package my.app.pimaaster77;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import my.app.pimaaster77.commands.LinkCommand;
import my.app.pimaaster77.commands.MensajearCommand;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;





public class Bot extends JavaPlugin {
    private GatewayDiscordClient discordClient;
    private static NgrokTunnel tunnel = new NgrokTunnel();
    public static Map<String, String> verificationCodes = new HashMap<>(); // Almacena códigos y nombres de jugadores
    public static Map<String, String> linkedAccounts = new HashMap<>(); // Almacena cuentas vinculadas

    private static final String LINKED_ACCOUNTS_FILE = "linkedAccounts.json";

    @Override
    public void onEnable() {
        getLogger().info("TuPlugin ha sido habilitado");
        loadConfig();
        loadLinkedAccounts();
        String tunnelUrl = tunnel.getNgrokTunnelUrl(getServer().getPort());
        if (tunnelUrl == null || tunnelUrl.isEmpty()) {
            getLogger().severe("No se pudo obtener la URL del túnel de ngrok");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        String token = getConfig().getString("token");
        String ipID = getConfig().getString("ipID");

        if (token == null || token.isEmpty()) {
            getLogger().severe("No token found in config.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DiscordClient client = DiscordClient.create(token);
        discordClient = client.login().block();

        // Enviar un mensaje al canal de anuncios cuando el bot se habilite
        if (ipID != null && !ipID.isEmpty()) {
            discordClient.getChannelById(Snowflake.of(ipID))
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage("```" + tunnelUrl + "```"))
                .block();
        } else {
            getLogger().severe("No ipID found in config.yml!");
        }

        // Manejar mensajes privados para vincular cuentas
        discordClient.getEventDispatcher().on(MessageCreateEvent.class)
            .subscribe(event -> {
                Message message = event.getMessage();
                User author = message.getAuthor().orElse(null);
                if (author != null && !author.isBot()) {
                    String content = message.getContent();
                    String playerName = verificationCodes.get(content);
                    MessageChannel channel = message.getChannel().block();
                    if (playerName != null) {
                        // Vínculo exitoso
                        linkedAccounts.put(playerName, author.getId().asString());
                        saveLinkedAccounts();
                        if (channel != null) {
                            channel.createMessage("Cuenta vinculada con éxito para el jugador: " + playerName).block();
                        }
                        verificationCodes.remove(content);
                    } else {
                        // Código no válido
                        if (channel != null) {
                            channel.createMessage("Código no válido. Por favor, verifica e intenta de nuevo.").block();
                        }
                    }
                }
            });

        // Registrar los comandos
        this.getCommand("link").setExecutor(new LinkCommand());
        this.getCommand("mensajear").setExecutor(new MensajearCommand());

        getLogger().info("Bot de Discord habilitado");
    }

    @Override
    public void onDisable() {
        if (discordClient != null) {
            discordClient.logout().block();
        }
        tunnel.closeNgrokTunnel();
        saveLinkedAccounts();
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

    private void loadLinkedAccounts() {
        File file = new File(getDataFolder(), LINKED_ACCOUNTS_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                linkedAccounts = new Gson().fromJson(reader, type);
            } catch (IOException e) {
                getLogger().severe("No se pudo cargar el archivo de cuentas vinculadas");
                e.printStackTrace();
            }
        }
    }

    private void saveLinkedAccounts() {
        File file = new File(getDataFolder(), LINKED_ACCOUNTS_FILE);
        try (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(linkedAccounts, writer);
        } catch (IOException e) {
            getLogger().severe("No se pudo guardar el archivo de cuentas vinculadas");
            e.printStackTrace();
        }
    }
}
