package com.tsbreuer.multilines;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MultiLinesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MultiLinesPlugin.class);
		RuneLite.main(args);
	}
}