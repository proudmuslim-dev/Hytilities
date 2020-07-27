package club.sk1er.hytilities.handlers.chat.cleaner;

import club.sk1er.hytilities.config.HytilitiesConfig;
import club.sk1er.hytilities.handlers.chat.ChatModule;
import club.sk1er.hytilities.handlers.game.GameChecker;
import club.sk1er.mods.core.util.MinecraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo: split up this class into separate modules
 */
public class ChatCleaner implements ChatModule {

    private final List<String> joinMessageTypes = Arrays.asList(
        "joined the lobby", // normal
        "spooked in the lobby" // halloween
    );

    private final Pattern mysteryBoxFind = Pattern.compile("(?<player>\\w{1,16}) found a .+ Mystery Box!");
    private final Pattern soulBoxFind = Pattern.compile(".+ has found .+ in the Soul Well!");
    private final Pattern gameAnnouncement = Pattern.compile("➤ A .+ game is (?:available to join|starting in .+ seconds)! CLICK HERE to join!");
    private final Pattern bedwarsPartyAdvertisement = Pattern.compile("(?<number>[1-3]/[2-4])");
    private final Pattern connectionStatus = Pattern.compile("(?:Friend|Guild) > (?<player>\\w{1,16}) (?:joined|left)\\.");

    // yall like regex?
    private final Pattern mvpEmotes = Pattern.compile("§r§(?:c❤|6✮|a✔|c✖|b☕|e➜|e¯\\\\_\\(ツ\\)_/¯|c\\(╯°□°）╯§r§f︵§r§7 ┻━┻|d\\( ﾟ◡ﾟ\\)/|a1§r§e2§r§c3|b☉§r§e_§r§b☉|e✎§r§6\\.\\.\\.|a√§r§e§l\\(§r§aπ§r§a§l\\+x§r§e§l\\)§r§a§l=§r§c§lL|e@§r§a'§r§e-§r§a'|6\\(§r§a0§r§6\\.§r§ao§r§c\\?§r§6\\)|b༼つ◕_◕༽つ|e\\(§r§b'§r§e-§r§b'§r§e\\)⊃§r§c━§r§d☆ﾟ\\.\\*･｡ﾟ|e⚔|a✌|c§lOOF|e§l<\\('O'\\)>)§r");

    @Override
    public void onChatEvent(ClientChatReceivedEvent event) {
        if (event.isCanceled()) {
            return;
        }

        String message = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());

        if (HytilitiesConfig.hytilitiesLobbyStatuses) {
            for (String messages : joinMessageTypes) {
                if (message.contains(messages)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if (HytilitiesConfig.hytilitiesMvpEmotes) {
            Matcher matcher = mvpEmotes.matcher(event.message.getFormattedText());

            if (matcher.find()) {
                event.message = new ChatComponentText(event.message.getFormattedText().replaceAll(mvpEmotes.pattern(), ""));
                return;
            }
        }

        // todo: figure out why chat events don't copy-over
        /*if (HytilitiesConfig.hytilitiesLineBreaker) {
            if (message.contains("-----------") && message.contains("\n")) {
                event.message = new ChatComponentText(reformatMessage(event.message.getFormattedText()));
                return;
            } else if (message.contains("-----------")){
                event.setCanceled(true);
                return;
            }
        }*/

        if (HytilitiesConfig.hytilitiesMysteryBoxAnnouncer) {
            Matcher matcher = mysteryBoxFind.matcher(message);

            if (matcher.find()) {
                String player = matcher.group("player");
                boolean playerBox = !player.contains(Minecraft.getMinecraft().thePlayer.getName());

                if (!playerBox || !player.startsWith("You")) {
                    event.setCanceled(true);
                    return;
                }
            } else if (message.startsWith("[Mystery Box]")) {
                event.setCanceled(true);
                return;
            }
        }

        if (HytilitiesConfig.hytilitiesGameAnnouncements) {
            if (gameAnnouncement.matcher(message).find()) {
                event.setCanceled(true);
                return;
            }
        }

        if (HytilitiesConfig.hytilitiesHypeLimitReminder && message.startsWith("  ➤ You have reached your Hype limit!")) {
            event.setCanceled(true);
            return;
        }

        if (HytilitiesConfig.hytilitiesSoulBoxAnnouncer) {
            if (soulBoxFind.matcher(message).find()) {
                event.setCanceled(true);
                return;
            }
        }

        if (HytilitiesConfig.hytilitiesBedwarsAdvertisements && GameChecker.isBedwars()) {
            if (bedwarsPartyAdvertisement.matcher(message).find()) {
                event.setCanceled(true);
                return;
            }
        }

        if (HytilitiesConfig.hytilitiesConnectionStatus && connectionStatus.matcher(message).matches()) {
            event.setCanceled(true);
        }
    }

    // taken from ToggleChat
    private String reformatMessage(String formattedText) {
        if (formattedText.contains("▬▬")) {
            formattedText = formattedText
                .replace("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", "")
                .replace("▬▬", "");
            return formattedText;
        } else if (formattedText.contains("---")) {
            formattedText = formattedText
                .replace("----------------------------------------------------\n", "");
            return formattedText.replace("--\n", "").replace("\n--", "").replace("-", "");
        }

        return formattedText;
    }
}
