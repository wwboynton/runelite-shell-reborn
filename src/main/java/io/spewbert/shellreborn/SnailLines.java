package io.spewbert.shellreborn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * What examining her says: one of these at random, never the same one twice in a row, with her name and the
 * chosen pronouns filled in ({@link Pronouns} lists the placeholders). Written in the voice of the quest's
 * narration boxes ("[name] wobbles excitedly. You think it's happy."). Pure.
 */
final class SnailLines
{
	static final List<String> DESCRIPTIONS = Collections.unmodifiableList(Arrays.asList(
		"{name} is nibbling contentedly on a pile of fresh leaves. {They} look{s} well fed.",
		"{name} wobbles excitedly. You think {theyre} happy to see you.",
		"{name} playfully goes in and out of {their} shell. You think {theyre} showing off.",
		"{name} peeks out of {their} shelter, then sways in greeting. The leeches don't seem to bother {them} any more.",
		"{name}'s wounds have healed completely. {Their} shell has a healthy shine to it.",
		"{name} looks up at you with {their} wide and round eyes. You feel like you understand each other.",
		"{name} is glistening with fresh dew. {They} look{s} very comfortable.",
		"{name} has left a shiny trail all around {their} new home. {They} seem{s} to have settled in nicely.",
		"{name} gives you a look of affirmation. You kept your promise to visit.",
		"{name} wiggles in anticipation. You suspect {theyre} hoping you brought more leaves."));

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

	/** A random description with her name and pronouns filled in, or null when the pool is empty. */
	String next(Random random, String name, Pronouns pronouns)
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
		return pronouns.fill(pool.get(pick), name);
	}
}
