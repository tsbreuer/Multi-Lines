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
			new Rectangle(2877, 3696, 5, 8),

			// Death Plateau
			new Rectangle(2848, 3601, 31, 6),
			new Rectangle(2872, 3600, 7, 1),
			new Rectangle(2871, 3607, 5, 1),
			new Rectangle(2879, 3602, 1, 5),

			//Burthorpe & White wolf mountain
			new Rectangle(2880, 3520, 24, 24),
			new Rectangle(2816, 3456, 64, 64),

			// Chaos Altar Fally
			new Rectangle(2928, 3512, 16, 8),

			// Falador
			new Rectangle(2944, 3304, 64, 152),
			new Rectangle(3008, 3303, 8, 25),
			new Rectangle(3007, 3304, 1, 25),
			new Rectangle(3014, 3299, 7, 4),
			new Rectangle(3016, 3303, 5, 9),
			new Rectangle(3021, 3297, 11, 15),
			new Rectangle(3021, 3312, 23, 1),
			new Rectangle(3022, 3313, 21, 1),
			new Rectangle(3032, 3299, 12, 13),
			new Rectangle(3032, 3298, 11, 1),
			new Rectangle(3038, 3297, 4, 1),

			//Barb village
			new Rectangle(3072, 3448, 64, 8),
			new Rectangle(3056, 3392, 80, 48),
			new Rectangle(3064, 3440, 72, 8),
			new Rectangle(3048, 3392, 8, 16),

			//Draynor jail
			new Rectangle(3102, 3248, 2, 3),
			new Rectangle(3104, 3236, 28, 19),
			new Rectangle(3132, 3236, 1, 15),
			new Rectangle(3133, 3238, 1, 12),
			new Rectangle(3134, 3239, 1, 7),
			new Rectangle(3113, 3255, 19, 3),
			new Rectangle(3114, 3258, 18, 1),
			new Rectangle(3119, 3259, 12, 1),
			new Rectangle(3123, 3260, 4, 1),
			new Rectangle(3106, 3255, 7, 1),
			new Rectangle(3108, 3256, 5, 2),
			new Rectangle(3103, 3237, 1, 4),
			new Rectangle(3112, 3235, 21, 1),
			new Rectangle(3117, 3234, 15, 1),
			new Rectangle(3119, 3233, 9, 1),

			// wizard tower
			new Rectangle(3094, 3145, 31, 31),
			new Rectangle(3103, 3176, 5, 1),

			// Al kharid
			new Rectangle(3264, 3136, 64, 64),

			// Mort myre swamp
			new Rectangle(3456, 3328, 64, 64),

			// South burgh de rott and that one weird shop patch
			new Rectangle(3512, 3232, 7, 14),
			// This area does not seem to be multi, might be on DMM worlds
			//new Rectangle(3520, 3158, 66, 42),
			//new Rectangle(3586, 3183, 1, 17)

			// South east of port phasmatys
			new Rectangle(3694, 3433, 15, 20),
			new Rectangle(3702, 3453, 7, 1),
			new Rectangle(3690, 3433, 4, 20),
			new Rectangle(3692, 3453, 10, 1),
			new Rectangle(3689, 3450, 1, 3),
			new Rectangle(3684, 3440, 6, 10),
			new Rectangle(3684, 3439, 6, 1),
			new Rectangle(3683, 3440, 1, 7),
			new Rectangle(3685, 3433, 5, 6),

			// south west of port phasmatys
			new Rectangle(3589, 3451, 54, 5),
			new Rectangle(3590, 3448, 53, 3),
			new Rectangle(3592, 3444, 51, 4),
			new Rectangle(3594, 3441, 49, 3),
			new Rectangle(3596, 3432, 47, 9),
			new Rectangle(3604, 3426, 39, 6),
			new Rectangle(3621, 3415, 22, 11),
			new Rectangle(3608, 3420, 13, 6),
			new Rectangle(3643, 3419, 10, 9),
			new Rectangle(3643, 3428, 9, 2),
			new Rectangle(3643, 3437, 6, 8),
			new Rectangle(3643, 3445, 5, 2),
			new Rectangle(3643, 3447, 4, 2),
			new Rectangle(3643, 3449, 3, 4),

			// Past bridge south
			new Rectangle(3589, 3408, 19, 4),
			new Rectangle(3584, 3407, 5, 5),
			new Rectangle(3584, 3406, 2, 1),
			new Rectangle(3597, 3412, 10, 11),
			new Rectangle(3586, 3423, 19, 4),
			new Rectangle(3586, 3427, 15, 4),
			new Rectangle(3584, 3412, 13, 11),
			new Rectangle(3608, 3411, 1, 1),

			// Fossil island
			// Top right
			new Rectangle(3653, 3868, 10, 17),
			new Rectangle(3663, 3872, 1, 10),

			// Bottom left
			new Rectangle(3801, 3751, 2, 11),
			new Rectangle(3798, 3748, 3, 11),
			new Rectangle(3803, 3752, 23, 10),
			new Rectangle(3803, 3762, 23, 1),
			new Rectangle(3806, 3763, 20, 1),
			new Rectangle(3808, 3764, 18, 1),
			new Rectangle(3810, 3765, 16, 1),
			new Rectangle(3812, 3766, 12, 1),
			new Rectangle(3814, 3767, 6, 1),

			// Top beach
			new Rectangle(3684, 3896, 2, 1),
			new Rectangle(3684, 3897, 4, 1),
			new Rectangle(3684, 3898, 8, 1),
			new Rectangle(3684, 3899, 58, 5),
			new Rectangle(3704, 3898, 38, 1),
			new Rectangle(3705, 3897, 37, 1),
			new Rectangle(3712, 3896, 30, 1),
			new Rectangle(3713, 3895, 29, 1),
			new Rectangle(3714, 3894, 28, 1),
			new Rectangle(3716, 3893, 26, 1),
			new Rectangle(3717, 3892, 25, 1),
			new Rectangle(3716, 3891, 26, 1),
			new Rectangle(3714, 3890, 28, 1),
			new Rectangle(3712, 3888, 30, 2),
			new Rectangle(3710, 3881, 32, 7),
			new Rectangle(3712, 3876, 14, 5),
			new Rectangle(3713, 3873, 13, 3),
			new Rectangle(3714, 3870, 12, 3),
			new Rectangle(3713, 3866, 13, 4),
			new Rectangle(3712, 3861, 14, 5),
			new Rectangle(3710, 3860, 16, 1),
			new Rectangle(3709, 3859, 17, 1),
			new Rectangle(3708, 3848, 18, 11),
			new Rectangle(3709, 3847, 17, 1),
			new Rectangle(3710, 3846, 16, 1),
			new Rectangle(3712, 3842, 5, 4),
			new Rectangle(3717, 3843, 7, 3),
			new Rectangle(3724, 3842, 6, 4),
			new Rectangle(3726, 3846, 22, 11),
			new Rectangle(3730, 3843, 2, 3),
			new Rectangle(3732, 3844, 8, 2),
			new Rectangle(3740, 3845, 2, 1),
			new Rectangle(3742, 3844, 3, 2),
			new Rectangle(3748, 3850, 6, 7),
			new Rectangle(3748, 3847, 1, 3),
			new Rectangle(3749, 3848, 1, 2),
			new Rectangle(3750, 3849, 1, 1),
			new Rectangle(3754, 3851, 1, 6),
			new Rectangle(3755, 3852, 1, 4),

			// Relekka
			new Rectangle(2650, 3712, 86, 16),
			new Rectangle(2712, 3728, 24, 8),
			new Rectangle(2650, 3728, 54, 9),

			// Settlement Ruins
			new Rectangle(1536, 3875, 35, 33),
			new Rectangle(1543, 3874, 26, 1),
			new Rectangle(1544, 3873, 25, 1),
			new Rectangle(1545, 3872, 23, 1),
			new Rectangle(1546, 3871, 21, 1),
			new Rectangle(1547, 3870, 19, 1),
			new Rectangle(1548, 3869, 17, 1),
			new Rectangle(1549, 3868, 15, 1),
			new Rectangle(1554, 3867, 10, 1),
			new Rectangle(1558, 3866, 6, 1),
			new Rectangle(1560, 3865, 3, 1),
			new Rectangle(1561, 3864, 1, 1),
			new Rectangle(1569, 3874, 1, 1),
			new Rectangle(1571, 3875, 1, 6),
			new Rectangle(1572, 3876, 1, 4),
			new Rectangle(1535, 3888, 1, 2),
			new Rectangle(1534, 3890, 2, 8),
			new Rectangle(1533, 3896, 1, 2),
			new Rectangle(1532, 3898, 4, 10),
			new Rectangle(1531, 3902, 1, 2),
			new Rectangle(1529, 3903, 2, 1),
			new Rectangle(1528, 3904, 4, 4),
			new Rectangle(1527, 3905, 1, 1),
			new Rectangle(1529, 3908, 6, 1),
			new Rectangle(1530, 3909, 4, 1),
			new Rectangle(1530, 3910, 4, 1),
			new Rectangle(1556, 3908, 2, 1),
			new Rectangle(1557, 3909, 1, 1),
			new Rectangle(1558, 3908, 13, 3),
			new Rectangle(1562, 3911, 2, 1),
			new Rectangle(1564, 3911, 7, 2),
			new Rectangle(1568, 3913, 18, 1),
			new Rectangle(1572, 3914, 12, 1),
			new Rectangle(1574, 3915, 1, 1),
			new Rectangle(1575, 3915, 7, 2),
			new Rectangle(1582, 3915, 1, 1),
			new Rectangle(1571, 3884, 1, 1),
			new Rectangle(1571, 3885, 2, 3),
			new Rectangle(1573, 3886, 1, 2),
			new Rectangle(1574, 3887, 2, 1),
			new Rectangle(1571, 3888, 21, 25),
			new Rectangle(1591, 3913, 1, 1),
			new Rectangle(1592, 3889, 1, 1),
			new Rectangle(1592, 3890, 2, 2),
			new Rectangle(1592, 3892, 3, 3),
			new Rectangle(1592, 3895, 4, 1),
			new Rectangle(1592, 3896, 5, 1),
			new Rectangle(1592, 3897, 6, 2),
			new Rectangle(1592, 3899, 7, 1),
			new Rectangle(1592, 3900, 8, 2),
			new Rectangle(1592, 3902, 2, 12),
			new Rectangle(1594, 3911, 1, 2),
			new Rectangle(1595, 3911, 1, 1),
			new Rectangle(1594, 3902, 3, 9),
			new Rectangle(1597, 3909, 1, 1),
			new Rectangle(1597, 3908, 2, 1),
			new Rectangle(1597, 3902, 3, 6),
			new Rectangle(1600, 3902, 1, 5),
			new Rectangle(1601, 3904, 1, 3),
			new Rectangle(1602, 3906, 1, 1)

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
