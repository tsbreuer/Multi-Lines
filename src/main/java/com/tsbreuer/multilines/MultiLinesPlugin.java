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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@PluginDescriptor(
	name = "Multi Lines",
	description = "Show Multi mullti-combat areas and the dragon spear range to those areas outside the wilderness",
	tags = {"dragon spear", "multicombat", "multi-combat", "multi", "dmm"}
)
public class MultiLinesPlugin extends Plugin {
	private List<Rectangle> Multi_MULTI_AREAS = new ArrayList<Rectangle>();
	private static final int SPEAR_RANGE = 4;
	private int LoadedAreas = 0;

	private Area MULTI_AREA = new Area();
	private Area SPEAR_MULTI_AREA = new Area();
	GeneralPath drawPathsNormal = new GeneralPath();
	GeneralPath drawPathsSpear = new GeneralPath();

	@Inject
	private MultiLinesOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MultiLinesConfig config;

	@Provides
	MultiLinesConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(MultiLinesConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) // Each time map is updated
		{
			log.debug("LOGGED IN");
			if (LoadedAreas == 0) {
				log.debug("Requested Multi Lines Load");
				executor.execute(() -> {
							UpdateMultiLines(Multi_MULTI_AREAS, client.getWorldType());
						}
				);
				LoadedAreas = 1;
			}
			executor.execute(() ->
					updateLinesToDisplayNormal(MULTI_AREA)
			);
			executor.execute(() ->
					updateLinesToDisplaySpear(SPEAR_MULTI_AREA)
			);
		} else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.LOGGING_IN) {
			LoadedAreas = 0;
		} else if (gameStateChanged.getGameState() == GameState.HOPPING) {
			LoadedAreas = 0;
		}
	}

	@Override
	public void startUp() {
		overlayManager.add(overlay);
		config.setWarning("Warning, this plugin does not include Wilderness Multi Areas. Please use Wilderness Lines for that.");
	}

	@Override
	public void shutDown() {
		overlayManager.remove(overlay);
	}

	public Runnable UpdateMultiLines(List<Rectangle> arrayListToUpdate, EnumSet<WorldType> worldType) {
		// Lookup lastest data
		String githubURL;
		if (worldType.contains(WorldType.DEADMAN)){
			githubURL = "https://raw.githubusercontent.com/tsbreuer/Multi-Lines/geoJSON/src/main/java/com/tsbreuer/multilines/MultiLinesDataDMM.json?_=" + System.currentTimeMillis();
		}
		else {
			githubURL = "https://raw.githubusercontent.com/tsbreuer/Multi-Lines/geoJSON/src/main/java/com/tsbreuer/multilines/MultiLinesData.json?_=" + System.currentTimeMillis();
		}


		try {
			HttpClient hClient = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_1_1)
					.followRedirects(HttpClient.Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(20))
					.build();

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(githubURL))
					.timeout(Duration.ofSeconds(15))
					.GET()
					.build();
			HttpResponse<String> response = hClient.send(request, HttpResponse.BodyHandlers.ofString());
			// Convert to a JSON object to print data
			JsonParser jp = new JsonParser(); //from gson
			JsonElement root = jp.parse(response.body()); //Convert response to a json element
			//log.debug(githubURL);
			//log.debug(root.toString()); // Debug connection info
			JsonObject rootobj = root.getAsJsonObject();
			JsonObject MultiLines = rootobj.get("MultiLines").getAsJsonObject(); // Main object
			JsonArray MultiAreas = MultiLines.get("Areas").getAsJsonArray(); // Areas List
			List<Rectangle> tempArray = new ArrayList<Rectangle>(); // Clean existing Areas
			for (JsonElement obj : MultiAreas) { // Map through each area to add tiles
				//log.debug("Area: " + obj.getAsJsonObject().get("Name").getAsString());
				//log.debug("" + obj.getAsJsonObject().get("Enabled").getAsBoolean());
				//log.debug("" + obj.getAsJsonObject().get("Disabled").getAsBoolean());
				//log.debug("" + obj.getAsJsonObject().get("Wilderness").getAsBoolean());
				if (obj.getAsJsonObject().get("Enabled").getAsBoolean() && !obj.getAsJsonObject().get("Removed").getAsBoolean() && !obj.getAsJsonObject().get("Wilderness").getAsBoolean()) {
					JsonArray tiles = obj.getAsJsonObject().get("Tiles").getAsJsonArray();
					for (JsonElement tile : tiles) { // Loop through each rectangle
						JsonObject tileObject = tile.getAsJsonObject();
						// Add each rectangle of tiles
						tempArray.add(
								new Rectangle(
										tileObject.get("x").getAsInt(),
										tileObject.get("y").getAsInt(),
										tileObject.get("width").getAsInt(),
										tileObject.get("height").getAsInt()
								)
						);
					}
				}
			}
			arrayListToUpdate.clear();
			arrayListToUpdate.addAll(tempArray);
			UpdateSpearRanges(); // Once we're done, update Spear Ranges
			//log.debug("Multi Areas Updated");
			if (config.showLoginMessage()){
				clientThread.invokeLater(() -> {
					if (worldType.contains(WorldType.DEADMAN)){
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "MultiLines", "Multi Lines Loaded for DMM. If you see any areas incorrectly mapped, please let me know", null);
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "MultiLines", "You can contact me on discord @hypex or make an issue on github. You can turn off this message on settings", null);
					}
					else {
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "MultiLines", "Normal Multi Lines Loaded. You can turn off this message on settings.", null);
					}
				});
			}
		} catch (IOException | InterruptedException | IllegalStateException e) {
			clientThread.invokeLater(() -> {
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "MultiLines", "Error Loading Multi Lines", null);
			});

			//log.debug("Error Loading Multi Tiles from Github");;
		}
		return null;
	}

	public void UpdateSpearRanges() {
		SPEAR_MULTI_AREA = new Area();
		MULTI_AREA = new Area();
		for (final Rectangle multiArea : Multi_MULTI_AREAS) {
			MULTI_AREA.add(new Area(multiArea));
			for (int i = 0; i <= SPEAR_RANGE; i++) {
				final Rectangle spearArea = new Rectangle(multiArea);
				spearArea.grow(SPEAR_RANGE - i, i);
				SPEAR_MULTI_AREA.add(new Area(spearArea));
			}
		}
	}

	private void transformWorldToLocal(float[] coords) {
		final LocalPoint lp = LocalPoint.fromWorld(client, (int) coords[0], (int) coords[1]);
		coords[0] = lp.getX() - Perspective.LOCAL_TILE_SIZE / 2f;
		coords[1] = lp.getY() - Perspective.LOCAL_TILE_SIZE / 2f;
	}

	GeneralPath getMultiLinesToDisplay() {
		return drawPathsNormal;
	}

	GeneralPath getSpearLinesToDisplay() {
		return drawPathsSpear;
	}


	private void updateLinesToDisplayNormal(final Shape... shapes) {
		final Rectangle sceneRect = new Rectangle(
				client.getBaseX() + 1, client.getBaseY() + 1,
				Constants.SCENE_SIZE - 2, Constants.SCENE_SIZE - 2);


		final GeneralPath paths = new GeneralPath();
		for (final Shape shape : shapes) {
			GeneralPath lines = new GeneralPath(shape);
			lines = Geometry.clipPath(lines, sceneRect);
			lines = Geometry.splitIntoSegments(lines, 1);
			lines = Geometry.transformPath(lines, this::transformWorldToLocal);
			paths.append(lines, false);
		}
		//log.debug("Updated Normal Paths");
		drawPathsNormal = paths;
	}

	private void updateLinesToDisplaySpear(final Shape... shapes)
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
		//log.debug("Updated Spear Paths");
		drawPathsSpear = paths;
	}
}
