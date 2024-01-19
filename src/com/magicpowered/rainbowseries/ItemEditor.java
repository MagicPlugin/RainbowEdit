package com.magicpowered.rainbowseries;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Supplier;

public class ItemEditor implements Listener {

    private final RainbowEdit plugin;
    private FileManager fileManager;

    private Map<UUID, ItemStack> previewBackup = new HashMap<>();

    public ItemEditor(RainbowEdit plugin, FileManager fileManager) {
        this.fileManager = fileManager;
        this.plugin = plugin;
    }

    /**
     * 为玩家手中的物品设置名称。
     *
     * @param player 玩家
     * @param name 新的物品名称
     */
    public void setName(Player player, String name) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
            player.sendMessage(fileManager.getMessage("name-set-successfully").replace("%new_name%", name));
        }
    }

    /**
     * 向玩家手中的物品添加一行Lore。
     *
     * @param player 玩家
     * @param loreToAdd 要添加的Lore
     */
    public void addLore(Player player, String loreToAdd) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            lore.add(loreToAdd);
            meta.setLore(lore);
            item.setItemMeta(meta);
            player.sendMessage(fileManager.getMessage("lore-add-successfully").replace("%add_str%", loreToAdd));
        }
    }

    /**
     * 修改玩家手中物品的指定行Lore。
     *
     * @param player 玩家
     * @param lineToSet 要修改的Lore行号
     * @param newLore 要设置的Lore内容
     */
    public void setLore(Player player, int lineToSet, String newLore) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查玩家手中是否有物品
        if (item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();

            // 检查物品是否有Lore
            if (lore == null || lore.isEmpty()) {
                player.sendMessage(fileManager.getMessage("item-has-no-lore"));
                return;
            }

            // 检查指定的lore行是否存在
            if (lineToSet < 1 || lineToSet > lore.size()) {
                player.sendMessage(fileManager.getMessage("invalid-lore-line").replace("%line_invalid%", Integer.toString(lineToSet)));
                return;
            }

            // 设置lore的内容
            lore.set(lineToSet - 1, newLore); // List是0基索引，而我们的行号是1基
            meta.setLore(lore);
            item.setItemMeta(meta);
            player.sendMessage(fileManager.getMessage("lore-line-set")
                    .replace("%set_line%", Integer.toString(lineToSet))
                    .replace("%set_new%", newLore));
        }
    }

    /**
     * 在指定行之前插入Lore。
     *
     * @param player 玩家
     * @param line 插入位置的行号
     * @param loreToInsert 要插入的Lore内容
     */
    public void insertLoreBefore(Player player, int line, String loreToInsert) {
        handleLoreInsertion(player, line, loreToInsert, true);
    }

    /**
     * 在指定行之后插入Lore。
     *
     * @param player 玩家
     * @param line 插入位置的行号
     * @param loreToInsert 要插入的Lore内容
     */
    public void insertLoreAfter(Player player, int line, String loreToInsert) {
        handleLoreInsertion(player, line, loreToInsert, false);
    }

    private void handleLoreInsertion(Player player, int line, String loreToInsert, boolean before) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查玩家手中是否有物品
        if (item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();

            // 检查物品是否有Lore
            if (lore == null) {
                lore = new ArrayList<>();
            }

            // 检查插入的行是否在有效范围内
            if (before && (line < 1 || line > lore.size() + 1) || !before && (line < 1 || line > lore.size())) {
                player.sendMessage(fileManager.getMessage("invalid-lore-line").replace("%line_invalid%", String.valueOf(line)));
                return;
            }

            if (before) {
                lore.add(line - 1, loreToInsert);
            } else {
                lore.add(line, loreToInsert);
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            String mode = fileManager.getConfig().getString("lore-insert-before", "前").replace("&", "§");
            if (!before) mode = fileManager.getConfig().getString("lore-insert-after", "后").replace("&", "§");
            player.sendMessage(fileManager.getMessage("lore-line-inserted").replace("%insert_mode%", mode)
                                                                                .replace("%insert_line%", String.valueOf(line))
                                                                                .replace("%insert_str%", loreToInsert));
        }
    }

    /**
     * 删除指定行的Lore。
     *
     * @param player 玩家
     * @param lineToRemove 要删除的Lore行号
     */
    public void removeLoreLine(Player player, int lineToRemove) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查玩家手中是否有物品
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();

            // 检查物品是否有Lore
            if (lore == null || lore.isEmpty()) {
                player.sendMessage(fileManager.getMessage("item-has-no-lore"));
                return;
            }

            // 检查指定的lore行是否存在
            if (lineToRemove < 1 || lineToRemove > lore.size()) {
                player.sendMessage(fileManager.getMessage("invalid-lore-line").replace("%line_invalid%", Integer.toString(lineToRemove)));
                return;
            }

            // 移除指定行的lore
            lore.remove(lineToRemove - 1); // List是0基索引，而我们的行号是1基
            meta.setLore(lore);
            item.setItemMeta(meta);
            player.sendMessage(fileManager.getMessage("lore-line-removed").replace("%remove_line%", Integer.toString(lineToRemove)));
        }
    }

    /**
     * 删除所有Lore。
     *
     * @param player 玩家
     */
    public void clearLore(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查玩家手中是否有物品
        if (item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 移除所有lore
            meta.setLore(new ArrayList<>());
            item.setItemMeta(meta);
            player.sendMessage(fileManager.getMessage("lore-cleared-successfully"));
        }
    }

    /**
     * 在指定的 Lore 行中替换 OldString 为 NewString。
     *
     * @param player 玩家
     * @param line 指定的Lore行
     * @param oldString 要被替换的字符串
     * @param newString 新的字符串
     */
    public void replaceInLore(Player player, int line, String oldString, String newString) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查玩家手中是否有物品
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();

            // 检查物品是否有Lore
            if (lore == null || lore.isEmpty()) {
                player.sendMessage(fileManager.getMessage("item-has-no-lore"));
                return;
            }

            // 检查指定的lore行是否存在
            if (line < 1 || line > lore.size()) {
                player.sendMessage(fileManager.getMessage("invalid-lore-line").replace("%line_invalid%", Integer.toString(line)));
                return;
            }

            // 检查OldString是否存在于指定的lore行
            String currentLine = lore.get(line - 1);
            if (!currentLine.contains(oldString)) {
                player.sendMessage(fileManager.getMessage("old-string-not-found").replace("%replace_line%", String.valueOf(line)).replace("%replace_old%", oldString));
                return;
            }

            // 替换OldString为NewString
            String replacedLine = currentLine.replace(oldString, newString.replace("&", "§"));
            lore.set(line - 1, replacedLine);
            meta.setLore(lore);
            item.setItemMeta(meta);
            player.sendMessage(fileManager.getMessage("lore-replaced-successfully")
                            .replace("%replace_line%", String.valueOf(line))
                    .replace("%replace_old%", oldString)
                    .replace("%replace_new%", newString));
        }
    }

    /*
     #############################################################################################################

                                                       预览模式处理开始

     #############################################################################################################
     */

    private Map<UUID, Integer> previewTaskIds = new HashMap<>();
    private Map<UUID, Integer> previewItemSlots = new HashMap<>();


    /**
     * 为玩家发送编辑时消息
     *
     * @param player 玩家
     * @param message 消息
     */
    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    /**
     * 为玩家启用预览模式并备份物品。
     *
     * @param player 玩家
     */
    public void enterPreviewMode(Player player) {
        if (isInPreviewMode(player)) {
            player.sendMessage(fileManager.getMessage("now-in-preview-mode"));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            player.sendMessage(fileManager.getMessage("no-item-in-hand"));
            return;
        }

        // 备份物品
        ItemStack backup = item.clone();
        previewBackup.put(player.getUniqueId(), backup);

        player.sendMessage(fileManager.getMessage("entered-preview-mode"));

        int slot = getHotbarSlot(player, item);

        if (slot == -1) {
            player.sendMessage(fileManager.getMessage("item-not-in-hotbar"));
            return;
        }

        previewItemSlots.put(player.getUniqueId(), slot);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            String message = fileManager.getActionBarMessage(slot);
            sendActionBar(player, message);
        }, 0L, 20L);

        previewTaskIds.put(player.getUniqueId(), taskId);

    }

    /**
     * 应用玩家的修改并退出预览模式。
     *
     * @param player 玩家
     */
    public void applyPreviewChanges(Player player) {
        if (!isInPreviewMode(player)) {
            player.sendMessage(fileManager.getMessage("not-in-preview-mode"));
            return;
        }

        // 获取物品所在的快捷栏位置
        int slot = previewItemSlots.get(player.getUniqueId()) - 1;
        if (slot >= 0 && slot < 9) {
            // 应用更改到正确的快捷栏位置
            player.getInventory().setItem(slot, player.getInventory().getItemInMainHand());
        }

        // 清理
        cleanupPreviewMode(player);
        player.sendMessage(fileManager.getMessage("applied-preview-changes"));
    }

    /**
     * 取消玩家的修改，还原物品，并退出预览模式。
     *
     * @param player 玩家
     */
    public void cancelPreviewChanges(Player player) {
        if (!isInPreviewMode(player)) {
            player.sendMessage(fileManager.getMessage("not-in-preview-mode"));
            return;
        }

        // 获取物品所在的快捷栏位置
        int slot = previewItemSlots.get(player.getUniqueId()) - 1;
        if (slot >= 0 && slot < 9) {
            // 用备份的物品还原正确的快捷栏位置
            player.getInventory().setItem(slot, previewBackup.get(player.getUniqueId()));
        }

        // 清理
        cleanupPreviewMode(player);
        player.sendMessage(fileManager.getMessage("canceled-preview-changes"));
    }

    private void cleanupPreviewMode(Player player) {
        // 删除备份并退出预览模式
        previewBackup.remove(player.getUniqueId());
        previewItemSlots.remove(player.getUniqueId());

        // 停止ActionBar消息发送任务
        Integer taskId = previewTaskIds.get(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            previewTaskIds.remove(player.getUniqueId());
        }
    }

    /**
     * 检查玩家是否在预览模式。
     *
     * @param player 玩家
     * @return 是否在预览模式
     */
    public boolean isInPreviewMode(Player player) {
        return previewBackup.containsKey(player.getUniqueId());
    }


    /**
     * 寻找物品的位置。
     *
     * @param player 玩家
     * @param item 要查询的物品
     * @return 物品的位置
     */
    private int getHotbarSlot(Player player, ItemStack item) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (item.equals(inventory.getItem(i))) {
                return i + 1; // 返回 1 到 9，对应快捷栏的位置
            }
        }
        return -1; // 如果物品不在快捷栏，返回-1
    }


    // 当玩家打开了预览模式但退出游戏
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isInPreviewMode(player)) {
            cancelPreviewChanges(player);
        }
    }

    // 防止玩家切换处于预览模式的物品的位置
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        if (isInPreviewMode(player)) {
            // 获取预览模式物品的快捷栏位置
            Integer slot = previewItemSlots.get(playerUUID);
            if (slot == null) {
                return;
            }

            // 如果玩家尝试从预览模式物品的快捷栏位置操作物品，取消事件
            if (event.getSlot() == slot - 1) {
                event.setCancelled(true);
                player.sendMessage(fileManager.getMessage("cannot-move-preview-item"));
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 检查玩家是否在预览模式中
        if (isInPreviewMode(player)) {
            // 获取预览模式物品的快捷栏位置
            Integer slot = previewItemSlots.get(playerUUID);
            if (slot == null) {
                return;
            }

            // 检查玩家是否尝试丢弃锁定格子中的物品
            if (event.getPlayer().getInventory().getHeldItemSlot() == slot - 1) { // 格子位置从0开始计算
                event.setCancelled(true);
                player.sendMessage(fileManager.getMessage("cannot-drop-preview-item"));
            }
        }
    }



}

