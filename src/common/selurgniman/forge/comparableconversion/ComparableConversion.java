package selurgniman.forge.comparableconversion;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MerchantRecipe;
import net.minecraft.src.MerchantRecipeList;
import net.minecraft.src.ServerConfigurationManager;
import net.minecraft.src.WorldServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import selurgniman.forge.comparableconversion.util.Message;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.VillagerRegistry;

@Mod(modid = "ComparableConversion", name = "ComparableConversion", version = "0.0.3")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ComparableConversion {
	private static final Logger			log				= Logger.getLogger("Minecraft");
	private static final String			CONFIG_CATEGORY	= "trades";

	private Configuration				config			= null;
	private boolean						isDebug			= false;
	private ConverterTradeHandler		handler			= null;
	@Instance("ComparableConversion")
	public static ComparableConversion	instance;

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		ComparableConversion.instance = this;
		this.config = new Configuration(event.getSuggestedConfigurationFile());
		this.config.load();
		this.isDebug = this.config.get("debug", "debug", false).getBoolean(false);
		this.config.get(CONFIG_CATEGORY, "trades", 0);

		this.config.save();
	}

	@Init
	public void load(FMLInitializationEvent event) {
		MerchantRecipeList buyingList = new MerchantRecipeList();
		int maxTrades = this.config.get(CONFIG_CATEGORY, "trades", 0).getInt();
		for (int i = 0; i < maxTrades; i++) {
			String[] recipe = this.config.get(CONFIG_CATEGORY, "trade" + i, "").value.split(",");
			ItemStack ingredient1 = null;
			ItemStack ingredient2 = null;
			ItemStack result = null;
			MerchantRecipe merchantRecipe = null;
			try {
				if (recipe.length == 4) {
					ingredient1 = getItemStack(recipe[0], recipe[1]);
					result = getItemStack(recipe[2], recipe[3]);
					if (ingredient1 != null && result != null) {
						merchantRecipe = new MerchantRecipe(ingredient1, result);
//						log(ingredient1.getDisplayName() + ":" + ingredient1.stackSize + " = " + result.getDisplayName() + ":" + result.stackSize);
					}
				} else if (recipe.length == 6) {
					ingredient1 = getItemStack(recipe[0], recipe[1]);
					ingredient2 = getItemStack(recipe[2], recipe[3]);
					result = getItemStack(recipe[4], recipe[5]);
					if (ingredient1 != null && ingredient2 != null && result != null) {
						merchantRecipe = new MerchantRecipe(ingredient1, result);
//						log(ingredient1.getDisplayName() + ":" + ingredient1.stackSize + " + " + ingredient2.getDisplayName() + ":" + ingredient2.stackSize + " = "
//								+ result.getDisplayName() + ":" + result.stackSize);
					}
				} else {
					continue;
				}
				merchantRecipe.func_82783_a(1000);
				buyingList.addToListWithCheck(merchantRecipe);
			} catch (NumberFormatException | IndexOutOfBoundsException ex) {
				log("Error in recipe format: " + recipe.length);
			}
		}

		handler = new ConverterTradeHandler(buyingList);
		for (int i = 0; i < 6; i++) {
			VillagerRegistry.instance().registerVillageTradeHandler(i, handler);
			log("registered recipe for profession " + i);
		}
		MinecraftForge.EVENT_BUS.register(this);
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {

	}

	@ForgeSubscribe
	public void entitySpawn(EntityJoinWorldEvent evt) {
		if (evt.entity instanceof EntityVillager) {
			handler.resetVillager((EntityVillager) evt.entity);
		}
	}

	private ItemStack getItemStack(String item, String amount) {
		ItemStack itemStack = null;
		int itemId = 0;
		int damage = 0;
		try {
			if (item.contains(":")) {
				itemId = Integer.parseInt(item.split(":")[0]);
				damage = Integer.parseInt(item.split(":")[1]);
			} else {
				itemId = Integer.parseInt(item);
			}
		} catch (NumberFormatException | IndexOutOfBoundsException ex) {
			log("Error in recipe format: " + item);
		}

		return new ItemStack(itemId, Integer.parseInt(amount), damage);
	}

	public Configuration getConfig() {
		return this.config;
	}

	public static void messagePlayer(EntityPlayer player, String message) {
		player.sendChatToPlayer(Message.PREFIX + " " + message);
	}

	public static void log(String message) {
		log.info(Message.PREFIX + " " + message);
	}

	public void error(Exception e) {
		log.log(Level.WARNING, Message.DEBUG_MESSAGE.toString(), e);
		debug("[ERROR] " + e.getMessage());
	}

	public void debug(String message) {
		if (isDebug) {
			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if (side == Side.SERVER) {
				ServerConfigurationManager configManager = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager();
				for (Object player : configManager.playerEntityList) {
					messagePlayer((EntityPlayer) player, Message.DEBUG_MESSAGE + message);
				}
			}
			log(Message.DEBUG_MESSAGE + message);
		}
	}

	public boolean isDebug() {
		return this.isDebug;
	}
}