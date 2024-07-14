/*
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
 * Copyright (c) 2021, Jordan Atwood <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tsbreuer.multilines;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;

class MultiLinesOverlay extends Overlay
{
	private final MultiLinesPlugin plugin;
	private final MultiLinesConfig config;
	private final Client client;

	@Inject
	private MultiLinesOverlay(MultiLinesPlugin plugin, MultiLinesConfig config, Client client)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showSpearLines())
		{
			renderPath(graphics, plugin.getSpearLinesToDisplay(), config.spearLinesColor());
		}
		renderPath(graphics, plugin.getMultiLinesToDisplay(), config.multiLinesColor());

		return null;
	}

	public double getSlope(Line2D line) {
		return (
				(line.getY2() - line.getY1()) / ((line.getX2() - line.getX1()))
		);
	}

	public boolean areParallel(double slope1, double slope2, double marginOfError){
		if (Math.abs(slope1 - slope2) < marginOfError) {
			//System.out.println(slope1 + " " + slope2 + " " + marginOfError + " " + Math.abs(slope1 - slope2) * 100);
		}
		return Math.abs(slope1 - slope2) < marginOfError;
	}

	public GeneralPath simplifyPath(PathIterator it)
	{
		GeneralPath newPath = new GeneralPath();
		float[] coords = new float[2];
		float[] startCoords = new float[2];
		float[] currentCoords = new float[2];
		Line2D prevLine = null;
		//System.out.println("Start of simplifyPath");
		int iterations = 0;
		int lastOperation = 0;
		boolean	parallel = false;
		while (!it.isDone())
		{
			iterations++;
			//System.out.println(iterations);
			int type = it.currentSegment(coords);
			//System.out.println("Type " + type);
			if (type == PathIterator.SEG_MOVETO)
			{
				if (prevLine != null){
					newPath.lineTo(currentCoords[0], currentCoords[1]);
				}
				startCoords[0] = coords[0];
				startCoords[1] = coords[1];
				//System.out.println("Moved to " + startCoords[0] + " " + startCoords[1]);
				newPath.moveTo(coords[0], coords[1]);
				prevLine = null;
			}
			else if (type == PathIterator.SEG_LINETO)
			{
				if (prevLine != null) {
					Line2D currentLine = new Line2D.Float(currentCoords[0], currentCoords[1], coords[0], coords[1]);
					if (areParallel(getSlope(currentLine),getSlope(prevLine), 0.07)) {
						// We save coords of current pos and go next
						currentCoords[0] = coords[0];
						currentCoords[1] = coords[1];
						parallel = true;
						//System.out.println("Parallel to " + currentCoords[0] + " " + currentCoords[1]);
					}
					else
					{
						//System.out.println("line " + prevLine);
						//System.out.println("NewLineEnd: " + coords[0] + " " + coords[1]);
						// No longer parallel so draw line up to current point
						newPath.lineTo(currentCoords[0], currentCoords[1]);
						parallel = false;
						//System.out.println("Drawed line "+ startCoords[0] + " " +  startCoords[1] + " " +  currentCoords[0] + " " + currentCoords[1]);
						// Move start coordination to last point we drew to
						startCoords[0] = currentCoords[0];
						startCoords[1] = currentCoords[1];
						// Move current coords to next target
						currentCoords[0] = coords[0];
						currentCoords[1] = coords[1];
						prevLine = currentLine;
						lastOperation = iterations; // Janky because we dont have hasNext() for PathIterator
					}
				}
				else {
					prevLine = new Line2D.Float(startCoords[0], startCoords[1], coords[0], coords[1]);
					currentCoords[0] = coords[0];
					currentCoords[1] = coords[1];
					//System.out.println("First Line, moved to " + currentCoords[0] + " " + currentCoords[1]);
				}
			}
			else if (type == PathIterator.SEG_CLOSE)
			{
				if (prevLine != null) {
					//System.out.println(" Current coords " + currentCoords[0] + " " + currentCoords[1] + " new Coords: " + coords[0] + " " + coords[1]);
					newPath.lineTo(coords[0], coords[1]);
				}
				else
				{
					System.out.println("Error! Not sure how we got here");
				}
				newPath.closePath();
			}
			it.next();
			if (it.isDone()){
					//System.out.println("End of iteration");
					newPath.lineTo(coords[0], coords[1]);
			}
		}
		//System.out.println(iterations);

		return newPath;
	}

	public int getPathLength(PathIterator it){
		int length = 0;
		while (!it.isDone()){
			length++;
			it.next();
		}
		return length;
	};

	private void renderPath(Graphics2D graphics, GeneralPath path, Color color)
	{
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(1));

		// Filter out not from our same plane
		path = Geometry.filterPath(path, (p1, p2) ->
				Perspective.localToCanvas(client, new LocalPoint((int) p1[0], (int) p1[1]), client.getPlane()) != null &&
						Perspective.localToCanvas(client, new LocalPoint((int) p2[0], (int) p2[1]), client.getPlane()) != null);


		path = Geometry.transformPath(path, coords ->
		{
			Point point = Perspective.localToCanvas(client, new LocalPoint((int) coords[0], (int) coords[1]), client.getPlane());
			coords[0] = point.getX();
			coords[1] = point.getY();
		});

		// Filter off screen
		path = Geometry.filterPath(path, (p1, p2) ->
				// p1 within canvas
				(p1[0] > 0 && p1[0] < client.getCanvasWidth()) && (p1[1] > 0 && p1[1] < client.getCanvasHeight())
				||
				// p2 within canvas
				(p2[0] > 0 && p2[0] < client.getCanvasWidth()) && (p2[1] > 0 && p2[1] < client.getCanvasHeight())

		);
		//int count = getPathLength(path.getPathIterator((new AffineTransform())));
		// Reduce number of lines to draw
		path = simplifyPath(path.getPathIterator((new AffineTransform())));
		//System.out.println("Saved: " + (count - getPathLength(path.getPathIterator((new AffineTransform())))) + " out of " + count);

		graphics.draw(path);
	}
}
