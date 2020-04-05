package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class EventListener implements Listener {
    private static infernal_mobs plugin;
    private HashMap<String, Long> spawnerMap = new HashMap<>();

    EventListener(infernal_mobs instance) {
        plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
    	Player p = e.getPlayer();
    	try {
    		ItemStack s = plugin.getDiviningStaff();
    		if(p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(s.getItemMeta().getDisplayName())) {
    	        Entity b = GUI.getNearbyBoss(p);
    	        //System.out.println("GB");
    	        //Make Look At
    	        if(b != null) {
    	        	//Take Powder
    	        	boolean took = false;
    	        	for(ItemStack i : p.getInventory())
    	        		if(i != null && i.getType().equals(Material.BLAZE_POWDER)) {
    	        			if(i.getAmount() == 1) {
    	        				p.getInventory().remove(i);
    	        			}else
    	        				i.setAmount(i.getAmount()-1);
    	        			took = true;
    	        			break;
    	        		}
    	        	if(!took) {
    	        		p.sendMessage("§cYou need blaze powder to use this!");
    	        		return;
    	        	}
    	        	//Change Looking
	    			Entity source = b;
	    			Entity target = p;
	    	     
	    	        Vector direction = getVector(target).subtract(getVector(source)).normalize();
	    	        double x = direction.getX();
	    	        double y = direction.getY();
	    	        double z = direction.getZ();
	    	     
	    	        // Now change the angle
	    	        Location changed = target.getLocation().clone();
	    	        changed.setYaw(180 - toDegree(Math.atan2(x, z)));
	    	        changed.setPitch(90 - toDegree(Math.acos(y)));
	    	        target.teleport(changed);
	    	        //Beam
	    	        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
	    	        	public void run(){
	    	    			//Shoot Beam
	    	    			Location eyeLoc = p.getEyeLocation();
	    	    			double px = eyeLoc.getX();
	    	    			double py = eyeLoc.getY();
	    	    			double pz = eyeLoc.getZ();
	    	    			double yaw  = Math.toRadians(eyeLoc.getYaw() + 90);
	    	    			double pitch = Math.toRadians(eyeLoc.getPitch() + 90);
	    	    			double x = Math.sin(pitch) * Math.cos(yaw);
	    	    			double y = Math.sin(pitch) * Math.sin(yaw);
	    	    			double z = Math.cos(pitch);
	    	    			for (int j = 1 ; j <= 10 ; j++) {
	    		    			for (int i = 1 ; i <= 10 ; i++) {
	    							Location loc = new Location(p.getWorld(), px + (i * x), py + (i * z), pz + (i * y));
	    							beamParticals(loc);
	    		    			}
	    	    			}
	    	        	}
	    	        }, 5);
    	        }
    		}
    	}catch(Exception x) {}
    }
    
    private void beamParticals(Location loc){
    	int speed = -1;
    	int amount = 1;
        double r = 0;
        plugin.displayParticle(Particle.DRIP_LAVA.toString(), loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), r, speed, amount);
    }
     
    private float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }
     
    private Vector getVector(Entity entity) {
        if (entity instanceof Player)
            return ((Player) entity).getEyeLocation().toVector();
        else
            return entity.getLocation().toVector();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity ent = e.getRightClicked();
        if (plugin.errorList.contains(p)) {
            plugin.errorList.remove(p);
            p.sendMessage("§6Error report:");

            String name = "";
            try {
                name = ent.getCustomName();
            } catch (Exception ignored) {
            }
            p.sendMessage("§eName: §f" + name);
            p.sendMessage("§eSaved: §f" + plugin.mobSaveFile.getString(ent.getUniqueId().toString()));
            p.sendMessage("§eHealth: §f" + ((LivingEntity) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            p.sendMessage("§eInfernal: §f" + plugin.idSearch(ent.getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnitityDamaged(EntityDamageEvent e) {
        Entity mob = e.getEntity();
        if (plugin.idSearch(mob.getUniqueId()) != -1) {
            for (Entity entity : mob.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                if ((entity instanceof Player)) {
                    GUI.fixBar((Player) entity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.levitateList.contains(p)) {
            p.setAllowFlight(false);
            plugin.levitateList.remove(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLightningStrike(LightningStrikeEvent e) {
        for (Entity m : e.getLightning().getNearbyEntities(6.0D, 6.0D, 6.0D)) {
            if (plugin.idSearch(m.getUniqueId()) != -1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World world = event.getPlayer().getWorld();
        plugin.giveMobsPowers(world);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        World world = event.getPlayer().getWorld();
        plugin.giveMobsPowers(world);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity ent : e.getChunk().getEntities()) {
            if (((ent instanceof LivingEntity)) && (ent.getCustomName() != null) && (plugin.mobSaveFile.getString(ent.getUniqueId().toString()) != null)) {
                plugin.giveMobPowers(ent);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent e) {
        for (Entity ent : e.getChunk().getEntities()) {
            int s = plugin.idSearch(ent.getUniqueId());
            if (s != -1) {
                plugin.infernalList.remove(plugin.infernalList.get(s));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        try {
            Entity attacker = event.getDamager();
            Entity victim = event.getEntity();
            Entity mob;
            if ((attacker instanceof Arrow)) {
                Arrow arrow = (Arrow) event.getDamager();
                if (((arrow.getShooter() instanceof Player)) && (!(victim instanceof Player))) {
                    mob = victim;
                    Player player = (Player) arrow.getShooter();
                    plugin.doEffect(player, mob, false);
                } else if ((!(arrow.getShooter() instanceof Player)) && ((victim instanceof Player))) {
                    mob = (Entity) arrow.getShooter();
                    Player player = (Player) victim;
                    plugin.doEffect(player, mob, true);
                }
            } else if ((attacker instanceof Snowball)) {
                Snowball snowBall = (Snowball) event.getDamager();
                if (snowBall.getShooter() != null) {
                    if (((snowBall.getShooter() instanceof Player)) && (!(victim instanceof Player))) {
                        mob = victim;
                        Player player = (Player) snowBall.getShooter();
                        plugin.doEffect(player, mob, false);
                    } else if ((!(snowBall.getShooter() instanceof Player)) && ((victim instanceof Player))) {
                        mob = (Entity) snowBall.getShooter();
                        Player player = (Player) victim;
                        plugin.doEffect(player, mob, true);
                    }
                }
            } else if (((attacker instanceof Player)) && (!(victim instanceof Player))) {
                Player player = (Player) attacker;
                mob = victim;
                plugin.doEffect(player, mob, false);
            } else if ((!(attacker instanceof Player)) && ((victim instanceof Player))) {
                Player player = (Player) victim;
                mob = attacker;
                plugin.doEffect(player, mob, true);
            }
            if (plugin.idSearch(victim.getUniqueId()) != -1) {
                for (Entity entity : victim.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
                    if ((entity instanceof Player)) {
                        GUI.fixBar((Player) entity);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onMobSpawn(CreatureSpawnEvent event) {
        World world = event.getEntity().getWorld();
        if ((!event.getEntity().hasMetadata("NPC")) && (!event.getEntity().hasMetadata("shopkeeper")) && event.getEntity().getCustomName() == null) {
            if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                Block spawner = plugin.blockNear(event.getEntity().getLocation(), Material.SPAWNER, 10);
                if (spawner != null) {
                    String name = plugin.getLocationName(spawner.getLocation());
                    if (plugin.mobSaveFile.getString("infernalSpanwers." + name) != null) {
                        if (this.spawnerMap.get(name) == null) {
                            plugin.makeInfernal(event.getEntity(), true);
                            this.spawnerMap.put(name, plugin.serverTime);
                        } else {
                            long startTime = this.spawnerMap.get(name);
                            long endTime = plugin.serverTime;
                            long timePassed = endTime - startTime;
                            int delay = plugin.mobSaveFile.getInt("infernalSpanwers." + name);
                            if (timePassed >= delay) {
                                plugin.makeInfernal(event.getEntity(), true);
                                this.spawnerMap.put(name, plugin.serverTime);
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }/*else if (!plugin.getConfig().getBoolean("enableFarmingDrops")) {
						return;
					}*/
                }
            }
            //System.out.println("InfernalMob Spawn 2");
            if ((event.getEntity().hasMetadata("NPC")) || (event.getEntity().hasMetadata("shopkeeper"))) {
                return;
            }
            String entName = event.getEntity().getType().name();
            if (((plugin.getConfig().getStringList("mobworlds").contains(world.getName())) || (plugin.getConfig().getStringList("mobworlds").contains("<all>"))) &&
                    (plugin.getConfig().getStringList("enabledmobs").contains(entName)) &&
                    (plugin.getConfig().getInt("naturalSpawnHeight") < event.getEntity().getLocation().getY()) &&
                    (plugin.getConfig().getStringList("enabledSpawnReasons").contains(event.getSpawnReason().toString()))) {
                plugin.makeInfernal(event.getEntity(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e)
            throws IOException {
        if (e.getBlock().getType().equals(Material.SPAWNER)) {
            String name = plugin.getLocationName(e.getBlock().getLocation());
            if (plugin.mobSaveFile.getString("infernalSpanwers." + name) != null) {
                plugin.mobSaveFile.set("infernalSpanwers." + name, null);
                plugin.mobSaveFile.save(plugin.saveYML);
                if (e.getPlayer().isOp()) {
                    e.getPlayer().sendMessage("§cYou broke an infernal mob spawner!");
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            UUID id = event.getEntity().getUniqueId();
            int mobIndex = plugin.idSearch(id);
            if (mobIndex != -1) {
                ArrayList<String> aList;
                if (plugin.findMobAbilities(id) != null) {
                    aList = (ArrayList<String>) plugin.findMobAbilities(id);
                } else {
                    return;
                }
                //ArrayList<String> aList;
                if (aList.contains("explode")) {
                    TNTPrimed tnt = (TNTPrimed) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(1);
                }
                boolean isGhost = false;
                try {
                    if (event.getEntity().getEquipment().getHelmet().getItemMeta().getDisplayName().equals("§fGhost Head")) {
                        isGhost = true;
                    }
                } catch (Exception localException1) {
                }
                if (aList.contains("ghost")) {
                    plugin.spawnGhost(event.getEntity().getLocation());
                }
                Location dropSpot;
                if (aList.contains("molten")) {
                    Location lavaSpot = event.getEntity().getLocation();
                    dropSpot = lavaSpot;
                    dropSpot.setX(dropSpot.getX() - 2.0D);
                } else {
                    dropSpot = event.getEntity().getLocation();
                }
                if ((plugin.getConfig().getBoolean("enableDeathMessages")) && ((event.getEntity().getKiller() instanceof Player)) && (!isGhost)) {
                    Player player = event.getEntity().getKiller();
                    if (plugin.getConfig().getList("deathMessages") != null) {
                        ArrayList<String> deathMessagesList = (ArrayList) plugin.getConfig().getList("deathMessages");
                        Random randomGenerator = new Random();
                        int index = randomGenerator.nextInt(deathMessagesList.size());
                        String deathMessage = deathMessagesList.get(index);
                        String tittle = plugin.gui.getMobNameTag(event.getEntity());
                        deathMessage = ChatColor.translateAlternateColorCodes('&', deathMessage);
                        deathMessage = deathMessage.replace("player", player.getName());
                        if ((player.getItemInHand() != null) && (!player.getItemInHand().getType().equals(Material.AIR))) {
                            if (player.getItemInHand().getItemMeta().getDisplayName() != null) {
                                deathMessage = deathMessage.replace("weapon", player.getItemInHand().getItemMeta().getDisplayName());
                            } else {
                                deathMessage = deathMessage.replace("weapon", player.getItemInHand().getType().name().replace("_", " ").toLowerCase());
                            }
                        } else {
                            deathMessage = deathMessage.replace("weapon", "fist");
                        }
                        if (event.getEntity().getCustomName() != null) {
                            deathMessage = deathMessage.replace("mob", event.getEntity().getCustomName());
                        } else {
                            deathMessage = deathMessage.replace("mob", tittle);
                        }
                        Bukkit.broadcastMessage(deathMessage);
                    } else {
                        System.out.println("No valid death messages found!");
                    }
                }
                if ((plugin.getConfig().getBoolean("enableDrops")) &&
                        ((plugin.getConfig().getBoolean("enableFarmingDrops")) || (event.getEntity().getKiller() != null)) &&
                        ((plugin.getConfig().getBoolean("enableFarmingDrops")) || ((event.getEntity().getKiller() instanceof Player)))) {
                    Player player = null;
                    if ((event.getEntity().getKiller() instanceof Player)) {
                        player = event.getEntity().getKiller();
                    }
                    if ((player != null) && (player.getGameMode().equals(GameMode.CREATIVE)) && (plugin.getConfig().getBoolean("noCreativeDrops"))) {
                        return;
                    }
                    ItemStack drop = plugin.getRandomLoot(player, event.getEntity().getType().getName(), aList.size());
                    if (drop != null) {
                        int min = 1;
                        int max = plugin.getConfig().getInt("dropChance");
                        int randomNum = new Random().nextInt(max - min + 1) + min;
                        if ((dropSpot != null) && (randomNum == 1)) {
                            Item dropedItem = event.getEntity().getWorld().dropItemNaturally(dropSpot, drop);
                            plugin.keepAlive(dropedItem);
                        }
                        int xpm = plugin.getConfig().getInt("xpMultiplier");
                        int xp = event.getDroppedExp() * xpm;
                        event.setDroppedExp(xp);
                    }
                }
                try {
                    plugin.removeMob(mobIndex);
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
            return;
        } catch (Exception e) {
            System.out.println("EntityDeathEvent: " + e);
        }
    }
}