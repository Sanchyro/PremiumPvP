package net.miraclepvp.kitpvp.listeners.player;

import net.miraclepvp.kitpvp.data.Data;
import net.miraclepvp.kitpvp.data.kit.Kit;
import net.miraclepvp.kitpvp.data.user.User;
import net.miraclepvp.kitpvp.inventories.*;
import net.miraclepvp.kitpvp.listeners.custom.PlayerDeployEvent;
import net.miraclepvp.kitpvp.listeners.custom.PlayerSpawnEvent;
import net.miraclepvp.kitpvp.listeners.player.movement.playerJoin;
import net.miraclepvp.kitpvp.objects.CosmeticType;
import net.miraclepvp.kitpvp.objects.hasKit;
import net.miraclepvp.kitpvp.utils.CooldownUtil;
import net.miraclepvp.kitpvp.utils.TeleportUtil;
import net.miraclepvp.kitpvp.bukkit.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

import static net.miraclepvp.kitpvp.bukkit.Text.color;

public class playerInventory implements Listener {

    public static HashMap<UUID, Integer> switcherBalls = new HashMap<>();

    @EventHandler
    public void onInteractWithoutKit(PlayerInteractEvent event) {
        if (event.getPlayer().hasMetadata("kit")) return;
        if (event.getPlayer().hasMetadata("build")) return;
        Player player = event.getPlayer();
        User user = Data.getUser(player);
        if (user == null) return;
        if (event.getItem() == null || event.getItem().getType() == null) return;
        switch (event.getItem().getType()) {
            case COMPASS:
                if (user.isQuickSelect() && (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
                    if (user.getPreviousKit() == null || (Data.getKit(user.getPreviousKit()).getPrice() > 0 && !(user.getKitsList().contains(user.getPreviousKit())))) {
                        player.sendMessage(color("&cWe failed to get your previous kit, please select a kit."));
                        break;
                    }
                    Kit kit = Data.getKit(user.getPreviousKit());
                    player.sendMessage(color("&aYou've selected the " + kit.getName() + " kit."));
                    if (user.isAutoDeploy()) Bukkit.getPluginManager().callEvent(new PlayerDeployEvent(player, true, true));
                    break;
                }
                player.openInventory(KitGUI.getInventory(player, false, 1));
                break;
            case EYE_OF_ENDER:
                player.openInventory(AbilityGUI.getMainInventory(player));
                event.setCancelled(true);
                break;
            case SKULL_ITEM:
                player.openInventory(ProfileGUI.getInventory(player));
                break;
            case BLAZE_POWDER:
                player.openInventory(CosmeticsGUI.getInventory(player, CosmeticType.valueOf(user.getLastCosmeticType().toUpperCase()), user.getCosmeticWasShop(), 1));
                break;
        }
    }

    @EventHandler
    public void interactWithKit(PlayerInteractEvent event) {
        if (!event.getPlayer().hasMetadata("kit")) return;
        if (event.getPlayer().hasMetadata("build")) return;
        Player player = event.getPlayer();
        User user = Data.getUser(player);
        if (user == null) return;
        if (event.getItem() == null || event.getItem().getType() == null) return;

        //              Enderpearl cooldown system

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getItem().getType().equals(Material.ENDER_PEARL)) {
                //ENDERPEARL TROWN
                CooldownUtil.Cooldown cooldown = CooldownUtil.getCooldown(player, "enderpearl");
                if (cooldown == null || !cooldown.hasTimeLeft()) {
                    CooldownUtil.Cooldown newCooldown = CooldownUtil.prepare(player, "enderpearl", 15);
                    newCooldown.start();
                } else {
                    event.setCancelled(true);
                    player.sendMessage(color("&cWait " + cooldown.getSecondsLeft() + " seconds before throwing a new enderpearl!"));
                    player.updateInventory();
                }
            }
            if(event.getItem().getType().equals(Material.COMPASS)){
                if(event.getItem().getItemMeta() != null &&
                        event.getItem().getItemMeta().getLore() != null &&
                        event.getItem().getItemMeta().getLore().get(0) != null &&
                        event.getItem().getItemMeta().getLore().get(0).equalsIgnoreCase(color("&7Player Tracker"))
                ) {
                    CooldownUtil.Cooldown cooldown = CooldownUtil.getCooldown(player, "tracker");
                    if (cooldown == null || !cooldown.hasTimeLeft()) {
                        CooldownUtil.Cooldown newCooldown = CooldownUtil.prepare(player, "tracker", 30);
                        newCooldown.start();

                        Player target = getNearest(player, 100.0);
                        if (target == null) {
                            player.setCompassTarget(player.getLocation());
                            ItemMeta meta = event.getItem().getItemMeta();
                            meta.setDisplayName(Text.color("&5Tracking nobody"));
                            event.getItem().setItemMeta(meta);
                            return;
                        }
                        player.setCompassTarget(target.getLocation());
                        ItemMeta meta = event.getItem().getItemMeta();
                        meta.setDisplayName(Text.color("&5Tracking: " + target.getName()));
                        event.getItem().setItemMeta(meta);
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(color("&cYou have to wait " + cooldown.getSecondsLeft() + " seconds before you can get a new location!"));
                        player.updateInventory();
                    }
                    return;
                }
            }
            if (event.getItem().getType() == Material.MUSHROOM_SOUP) {
                if(player.getHealth()>=player.getMaxHealth()) return;
                player.setHealth(player.getHealth() + 7 > player.getMaxHealth() ? player.getMaxHealth() : player.getHealth() + 7);
                player.getItemInHand().setType(Material.BOWL);
                return;
            }
            if(
                    event.getItem().getType().equals(Material.SNOW_BALL) &&
                    event.getItem().getItemMeta() != null &&
                    event.getItem().getItemMeta().getLore() != null &&
                    event.getItem().getItemMeta().getLore().get(0) != null &&
                    event.getItem().getItemMeta().getLore().get(0).equalsIgnoreCase(color("&7Throw this item at other"))
            ) {
                CooldownUtil.Cooldown cooldown = CooldownUtil.getCooldown(player, "switcherball");
                if (cooldown == null || !cooldown.hasTimeLeft()) {
                    CooldownUtil.Cooldown newCooldown = CooldownUtil.prepare(player, "switcherball", 20);
                    newCooldown.start();
                    if(switcherBalls.containsKey(player.getUniqueId())){
                        event.setCancelled(true);
                        player.sendMessage(color("&cYou can not throw multiple switcherballs at the same time!"));
                        player.updateInventory();
                        return;
                    }
                    switcherBalls.put(player.getUniqueId(), null);
                } else {
                    event.setCancelled(true);
                    player.sendMessage(color("&cWait " + cooldown.getSecondsLeft() + " seconds before throwing a new switcherball!"));
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event){
        if(!(event.getEntity().getShooter() instanceof Player)) return;
        if(switcherBalls.containsKey(((Player) event.getEntity().getShooter()).getUniqueId()) && switcherBalls.get(((Player) event.getEntity().getShooter()).getUniqueId()) == null)
            switcherBalls.put(((Player) event.getEntity().getShooter()).getUniqueId(), event.getEntity().getEntityId());
    }

    @EventHandler
    public void onMove(InventoryClickEvent event) {
        if (event == null) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getWhoClicked() == null) return;
        if (event.getInventory() == null) return;
        if (event.getClickedInventory() == null) return;
        if (event.getWhoClicked().getInventory() == null) return;
        if (event.getWhoClicked().hasMetadata("kit") || event.getWhoClicked().hasMetadata("build")) return;
        if (!(event.getClickedInventory().equals(event.getWhoClicked().getInventory()))) return;
        event.setCancelled(true);
    }

    public Player getNearest(Player p, Double range) {
        double distance = Double.POSITIVE_INFINITY;
        Player target = null;
        for (Entity e : p.getNearbyEntities(range, range, range)) {
            if (!(e instanceof Player))
                continue;
            if (e == p) continue;
            if (!((Player) e).getGameMode().equals(GameMode.ADVENTURE))
                continue;
            if (e.hasMetadata("build") || e.hasMetadata("vanished") || !e.hasMetadata("kit"))
                continue;
            double distanceto = p.getLocation().distance(e.getLocation());
            if (distanceto > distance)
                continue;
            distance = distanceto;
            target = (Player) e;
        }
        return target;
    }
}
