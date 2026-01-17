
package net.mcreator.jjkstrongest.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class BFTestItemItem extends Item {
	public BFTestItemItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	}
}
