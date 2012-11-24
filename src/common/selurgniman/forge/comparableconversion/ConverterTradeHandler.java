package selurgniman.forge.comparableconversion;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.MerchantRecipeList;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

public class ConverterTradeHandler implements IVillageTradeHandler {

	private final MerchantRecipeList	buyingList;
	private final String DEOBFUSCATED_FIELD = "buyingList";
	private final String OBFUSCATED_FIELD = "i";
	
	public ConverterTradeHandler(MerchantRecipeList recipeList) {
		this.buyingList = recipeList;
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
		setBuyingList(villager, DEOBFUSCATED_FIELD);
	}
	
	private void setBuyingList(EntityVillager villager, String fieldName) {
		Class<?> c = villager.getClass();

		try {
			Field buyingListField = c.getDeclaredField(fieldName);
			buyingListField.setAccessible(true);
			buyingListField.set(villager, this.buyingList);
		} catch (NoSuchFieldException e) {
			if (fieldName.equals(DEOBFUSCATED_FIELD)) {
				setBuyingList(villager, OBFUSCATED_FIELD);
			} else {
				ComparableConversion.instance.error(e);
			}
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			ComparableConversion.instance.error(e);
		}
	}

	public void resetVillager(EntityVillager villager) {
		VillagerRegistry.manageVillagerTrades(buyingList, villager, 0, new Random());
	}
}
