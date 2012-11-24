package selurgniman.forge.comparableconversion.util;

import net.minecraftforge.common.Configuration;

public enum Message {
	PREFIX("mesages", "prefix", ChatColor.BLUE + "ComparableConversion" + ChatColor.RESET),
	CC_ENABLED_MESSAGE("messages", "enabled", ChatColor.GREEN + "Enabled!" + ChatColor.RESET),
	CC_DISABLED_MESSAGE("messages", "disabled", ChatColor.RED + "Disabled!" + ChatColor.RESET),
	DEBUG_MESSAGE("messages", "debug", ChatColor.GREEN + "DEBUG: " + ChatColor.RESET);

	private final String	category;
	private final String	key;
	private String			message;

	private Message(String category, String key, String message) {
		this.category = category;
		this.key = key;
		this.message = message;
	}

	/**
	 * @return the category
	 */
	private String getCategory() {
		return category;
	}

	/**
	 * @return the key
	 */
	private String getKey() {
		return key;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	private void setMessage(String message) {
		this.message = message;
	}

	public static void populateMessages(Configuration config) {
		for (Message message : Message.values()) {
			message.setMessage(config.get(message.getCategory(), message.getKey(), message.getMessage()).value);
		}
	}

	public String with(Object... values) {
		switch (values.length) {
			case 0: {
				return message;
			}
			case 1: {
				return String.format(message, values[0]);
			}
			case 2: {
				return String.format(message, values[0], values[1]);
			}
			case 3: {
				return String.format(message, values[0], values[1], values[2]);
			}
			default: {
				return "Unknown message format";
			}
		}
	}

	@Override
	public String toString() {
		return message;
	}
}