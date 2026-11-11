package io.spewbert.shellreborn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Random;
import org.junit.jupiter.api.Test;

class SnailLinesTest
{
	@Test
	void skipsBlanksAndComments()
	{
		SnailLines lines = new SnailLines("# a comment\n\n  {name} wobbles.  \r\n{name} sways.\n");
		assertEquals(2, lines.size());
	}

	@Test
	void fillsInTheName()
	{
		assertEquals("Smudge wobbles happily.", new SnailLines("{name} wobbles happily.").next(new Random(), "Smudge"));
	}

	@Test
	void neverTheSameLineTwiceInARow()
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
	void emptyPoolSaysNothing()
	{
		assertNull(new SnailLines("# only a comment").next(new Random(), "x"));
		assertNull(new SnailLines(null).next(new Random(), "x"));
	}

}
