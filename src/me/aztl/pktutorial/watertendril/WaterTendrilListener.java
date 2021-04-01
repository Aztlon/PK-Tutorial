package me.aztl.pktutorial.watertendril;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

/**
 * WaterTendrilListener listens for events that might have to do with WaterTendril
 * The objective of a Listener is to narrow it down and do something about it.
 * In this case, we want to narrow down the sneak events and click events.
 */
public class WaterTendrilListener implements Listener {
	
	// Must annotate with @EventHandler
	// Can also use @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true),
	// for instance, but that's more advanced
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) return;
		
		String abil = bPlayer.getBoundAbilityName();
		if (abil.equalsIgnoreCase("WaterTendril")) {
			new WaterTendril(player);
		}
	}
	
	// Can also use PlayerAnimationEvent
	@EventHandler
	public void onLeftClick(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND
				|| (event.getAction() != Action.LEFT_CLICK_AIR 
					&& event.getAction() != Action.LEFT_CLICK_BLOCK))
			return;
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) return;
		
		String abil = bPlayer.getBoundAbilityName();
		// If they are on WaterTendril slot and they already have an active instance
		if (abil.equalsIgnoreCase("WaterTendril") && CoreAbility.hasAbility(player, WaterTendril.class)) {
			// Can access public instance methods this way
			CoreAbility.getAbility(player, WaterTendril.class).onClick();
		}
	}

}
