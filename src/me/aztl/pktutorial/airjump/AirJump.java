package me.aztl.pktutorial.airjump;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;

/**
 * AirJump is an addon ability that was made a few years ago.
 * Here, I'm making my own simplistic version to demonstrate how combos work
 * and how to make a player propel themselves.
 */

/* 
 * The class name should (in almost every case) be the same as the ability name,
 * but that's not always the case. AirJump extends AirAbility, giving the Element of AIR to this ability.
 * It implements the AddonAbility and ComboAbility interfaces, which give additional methods
 * such as getAuthor() and createNewComboInstance().
 * The inheritance of AirJump is as follows:
 * AirJump extends AirAbility, which extends ElementalAbility, which extends CoreAbility, which implements Ability
 */
public class AirJump extends AirAbility implements AddonAbility, ComboAbility {
	
	/*
	 * Here are your instance variables.
	 * These are used for each part of your ability that you want to make configurable.
	 * Even if you're making the ability without the intention of distributing it to
	 * server owners, you should make it configurable because you may want to tweak something
	 * or simply change something like the cooldown for testing purposes.
	 * 
	 * Before each variable is an Attribute annotation. Though not widely used (yet),
	 * Attributes are supposed to allow side plugins such as PKRPG to modify all values.
	 * Statuses like the AvatarState, day/night, Sozin's Comet, full moon, RPG levels,
	 * anything really, can use Attributes to modify any value.
	 * PK comes with several Attributes (Attribute.COOLDOWN for example), but you can
	 * add your own by using a String.
	 * That being said, Attributes are optional and are simply good practice.
	 * 
	 * Time values such as cooldowns, durations, and charge times are represented as longs.
	 * This is because they use System.currentTimeMillis() which returns a long number representing
	 * the number of milliseconds since January 1st, 1970. Differences in these numbers
	 * are used across many platforms to represent time.
	 * 
	 * Doubles are also a common data type; any number that could possibly be a decimal should
	 * be represented as a double (or float). However, doubles also take up the most space,
	 * so keep that in mind.
	 */
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double velocity;

	/**
	 * This is your constructor.
	 * You should check if the player has permission to use the ability,
	 * then initialize your variables, and finally call start() to begin progressing the ability.
	 * You can add any parameters you want to the constructor, such as a ClickType or boolean
	 * to determine the activation method.
	 */
	public AirJump(Player player) {
		// super(player) means this parameter is passed to the superclass, which is AirAbility
		super(player);
		
		/*
		 * The instance variables "player" and "bPlayer" (or this.player and this.bPlayer)
		 * are inherited from the CoreAbility superclass. You can use them anywhere in any
		 * class that is an instance of a CoreAbility (that includes this one).
		 * bPlayer is a BendingPlayer object, which comes with its own methods relating to
		 * a Player and their bending information.
		 * 
		 * BendingPlayer#canBend is a boolean method that checks for all sorts of things,
		 * such as whether the player is online, alive, has the appropriate element,
		 * has the right permission, etc. What's important is that it checks if the
		 * current bound ability is the same name as this ability!
		 * Because AirJump is a COMBO, you can't bind it, meaning BendingPlayer#canBend will return FALSE!
		 * Therefore we use canBendIgnoreBinds, which takes a CoreAbility parameter (this class is a CoreAbility).
		 * There's also canBendIgnoreCooldowns and canBendIgnoreBindsCooldowns.
		 * 
		 * Therefore, this method returns if the player can't use this ability for whatever reason.
		 * The program never makes it down to start(), so the ability never starts.
		 */
		if (!bPlayer.canBendIgnoreBinds(this)) return;
		
		/*
		 * Getting the velocity value from the ProjectKorra config.yml.
		 * The convention is to use ExtraAbilities as the configuration section
		 * (other sections include Properties and Abilities),
		 * then your name to distinguish yourself from other addon developers,
		 * then the ability name, then the variable name.
		 * 
		 * I like to initialize variables in my constructor, but some developers prefer the use of
		 * a setFields() void method to make the code more readable. I find it unnecessary
		 * unless I have multiple constructors for the same ability.
		 */
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Aztl.AirJump.Cooldown");
		velocity = ConfigManager.getConfig().getDouble("ExtraAbilities.Aztl.AirJump.Velocity");
		
		/*
		 * Calling start() means progress() begins repeating itself every tick.
		 * Make sure everything is accounted for before calling start().
		 */
		start();
		/*
		 * Simple method that adds the cooldown for this ability,
		 * as specified by getCooldown().
		 * Abilities that remove shortly after starting (such as projectiles,
		 * quick attacks, etc.) usually add the cooldown at the start.
		 * Alternatively, we could add the cooldown when the ability removes
		 * by making our own remove() method. See below.
		 */
		bPlayer.addCooldown(this);
	}

	/**
	 * progress() is the heart of your ability. It is called every single tick (50 milliseconds).
	 * This is what allows things in your ability to be updated, such as locations.
	 * This is how FireBlast can travel slowly from one point to another; 
	 * each tick the location is slightly moved in one direction.
	 * progress() will go on forever until remove() is called (or the server goes offline).
	 */
	@Override
	public void progress() {
		/*
		 * getAirbendingParticles() (which we inherited from AirAbility)
		 * returns whatever ParticleEffect the current server uses
		 * for airbending particles, as specified in the ProjectKorra config.
		 * Alternatively, we could've used playAirbendingParticles(),
		 * which is similar to the line below but isn't as precise.
		 * 
		 * display() is a method in the ParticleEffect enum, which was created
		 * by ProjectKorra. Here we're displaying the airbending particles
		 * at the player's location, which is the center of the bottom of their feet.
		 * The amount of particles is 1, and the X, Y, and Z offsets are all randomly determined
		 * distances, measured in blocks.
		 */
		getAirbendingParticles().display(player.getLocation(), 1, Math.random(), Math.random(), Math.random());
		
		/*
		 * Vector is a Bukkit-provided class. If you're familiar with vectors, you'll know that
		 * they have a magnitude and direction. Here, on the Minecraft coordinate plane,
		 * Vectors have X, Y, and Z components corresponding to the axes.
		 * Get magnitudes with Vector#length.
		 * 
		 * This Vector we call "direction" is a unit vector (length of 1 block)
		 * that represents the direction the player is looking.
		 * Player#getEyeLocation is the Location at the player's eye level.
		 * The Vector in getDirection() is determined from the eye location's yaw and pitch.
		 */
		Vector direction = player.getEyeLocation().getDirection();
		
		/*
		 * Setting the velocity of the player to go in the direction they're looking
		 * will launch them 1 block in that direction because the length of "direction"
		 * is 1 (unit vector/normalized vector).
		 * To increase/decrease this, we clone the Vector so any changes we make to it
		 * don't affect the original Vector (not important here because we never use the
		 * above Vector again, but good practice).
		 * Then we multiply it by our velocity.
		 */
		player.setVelocity(direction.clone().multiply(velocity));
		
		/*
		 * The purpose of AirJump is fulfilled; the air particles have been displayed
		 * and the player has been flung in the direction they're looking.
		 * It's time we remove this instance.
		 * 
		 * You might ask what's the point of removing instantly, since progress()
		 * is best used as an updater. We could've just put all of this in the
		 * constructor and never even called start().
		 * That's true, but other plugins could listen for the AbilityStartEvent
		 * and cancel it if they need to, and by putting everything in our constructor,
		 * we would be ignoring that. It's important we try to adhere to these conventions
		 * because it will make other people's lives easier.
		 */
		remove();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	/**
	 * getLocation() is used for ability collisions. If you want to add collisions with other abilities,
	 * you'll need to return a useful Location. Additionally, if other addon developers want to
	 * register collisions with your abilities, they would use this method to get your ability's location.
	 * 
	 * This ability doesn't need collisions at all, but it's still good to put something.
	 * The only relevant location is the player's.
	 */
	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	/**
	 * Whatever returns here will be used to display your ability in /b display, on sideboards, etc.
	 */
	@Override
	public String getName() {
		return "AirJump";
	}

	/**
	 * Abilities that are harmless are, by definition,
	 * abilities that don't harm or move or act as a detriment to other people or mobs.
	 * AirSwipe is not harmless because it reduces health,
	 * and AirBlast is not harmless because it pushes people and can eventually cause harm.
	 * This has an effect on protected region checking. Harmless abilities
	 * such as AirSpout and AirScooter defy region protection.
	 * 
	 * This ability doesn't affect other entities at all, so it is harmless.
	 */
	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	/**
	 * Should return true if sneak is part of this ability.
	 * This has an effect on whether the user can FastSwim while
	 * this ability is progressing. If true, the user will
	 * not be able to use FastSwim.
	 * 
	 * Although we use sneak to activate the combo, this method
	 * will only come into effect while the ability is progressing.
	 * We don't need to sneak while progressing, so it's not a sneak ability.
	 */
	@Override
	public boolean isSneakAbility() {
		return false;
	}

	/**
	 * This method is the reason combos don't need listeners
	 * to activate. Once {@link #getCombination()} is satisfied,
	 * this method runs and instantiates a new AirJump.
	 * You don't need to do anything else in this method except
	 * create a new AirJump instance.
	 */
	@Override
	public Object createNewComboInstance(Player player) {
		return new AirJump(player);
	}

	/**
	 * This determines how the combo is activated.
	 * AbilityInformation's constructor takes two parameters,
	 * 1) a String matching an ability's name
	 * 2) a ClickType
	 * The following combination is
	 * AirBurst (press shift) > AirBurst (release shift) 3 times,
	 * i.e. tap sneak with AirBurst 3 times.
	 * There are other ClickTypes as well, notably LEFT_CLICK and RIGHT_CLICK_BLOCK
	 */
	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("AirBurst", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirBurst", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("AirBurst", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirBurst", ClickType.SHIFT_UP));
		combo.add(new AbilityInformation("AirBurst", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("AirBurst", ClickType.SHIFT_UP));
		return combo;
	}

	/**
	 * Addon abilities need authors. This is displayed when someone does /b h AirJump
	 */
	@Override
	public String getAuthor() {
		return "Aztl";
	}

	/**
	 * Try to stick to a version convention,
	 * <api-changing release>.<minor release>.<bug fix> is a decent way to understand the following three numbers.
	 */
	@Override
	public String getVersion() {
		return "1.0.0";
	}

	/**
	 * This method is called when your ability loads from the /plugins/ProjectKorra/Abilities/ folder
	 * It should instantiate a Listener and ideally add Permissions and config options.
	 */
	@Override
	public void load() {		
		// Adding permission. Follow the bending.ability.<abilityname> format.
		ProjectKorra.plugin.getServer().getPluginManager().addPermission(new Permission("bending.ability.airjump"));
		
		// Default velocity of 3
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.AirJump.Velocity", 3);
		// Default cooldown of 5000 milliseconds, or 5 seconds
		ConfigManager.getConfig().addDefault("ExtraAbilities.Aztl.AirJump.Cooldown", 5000);
		
		// Run enabled messages at the end of methods, not the beginning, because if something goes wrong, the "successful" message won't run.
		ProjectKorra.plugin.getLogger().info(getName() + " " + getVersion() + " by " + getAuthor() + " has been successfully enabled.");
	}

	/**
	 * Called when the server stops.
	 */
	@Override
	public void stop() {
		// remove in case of server stop
		remove();
	}
	
	// Methods that you may have to add manually
	
	/**
	 * Description of your ability displayed in /b h AirJump
	 */
	@Override
	public String getDescription() {
		return "This combo allows an airbender to jump high into the air.";
	}
	
	/**
	 * Instructions displayed in /b h AirJump
	 */
	@Override
	public String getInstructions() {
		return "AirBurst (Tap sneak 3x)";
	}
	
	/**
	 * Individual addon abilities DO NOT USUALLY NEED THIS
	 * because server owners can simply delete the jar files from the Abilities folder,
	 * but in case you do need it, here it is.
	 * 
	 * Usually this will return a config boolean, such as ConfigManager.getConfig().getBoolean("ExtraAbilities.Aztl.AirJump.Enabled")
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	/**
	 * Sometimes you want to add your own code on removal,
	 * such as cleaning up TempBlocks or removing PotionEffects, whatever.
	 * In that case you should Override remove() and call super.remove()
	 * along with your own stuff.
	 */
	@Override
	public void remove() {
		super.remove();
		// If I wanted to add cooldown on removal (pretty common), I would add this:
		// bPlayer.addCooldown(this);
	}
	
}
