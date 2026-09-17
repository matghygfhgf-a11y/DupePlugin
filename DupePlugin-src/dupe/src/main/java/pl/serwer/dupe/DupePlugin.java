package pl.serwer.dupe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DupePlugin extends JavaPlugin implements TabExecutor {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<Material> blocked = new HashSet<>();

    private int cooldownSeconds;
    private int maxAmount;
    private boolean requirePermission;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        Command cmd = getCommand("dupe");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }

        getLogger().info("DupePlugin wlaczony. Cooldown: " + cooldownSeconds + "s, max ilosc: " + maxAmount);
    }

    private void loadSettings() {
        reloadConfig();
        cooldownSeconds = getConfig().getInt("cooldown-sekundy", 3);
        maxAmount = Math.max(1, getConfig().getInt("max-ilosc", 64));
        requirePermission = getConfig().getBoolean("wymagaj-permisji", false);

        blocked.clear();
        List<String> list = getConfig().getStringList("zabronione-przedmioty");
        for (String name : list) {
            Material m = Material.matchMaterial(name.trim().toUpperCase(Locale.ROOT));
            if (m != null) {
                blocked.add(m);
            } else {
                getLogger().warning("Nieznany przedmiot w config.yml: " + name);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("dupe.admin")) {
                sender.sendMessage(Component.text("Nie masz uprawnien.", NamedTextColor.RED));
                return true;
            }
            loadSettings();
            sender.sendMessage(Component.text("Konfiguracja przeladowana.", NamedTextColor.GREEN));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Tej komendy uzyje tylko gracz w grze.", NamedTextColor.RED));
            return true;
        }

        if (requirePermission && !player.hasPermission("dupe.use")) {
            player.sendMessage(Component.text("Nie masz uprawnien do kopiowania.", NamedTextColor.RED));
            return true;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand.getType().isAir()) {
            player.sendMessage(Component.text("Wez przedmiot do reki, ktory chcesz skopiowac.", NamedTextColor.RED));
            return true;
        }

        if (blocked.contains(inHand.getType())) {
            player.sendMessage(Component.text("Tego przedmiotu nie mozna kopiowac.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("dupe.bypass") && cooldownSeconds > 0) {
            long now = System.currentTimeMillis();
            Long last = cooldowns.get(player.getUniqueId());
            if (last != null) {
                long left = (last + cooldownSeconds * 1000L) - now;
                if (left > 0) {
                    long sec = (left / 1000L) + 1;
                    player.sendMessage(Component.text("Odczekaj jeszcze " + sec + "s.", NamedTextColor.YELLOW));
                    return true;
                }
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        int amount = inHand.getAmount();
        if (args.length >= 1) {
            int requested;
            if (args[0].equalsIgnoreCase("stack")) {
                requested = inHand.getMaxStackSize();
            } else {
                try {
                    requested = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {
                    player.sendMessage(Component.text("Podaj ilosc, np. /dupe 16", NamedTextColor.RED));
                    return true;
                }
            }
            if (requested < 1) {
                player.sendMessage(Component.text("Ilosc musi byc wieksza od zera.", NamedTextColor.RED));
                return true;
            }
            int limit = player.hasPermission("dupe.bypass") ? 2304 : maxAmount;
            if (requested > limit) {
                player.sendMessage(Component.text("Maksymalna ilosc na raz to " + limit + ".", NamedTextColor.YELLOW));
                requested = limit;
            }
            amount = requested;
        }

        int perStack = Math.max(1, inHand.getMaxStackSize());
        int left = amount;
        while (left > 0) {
            int size = Math.min(left, perStack);
            ItemStack copy = inHand.clone();
            copy.setAmount(size);
            left -= size;

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(copy);
            for (ItemStack rest : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), rest);
            }
        }

        player.sendMessage(Component.text("Skopiowano: ", NamedTextColor.GREEN)
                .append(Component.text(inHand.getType().name().toLowerCase(Locale.ROOT).replace('_', ' '), NamedTextColor.WHITE))
                .append(Component.text(" x" + amount, NamedTextColor.GRAY)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>(List.of("1", "16", "32", "64", "stack"));
            if (sender.hasPermission("dupe.admin")) {
                out.add("reload");
            }
            String start = args[0].toLowerCase(Locale.ROOT);
            out.removeIf(s -> !s.startsWith(start));
            return out;
        }
        return Collections.emptyList();
    }
}
