package io.spewbert.shellreborn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import org.junit.Test;

public class SnailLinesTest
{
	@Test
	public void fillsInTheName()
	{
		SnailLines lines = new SnailLines(Collections.singletonList("{name} wobbles happily."));
		assertEquals("Smudge wobbles happily.", lines.next(new Random(), "Smudge"));
	}

	@Test
	public void neverTheSameLineTwiceInARow()
	{
		SnailLines lines = new SnailLines(Arrays.asList("a", "b", "c"));
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
		assertNull(new SnailLines(Collections.emptyList()).next(new Random(), "x"));
	}

	@Test
	public void tenDescriptionsEachNamingHer()
	{
		assertEquals(10, SnailLines.DESCRIPTIONS.size());
		for (String d : SnailLines.DESCRIPTIONS)
		{
			assertTrue(d, d.contains("{name}"));
		}
		SnailLines lines = new SnailLines();
		Random random = new Random(4);
		for (int i = 0; i < 50; i++)
		{
			String text = lines.next(random, "Sadie");
			assertFalse(text, text.contains("{name}"));
		}
	}
}
