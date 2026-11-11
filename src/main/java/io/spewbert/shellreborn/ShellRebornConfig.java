package io.spewbert.shellreborn;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ShellRebornConfig.GROUP)
public interface ShellRebornConfig extends Config
{
	String GROUP = "shellreborn";

	@ConfigItem(
		keyName = "showSnail",
		name = "Show the snail",
		description = "Draw her on the island in the Haunted Woods where you met her",
		position = 0
	)
	default boolean showSnail()
	{
		return true;
	}

	@ConfigItem(
		keyName = "onlyAfterQuest",
		name = "Only after the quest",
		description = "Draw her only once A Shell of a Friend is complete",
		position = 1
	)
	default boolean onlyAfterQuest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "nameOverride",
		name = "Name override",
		description = "Leave blank to use the name you picked in the quest; type a name to use that instead",
		position = 2
	)
	default String nameOverride()
	{
		return "";
	}
}
