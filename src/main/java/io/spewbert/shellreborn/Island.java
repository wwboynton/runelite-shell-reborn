package io.spewbert.shellreborn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.CollisionDataFlag;
import java.util.Random;

/**
 * The patch of ground the snail lives on: the tiles within {@link #leash} of her home tile that the scene's
 * collision flags let an NPC stand on. She moves as the client moves an NPC: one tile in any of eight
 * directions, never into a blocked tile or through a wall, and a diagonal only when both straight halves are
 * open. The island's water blocks movement, so the flags alone keep her ashore. Adapted from SpewKit's
 * TecuHouse. Pure: the flags are the scene's live array, indexed [sceneX][sceneY].
 */
final class Island
{
	/** the client's mark on a tile no map data was loaded into, beside the scene border's 0xFFFFFF */
	static final int UNLOADED = 0x1000000;
	private static final int BLOCKED = CollisionDataFlag.BLOCK_MOVEMENT_FULL | UNLOADED;
	private static final int N = CollisionDataFlag.BLOCK_MOVEMENT_NORTH;
	private static final int E = CollisionDataFlag.BLOCK_MOVEMENT_EAST;
	private static final int S = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
	private static final int W = CollisionDataFlag.BLOCK_MOVEMENT_WEST;
	private static final int NE = CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST;
	private static final int NW = CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST;
	private static final int SE = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST;
	private static final int SW = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST;

	private static final int[][] STEPS = {
		{0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1},
	};

	final int plane;
	final int homeX;
	final int homeY;
	final int leash;
	private final int[][] flags;

	/**
	 * @param homeX the home tile in scene coordinates
	 * @param leash how far from home (in tiles, either axis) she may go
	 */
	Island(int plane, int[][] flags, int homeX, int homeY, int leash)
	{
		this.plane = plane;
		this.flags = flags;
		this.homeX = homeX;
		this.homeY = homeY;
		this.leash = leash;
	}

	/** Inside the scene, off its outermost ring (which the client walls off). */
	boolean inScene(int x, int y)
	{
		return flags != null && x > 0 && y > 0 && x < flags.length - 1 && y < flags[x].length - 1;
	}

	boolean onLeash(int x, int y)
	{
		return Math.abs(x - homeX) <= leash && Math.abs(y - homeY) <= leash;
	}

	/** A tile she can stand on. */
	boolean open(int x, int y)
	{
		return inScene(x, y) && onLeash(x, y) && (flags[x][y] & BLOCKED) == 0;
	}

	/** Whether one step from (x, y) by (dx, dy), each -1..1, is allowed. */
	boolean canStep(int x, int y, int dx, int dy)
	{
		int tx = x + dx;
		int ty = y + dy;
		if ((dx == 0 && dy == 0) || !open(x, y) || !open(tx, ty))
		{
			return false;
		}
		if (dx == 0 || dy == 0)
		{
			return (flags[tx][ty] & entering(dx, dy)) == 0;
		}
		// a diagonal: the corner of the tile entered, and both straight halves
		return (flags[tx][ty] & (entering(dx, 0) | entering(0, dy) | corner(dx, dy))) == 0
			&& open(tx, y) && (flags[tx][y] & entering(dx, 0)) == 0
			&& open(x, ty) && (flags[x][ty] & entering(0, dy)) == 0;
	}

	/** The wall flag on the tile entered that blocks a straight step in this direction. */
	private static int entering(int dx, int dy)
	{
		if (dx > 0)
		{
			return W;
		}
		if (dx < 0)
		{
			return E;
		}
		return dy > 0 ? S : N;
	}

	/** The corner flag on the tile entered that blocks a diagonal step in this direction. */
	private static int corner(int dx, int dy)
	{
		if (dx > 0)
		{
			return dy > 0 ? SW : NW;
		}
		return dy > 0 ? SE : NE;
	}

	/**
	 * Where she appears: home when it is open, else the open tile nearest home, else null (the scene has no
	 * ground for her, which would mean the home tile is wrong).
	 */
	int[] spawnTile()
	{
		if (open(homeX, homeY))
		{
			return new int[]{homeX, homeY};
		}
		for (int r = 1; r <= leash; r++)
		{
			for (int x = homeX - r; x <= homeX + r; x++)
			{
				for (int y = homeY - r; y <= homeY + r; y++)
				{
					if ((Math.abs(x - homeX) == r || Math.abs(y - homeY) == r) && open(x, y))
					{
						return new int[]{x, y};
					}
				}
			}
		}
		return null;
	}

	/**
	 * Somewhere to wander to: a random tile reachable from (x, y) within {@code maxSteps}, on the leash, other
	 * than where she stands. The route comes back step by step (the start left out); empty when there is
	 * nowhere to go.
	 */
	List<int[]> wander(int x, int y, int maxSteps, Random random)
	{
		if (!open(x, y) || maxSteps <= 0)
		{
			return Collections.emptyList();
		}
		int side = 2 * maxSteps + 1;
		int ox = x - maxSteps;
		int oy = y - maxSteps;
		int[] dist = new int[side * side];
		int[] from = new int[side * side];
		Arrays.fill(dist, -1);
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		dist[(x - ox) * side + (y - oy)] = 0;
		queue.add(new int[]{x, y});
		List<int[]> reached = new ArrayList<>();
		while (!queue.isEmpty())
		{
			int[] at = queue.poll();
			int here = (at[0] - ox) * side + (at[1] - oy);
			if (dist[here] >= maxSteps)
			{
				continue;
			}
			for (int[] step : STEPS)
			{
				int nx = at[0] + step[0];
				int ny = at[1] + step[1];
				int there = (nx - ox) * side + (ny - oy);
				if (dist[there] >= 0 || !canStep(at[0], at[1], step[0], step[1]))
				{
					continue;
				}
				dist[there] = dist[here] + 1;
				from[there] = here;
				int[] next = {nx, ny};
				queue.add(next);
				reached.add(next);
			}
		}
		if (reached.isEmpty())
		{
			return Collections.emptyList();
		}
		int[] goal = reached.get(random.nextInt(reached.size()));
		List<int[]> route = new ArrayList<>();
		int at = (goal[0] - ox) * side + (goal[1] - oy);
		int start = (x - ox) * side + (y - oy);
		while (at != start)
		{
			route.add(new int[]{ox + at / side, oy + at % side});
			at = from[at];
		}
		Collections.reverse(route);
		return route;
	}

	/** The client's orientation for facing along (dx, dy): 0 south, 512 west, 1024 north, 1536 east. */
	static int facing(int dx, int dy)
	{
		return (int) Math.round(Math.atan2(-dx, -dy) * 1024 / Math.PI) & 2047;
	}
}
