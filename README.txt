AlecsOpMobs

A Bukkit/Spigot plugin that significantly buffs various mobs and player mechanics.
Video: https://youtu.be/ePbAFowJuqc

FEATURES

Player Changes
- Removes the sword swing delay from the "combat update."
- TNT is twice as powerful.

Mob Buffs
Most common mobs have 4x follow range / line of sight.

Zombies & Villager Zombies
- No longer take knockback when hit.
- 4x their normal follow range.
- Giant Zombies: Very rare chance (0.1%) to spawn a Giant Zombie with 200 HP, increased size, speed, and a Knockback VIII "Giant's Axe" that it always drops upon death.

Creepers
- 2x explosion radius and set fire.

Skeletons & Strays
- Arrows have 2x velocity.
- 4x normal follow range.

Spiders
- Can outrun players.
- Dolphin's Grace makes them faster on water too.

Wither
- Thunderstorm triggers upon spawning.
- Plays 'Ward' music disc locally to nearby players.
- At 50% HP, releases a volley of arrows (doubled for every hit it took).
- 30% chance for skulls to strike lightning upon impact.
- 1% chance for skulls to spawn 10 angry Bees or 10 Silverfish.
- Triggers a power 12 explosion if damaged while surrounded by too many blocks.

Ender Dragon
- Regenerates 2 HP every 10 seconds.
- Teleports if attacked from behind.
- Breath deals 2x damage and applies Poison II and Fire for 5 seconds.
- Players hit are launched with 4x velocity.
- 50% less damage from projectiles and 75% less damage from explosions.

Ghasts
- 5x base HP.
- Fireballs have 4x explosion yield.

Endermen
- Permanent Strength II.
- 25% chance to spawn invisible.

Raiders
- 4x normal follow range.
- Pillagers spawn with Quick Charge III and Piercing IV crossbows and Resistance I.
- Vindicators & Ravagers: Permanent Regeneration and Resistance effects.
- Evokers: Permanent Resistance III.

Guardians & Elder Guardians
- Permanent Speed II, Regeneration, Resistance I, and Strength I.

Other Mobs
- Wither Skeletons: Permanent Strength II, Regeneration III, Speed I, and 4x follow range.
- Blazes: Permanent Speed III, Regeneration I, and Resistance III. Small fireballs cause power 2 explosions.
- Cave Spiders: Bites apply Nausea (15s) and Slowness II (10s).
- Bees: 4x follow range. Stings apply Poison III, Night Vision, and Nausea. No death after stinging.
- Iron Golems: Permanent Regeneration III, Resistance I, and Speed II.
- Wolves: Tamed wolves gain permanent Strength I and Speed II.
- Horses (Skeleton/Zombie): Typical horse stats and Frost Walker II iron boots.
- Llamas: Spit applies Poison II for 10 seconds.
- Warden: Sonic booms cause a power 6 explosion on the target.
- Shulkers: Bullets apply Levitation 100 for 2 seconds.
- Snow Golems: If on fire, snowballs set targets on fire.

World & Mechanics
- 10% chance for gear/books in loot chests to have Level VI enchantments.
- Fire has a 90% chance to not extinguish naturally.
- Bed explosions in the Nether/End have 2x power (10.0 yield).

PLAYTESTING COMMANDS
- /abm siege (count): Spawns random mobs in a circle around you.
- /abm giant: Spawns a giant zombie.
- /abm knight: Spawns a zombie knight on a zombie horse.

BUILDING THE PLUGIN
Requires JDK 21+.

1. Download the shaded Spigot API jar from:
https://hub.spigotmc.org/nexus/service/rest/repository/browse/snapshots/org/spigotmc/spigot-api/1.21.11-R0.2-SNAPSHOT
2. Place it in the 'lib/' folder.
3. Run ./build-plugin.sh
4. Output is dist/AlecsOpMobs.jar
