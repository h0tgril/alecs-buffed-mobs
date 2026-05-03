# AlecsOpMobs

A Bukkit/Spigot plugin that significantly buffs various mobs and player mechanics to make Minecraft more challenging and interesting. Video: https://youtu.be/DTBZKJhPKgA?si=cibeox9m7guzayYe

## Features

### Player Changes
*   **Old sword cooldown:** Removes the sword swing delay introduced in the infamous "combat update."
*   **TNT buff:** TNT is twice as powerful to make it fair that creepers are too.

### Mob Buffs

The largest overall change is that most common mobs have 4x follow range / line of sight, which means typically you'll end up aggro'ing way more of them at once when out at night or inside a cave.

#### Zombies & Villager Zombies

Zombies can corner you now.

*   **No Knockback:** Zombies no longer take knockback when hit.
*   **Enhanced Follow Range:** Zombies have 4x their normal follow range.
*   **Giant Zombies:** Very rare chance (0.1%) to spawn a Giant Zombie with 200 HP, increased size, speed, and a Knockback VIII Stone Axe.

#### Creepers
*   **Double Explosion:** Default creepers have 2x explosion radius and set fire.

#### Skeletons & Strays

These now try to take potshots at you from very far away.

*   **Fast Arrows:** Arrows fired by skeletons and strays have 2x velocity.
*   **Enhanced Follow Range:** 4x normal follow range.

#### Spiders
*   **Speed:** Spiders can outrun players, like how it was in 1.7-beta before sprinting existed. Their haphazard pathing also makes them very hard to hit with arrows this way.
*   **Swimming:** Dolphin's Grace makes them faster on water too, but their pathing makes them act more like the "sceeters" in Super Mario Sunshine.

#### Wither

This boss now spawns minions to cover its deficiencies.

*   **Weather Effects:** Spawning a Wither triggers a thunderstorm.
*   **Boss Music:** Plays the 'Ward' music disc locally to all nearby players while the Wither is alive.
*   **Arrow Counterattack:** When reaching 50% HP, the Wither releases a volley of arrows (doubled for every arrow hit it took) at nearby players.
*   **Lightning Strikes:** 30% chance for Wither Skulls to strike lightning upon impact.
*   **Mob Summoning:** 1% chance for skulls to spawn 10 angry Bees or 10 Silverfish nearby.
*   **Defensive Explosion:** Triggers a power 12 explosion if it takes damage while surrounded by too many blocks. This is to prevent you from trapping it in a cave and letting iron golems do the work.

#### Ender Dragon

This boss used to be weak to swords when perched, but now you have to face it directly if you want it not to teleport away, which comes with serious risks.

*   **Regeneration:** Regenerates 2 HP every 10 seconds.
*   **Evasive Maneuvers:** Teleports to a new location if a player attacks it from behind (not facing the dragon).
*   **Enhanced Breath:** Dragon fireballs and breath clouds deal 2x damage and apply Poison II and Fire for 5 seconds.
*   **Melee Launch:** Players hit by the dragon are launched with 4x velocity.
*   **Projectile Resistance:** Takes 50% less damage from projectiles and 75% less damage from explosions.

#### Ghasts

These used to die from just one arrow and never cause meaningful damage. Since they spawn so rarely, we figured they should be a real threat.

*   **High Health:** 5x base HP. Unless it's close enough to stab, it'll take several bow shots to kill.
*   **Mega Fireballs:** Fireballs have 4x explosion yield. It'll pretty much one-shot you unless you have blast protection or full prot 4.

#### Endermen
*   **Strength:** Permanent Strength II effect. We made it so strong because it's optional to fight anyway.
*   **Invisibility:** 25% chance to spawn invisible.

#### Raiders
*   **Enhanced Follow Range:** 4x normal follow range.
*   **Pillager Gear:** Spawn with Quick Charge III and Piercing IV crossbows and Resistance I. Yes you can farm the bows, but they're not very good anyway.
*   **Vindicators & Ravagers:** Permanent Regeneration and Resistance effects.
*   **Evokers:** Permanent Resistance III.

#### Guardians & Elder Guardians
*   **Sea Buffs:** Permanent Speed II, Regeneration, Resistance I, and Strength I.

#### Other Mobs
*   **Wither Skeletons:** Permanent Strength II, Regeneration III, Speed I, and 4x follow range.
*   **Blazes:** Permanent Speed III, Regeneration I, and Resistance III. Small fireballs now cause power 2 explosions on impact.
*   **Cave Spiders:** Bites apply Nausea (15s) and Slowness II (10s).
*   **Bees:** 4x follow range. Stings apply Poison III, Night Vision, and Nausea. Bees do not die after stinging.
*   **Iron Golems:** Permanent Regeneration III, Resistance I, and Speed II.
*   **Wolves:** Tamed wolves gain permanent Strength I and Speed II.
*   **Horses (Skeleton/Zombie):** Spawn with typical horse stats instead of being nerfed as usual, plus Frost Walker II iron boots.
*   **Llamas:** Spit applies Poison II for 10 seconds.
*   **Warden:** Sonic booms cause a power 6 explosion on the target.
*   **Shulkers:** Bullets apply Levitation 100 for 2 seconds (launching players upward).
*   **Snow Golems:** If on fire, their snowballs set targets on fire for 5 seconds.

### World & Mechanics
*   **Enhanced Loot:** 10% chance for gear or books in loot chests to have Level VI enchantments. This gives exploration a purpose if you've already built farms to attain the normal max level enchants.
*   **Fire Persistence:** Fire has a 90% chance to not extinguish naturally.
*   **Bed Explosions:** Bed explosions in the Nether/End have 2x power (10.0 yield).

---

## Building the Plugin

The project includes a portable build script (`build-plugin.sh`) designed to work on any system with a JDK installed. You don't need Maven, Eclipse, IntelliJ, or whatever heavy stuff most tutorials will suggest.

### Prerequisites
*   **Java Development Kit (JDK):** Version 21 or higher.
*   **Spigot API JAR:** You must provide the Spigot API dependency for compilation.

### Build Steps
1.  **Download the Dependency:**
    *   Go to: [Spigot API 1.21.11 Snapshots](https://hub.spigotmc.org/nexus/service/rest/repository/browse/snapshots/org/spigotmc/spigot-api/1.21.11-R0.2-SNAPSHOT)
    *   Download the file ending in **`-shaded.jar`**.
2.  **Setup Folders:**
    *   Place the downloaded jar into the `lib/` folder in the project root.
3.  **Run the Script:**
    ```bash
    chmod +x build-plugin.sh
    ./build-plugin.sh
    ```
4.  **Output:**
    *   The compiled plugin will be located at `dist/AlecsOpMobs.jar`.

