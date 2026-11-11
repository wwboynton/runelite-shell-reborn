package io.spewbert.shellreborn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The names the quest offers ("Pick a name for the snail."), in the order its menu pages through them, per the
 * wiki transcript of 2026-11-11. The game keeps the choice in varbit 16855 ({@code snailquest_snail_name}, bits
 * 7-11 of varp 6250); the list itself is not in the cache. The value is the menu position counted from 1
 * (confirmed 2026-11-11: an account that picked Sadie reads 3); 0 is taken to mean no name picked yet.
 */
final class SnailNames
{
	static final List<String> NAMES = Collections.unmodifiableList(Arrays.asList(
		"Sprout", "Sunny", "Sadie", "Scout",
		"Sasha", "Sushi", "Sophie",
		"Samantha", "Sebastian", "Snowball",
		"Squishy", "Orlando", "Smudge"));

	/** what she is called while no name is known: her NPC name */
	static final String UNNAMED = "Juvenile Snail";

	private SnailNames()
	{
	}

	/** The name for a varbit value, or null when the value names nobody (0, or out of range). */
	static String fromVarbit(int value)
	{
		return value >= 1 && value <= NAMES.size() ? NAMES.get(value - 1) : null;
	}

	/**
	 * The name to show: the user's override when set, else the one the game remembers, else her NPC name.
	 */
	static String resolve(String override, int varbitValue)
	{
		if (override != null && !override.trim().isEmpty())
		{
			return override.trim();
		}
		String fromGame = fromVarbit(varbitValue);
		return fromGame != null ? fromGame : UNNAMED;
	}
}
