package io.spewbert.shellreborn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The pool of examine descriptions, from the config's text box: one per line, blank lines and lines starting
 * with {@code #} skipped, {@code {name}} replaced by her name. Picks at random, never the same line twice in a
 * row while the pool has more than one. Pure.
 */
final class SnailLines
{
	private final List<String> pool;
	private int last = -1;

	SnailLines(String configText)
	{
		this.pool = parse(configText);
	}

	static List<String> parse(String configText)
	{
		if (configText == null)
		{
			return Collections.emptyList();
		}
		List<String> out = new ArrayList<>();
		for (String raw : configText.split("\\r?\\n"))
		{
			String line = raw.trim();
			if (!line.isEmpty() && !line.startsWith("#"))
			{
				out.add(line);
			}
		}
		return out;
	}

	int size()
	{
		return pool.size();
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
