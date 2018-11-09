package lemon.halite3.visualizer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.util.Map.Entry;

import javax.swing.JPanel;

import lemon.halite3.visualizer.Visualizer.MinePlan;

public class Renderer extends JPanel {
	private static final long serialVersionUID = 1L;
	private Vector shipLocation;
	private Vector bestPlan;
	private int[][] halite;
	private MinePlan[][] minePlans;
	private int[][] scores;
	public Renderer() {
		this(null, null, null, null, null);
	}
	public Renderer(Vector shipLocation, Vector bestPlan, int[][] halite, MinePlan[][] minePlans, int[][] scores) {
		this.halite = halite;
		this.minePlans = minePlans;
		this.scores = scores;
		this.shipLocation = shipLocation;
		this.bestPlan = bestPlan;
	}
	public void setInfo(Vector shipLocation, Vector bestPlan, int[][] halite, MinePlan[][] minePlans, int[][] scores) {
		this.halite = halite;
		this.minePlans = minePlans;
		this.scores = scores;
		this.shipLocation = shipLocation;
		this.bestPlan = bestPlan;
	}
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		renderHalite(g);
		int tileWidth = this.getWidth() / halite.length;
		int tileHeight = this.getHeight() / halite[0].length;
		int mouseX = (int) (MouseInfo.getPointerInfo().getLocation().getX() - this.getLocationOnScreen().getX());
		int mouseY = (int) (MouseInfo.getPointerInfo().getLocation().getY() - this.getLocationOnScreen().getY());
		try {
			renderMinePlan(g, minePlans[(int) (mouseX / tileWidth)][(int) (mouseY / tileHeight)]);
			System.out.println("Rendering: " + (int) (mouseX / tileWidth) + " - " + (int) (mouseY / tileHeight));
		} catch (Exception ex) {
			System.out.println((int) (mouseX / tileWidth) + " - " + (int) (mouseY / tileHeight));
		}
		renderScores(g);

		// Render shipLocation
		g.setColor(Color.MAGENTA);
		g.drawRect(shipLocation.getX() * tileWidth, shipLocation.getY() * tileHeight, tileWidth, tileHeight);
		// Render bestPlan
		g.setColor(Color.ORANGE);
		g.drawRect(bestPlan.getX() * tileWidth, bestPlan.getY() * tileHeight, tileWidth, tileHeight);
		renderMinePlan(g, minePlans[bestPlan.getX()][bestPlan.getY()]);
		
		this.repaint();
	}
	public void renderHalite(Graphics g) {
		int tileWidth = this.getWidth() / halite.length;
		int tileHeight = this.getHeight() / halite[0].length;
		for (int i = 0; i < halite.length; ++i) {
			for (int j = 0; j < halite[0].length; ++j) {
				g.setColor(new Color(0, 0, 255, Math.min(255, (int)(255 * (halite[i][j] / 1000.0)))));
				g.fillRect(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
			}
		}
	}
	public void renderMinePlan(Graphics g, MinePlan plan) {
		int tileWidth = this.getWidth() / halite.length;
		int tileHeight = this.getHeight() / halite[0].length;
		g.setColor(Color.YELLOW);
		g.drawRect(plan.getQuad().getLocation().getX() * tileWidth, plan.getQuad().getLocation().getY() * tileHeight,
				plan.getQuad().getSize().getX() * tileWidth, plan.getQuad().getSize().getY() * tileHeight);
		for (Entry<Vector, Integer> entry : plan.getMineMap().entrySet()) {
			g.setColor(new Color(255, 0, 0, (int) (entry.getValue() / 1000.0 * 255)));
			g.fillRect(entry.getKey().getX() * tileWidth, entry.getKey().getY() * tileHeight, tileWidth, tileHeight);
		}
		System.out.println("Count: " + plan.getCount());
	}
	public void renderScores(Graphics g) {
		int tileWidth = this.getWidth() / halite.length;
		int tileHeight = this.getHeight() / halite[0].length;
		for (int i = 0; i < scores.length; ++i) {
			for (int j = 0; j < scores[0].length; ++j) {
				g.setColor(new Color(0, 255, 0, Math.min(255, (int)(255 * (scores[i][j] / 1000.0)))));
				g.fillRect(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
				g.setColor(Color.BLACK);
				g.drawString(scores[i][j] + "", i * tileWidth, j * tileHeight + tileHeight);
			}
		}
	}
}
