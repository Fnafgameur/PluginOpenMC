package fr.communaywen.core.luckyblocks.listeners;

import dev.lone.itemsadder.api.CustomBlock;
import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.credit.Credit;
import fr.communaywen.core.credit.Feature;
import fr.communaywen.core.luckyblocks.managers.LuckyBlockManager;
import fr.communaywen.core.luckyblocks.utils.LBUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

@Feature("Lucky Blocks")
@Credit("Fnafgameur")
public class LBBlockBreakListener implements Listener {

    private final LuckyBlockManager luckyBlockManager;

    public LBBlockBreakListener(LuckyBlockManager luckyBlockManager) {
        this.luckyBlockManager = luckyBlockManager;
    }

    @EventHandler()
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        String itemName = itemInHand.getType().name();

        if (itemName.toLowerCase().contains("sword") && player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        if (itemInHand.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();
        float blockHardness = block.getType().getHardness();
        float breakSpeed = block.getBreakSpeed(player);

        if (breakSpeed > blockHardness) {
            event.setCancelled(true);
            player.sendMessage("§cVous ne pouvez pas casser ce bloc aussi rapidement. (merci ItemsAdder)");
            return;
        }

        if (!LBUtils.canDestroyBlockInClaim(player, block)) {
            event.setCancelled(true);
            return;
        }

        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);

        if (customBlock == null) {
            return;
        }

        if (!customBlock.getNamespacedID().equals(LBUtils.getBlockNamespaceID())) {
            return;
        }

        block.setType(Material.AIR);
        luckyBlockManager.getRandomEvent().onOpen(player, block);
    }
}
