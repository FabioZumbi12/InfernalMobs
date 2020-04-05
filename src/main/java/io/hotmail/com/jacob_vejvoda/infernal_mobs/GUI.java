package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

public class GUI implements Listener {
    private static infernal_mobs plugin;
    private static HashMap<String, Scoreboard> playerScoreBoard = new HashMap<String, Scoreboard>();
    private static HashMap<Entity, Object> bossBars = new HashMap<Entity, Object>();

    GUI(infernal_mobs instance) {
        plugin = instance;
    }
    
    public static Entity getNearbyBoss(Player p) {
        double dis = 26.0D;
        for (InfernalMob m : plugin.infernalList) {
            if (m.entity.getWorld().equals(p.getWorld())) {
                Entity boss = m.entity;
                if (p.getLocation().distance(boss.getLocation()) < dis) {
                    dis = p.getLocation().distance(boss.getLocation());
                    return boss;
                }
            }
        }
        return null;
    }

    static void fixBar(Player p) {
        //System.out.println("fixBar");
        Entity b = getNearbyBoss(p);
        if (b != null) {
            //System.out.println("Dead: " + b.isDead());
            //System.out.println("HP: " + ((Damageable)b).getHealth());
            if (b.isDead() || ((Damageable) b).getHealth() <= 0) {
                if (plugin.getConfig().getBoolean("enableBossBar")) {
                    try {
                        for (Player p2 : ((BossBar) bossBars.get(b)).getPlayers())
                            ((BossBar) bossBars.get(b)).removePlayer(p2);
                        bossBars.remove(b);
                    } catch (Exception x) {
                    }
                }
                int mobIndex = plugin.idSearch(b.getUniqueId());
                try {
                    if (mobIndex != -1)
                        plugin.removeMob(mobIndex);
                } catch (IOException e) {
                }
                clearInfo(p);
            } else {
                if (plugin.getConfig().getBoolean("enableBossBar")) {
                    showBossBar(p, b);
                }
                if (plugin.getConfig().getBoolean("enableScoreBoard")) {
                    fixScoreboard(p, b, plugin.findMobAbilities(b.getUniqueId()));
                }
            }
        } else
            clearInfo(p);
    }

    @SuppressWarnings("deprecation")
    private static void showBossBar(Player p, Entity e) {
        List<String> oldMobAbilityList = plugin.findMobAbilities(e.getUniqueId());
        String tittle = plugin.getConfig().getString("bossBarsName", "&fLevel <powers> &fInfernal <mobName>");
        String mobName = e.getType().getName().replace("_", " ");
        if (e.getType().equals(EntityType.SKELETON)) {
            Skeleton sk = (Skeleton) e;
            if (sk.getSkeletonType().equals(Skeleton.SkeletonType.WITHER)) {
                mobName = "WitherSkeleton";
            }
        }
        String prefix = plugin.getConfig().getString("namePrefix", "&fInfernal");
        if (plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size()) != null) {
            prefix = plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size());
        }
        tittle = tittle.replace("<prefix>", prefix.substring(0, 1).toUpperCase() + prefix.substring(1));
        tittle = tittle.replace("<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
        tittle = tittle.replace("<mobLevel>", oldMobAbilityList.size() + "");
        String abilities = plugin.generateString(5, oldMobAbilityList);
        int count = 4;
        try {
            do {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
                if (count <= 0) {
                    break;
                }
            } while (tittle.length() + abilities.length() + mobName.length() > 64);
        } catch (Exception x) {
            System.out.println("showBossBar error: ");
            x.printStackTrace();
        }
        tittle = tittle.replace("<abilities>", abilities.substring(0, 1).toUpperCase() + abilities.substring(1));
        tittle = ChatColor.translateAlternateColorCodes('&', tittle);

        if (!bossBars.containsKey(e)) {
            BarColor bc = BarColor.valueOf(plugin.getConfig().getString("bossBarSettings.defaultColor"));
            BarStyle bs = BarStyle.valueOf(plugin.getConfig().getString("bossBarSettings.defaultStyle"));
            //Per Level Setings
            String lc = plugin.getConfig().getString("bossBarSettings.perLevel." + oldMobAbilityList.size() + ".color");
            if (lc != null)
                bc = BarColor.valueOf(lc);
            String ls = plugin.getConfig().getString("bossBarSettings.perLevel." + oldMobAbilityList.size() + ".style");
            if (ls != null)
                bs = BarStyle.valueOf(ls);
            //Per InfernalMob Setings
            String mc = plugin.getConfig().getString("bossBarSettings.perMob." + e.getType().getName() + ".color");
            if (mc != null)
                bc = BarColor.valueOf(mc);
            String ms = plugin.getConfig().getString("bossBarSettings.perMob." + e.getType().getName() + ".style");
            if (ms != null)
                bs = BarStyle.valueOf(ms);
            BossBar bar = Bukkit.createBossBar(tittle, bc, bs, BarFlag.CREATE_FOG);
            bar.setVisible(true);
            bossBars.put(e, bar);
        }
        if (!((BossBar) bossBars.get(e)).getPlayers().contains(p))
            ((BossBar) bossBars.get(e)).addPlayer(p);
        float health = (float) ((Damageable) e).getHealth();
        float maxHealth = (float) ((Damageable) e).getMaxHealth();
        float setHealth = (health * 100.0f) / maxHealth;
        ((BossBar) bossBars.get(e)).setProgress(setHealth / 100.0f);
    }

    @SuppressWarnings("deprecation")
    private static void clearInfo(Player player) {
        if (plugin.getConfig().getBoolean("enableBossBar")) {
            //BossBarAPI.removeBar(player);
            for (Entry<Entity, Object> hm : bossBars.entrySet())
                if (((BossBar) hm.getValue()).getPlayers().contains(player))
                    ((BossBar) hm.getValue()).removePlayer(player);
        }
        if (plugin.getConfig().getBoolean("enableScoreBoard")) {
            try {
                player.getScoreboard().resetScores(player);
                player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
            } catch (Exception localException1) {
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void fixScoreboard(Player player, Entity e, List<String> abilityList) {
        if (plugin.getConfig().getBoolean("enableScoreBoard") && (e instanceof Damageable)) {
            //String name = getMobNameTag(e);
            //Get Display
            //System.out.println("sb1");
            if (playerScoreBoard.get(player.getName()) == null) {
                //System.out.println("Creating ScoreBoard");
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getNewScoreboard();
                playerScoreBoard.put(player.getName(), board);
            }
            //System.out.println("sb2");
            Objective o;
            Scoreboard board = playerScoreBoard.get(player.getName());
            //System.out.println("Board = " + board);
            if (board.getObjective(DisplaySlot.SIDEBAR) == null) {
                o = board.registerNewObjective(player.getName(), "dummy");
                o.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                o = board.getObjective(DisplaySlot.SIDEBAR);
            }
            //System.out.println("sb3");
            //Name
            //System.out.println("Name: " + e.getType().getName());
            //System.out.println("OBJ = " + o);
            o.setDisplayName(e.getType().getName());
            //System.out.println("Set ScoreBoard Name");
            //Remove Old
            //for(OfflinePlayer p : board.getPlayers())
            //    board.resetScores(p);
            for (String s : board.getEntries())
                board.resetScores(s);
            //Power Display
            int score = 1;
            //System.out.println("sb4");
            for (String ability : abilityList) {
                //Score pointsDisplayScore = o.getScore(Bukkit.getOfflinePlayer("§r" + ability));
                //pointsDisplayScore.setScore(score);
                o.getScore("§r" + ability).setScore(score);
                score = score + 1;
            }
            //Score abDisplayScore = o.getScore(Bukkit.getOfflinePlayer("§e§lAbilities:"));
            //abDisplayScore.setScore(score);
            o.getScore("§e§lAbilities:").setScore(score);
            //Health
            //System.out.println("sb5");
            if (plugin.getConfig().getBoolean("showHealthOnScoreBoard") == true) {
                //System.out.println("shosb");
                //Display HP
                score = score + 1;
                float health = (float) ((Damageable) e).getHealth();
                float maxHealth = (float) ((Damageable) e).getMaxHealth();
                double roundOff = Math.round(health * 100.0) / 100.0;
                //Score hDisplayScore = o.getScore(Bukkit.getOfflinePlayer(roundOff + "/" + maxHealth));
                //hDisplayScore.setScore(score);
                o.getScore(roundOff + "/" + maxHealth).setScore(score);
                score = score + 1;
                //Score htDisplayScore = o.getScore(Bukkit.getOfflinePlayer("§e§lHealth:"));
                //htDisplayScore.setScore(score);
                o.getScore("§e§lHealth:").setScore(score);
            }
            //System.out.println("sb6");
            //Display
            if ((player.getScoreboard() == null) || (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR) == null) || (player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName() == null) || (!player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals(board.getObjective(DisplaySlot.SIDEBAR).getName()))) {
                //System.out.println("Set SB");
                player.setScoreboard(board);
            }
            //System.out.println("sb7");
        }
    }

    public void setName(Entity ent) {
        try {
            //System.out.println("SN1 " + ent);
            if (plugin.getConfig().getInt("nameTagsLevel") != 0) {
                String tittle = getMobNameTag(ent);
                ent.setCustomName(tittle);
                if (plugin.getConfig().getInt("nameTagsLevel") == 2) {
                    ent.setCustomNameVisible(true);
                }
            }
        } catch (Exception x) {
            System.out.println("Error in setName: ");
            x.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public String getMobNameTag(Entity entity) {
        List<String> oldMobAbilityList = plugin.findMobAbilities(entity.getUniqueId());
        String tittle = null;
        try {
            tittle = plugin.getConfig().getString("nameTagsName", "&fInfernal <mobName>");
            String mobName = entity.getType().getName().replace("_", " ");

            tittle = tittle.replace("<mobName>", mobName.substring(0, 1).toUpperCase() + mobName.substring(1));
            tittle = tittle.replace("<mobLevel>", "" + oldMobAbilityList.size());
            String abilities;
            int count = 4;
            do {
                abilities = plugin.generateString(count, oldMobAbilityList);
                count--;
            } while ((tittle.length() + abilities.length() + mobName.length()) > 64);
            tittle = tittle.replace("<abilities>", abilities.substring(0, 1).toUpperCase() + abilities.substring(1));
            //Prefix
            String prefix = plugin.getConfig().getString("namePrefix");
            if (plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size()) != null)
                prefix = plugin.getConfig().getString("levelPrefixs." + oldMobAbilityList.size());
            tittle = tittle.replace("<prefix>", prefix.substring(0, 1).toUpperCase() + prefix.substring(1));
            tittle = ChatColor.translateAlternateColorCodes('&', tittle);
        } catch (Exception x) {
            plugin.getLogger().log(Level.SEVERE, x.getMessage());
            x.printStackTrace();
        }
        return tittle;
    }
}