package nl.svenar.powercamera.commands.subcommand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import nl.svenar.powercamera.CameraHandler;
import nl.svenar.powercamera.PowerCamera;
import nl.svenar.powercamera.commands.PowerCameraCommand;
import nl.svenar.powercamera.commands.structure.CommandExecutionContext;
import nl.svenar.powercamera.data.CameraMode;
import nl.svenar.powercamera.data.PlayerCameraData;

@SuppressWarnings({ "PMD.AvoidLiteralsInIfCondition", "PMD.CommentRequired", "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal" })
public class SubcommandStartOther extends PowerCameraCommand {

    public SubcommandStartOther(PowerCamera plugin, String commandName) {
        super(plugin, commandName, CommandExecutionContext.ALL);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length != 2) {
            sendMessage(sender,
                    ChatColor.DARK_RED + "Usage: /" + commandLabel + " startother <playername> <cameraname>");
            return false;
        }

        String targetName = args[0];
        String cameraName = args[1];

        if (!sender.hasPermission("powercamera.cmd.startother")) {
            sendMessage(sender, ChatColor.DARK_RED + "You do not have permission to execute this command");
            return false;
        }

        List<Player> targetPlayers = getPlayersFromSelector(sender, targetName);

        if (targetPlayers.isEmpty()) {
            sendMessage(sender, ChatColor.DARK_RED + "No matching players found for selector '" + targetName + "'");
            return false;
        }

        for (Player targetPlayer : targetPlayers) {
            PlayerCameraData cameraData = plugin.getPlayerData().get(targetPlayer);

            if (cameraData.getCameraMode() != CameraMode.NONE) {
                sendMessage(sender,
                        ChatColor.DARK_RED + "Player '" + targetPlayer.getName() + "' already has a camera active!");
                continue;
            }

            if (this.plugin.getConfigCameras().cameraExists(cameraName)) {
                cameraData.setCameraHandler(new CameraHandler(plugin, targetPlayer, cameraName).generatePath().start());
                sender.sendMessage(plugin.getPluginChatPrefix() + ChatColor.GREEN + "Playing '" + cameraName
                        + "' on player: " + targetPlayer.getName());
            } else {
                sendMessage(sender, ChatColor.RED + "Camera '" + cameraName + "' not found!");
                break;
            }
        }

        return false;
    }

    private List<Player> getPlayersFromSelector(CommandSender sender, String selector) {
        List<Player> players = new ArrayList<>();
        
        try {
            // Pokud to není selector, zkusí najít hráče přímo podle jména
            if (!selector.startsWith("@")) {
                Player namedPlayer = Bukkit.getPlayer(selector);
                if (namedPlayer != null) {
                    players.add(namedPlayer);
                }
                return players;
            }

            // Využití nativního parsování selectorů v Bukkitu
            for (Entity entity : Bukkit.selectEntities(sender, selector)) {
                if (entity instanceof Player player) {
                    players.add(player);
                }
            }
        } catch (IllegalArgumentException e) {
            // Nastane, pokud je syntaxe selectoru neplatná (např. překlep v argumentech)
            sendMessage(sender, ChatColor.DARK_RED + "Invalid selector syntax: " + selector);
        }
        
        return players;
    }
}
