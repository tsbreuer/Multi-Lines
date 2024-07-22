/*
 * Copyright (c) 2020, Jordan <nightfirecat@protonmail.com>
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

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("Multi-lines")
public interface MultiLinesConfig extends Config
{
	String warning = "Warning, this plugin does not include Wilderness Multi Areas. Please use Wilderness Lines for that.";

	@ConfigItem(
			keyName = "UsageWarning",
			name = "Warning",
			description = "Warning about plugin",
			position = 0,
			section = warning
	)
	default String getWarning()
	{
		return "";
	}

	@ConfigItem(
			keyName = "UsageWarning",
			name = "",
			description = ""
	)
	void setWarning(String key);

	@ConfigItem(
			position = 1,
			keyName = "showLoginMessage",
			name = "Show Login Message on chat",
			description = "Enable or disable the message in chat when loggin in or hopping",
			section = multiLines
	)
	default boolean showLoginMessage()
	{
		return true;
	}

	@ConfigSection(
		name = "Multi Lines",
		description = "",
		position = 2
	)
	String multiLines = "multiLines";

	@ConfigItem(
		position = 1,
		keyName = "multiLinesColor",
		name = "Multi lines color",
		description = "Color of lines bordering multi-combat zones",
		section = multiLines
	)
	@Alpha
	default Color multiLinesColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 2,
		keyName = "showSpearLines",
		name = "Show spear lines",
		description = "Show the area in which you can be potentially speared into a multi-combat zone",
		section = multiLines
	)
	default boolean showSpearLines()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "spearLinesColor",
		name = "Spear lines color",
		description = "Color of lines bordering spear areas surrounding multi-combat zones",
		section = multiLines
	)
	@Alpha
	default Color spearLinesColor()
	{
		return Color.ORANGE;
	}
}
