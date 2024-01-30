package com.magicpowered.rainbowseries;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListener implements CommandExecutor, TabCompleter {

    private RainbowEdit plugin;
    private FileManager fileManager;
    private ItemEditor itemEditor;

    public CommandListener(RainbowEdit plugin, FileManager fileManager, ItemEditor itemEditor) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.itemEditor = itemEditor;
    }

    public String replaceUnderscores(String input) {
        // 使用StringBuilder来构建最终的字符串
        StringBuilder output = new StringBuilder();
        // 标记是否在转义模式
        boolean escaping = false;

        // 遍历输入字符串的每一个字符
        for (char c : input.toCharArray()) {
            if (escaping) {
                // 如果前一个字符是转义字符'\'
                if (c == '_') {
                    // 如果当前字符是'_',则添加一个'_'
                    output.append(c);
                } else {
                    // 如果当前字符不是'_',则添加一个'\'
                    output.append('\\').append(c);
                }
                // 关闭转义模式
                escaping = false;
            } else if (c == '\\') {
                // 如果当前字符是转义字符，开启转义模式
                escaping = true;
            } else if (c == '_') {
                // 如果当前字符是'_'，替换为一个空格
                output.append(' ');
            } else {
                // 否则，直接添加字符
                output.append(c);
            }
        }

        // 如果字符串以转义字符结束，添加它
        if (escaping) {
            output.append('\\');
        }

        return output.toString();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length == 0) {
            sender.sendMessage("§7[§b彩虹编辑§7] 帮助:");
            sender.sendMessage("§7必要参数: <?>, 可选必要参数: <?/?>, 非必要参数: [?], 可选非必要参数: [?/?]");
            sender.sendMessage("§7  |- §b/re command §7- 查看文字指令帮助");
            sender.sendMessage("§7  |- §b/ru ? §7- 查看 §b查看符号指令帮助");
            sender.sendMessage("§7  |- §7使用 §b_ §7- 可以表示空格, 用以输入多个连续空格");
            sender.sendMessage("§7  |- §7使用 §b\\_ §7- 表示下划线");
            sender.sendMessage("§7  |- §b/ru reload §7- 重新载入配置文件 (管理员)");
            return true;
        }

        for (int i = 0; i < args.length; i++) {
            args[i] = replaceUnderscores(args[i]);
        }

        switch (args[0].toLowerCase()) {
            case "!":
            case "name":
                if (!sender.hasPermission("rainbowedit.name")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <String>");
                    return true;
                }


                String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).replace("&", "§");
                itemEditor.setName(player, name);
                return true;

            case "+":
            case "add":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <String>");
                    return true;
                }

                String loreToAdd = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).replace("&", "§");
                itemEditor.addLore(player, loreToAdd);
                return true;

            case "@":
            case "set":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <String>");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <line>");
                    return true;
                }

                int lineToSet;
                try {
                    lineToSet = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 参数 " + args[1] + " 不是数字");
                    return true;
                }

                String newLore = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).replace("&", "§");
                itemEditor.setLore(player, lineToSet, newLore);
                return true;
                
            case ">":    
            case "before":
            case "<":    
            case "after":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <line>");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <String>");
                    return true;
                }

                int line;
                try {
                    line = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 参数 " + args[1] + " 不是数字");
                    return true;
                }

                String lore = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).replace("&", "§");
                if (args[0].equalsIgnoreCase("before") || args[0].equalsIgnoreCase(">")) {
                    itemEditor.insertLoreBefore(player, line, lore);
                } else if (args[0].equalsIgnoreCase("after") || args[0].equalsIgnoreCase("<")) {
                    itemEditor.insertLoreAfter(player, line, lore);
                }
                return true;

            case "#":
            case "replace":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <line>");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <oldString> <newString>");
                    return true;
                }
                if (args.length < 4) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <newString>");
                    return true;
                }

                int lineToReplace;
                try {
                    lineToReplace = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 参数 " + args[1] + " 不是数字");
                    return true;
                }

                String oldText = args[2].replace("&", "§");
                String newText = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).replace("&", "§");
                itemEditor.replaceInLore(player, lineToReplace, oldText, newText);
                return true;

            case "-":
            case "remove":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 缺少必要参数: <line>");
                    return true;
                }

                int lineToRemove;
                try {
                    lineToRemove = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§7[§b彩虹编辑§7] 错误, 参数 " + args[1] + " 不是数字");
                    return true;
                }

                itemEditor.removeLoreLine(player, lineToRemove);
                return true;

            case "%":
            case "clear":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                itemEditor.clearLore(player);
                return true;

            case "$":
            case "preview":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                itemEditor.enterPreviewMode(player);
                return true;

            case ".":
            case "apply":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (!itemEditor.isInPreviewMode(player)) {
                    player.sendMessage(fileManager.getMessage("not-in-preview-mode"));
                    return true;
                }
                itemEditor.applyPreviewChanges(player);
                return true;

            case ":":
            case "cancel":
                if (!sender.hasPermission("rainbowedit.lore")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                if (!itemEditor.isInPreviewMode(player)) {
                    player.sendMessage(fileManager.getMessage("not-in-preview-mode"));
                    return true;
                }
                itemEditor.cancelPreviewChanges(player);
                return true;

            case ",":
            case "reload":
                if (!sender.hasPermission("rainbowedit.reload")) {
                    sender.sendMessage("§7[§b彩虹编辑§7] 您没有执行此命令的权限");
                    return true;
                }

                fileManager.reloadConfig();
                sender.sendMessage("§7[§b彩虹编辑§7] 配置文件已重新加载");
                return true;

            case "?":
                sender.sendMessage("§7[§b彩虹编辑§7] 符号指令帮助:");
                sender.sendMessage("§7必要参数: <?>, 可选必要参数: <?/?>, 非必要参数: [?], 可选非必要参数: [?/?]");
                sender.sendMessage("§7  |- §b/re ! <String> §7- 修改名字");
                sender.sendMessage("§7  |- §b/re + <String> §7- 添加新的行");
                sender.sendMessage("§7  |- §b/re @ <line> <String> §7- 修改指定行");
                sender.sendMessage("§7  |- §b/re > <line> <String> §7- 在指定行前插入一行");
                sender.sendMessage("§7  |- §b/re < <line> <String> §7- 在指定行后插入一行");
                sender.sendMessage("§7  |- §b/re # <line> <oldString> <newString> §7- 替换指定行中的字符");
                sender.sendMessage("§7  |- §b/re - <line> §7- 删除指定行");
                sender.sendMessage("§7  |- §b/re % §7- 清除所有行");
                sender.sendMessage("§7  |- §b/re $ §7- 进入预览模式");
                sender.sendMessage("§7  |- §b/re . §7- 应用更改");
                sender.sendMessage("§7  |- §b/re : §7- 取消更改并退出预览模式");
                return true;
            case "help":
                sender.sendMessage("§7[§b彩虹编辑§7] 文字指令帮助:");
                sender.sendMessage("§7必要参数: <?>, 可选必要参数: <?/?>, 非必要参数: [?], 可选非必要参数: [?/?]");
                sender.sendMessage("§7  |- §b/re name <String> §7- 修改名字");
                sender.sendMessage("§7  |- §b/re add <String> §7- 添加新的行");
                sender.sendMessage("§7  |- §b/re set <line> <String> §7- 修改指定行");
                sender.sendMessage("§7  |- §b/re before <line> <String> §7- 在指定行前插入一行");
                sender.sendMessage("§7  |- §b/re after <line> <String> §7- 在指定行后插入一行");
                sender.sendMessage("§7  |- §b/re replace <line> <oldString> <newString> §7- 替换指定行中的字符");
                sender.sendMessage("§7  |- §b/re remove <line> §7- 删除指定行");
                sender.sendMessage("§7  |- §b/re preview §7- 进入预览模式");
                sender.sendMessage("§7  |- §b/re clear §7- 清除所有的Lore");
                sender.sendMessage("§7  |- §b/re apply §7- 应用更改");
                sender.sendMessage("§7  |- §b/re cancel §7- 取消更改并退出预览模式");
                return true;

            default:
                sender.sendMessage("§7[§b彩虹编辑§7] 这是一个不存在的命令或拼写错误: " + args[0]);
                sender.sendMessage("§7  |- 输入 §b/re <help/?> §7查看帮助");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("!", "+", "@", ">", "<", "#", "-", "%", "$", ".", ":"));
            completions.addAll(Arrays.asList("name", "add", "set", "before", "after", "replace", "remove", "preview", "clear", "apply", "cancel"));
        } else if (args.length == 2 && Arrays.asList("set", "before", "after", "replace", "remove", "@", ">", "<", "#", "-").contains(args[0])) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    int loreSize = item.getItemMeta().getLore().size();
                    for (int i = 1; i <= loreSize; i++) {
                        completions.add(String.valueOf(i));
                    }
                }
            }
        }

        return completions;
    }
}
