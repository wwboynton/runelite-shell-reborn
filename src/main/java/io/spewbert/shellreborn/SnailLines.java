package io.spewbert.shellreborn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * What examining her says: one of these at random, never the same one twice in a row, with {@code {name}}
 * replaced by her name. Written in the voice of the quest's narration boxes ("[name] wobbles excitedly. You
 * think it's happy."). Pure.
 */
final class SnailLines
{
	static final List<String> DESCRIPTIONS = Collections.unmodifiableList(Arrays.asList(
		"{name} is nibbling contentedly on a pile of fresh leaves. She looks well fed.",
		"{name} wobbles excitedly. You think she's happy to see you.",
		"{name} playfully goes in and out of her shell. You think she's showing off.",
		"{name} peeks out of her shelter, then sways in greeting. The leeches don't seem to bother her any more.",
		"{name}'s wounds have healed completely. Her shell has a healthy shine to it.",
		"{name} looks up at you with her wide and round eyes. You feel like you understand each other.",
		"{name} is glistening with fresh dew. She looks very comfortable.",
		"{name} has left a shiny trail all around her new home. She seems to have settled in nicely.",
		"{name} gives you a look of affirmation. You kept your promise to visit.",
		"{name} wiggles in anticipation. You suspect she's hoping you brought more leaves."));

	private final List<String> pool;
	private int last = -1;

	SnailLines()
	{
		this(DESCRIPTIONS);
	}

	SnailLines(List<String> pool)
	{
		this.pool = pool;
	}

	/** A random description with her name filled in, or null when the pool is empty. */
	String next(Random random, String name)
	{
		if (pool.isEmpty())
		{
			return null;
		}
		int pick = random.nextInt(pool.size());
		if (pool.size() > 1 && pick == last)
		{
			pick = (pick + 1 + random.nextInt(pool.size() - 1)) % pool.size();
		}
		last = pick;
		return pool.get(pick).replace("{name}", name);
	}
}
