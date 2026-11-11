package io.spewbert.shellreborn;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;
import net.runelite.api.CollisionDataFlag;
import org.junit.Test;

public class IslandTest
{
	private static final int FULL = CollisionDataFlag.BLOCK_MOVEMENT_FULL;

	/** A 20x20 scene of open ground, ringed by the border the client walls off. */
	private static int[][] meadow()
	{
		int[][] flags = new int[20][20];
		for (int i = 0; i < 20; i++)
		{
			flags[0][i] = flags[19][i] = flags[i][0] = flags[i][19] = 0xFFFFFF;
		}
		return flags;
	}

	@Test
	public void wanderStaysOnTheLeash()
	{
		Island island = new Island(0, meadow(), 10, 10, 3);
		Random random = new Random(1);
		int x = 10;
		int y = 10;
		for (int walk = 0; walk < 500; walk++)
		{
			for (int[] step : island.wander(x, y, 1 + random.nextInt(3), random))
			{
				assertTrue("one tile at a time", Math.abs(step[0] - x) <= 1 && Math.abs(step[1] - y) <= 1);
				x = step[0];
				y = step[1];
				assertTrue("left the leash at " + x + "," + y, island.onLeash(x, y));
			}
		}
	}

	@Test
	public void waterIsNotGround()
	{
		int[][] flags = meadow();
		// water everywhere but a plus shape round home
		for (int x = 1; x < 19; x++)
		{
			for (int y = 1; y < 19; y++)
			{
				flags[x][y] = FULL;
			}
		}
		flags[10][10] = flags[11][10] = flags[9][10] = flags[10][11] = flags[10][9] = 0;
		Island island = new Island(0, flags, 10, 10, 3);
		Random random = new Random(2);
		for (int i = 0; i < 200; i++)
		{
			for (int[] step : island.wander(10, 10, 3, random))
			{
				assertEquals("walked into water at " + step[0] + "," + step[1], 0, flags[step[0]][step[1]]);
			}
		}
		// a diagonal with water on one of its straight halves is not allowed, nor one into water
		assertFalse(island.canStep(11, 10, -1, 1));
		assertFalse(island.canStep(10, 10, 1, 1));
	}

	@Test
	public void wallsBlockStraightSteps()
	{
		int[][] flags = meadow();
		flags[11][10] |= CollisionDataFlag.BLOCK_MOVEMENT_WEST;
		Island island = new Island(0, flags, 10, 10, 3);
		assertFalse(island.canStep(10, 10, 1, 0));
		assertTrue(island.canStep(10, 10, -1, 0));
	}

	@Test
	public void spawnsAtHomeOrTheNearestOpenTile()
	{
		int[][] flags = meadow();
		assertArrayEquals(new int[]{10, 10}, new Island(0, flags, 10, 10, 3).spawnTile());
		flags[10][10] = FULL;
		int[] tile = new Island(0, flags, 10, 10, 3).spawnTile();
		assertTrue(Math.abs(tile[0] - 10) <= 1 && Math.abs(tile[1] - 10) <= 1);
	}

	@Test
	public void noGroundNoSnail()
	{
		int[][] flags = meadow();
		for (int x = 1; x < 19; x++)
		{
			for (int y = 1; y < 19; y++)
			{
				flags[x][y] = FULL;
			}
		}
		Island island = new Island(0, flags, 10, 10, 3);
		assertNull(island.spawnTile());
		List<int[]> route = island.wander(10, 10, 3, new Random());
		assertTrue(route.isEmpty());
	}
}
