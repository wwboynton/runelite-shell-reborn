package io.spewbert.shellreborn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.util.Random;
import org.junit.Test;

public class SnailLinesTest
{
	@Test
	public void skipsBlanksAndComments()
	{
		SnailLines lines = new SnailLines("# a comment\n\n  {name} wobbles.  \r\n{name} sways.\n");
		assertEquals(2, lines.size());
	}

	@Test
	public void fillsInTheName()
	{
		assertEquals("Smudge wobbles happily.", new SnailLines("{name} wobbles happily.").next(new Random(), "Smudge"));
	}

	@Test
	public void neverTheSameLineTwiceInARow()
	{
		SnailLines lines = new SnailLines("a\nb\nc");
		Random random = new Random(3);
		String last = null;
		for (int i = 0; i < 300; i++)
		{
			String text = lines.next(random, "x");
			assertNotEquals(last, text);
			last = text;
		}
	}

	@Test
	public void emptyPoolSaysNothing()
	{
		assertNull(new SnailLines("# only a comment").next(new Random(), "x"));
		assertNull(new SnailLines(null).next(new Random(), "x"));
	}

	@Test
	public void defaultPoolHasTenNamedDescriptions()
	{
		ShellRebornConfig defaults = new ShellRebornConfig()
		{
		};
		SnailLines lines = new SnailLines(defaults.descriptions());
		assertEquals(10, lines.size());
		Random random = new Random(4);
		for (int i = 0; i < 50; i++)
		{
			String text = lines.next(random, "Sadie");
			assertFalse(text, text.contains("{name}"));
		}
	}
}
