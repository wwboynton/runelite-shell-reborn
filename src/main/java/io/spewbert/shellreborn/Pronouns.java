package io.spewbert.shellreborn;

/**
 * The pronouns the examine descriptions use for her. A description is a template:
 * <ul>
 *   <li>{@code {name}}: her name</li>
 *   <li>{@code {they}} / {@code {They}}: he, she, they</li>
 *   <li>{@code {them}}: him, her, them</li>
 *   <li>{@code {their}} / {@code {Their}}: his, her, their</li>
 *   <li>{@code {theyre}}: he's, she's, they're</li>
 *   <li>{@code {s}}: the verb ending after {@code {they}}, "s" except for they ("{They} look{s} well fed.")</li>
 * </ul>
 * They/Them is the default: once named, the quest itself calls the snail "they" ("[name] playfully goes in and
 * out of their shell"). Pure.
 */
public enum Pronouns
{
	HE("He/Him", "he", "him", "his", "he's", "s"),
	SHE("She/Her", "she", "her", "her", "she's", "s"),
	THEY("They/Them", "they", "them", "their", "they're", "");

	private final String label;
	private final String subject;
	private final String object;
	private final String possessive;
	private final String contraction;
	private final String verbEnding;

	Pronouns(String label, String subject, String object, String possessive, String contraction, String verbEnding)
	{
		this.label = label;
		this.subject = subject;
		this.object = object;
		this.possessive = possessive;
		this.contraction = contraction;
		this.verbEnding = verbEnding;
	}

	/** A description template with her name and these pronouns filled in. */
	String fill(String template, String name)
	{
		return template
			.replace("{name}", name)
			.replace("{They}", capital(subject))
			.replace("{they}", subject)
			.replace("{them}", object)
			.replace("{Their}", capital(possessive))
			.replace("{their}", possessive)
			.replace("{theyre}", contraction)
			.replace("{s}", verbEnding);
	}

	private static String capital(String word)
	{
		return Character.toUpperCase(word.charAt(0)) + word.substring(1);
	}

	/** What the settings dropdown shows. */
	@Override
	public String toString()
	{
		return label;
	}
}
