package io.spewbert.shellreborn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SnailNamesTest
{
	@Test
	void thirteenNamesFromOne()
	{
		assertEquals(13, SnailNames.NAMES.size());
		assertNull(SnailNames.fromVarbit(0));
		assertEquals("Sprout", SnailNames.fromVarbit(1));
		assertEquals("Smudge", SnailNames.fromVarbit(13));
		assertNull(SnailNames.fromVarbit(14));
		assertNull(SnailNames.fromVarbit(-1));
	}

	@Test
	void overrideWinsThenGameThenNpcName()
	{
		assertEquals("Gary", SnailNames.resolve("  Gary ", 6));
		assertEquals("Sushi", SnailNames.resolve("", 6));
		assertEquals("Sushi", SnailNames.resolve(null, 6));
		assertEquals(SnailNames.UNNAMED, SnailNames.resolve(" ", 0));
	}
}
