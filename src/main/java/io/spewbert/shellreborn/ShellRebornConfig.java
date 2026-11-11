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

	@ConfigItem(
		keyName = "descriptions",
		name = "Examine descriptions",
		description = "Examining her prints one of these at random, one per line. {name} becomes her name."
			+ " Lines starting with # are skipped",
		position = 3
	)
	default String descriptions()
	{
		return "{name} is nibbling contentedly on a pile of fresh leaves. She looks well fed.\n"
			+ "{name} wobbles excitedly. You think she's happy to see you.\n"
			+ "{name} playfully goes in and out of her shell. You think she's showing off.\n"
			+ "{name} peeks out of her shelter, then sways in greeting. The leeches don't seem to bother her any more.\n"
			+ "{name}'s wounds have healed completely. Her shell has a healthy shine to it.\n"
			+ "{name} looks up at you with her wide and round eyes. You feel like you understand each other.\n"
			+ "{name} is glistening with fresh dew. She looks very comfortable.\n"
			+ "{name} has left a shiny trail all around her new home. She seems to have settled in nicely.\n"
			+ "{name} gives you a look of affirmation. You kept your promise to visit.\n"
			+ "{name} wiggles in anticipation. You suspect she's hoping you brought more leaves.";
	}
}
