package fr.communaywen.core.listeners;

import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.claim.RegionManager;
import fr.communaywen.core.corpse.CorpseBlock;
import fr.communaywen.core.corpse.CorpseManager;
import fr.communaywen.core.corpse.CorpseMenu;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CorpseListener implements Listener {

    private final CorpseManager corpseManager;

    public CorpseListener(CorpseManager corpseManager) {
        this.corpseManager = corpseManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        Location deathLocation = player.getLocation();
        double y = deathLocation.getY();
        boolean isArea = false;

        // Si le joueur est mort à une coos non entière, on arrondit à l'entier supérieur (ex : 66.2 -> 67)
        y = roundUpIfNotInteger(y);
        deathLocation.setY(y);

        while (player.getWorld().getBlockAt(deathLocation).getType().isSolid()) {
            y++;
            deathLocation.setY(y);
        }

        // S'il meurt au dessus de la hauteur max du monde, tant pis, le stuff est drop
        if (y >= player.getWorld().getMaxHeight()) {
            isArea = true;
        } else {
            for (RegionManager region : AywenCraftPlugin.getInstance().regions) {
                if(region.isInArea(deathLocation)){
                    isArea = true;
                    break;
                }
            }
        }

        if (isArea) {
            return;
        }

        e.getDrops().clear();
        corpseManager.addCorpse(e.getEntity(), e.getEntity().getInventory(), deathLocation);
    }

    private double roundUpIfNotInteger(double value) {
        return (value % 1 == 0) ? value : Math.ceil(value);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            Block block = e.getClickedBlock();

            if (block == null || block.getType().isAir()) {
                return;
            }

            corpseManager.open(block, e.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (corpseManager.isCorpseInventory(e.getClickedInventory())) {
            ItemStack clickedItem = e.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                e.setCancelled(true);
            } /*else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PAPER) {
                Player p = (Player) e.getWhoClicked();

                Inventory corpseInventory = e.getClickedInventory();
                Inventory playerInventory = p.getInventory();

                CorpseMenu corpseMenu = corpseManager.getCorpseMenuByPlayer(p);

                if(corpseMenu == null){
                    return;
                }

                corpseMenu.swapContents(playerInventory, corpseInventory);

                p.sendMessage("§aVous avez récupéré tout le stuff de la tombe.");
                p.closeInventory();
            }*/
        }
    }

    @EventHandler
    public void onBreak(CustomBlockBreakEvent e) {
        ItemStack block = e.getCustomBlockItem();

        if (block.hasItemMeta() && block.getItemMeta().hasDisplayName() && block.getItemMeta().getDisplayName().equalsIgnoreCase("§fgrave")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (corpseManager.isCorpseInventory(e.getInventory())) {
            corpseManager.close(e.getInventory());
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();

        for (Block block : blocks) {
            for (CorpseBlock corpseBlock : corpseManager.getCorpses()) {
                if (!corpseBlock.getLocation().getBlock().equals(block)) {
                    continue;
                }
               corpseManager.remove(corpseBlock);
           }
        }
    }

}
