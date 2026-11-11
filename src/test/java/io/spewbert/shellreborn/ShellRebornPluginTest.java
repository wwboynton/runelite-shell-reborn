package io.spewbert.shellreborn;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ShellRebornPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ShellRebornPlugin.class);
		RuneLite.main(args);
	}
}
