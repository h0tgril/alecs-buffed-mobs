package com.example.nozombieknockback;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.Tag;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoZombieKnockback extends JavaPlugin implements Listener {

    private final Map<java.util.UUID, Integer> witherArrowCounts = new HashMap<>();
    private final Set<java.util.UUID> witherReleasedArrows = new HashSet<>();
    private final Map<java.util.UUID, BukkitTask> witherMusicTasks = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        // Ender dragon regenerates 1 heart (2 HP) every 10 seconds (200 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (World world : getServer().getWorlds()) {
                for (EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
                    double maxHealth = dragon.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).getValue();
                    double currentHealth = dragon.getHealth();
                    if (currentHealth < maxHealth) {
                        dragon.setHealth(Math.min(maxHealth, currentHealth + 2.0));
                    }
                }
            }
        }, 200L, 200L);
    }

    private void updateAttackSpeed(Player player) {
        if (!player.isOnline()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType().name().endsWith("_SWORD")) {
            player.getAttribute(Attribute.valueOf("GENERIC_ATTACK_SPEED")).setBaseValue(1024.0);
        } else {
            player.getAttribute(Attribute.valueOf("GENERIC_ATTACK_SPEED")).setBaseValue(4.0);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateAttackSpeed(event.getPlayer());
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        // Delay 1 tick to ensure main hand is updated
        getServer().getScheduler().runTask(this, () -> {
            if (event.getPlayer().isOnline()) {
                updateAttackSpeed(event.getPlayer());
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            // Delay 1 tick for inventory update
            getServer().getScheduler().runTask(this, () -> {
                if (player.isOnline()) {
                    updateAttackSpeed(player);
                }
            });
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !Tag.BEDS.isTagged(block.getType())) {
            return;
        }
        World.Environment env = block.getWorld().getEnvironment();
        if (env == World.Environment.NETHER || env == World.Environment.THE_END) {
            event.setCancelled(true);
            // Default bed explosion power is 5.
            block.getWorld().createExplosion(block.getLocation(), 10.0F, true, true);
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (event.getBlock().getType() == Material.FIRE) {
            if (Math.random() <= 0.9) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType type = event.getEntityType();

        switch (type) {
            case GHAST:
                // Set Ghast HP to 5x
                entity.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).setBaseValue(entity.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).getBaseValue() * 5);
                entity.setHealth(entity.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).getValue());
                break;
            case ZOMBIE:
                if (Math.random() < 0.001) {
                    makeGiantZombie(entity);
                }
            case PIGLIN:
            case ZOMBIFIED_PIGLIN:
            case ZOMBIE_VILLAGER:
            case SKELETON:
            case PILLAGER:
            case CREEPER:
            case WITHER_SKELETON:
//            case GUARDIAN:
//            case ELDER_GUARDIAN:
            case SPIDER:
            case BEE:
                // Set follow range to 4x
                if (entity.getAttribute(Attribute.valueOf("GENERIC_FOLLOW_RANGE")) != null) {
                    entity.getAttribute(Attribute.valueOf("GENERIC_FOLLOW_RANGE")).setBaseValue(entity.getAttribute(Attribute.valueOf("GENERIC_FOLLOW_RANGE")).getBaseValue() * 4);
                } else {
                    getLogger().warning("Could not set follow range for " + type.name() + ": Attribute GENERIC_FOLLOW_RANGE not found.");
                }
                break;
            case ENDERMAN:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
                if (Math.random() < 0.25) {
                  entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                }
                break;
            case WITHER:
                Wither wither = (Wither) entity;
                World world = wither.getWorld();
                world.setStorm(true);
                world.setThundering(true);
                world.setWeatherDuration(120000); // 100 minutes
                world.setThunderDuration(120000);
                // Start repeating music task (Ward is 251 seconds)
                BukkitTask task = getServer().getScheduler().runTaskTimer(this, () -> {
                    if (wither.isValid()) {
                        for (Player player : wither.getWorld().getPlayers()) {
                            player.playSound(wither, Sound.MUSIC_DISC_WARD, SoundCategory.RECORDS, 4.0f, 1.0f);
                        }
                    }
                }, 0L, 5020L); // 251 * 20 = 5020 ticks
                witherMusicTasks.put(wither.getUniqueId(), task);
                break;
            case IRON_GOLEM:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
//                entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                break;
            case WOLF:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                break;
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
                AbstractHorse horse = (AbstractHorse) entity;
                // Improve stats to be like a high-quality regular horse
                // Health: 20-30 (10-15 hearts)
                double maxH = 20.0 + (Math.random() * 10.0);
                horse.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).setBaseValue(maxH);
                horse.setHealth(maxH);

                // Movement Speed: 0.225 - 0.35 (Regular is 0.1125 - 0.3375)
                double speed = 0.225 + (Math.random() * 0.125);
                horse.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED")).setBaseValue(speed);

                // Jump Strength: 0.6 - 1.0 (Regular is 0.4 - 1.0)
                double jump = 0.6 + (Math.random() * 0.4);
                horse.setJumpStrength(jump);

                ItemStack boots = new ItemStack(Material.IRON_BOOTS);
                boots.addEnchantment(Enchantment.FROST_WALKER, 2);
                horse.getEquipment().setBoots(boots);
                break;
            default:
                break;
        }
        switch (type) {
            case EVOKER:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 3));
                break;
            case VINDICATOR:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
                break;
            case RAVAGER:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
                break;
            case WARDEN:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
                break;
            case PILLAGER:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
                ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                crossbow.addEnchantment(Enchantment.QUICK_CHARGE, 3);
                crossbow.addEnchantment(Enchantment.PIERCING, 4);
                entity.getEquipment().setItemInMainHand(crossbow);
                break;
            case GUARDIAN:
            case ELDER_GUARDIAN:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
     //           entity.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 1));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
                break;
            case SPIDER:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 1));
                break;
            case WITHER_SKELETON:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                break;
            case BLAZE:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2));
                break;
            case ENDER_DRAGON:
//                entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
//                entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0));
                break;
            case BEE:
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Wither && (event.getTarget() instanceof EnderDragon || event.getTarget() instanceof Bee)) {
            event.setCancelled(true);
        }
        if (event.getTarget() instanceof Wither && event.getEntity() instanceof Bee) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (event.getEntity() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getEntity();
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0));
            wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        }
    }

    @EventHandler
    public void onEntityShootBow(org.bukkit.event.entity.EntityShootBowEvent event) {
        if (event.getEntity() instanceof Skeleton || event.getEntity() instanceof Stray) {
            Entity projectile = event.getProjectile();
            projectile.setVelocity(projectile.getVelocity().multiply(2.0));
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getEntity();
            if (fireball.getShooter() instanceof Ghast) {
                fireball.setYield(fireball.getYield() * 4);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof WitherSkull) {
            WitherSkull skull = (WitherSkull) event.getEntity();
            Location loc = skull.getLocation();
            
            // Create explosion similar to Blaze logic
            if (skull.getShooter() instanceof LivingEntity) {
               // LivingEntity shooter = (LivingEntity) skull.getShooter();
               // if (shooter != null && loc.distance(shooter.getLocation()) >= 3.0) {
               //     loc.getWorld().createExplosion(loc, 2.0F, true, true, shooter);
               // }
            }

            if (Math.random() < 0.1) {
                getServer().getScheduler().runTaskLater(this, () -> {
                    if (loc.getWorld() != null) {
                        List<WitherSkeleton> skeletons = loc.getWorld().getEntitiesByClass(WitherSkeleton.class).stream()
                            .filter(s -> s.hasMetadata("wither_spawned"))
                            .collect(java.util.stream.Collectors.toList());
                        
                        // Kill existing to make room for 1 more
                        if (skeletons.size() >= 20) {
                            for (int i = 0; i <= skeletons.size() - 20; i++) {
                                skeletons.get(i).remove();
                            }
                        }

                        Entity skeleton = loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
                        skeleton.setMetadata("wither_spawned", new org.bukkit.metadata.FixedMetadataValue(this, true));
                    }
                }, 1L);
            }

            if (skull.getShooter() instanceof Wither) {
                Wither wither = (Wither) skull.getShooter();
                
                if (Math.random() < 0.3) {
                  // Random angle and distance between 5 and 20 blocks
                  double angle = Math.random() * 2 * Math.PI;
                  double distance = 5 + (Math.random() * 15);
                  double x = loc.getX() + (Math.cos(angle) * distance);
                  double z = loc.getZ() + (Math.sin(angle) * distance);
                  // Find the highest block at this location to strike lightning correctly
                  Location strikeLoc = new Location(loc.getWorld(), x, loc.getY(), z);
                  strikeLoc.setY(loc.getWorld().getHighestBlockYAt(strikeLoc));
                  loc.getWorld().strikeLightning(strikeLoc);
                }

                // rare chance to spawn 10 angry bees 50 blocks away (cap 40)
                if (Math.random() < 0.01) {
                    List<Bee> bees = loc.getWorld().getEntitiesByClass(Bee.class).stream()
                        .filter(b -> b.hasMetadata("wither_spawned_bee"))
                        .collect(java.util.stream.Collectors.toList());
                    
                    int toSpawn = 10;
                    if (bees.size() + toSpawn > 40) {
                        for (int i = 0; i < (bees.size() + toSpawn) - 40; i++) {
                            bees.get(i).remove();
                        }
                    }
                    
                    getLogger().info("Wither at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " in " + loc.getWorld().getName() + " is spawning " + toSpawn + " bees!");
                    
                    // Play several angry bee buzz sounds
                    for (int j = 0; j < 5; j++) {
                        getServer().getScheduler().runTaskLater(this, () -> {
                            if (loc.getWorld() != null) {
                                loc.getWorld().playSound(loc, Sound.ENTITY_BEE_LOOP_AGGRESSIVE, 1.0f, (float) (0.7 + Math.random() * 0.6));
                            }
                        }, j * 3L);
                    }

                    for (int i = 0; i < toSpawn; i++) {
                            double beeAngle = Math.random() * 2 * Math.PI;
                            double beeDistance = 50.0;
                            double beeX = loc.getX() + (Math.cos(beeAngle) * beeDistance);
                            double beeZ = loc.getZ() + (Math.sin(beeAngle) * beeDistance);
                            Location beeLoc = new Location(loc.getWorld(), beeX, loc.getY() + 2, beeZ);
                            beeLoc.setY(loc.getWorld().getHighestBlockYAt(beeLoc) + 2);

                            Bee bee = (Bee) loc.getWorld().spawnEntity(beeLoc, EntityType.BEE);
                            bee.setMetadata("wither_spawned_bee", new org.bukkit.metadata.FixedMetadataValue(this, true));
                            bee.setAnger(100000);

                            Player targetPlayer = null;
                            double minDistance = Double.MAX_VALUE;
                            for (Player player : loc.getWorld().getPlayers()) {
                                double d = player.getLocation().distance(beeLoc);
                                if (d < minDistance) {
                                    minDistance = d;
                                    targetPlayer = player;
                                }
                            }
                            if (targetPlayer != null) {
                                bee.setTarget(targetPlayer);
                            }
                        }
                    }

                // rare chance to spawn 10 silverfish 10 blocks away (cap 40)
                if (Math.random() < 0.01) {
                    long silverfishCount = loc.getWorld().getEntitiesByClass(Silverfish.class).stream()
                        .filter(s -> s.hasMetadata("wither_spawned_silverfish"))
                        .count();
                    
                    if (silverfishCount < 40) {
                        int toSpawn = Math.min(10, 40 - (int)silverfishCount);
                        getLogger().info("Wither at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " in " + loc.getWorld().getName() + " is spawning " + toSpawn + " silverfish!");
                        
                        for (int i = 0; i < toSpawn; i++) {
                            double sfAngle = Math.random() * 2 * Math.PI;
                            double sfDistance = 10.0;
                            double sfX = loc.getX() + (Math.cos(sfAngle) * sfDistance);
                            double sfZ = loc.getZ() + (Math.sin(sfAngle) * sfDistance);
                            Location sfLoc = new Location(loc.getWorld(), sfX, loc.getY(), sfZ);
                            sfLoc.setY(loc.getWorld().getHighestBlockYAt(sfLoc));

                            Entity silverfish = loc.getWorld().spawnEntity(sfLoc, EntityType.SILVERFISH);
                            silverfish.setMetadata("wither_spawned_silverfish", new org.bukkit.metadata.FixedMetadataValue(this, true));
                            
                            if (silverfish instanceof Mob) {
                                Player targetPlayer = null;
                                double minDistance = Double.MAX_VALUE;
                                for (Player player : loc.getWorld().getPlayers()) {
                                    double d = player.getLocation().distance(sfLoc);
                                    if (d < minDistance) {
                                        minDistance = d;
                                        targetPlayer = player;
                                    }
                                }
                                if (targetPlayer != null) {
                                    ((Mob) silverfish).setTarget(targetPlayer);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (event.getEntity() instanceof DragonFireball) {
            if (event.getHitEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getHitEntity();
                target.damage(10.0, (Entity) event.getEntity()); // Base damage, will be doubled in EntityDamageByEntity
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                target.setFireTicks(100);
            }
        }
        if (event.getEntity() instanceof org.bukkit.entity.SmallFireball) {
            org.bukkit.entity.SmallFireball smallFireball = (org.bukkit.entity.SmallFireball) event.getEntity();
            if (smallFireball.getShooter() instanceof org.bukkit.entity.Blaze || smallFireball.getShooter() instanceof org.bukkit.projectiles.BlockProjectileSource) {
              if (smallFireball.getShooter() instanceof LivingEntity) {
                LivingEntity shooter = (LivingEntity) smallFireball.getShooter();
                if (shooter != null && smallFireball.getLocation().distance(shooter.getLocation()) >= 3.0) {
                  // Create an explosion at the location where the fireball hits
                  smallFireball.getWorld().createExplosion(smallFireball.getLocation(), 2.0F, true, true, shooter); // power, setFire, breakBlocks
                }
              } else {
                // Shot from dispenser
                smallFireball.getWorld().createExplosion(smallFireball.getLocation(), 2.0F, true, true);
              }
            }
        }
        // New logic for snowballs from snow golems
        if (event.getEntity() instanceof org.bukkit.entity.Snowball) {
            org.bukkit.entity.Snowball snowball = (org.bukkit.entity.Snowball) event.getEntity();
            if (snowball.getShooter() instanceof org.bukkit.entity.Snowman) {
                org.bukkit.entity.Snowman snowGolem = (org.bukkit.entity.Snowman) snowball.getShooter();
                // Check if the snow golem is on fire
                if (snowGolem.getFireTicks() > 0) {
                    if (event.getHitEntity() instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) event.getHitEntity();
                        // Set the target on fire for 5 seconds (100 ticks)
                        target.setFireTicks(100);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Wither) {
            Wither wither = (Wither) event.getEntity();
            
            // Check if damager is the same Wither (from its own explosion)
            if (event.getDamager().equals(wither)) {
                event.setCancelled(true);
                return;
            }

            // Count blocks within 2 blocks radius
            int blockCount = 0;
            Location witherLoc = wither.getLocation();
            for (int x = -2; x <= 2; x++) {
                for (int y = 0; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block b = witherLoc.clone().add(x, y, z).getBlock();
                        if (b.getType() != Material.AIR) {
                            blockCount++;
                        }
                    }
                }
            }
            if (blockCount > 20) {
                wither.getWorld().createExplosion(wither.getLocation(), 12.0F, true, true, wither);
            }

            if (event.getDamager() instanceof Arrow) {
                java.util.UUID uuid = wither.getUniqueId();
                witherArrowCounts.put(uuid, witherArrowCounts.getOrDefault(uuid, 0) + 1);
            }

            double maxHealth = wither.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).getValue();
            // Trigger when health is below 50% and hasn't released yet.
            if (wither.getHealth() - event.getFinalDamage() <= maxHealth / 2.0 && !witherReleasedArrows.contains(wither.getUniqueId())) {
                witherReleasedArrows.add(wither.getUniqueId());
                int count = witherArrowCounts.getOrDefault(wither.getUniqueId(), 0);
                if (count > 0) {
                    getLogger().info("Wither at " + wither.getLocation().getBlockX() + ", " + wither.getLocation().getBlockY() + ", " + wither.getLocation().getBlockZ() + " in " + wither.getWorld().getName() + " is performing arrow counterattack with " + count + " arrows!");
                    getServer().getScheduler().runTaskLater(this, () -> {
                        Location loc = wither.getLocation().add(0, 1, 0);
                        List<Player> nearbyPlayers = new java.util.ArrayList<>();
                        for (Entity e : wither.getNearbyEntities(32, 32, 32)) {
                            if (e instanceof Player) {
                                nearbyPlayers.add((Player) e);
                            }
                        }

                        for (int i = 0; i < count * 2 && i < 20 ; i++) {
                            Vector direction;
                            if (!nearbyPlayers.isEmpty()) {
                                Player target = nearbyPlayers.get(i % nearbyPlayers.size());
                                direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();
                                // Add some randomness
                                direction.add(new Vector(Math.random() * 0.4 - 0.2, Math.random() * 0.4 - 0.2, Math.random() * 0.4 - 0.2)).normalize();
                            } else {
                                direction = new Vector(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize();
                            }

                            Arrow arrow = loc.getWorld().spawn(loc, Arrow.class);
                            arrow.setShooter(wither);
                            arrow.setVelocity(direction.multiply(1.5));
                            arrow.setFireTicks(200);
                            arrow.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1), true);
                        }
                        witherArrowCounts.put(wither.getUniqueId(), 0);
                    }, 20L); // Delay slightly for effect
                }
            }
        }

        if (event.getDamager() instanceof DragonFireball) {
            event.setDamage(event.getDamage() * 2.0);
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                target.setFireTicks(100);
            }
        }
        if (event.getDamager() instanceof AreaEffectCloud) {
            AreaEffectCloud cloud = (AreaEffectCloud) event.getDamager();
            if (cloud.getSource() instanceof EnderDragon) {
                event.setDamage(event.getDamage() * 2.0);
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    target.setFireTicks(100);
                }
            }
        }
        if (event.getDamager() instanceof org.bukkit.entity.LlamaSpit) {
            org.bukkit.entity.LlamaSpit llamaSpit = (org.bukkit.entity.LlamaSpit) event.getDamager();
            if (llamaSpit.getShooter() instanceof org.bukkit.entity.TraderLlama) {
                if (event.getEntity() instanceof LivingEntity && !((event.getEntity() instanceof org.bukkit.entity.TraderLlama))) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    // Apply poison effect (10 seconds, Poison II)
                    target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1));
                }
            }
        }
        if (event.getDamager() instanceof CaveSpider && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            // Apply Nausea (15 seconds) and Slowness (10 seconds)
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 300, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 1));
        }
        if (event.getDamager() instanceof Warden) {
            if (event.getCause() == EntityDamageEvent.DamageCause.SONIC_BOOM) {
                // Create an explosion at the target's location when hit by a sonic boom
                event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 6.0F, true, true, (LivingEntity) event.getDamager());
            }
        }
        if (event.getDamager() instanceof ShulkerBullet && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            // Pick em up with your wheels, put em down
            getServer().getScheduler().runTaskLater(this, () -> {
              target.removePotionEffect(PotionEffectType.LEVITATION);
              target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 100));
            }, 3L);
        }

        if (event.getDamager() instanceof Bee) {
            Bee bee = (Bee) event.getDamager();
            
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0));
            }

            // Delay by 1 tick to ensure the internal Minecraft
            // logic has finished updating the bee's state.
            getServer().getScheduler().runTask(this, () -> {
                if (bee.isValid()) {
                    // Reset the stung state to prevent death
                    bee.setHasStung(false);
                    // Keep the bee angry
                    bee.setAnger(100000);
                }
            });
        }
        if ((event.getDamager() instanceof EnderDragon || event.getDamager() instanceof EnderDragonPart) && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            getServer().getScheduler().runTaskLater(this, () -> {
                if (player.isOnline() && !player.isDead()) {
                    Vector velocity = player.getVelocity().multiply(4.0);
                    double speed = velocity.length();
                    double maxSpeed = 5; // 100 blocks per second (40 ticks per second)
                    if (speed > maxSpeed) {
                        velocity.multiply(maxSpeed / speed);
                    }
                    player.setVelocity(velocity);
                }
            }, 1L);
        }
        if (event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.ZOMBIE_VILLAGER) {
            // Setting velocity to zero right after the damage event effectively cancels knockback.
            // A short delay is needed for this to take effect properly.
            getServer().getScheduler().runTaskLater(this, () -> {
                if (!event.getEntity().isDead()) {
                    event.getEntity().setVelocity(new Vector(0, 0, 0));
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onEnderDragonDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EnderDragon) && !(entity instanceof EnderDragonPart)) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageByEntityEvent.DamageCause.PROJECTILE) {
            event.setDamage(event.getDamage() * 0.5);
        } else if (cause == EntityDamageByEntityEvent.DamageCause.BLOCK_EXPLOSION ||
            cause == EntityDamageByEntityEvent.DamageCause.ENTITY_EXPLOSION) {
            event.setDamage(event.getDamage() * 0.25);
        }
    }

    @EventHandler
    public void onEnderDragonDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EnderDragon) && !(entity instanceof ComplexEntityPart)) {
            return;
        }

        EnderDragon dragon;
        if (entity instanceof EnderDragon) {
            dragon = (EnderDragon) entity;
        } else {
            ComplexEntityPart part = (ComplexEntityPart) entity;
            if (part.getParent() instanceof EnderDragon) {
                dragon = (EnderDragon) part.getParent();
            } else {
                return;
            }
        }

        LivingEntity attacker = null;
        if (event.getDamager() instanceof LivingEntity) {
            attacker = (LivingEntity) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof LivingEntity) {
                attacker = (LivingEntity) projectile.getShooter();
            }
        }

        if (attacker == null) {
            return;
        }

        Location dLoc = dragon.getLocation();
        Location aLoc = attacker.getLocation();

        // Calculate direction vectors in the XZ plane
        Vector dDir = dLoc.getDirection().setY(0).normalize();
        Vector aDir = aLoc.getDirection().setY(0).normalize();

        // Calculate displacement vectors in the XZ plane
        Vector d2a = aLoc.toVector().subtract(dLoc.toVector()).setY(0);
        Vector a2d = dLoc.toVector().subtract(aLoc.toVector()).setY(0);

        // Facing each other with 10% tolerance (dot product > 0.9)
        boolean facingEachOther = false;
        if (d2a.lengthSquared() > 0 && a2d.lengthSquared() > 0) {
            d2a.normalize();
            a2d.normalize();
            // Invert dDir because the dragon's head is considered its back
            double dDot = dDir.multiply(-1).dot(d2a);
            double aDot = aDir.dot(a2d);
            facingEachOther = (dDot > 0.9) && (aDot > 0.9);
        }

        if (!facingEachOther) {
            double currentDist = dLoc.distance(aLoc);
            double newDist = currentDist / 2.0;
            if (newDist < 20) {
              newDist = 20;
            }

            // Pick a random angle such that the attacker is not facing the dragon.
            // Attacker's direction in XZ plane.
            double lookAngle = Math.atan2(aDir.getZ(), aDir.getX());

            // Pick an angle in the 180-degree arc behind the attacker.
            double randomAngle = lookAngle + Math.PI / 2.0 + Math.random() * Math.PI;

            double newX = aLoc.getX() + Math.cos(randomAngle) * newDist;
            double newZ = aLoc.getZ() + Math.sin(randomAngle) * newDist;
            double newY = dLoc.getY(); // Keep the dragon's current height

            Location newLoc = new Location(dragon.getWorld(), newX, newY, newZ);

            // Dragon faces the attacker directly (invert because head is back)
            Vector newDir = aLoc.toVector().subtract(newLoc.toVector()).normalize().multiply(-1);
            newLoc.setDirection(newDir);

            dragon.teleport(newLoc);
            dragon.getWorld().playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntityType() == EntityType.CREEPER) {
            if (event.getEntity().hasMetadata("giant_creeper")) {
                event.setRadius(event.getRadius() * 30);
            } else {
                event.setRadius(event.getRadius() * 2);
            }
            event.setFire(true);
        } else if (event.getEntityType() == EntityType.TNT || event.getEntityType() == EntityType.TNT_MINECART) {
            event.setRadius(event.getRadius() * 2);
            if (event.getEntity().getFireTicks() > 0) {
                event.setFire(true);
            }
        } else if (event.getEntityType() == EntityType.WITHER) {
            // Wither initial spawn explosion
            Wither wither = (Wither) event.getEntity();
            Location loc = wither.getLocation();
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4.0;
                double x = loc.getX() + 8 * Math.cos(angle);
                double z = loc.getZ() + 8 * Math.sin(angle);
                Location strikeLoc = new Location(loc.getWorld(), x, loc.getY(), z);
                strikeLoc.setY(loc.getWorld().getHighestBlockYAt(strikeLoc));
                loc.getWorld().strikeLightning(strikeLoc);
            }
        }
    }

    @EventHandler
    public void onLootGenerate(org.bukkit.event.world.LootGenerateEvent event) {
        // Only apply to containers (chests, etc.), not mobs
        if (event.getInventoryHolder() == null) {
            return;
        }

        for (ItemStack item : event.getLoot()) {
            if (item == null || item.getType() == Material.AIR) continue;

            String type = item.getType().name();
            boolean isGear = type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE") ||
                             type.endsWith("_LEGGINGS") || type.endsWith("_BOOTS") ||
                             type.endsWith("_PICKAXE") || type.endsWith("_AXE") ||
                             type.endsWith("_SHOVEL") || type.endsWith("_HOE") ||
                             type.endsWith("_SWORD");
            boolean isBook = item.getType() == Material.ENCHANTED_BOOK;

            if ((isGear || isBook) && Math.random() < 0.10) {
                if (isBook) {
                    org.bukkit.inventory.meta.EnchantmentStorageMeta meta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) item.getItemMeta();
                    if (meta != null && meta.hasStoredEnchants()) {
                        for (Enchantment ench : new java.util.HashSet<>(meta.getStoredEnchants().keySet())) {
                            meta.addStoredEnchant(ench, 6, true);
                        }
                        item.setItemMeta(meta);
                    }
                } else {
                    Map<Enchantment, Integer> enchants = item.getEnchantments();
                    if (!enchants.isEmpty()) {
                        for (Enchantment ench : enchants.keySet()) {
                            item.addUnsafeEnchantment(ench, 6);
                        }
                    }
                }
            }
        }
    }

    private void makeGiantZombie(LivingEntity entity) {
        if (entity.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")) != null) {
            entity.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH")).setBaseValue(200.0);
            entity.setHealth(200.0);
        }
        if (entity.getAttribute(Attribute.valueOf("GENERIC_SCALE")) != null) {
            entity.getAttribute(Attribute.valueOf("GENERIC_SCALE")).setBaseValue(4.0);
        }
        if (entity.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED")) != null) {
            double currentSpeed = entity.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED")).getBaseValue();
            entity.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED")).setBaseValue(currentSpeed * 1.2);
        }
        entity.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 9));

        ItemStack spear = new ItemStack(Material.STONE_AXE);
        spear.addUnsafeEnchantment(Enchantment.KNOCKBACK, 8);
        entity.getEquipment().setItemInMainHand(spear);
        entity.getEquipment().setItemInMainHandDropChance(0.0f);
        entity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        entity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        entity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        entity.getEquipment().setChestplateDropChance(0.0f);
        entity.getEquipment().setLeggingsDropChance(0.0f);
        entity.getEquipment().setBootsDropChance(0.0f);
    }

