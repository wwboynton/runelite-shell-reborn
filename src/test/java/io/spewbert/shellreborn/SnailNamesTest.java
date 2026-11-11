package io.spewbert.shellreborn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SnailNamesTest
{
	@Test
	public void thirteenNamesFromOne()
	{
		assertEquals(13, SnailNames.NAMES.size());
		assertNull(SnailNames.fromVarbit(0));
		assertEquals("Sprout", SnailNames.fromVarbit(1));
		assertEquals("Sadie", SnailNames.fromVarbit(3));
		assertEquals("Smudge", SnailNames.fromVarbit(13));
		assertNull(SnailNames.fromVarbit(14));
		assertNull(SnailNames.fromVarbit(-1));
	}

	@Test
	public void overrideWinsThenGameThenNpcName()
	{
		assertEquals("Gary", SnailNames.resolve("  Gary ", 6));
		assertEquals("Sushi", SnailNames.resolve("", 6));
		assertEquals("Sushi", SnailNames.resolve(null, 6));
		assertEquals(SnailNames.UNNAMED, SnailNames.resolve(" ", 0));
	}
}
