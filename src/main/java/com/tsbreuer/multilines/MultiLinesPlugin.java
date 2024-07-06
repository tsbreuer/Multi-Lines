/*
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
 * Copyright (c) 2021, Jordan <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tsbreuer.multilines;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.List;

@PluginDescriptor(
	name = "Multi Lines",
	description = "Show Multi mullti-combat areas and the dragon spear range to those areas outside the wilderness",
	tags = {"dragon spear", "multicombat", "multi-combat", "multi", "dmm"}
)
public class MultiLinesPlugin extends Plugin
{
	private static final List<Rectangle> Multi_MULTI_AREAS = ImmutableList.of(
			// Non Wilderness Multi Lines for DMM/PK Worlds

			// Weiss
			new Rectangle(2852, 3922, 40, 25),
			new Rectangle(2854, 3947, 40, 1),
			new Rectangle(2892, 3934, 2, 13),
			new Rectangle(2892, 3933, 1, 1),
			new Rectangle(2894, 3936, 1, 10),
			new Rectangle(2871, 3948, 9, 1),
			new Rectangle(2872, 3949, 6, 1),
			new Rectangle(2882, 3948, 11, 1),
			new Rectangle(2884, 3949, 5, 1),
			new Rectangle(2895, 3937, 1, 8),
			new Rectangle(2896, 3941, 1, 3),
			new Rectangle(2892, 3927, 1, 3),
			new Rectangle(2847, 3921, 36, 1),
			new Rectangle(2847, 3922, 5, 7),
			new Rectangle(2848, 3929, 4, 1),
			new Rectangle(2850, 3930, 2, 1),
			new Rectangle(2851, 3931, 1, 13),
			new Rectangle(2850, 3939, 1, 4),
			new Rectangle(2847, 3920, 31, 1),
			new Rectangle(2848, 3919, 15, 1),
			new Rectangle(2849, 3918, 9, 1),
			new Rectangle(2852, 3917, 5, 1),
			new Rectangle(2873, 3919, 3, 1),
			new Rectangle(2886, 3921, 5, 1),
			new Rectangle(2860, 3948, 8, 1),

			// Troll Stronghold/GWD
			new Rectangle(2880, 3742, 32, 22),
			new Rectangle(2888, 3718, 24, 24),
			new Rectangle(2896, 3688, 18, 30),
			new Rectangle(2882, 3696, 14, 22),
			new Rectangle(2877, 3696, 5, 8)



	);
	private static final int SPEAR_RANGE = 4;

	private static final Area MULTI_AREA = new Area();
	private static final Area SPEAR_MULTI_AREA = new Area();

	static
	{
		for (final Rectangle multiArea : Multi_MULTI_AREAS)
		{
			MULTI_AREA.add(new Area(multiArea));
			for (int i = 0; i <= SPEAR_RANGE; i++)
			{
				final Rectangle spearArea = new Rectangle(multiArea);
				spearArea.grow(SPEAR_RANGE - i, i);
				SPEAR_MULTI_AREA.add(new Area(spearArea));
			}
		}
	}

	@Inject
	private MultiLinesOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Provides
	MultiLinesConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MultiLinesConfig.class);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(overlay);
	}

	private void transformWorldToLocal(float[] coords)
	{
		final LocalPoint lp = LocalPoint.fromWorld(client, (int)coords[0], (int)coords[1]);
		coords[0] = lp.getX() - Perspective.LOCAL_TILE_SIZE / 2f;
		coords[1] = lp.getY() - Perspective.LOCAL_TILE_SIZE / 2f;
	}

	GeneralPath getMultiLinesToDisplay()
	{
		return getLinesToDisplay(MULTI_AREA);
	}

	GeneralPath getSpearLinesToDisplay()
	{
		return getLinesToDisplay(SPEAR_MULTI_AREA);
	}

	private GeneralPath getLinesToDisplay(final Shape... shapes)
	{
		final Rectangle sceneRect = new Rectangle(
			client.getBaseX() + 1, client.getBaseY() + 1,
			Constants.SCENE_SIZE - 2, Constants.SCENE_SIZE - 2);

		final GeneralPath paths = new GeneralPath();
		for (final Shape shape : shapes)
		{
			GeneralPath lines = new GeneralPath(shape);
			lines = Geometry.clipPath(lines, sceneRect);
			lines = Geometry.splitIntoSegments(lines, 1);
			lines = Geometry.transformPath(lines, this::transformWorldToLocal);
			paths.append(lines, false);
		}
		return paths;
	}
}
