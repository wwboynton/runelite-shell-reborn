package io.spewbert.shellreborn;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
import net.runelite.api.Animation;
import net.runelite.api.AnimationController;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.RuneLiteObjectController;
import net.runelite.api.coords.LocalPoint;

/**
 * The snail, drawn and moved by the client each frame while registered. She behaves as a wandering NPC does:
 * stands a good while, then potters a tile or few somewhere else on her patch, at an NPC's walking pace and
 * turn rate, with her own stand and walk animations. Client thread. Adapted from SpewKit's TecuSalamander.
 */
final class Snail extends RuneLiteObjectController
{
	/** local units per client cycle while walking, the client's own NPC walk speed */
	private static final int WALK_SPEED = 4;
	/** orientation units per client cycle: her NPC definition's turn rate */
	private static final int TURN_SPEED = 32;
	/** client cycles (20 ms) she stands between walks: a snail is in no hurry */
	private static final int IDLE_MIN = 300;
	private static final int IDLE_MAX = 1200;
	/** the furthest a single walk goes, in steps */
	private static final int WANDER_STEPS = 3;

	private final Client client;
	private final Island island;
	private final Model model;
	private final Animation stand;
	private final Animation walk;
	private final Random random;
	private final AnimationController animation;
	private Animation playing;

	private int tileX;
	private int tileY;
	private final ArrayDeque<int[]> route = new ArrayDeque<>();
	private int idle;
	private int facing;

	Snail(Client client, Island island, int[] tile, Model model, Animation stand, Animation walk, Random random, int worldView)
	{
		this.client = client;
		this.island = island;
		this.model = model;
		this.stand = stand;
		this.walk = walk;
		this.random = random;
		this.tileX = tile[0];
		this.tileY = tile[1];
		this.facing = random.nextInt(2048);
		this.idle = random.nextInt(IDLE_MAX);
		this.animation = new AnimationController(client, stand);
		playing = stand;
		setWorldView(worldView);
		setLevel(island.plane);
		setX(centre(tileX));
		setY(centre(tileY));
		setOrientation(facing);
		ground();
	}

	/** The base model, unposed: what the clickbox is worked out from. */
	Model baseModel()
	{
		return model;
	}

	@Override
	public void tick(int cycles)
	{
		if (route.isEmpty())
		{
			idle -= cycles;
			if (idle <= 0)
			{
				List<int[]> next = island.wander(tileX, tileY, 1 + random.nextInt(WANDER_STEPS), random);
				route.addAll(next);
				idle = IDLE_MIN + random.nextInt(IDLE_MAX - IDLE_MIN);
			}
		}
		if (!route.isEmpty())
		{
			step(cycles);
		}
		turn(cycles);
		pose(route.isEmpty() ? stand : walk, cycles);
		ground();
	}

	/** Walks towards the next tile of the route; on arriving, that tile is where she stands. */
	private void step(int cycles)
	{
		int[] next = route.peek();
		int tx = centre(next[0]);
		int ty = centre(next[1]);
		int dx = tx - getX();
		int dy = ty - getY();
		if (dx != 0 || dy != 0)
		{
			facing = Island.facing(dx, dy);
		}
		int move = WALK_SPEED * Math.max(1, cycles);
		setX(getX() + clamp(dx, move));
		setY(getY() + clamp(dy, move));
		if (getX() == tx && getY() == ty)
		{
			route.poll();
			tileX = next[0];
			tileY = next[1];
			int[] after = route.peek();
			if (after != null && !island.canStep(tileX, tileY, after[0] - tileX, after[1] - tileY))
			{
				route.clear();
			}
		}
	}

	private void turn(int cycles)
	{
		int current = getOrientation();
		int diff = ((facing - current + 1024) & 2047) - 1024;
		int max = TURN_SPEED * Math.max(1, cycles);
		setOrientation((current + clamp(diff, max)) & 2047);
	}

	private void pose(Animation wanted, int cycles)
	{
		if (wanted != playing || animation.getAnimation() == null)
		{
			playing = wanted;
			animation.setAnimation(wanted);
		}
		animation.tick(cycles);
	}

	private void ground()
	{
		setZ(Perspective.getTileHeight(client, new LocalPoint(getX(), getY(), getWorldView()), island.plane));
	}

	@Override
	public Model getModel()
	{
		Model posed = animation.getAnimation() == null ? null : animation.animate(model);
		return posed == null ? model : posed;
	}

	private static int centre(int sceneTile)
	{
		return (sceneTile << Perspective.LOCAL_COORD_BITS) + Perspective.LOCAL_HALF_TILE_SIZE;
	}

	private static int clamp(int value, int max)
	{
		return Math.max(-max, Math.min(max, value));
	}
}
