package me.aztl.pktutorial.watertendril;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

/**
 * WaterTendril is a long line of water, very similar to a WaterArms Spear.
 * Water is the element of TempBlocks. We'll be using PK's TempBlock class
 * to represent changing, temporary blocks. We'll also be demonstrating
 * how to handle sourcing.
 */

/*
 * Extends WaterAbility, meaning this is a water ability, not ice, not plant, water.
 * For subelements we extend IceAbility, etc.
 * Implements AddonAbility interface and no other interfaces, meaning this is a bound ability,
 * not a combo, not a passive, not a multiability.
 */
public class WaterTendril extends WaterAbility implements AddonAbility {

	/*
	 * I usually separate my instance variables into two sections,
	 * configurable values, and non-configurable values.
	 * Just my preference; you can order & separate them however you want.
	 */
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double sourceRange;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.RADIUS)
	private double hitRadius;
	private boolean controllable;
	
	private Location location;
	// We'll use two Location variables to test for going out of range.
	private Location origin;
	private Vector direction;
	/*
	 * You can use booleans to demarcate states of abilities.
	 * "Progressing" is a common name for one of these booleans.
	 * This is false if the player has selected a source but hasn't yet launched the tendril,
	 * and true if they've launched the tendril.
	 * Our onClick() method sets this to true, which allows progress()
	 * to access more code, launching the tendril.
	 */
	private boolean progressing;
	/**
	 * This boolean checks if the tendril has reached the player's eye level.
	 */
	private boolean eyeLevel;
	/*
	 * This int increases every time the tendril moves a block.
	 * It's one of two ways to check for an ability's range.
	 * The other way is to use Location#distanceSquared.
	 * The benefits of this method is that it removes after moving
	 * a certain number of blocks, while the distanceSquared method
	 * calculates distance every tick and as long as the location is
	 * still close enough to the origin, it doesn't cancel
	 * (this lets you move the tendril around you forever).
	 * In some cases that's preferred, but in this one, it's not (imo).
	 * "travelled" also doesn't scale with speed in its current state,
	 * so if speed is configurable you might be better off using distanceSquared.
	 */
	private int travelled;
	
	public WaterTendril(Player player) {
		super(player);
		
		/*
		 * WaterTendril is a bound ability, so we don't need to ignore binds.
		 * canBend will return false if they're on cooldown as well.
		 */
		if (!bPlayer.canBend(this)) return;
		
		/**
		 * Simple way to check if they already have an active instance of WaterTendril
		 * If they do (i.e. the ability is not null), and that other instance is already
		 * progressing (the tendril is launching), we don't go through with this instance.
		 * In any other case (i.e. progressing == false and we haven't launched that tendril yet),
		 * we remove that old instance. This is how re-sourcing works.
		 * Players can safely change the block their source is on without going into a cooldown
		 * or whatever.
		 */
		WaterTendril wt = getAbility(player, WaterTendril.class);
		if (wt != null) {
			if (wt.progressing)
				return;
			wt.remove();
		}
		
		// Setting fields
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Aztl.WaterTendril.Cooldown");
		// Sometimes called "SelectRange" instead. Use whatever you want.
		sourceRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Aztl.WaterTendril.SourceRange");
		range = ConfigManager.getConfig().getDouble("ExtraAbilities.Aztl.WaterTendril.Range");
		damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Aztl.WaterTendril.Damage");
		hitRadius = ConfigManager.getConfig().getDouble("ExtraAbilities.Aztl.WaterTendril.HitRadius");
		// Perhaps not the best name for what this does, because aren't all abilities "controllable"?
		// But it's what JedCore uses in their config, and idk what better word to use, so let's follow precedence
		controllable = ConfigManager.getConfig().getBoolean("ExtraAbilities.Aztl.WaterTendril.Controllable");
		
		/*
		 * This is the standard way of selecting a source.
		 * BlockSource#getWaterSourceBlock needs a player, a source range, a source selection method (ClickType),
		 * and several booleans determining which types of waterbendable blocks (ice, plant, snow, bottle)
		 * are valid sources for this ability. I've said yes to all water blocks,
		 * yes to all ice blocks as long as the bPlayer can icebend, and no to all others.
		 * 
		 * Keep in mind if you use plants or snow as a source you should instantiate a new PlantRegrowth (even for snow),
		 * which is an "ability" that acts as a timer for plants to grow back. You'll also need to use Block#setType
		 * to turn them into AIR for the time being.
		 * 
		 * For bottles, you should read up on the WaterReturn class.
		 */
		Block sourceBlock = BlockSource.getWaterSourceBlock(player, sourceRange, ClickType.SHIFT_DOWN, true, bPlayer.canIcebend(), false, false, false);
		// Don't call start() if there is not a valid source. BlockSource#getWaterSourceBlock can return null values,
		// which is the only reason we can check if this Block is null.
		if (sourceBlock == null) return;
		
		/*
		 * A Block's Location (for instance sourceBlock.getLocation()) is at the corner of 4 blocks.
		 * The center of a Block is achieved when you add 0.5 to each component: X, Y, and Z
		 * 
		 * Normally we need to be careful when using Location#add and Vector#multiply, etc.
		 * The add() method will change the original Location. But Block#getLocation returns
		 * a copy of the Block's location anyway, so it can't be changed.
		 * 
		 * Now we know that sourceBlock != null, so we can safely use it. We're setting origin
		 * as the block's location because we need origin to mark the place where the ability started
		 * so we can check the distance from the current location to the start location (range).
		 * It's also useful for displaying the water select particles, which is why it's centered.
		 */
		origin = sourceBlock.getLocation().add(0.5, 0.5, 0.5);
		// Cloned because we want to change location without changing origin
		location = origin.clone();
		// Even though all instance booleans are false by default, it's often still good to be clear about it.
		progressing = false;
		eyeLevel = false;
		
		start();
	}

	@Override
	public void progress() {
		/*
		 * We're using removeWithCooldown most of the time, which just
		 * removes the ability along with applying the cooldown.
		 * We don't need that if we apply cooldown all the time.
		 * We could just override remove(), call super.remove(), and add cooldowns there.
		 */
		if (!bPlayer.canBend(this)) {
			removeWithCooldown();
			return;
		}
		
		if (travelled > range) {
			removeWithCooldown();
			return;
		}
		
		if (!progressing) {
			// "progressing" is false when the ability starts, so every WaterTendril
			// that is just starting will have access to this code.
			
			/*
			 * ParticleEffect is an enum, which has a set of options to choose from
			 * Here we display the SMOKE_NORMAL particle 4 times at the origin,
			 * which creates the "focus water effect".
			 * There's also WaterAbility#playFocusWaterEffect which takes a Block parameter
			 * and does the same exact thing, but here we have a Location so this is easier.
			 */
			ParticleEffect.SMOKE_NORMAL.display(origin, 4);
			/*
			 * Returning here means it won't go through with the rest of the method
			 * (i.e. the parts that require progressing to be true, or the tendril to be launching.)
			 * It's one way of doing this; the other way is to have the rest of the method
			 * be in an else statement (if !progressing is not the case, progressing must be true).
			 * There's no difference to Java, but I think having your whole method in an else block
			 * is a detriment to readability most of the time.
			 */
			return;
		}
		
		if (!eyeLevel && location.getBlockY() >= player.getEyeLocation().getBlockY()) {
			eyeLevel = true;
			direction = player.getEyeLocation().getDirection();
		}
		
		/*
		 * If controllable, we keep setting the direction where the player is looking
		 * If not, we use the original direction that was set when the location
		 * reached the player's eye level!
		 */
		if (controllable) {
			direction = player.getEyeLocation().getDirection();
		}
		
		if (!eyeLevel) {
			// Add location upwards until we get to eye level
			location.add(0, 1, 0);
		} else {
			// Move location in the launching direction
			location.add(direction);
		}
		// The following line does the same thing as above but quicker:
		// location.add(eyeLevel ? direction : new Vector(0, 1, 0));
		
		// Increase travelled by 1 every time we move the Location by 1 block
		travelled++;
		
		// Create a water block at the location with a revert time of 100 milliseconds
		TempBlock tb = new TempBlock(location.getBlock(), Material.WATER);
		tb.setRevertTime(100);
		
		// Use this method for hit radii. It detects for entities in a sphere around the location within a given radius.
		// Not the only way to detect entities but the most common way.
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, hitRadius)) {
			/*
			 * Checks to make sure the player doesn't hit themselves.
			 * Since Player is a subclass of Entity, you can use this
			 * Some devs use e.getUniqueId() != player.getUniqueId() but the outcome is the same
			 * Plus, this is faster and easier to write
			 * You're not really supposed to use the == or != operators with objects (only primitive types)
			 * but when you do, it just uses the equals() method inherited from the Object base class
			 * and when there is no implementation of the equals() method for that class,
			 * it checks if it's the same object in memory. You are the same object as yourself in memory,
			 * so in any other case this should be fine to use.
			 */
			if (e != player) {
				// Only call damage/health/potion effect stuff on LivingEntities
				// even though the parameter of this method just uses Entity
				if (e instanceof LivingEntity) {
					DamageHandler.damageEntity(e, damage, this);
				}
				// Knockback would go here if we wanted to add knockback
				
				// Remove once we hit someone or else it goes through people and can hit more people
				removeWithCooldown();
			}
		}
	}
	
	// public so that other classes can remove a WaterTendril if they need to
	public void removeWithCooldown() {
		remove();
		bPlayer.addCooldown(this);
	}
	
	// public so that our Listener can access this
	public void onClick() {
		progressing = true;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	// Try not to neglect this. It can be used for collisions.
	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "WaterTendril";
	}
	
	@Override
	public String getDescription() {
		return "This ability blah blah blah";
	}
	
	@Override
	public String getInstructions() {
		return "Tap sneak on a waterbendable block, then left-click to shoot the tendril.";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public String getAuthor() {
		return "Aztl";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public void load() {
		// Order them in a way you think will be useful to server owners/config people
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.WaterTendril.Damage", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.WaterTendril.HitRadius", 1.5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.WaterTendril.Cooldown", 8000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.WaterTendril.Range", 25);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.WaterTendril.SourceRange", 8);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.WaterTendril.Controllable", true);
		
		// Registers events in your Listener, with ProjectKorra as its corresponding plugin
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new WaterTendrilListener(), ProjectKorra.plugin);
		
		ProjectKorra.plugin.getLogger().info("Successfully enabled " + getName() + " " + getVersion() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		remove();
	}

}
