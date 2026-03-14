package my.app.pimaaster77.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

public class AssignRolesCommand {
    private final GatewayDiscordClient client;
    private final Map<String, String> roleMap; // Mapea nombres de roles a IDs de roles
    private final Map<String, String> voiceChannelMap; // Mapea nombres de roles a IDs de canales de voz

    public AssignRolesCommand(GatewayDiscordClient client, Map<String, String> roleMap, Map<String, String> voiceChannelMap) {
        this.client = client;
        this.roleMap = roleMap;
        this.voiceChannelMap = voiceChannelMap;
    }

    public void register() {
        client.getEventDispatcher().on(MessageCreateEvent.class)
            .flatMap(event -> {
                Message message = event.getMessage();
                if (message.getContent().equalsIgnoreCase("!assignroles")) {
                    return assignRolesAndMoveUsers(message);
                }
                return Mono.empty();
            }).subscribe();
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private Mono<Void> assignRolesAndMoveUsers(Message message) {
        return message.getGuild().flatMap(guild ->
            guild.getMembers().collectList().flatMap(members -> {
                Random random = new Random();
                for (Member member : members) {
                    if (!member.isBot()) {
                        int roleIndex = random.nextInt(roleMap.size());
                        String roleName = roleMap.keySet().toArray(new String[0])[roleIndex];
                        String roleId = roleMap.get(roleName);
                        String channelId = voiceChannelMap.get(roleName);

                        guild.getRoleById(Snowflake.of(roleId)).flatMap(role -> member.addRole(role.getId())).subscribe();

                        guild.getChannelById(Snowflake.of(channelId)).ofType(VoiceChannel.class).flatMap(voiceChannel -> {
                            Mono<PermissionSet> permissions = voiceChannel.getEffectivePermissions(member.getId());
                            if (((Collection<Permission>) permissions).contains(Permission.CONNECT)) {
                                return member.edit(spec -> spec.setNewVoiceChannel(voiceChannel.getId()));
                            }
                            return Mono.empty();
                        }).subscribe();
                    }
                }
                return Mono.empty();
            })
        ).then();
    }
}
