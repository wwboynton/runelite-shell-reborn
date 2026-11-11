package io.spewbert.shellreborn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class PronounsTest
{
	@Test
	public void fillsEachForm()
	{
		String t = "{name} peeks out of {their} shelter. {They} look{s} at you; {theyre} glad. Leeches don't bother {them}. {Their} shell shines.";
		assertEquals("Sadie peeks out of their shelter. They look at you; they're glad. Leeches don't bother them. Their shell shines.",
			Pronouns.THEY.fill(t, "Sadie"));
		assertEquals("Sadie peeks out of her shelter. She looks at you; she's glad. Leeches don't bother her. Her shell shines.",
			Pronouns.SHE.fill(t, "Sadie"));
		assertEquals("Sadie peeks out of his shelter. He looks at you; he's glad. Leeches don't bother him. His shell shines.",
			Pronouns.HE.fill(t, "Sadie"));
	}

	@Test
	public void everyDescriptionFillsCompletely()
	{
		for (Pronouns p : Pronouns.values())
		{
			for (String d : SnailLines.DESCRIPTIONS)
			{
				String filled = p.fill(d, "Sadie");
				assertFalse(filled, filled.contains("{") || filled.contains("}"));
			}
		}
	}

	@Test
	public void theDropdownLabels()
	{
		assertEquals(3, Pronouns.values().length);
		assertEquals("They/Them", Pronouns.THEY.toString());
	}
}
