package io.spewbert.shellreborn;

import com.google.inject.Provides;
import java.awt.Shape;
import java.util.Random;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.runelite.api.Animation;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Model;
import net.runelite.api.ModelData;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Shell Reborn: the juvenile snail from A Shell of a Friend, back on her island in the Haunted Woods once the
 * quest is done, wandering her patch by the shelter. She is a client-side object only: nothing here sends
 * anything to the server. Her name is the one picked in the quest (varbit 16855); hovering her shows it, and
 * Examine prints a random description of her doing well. She has no dialogue of her own.
 *
 * <p>IDs are raw because the quest is newer than RuneLite's gameval names (2026-11-11). All of them come from
 * the cache of that day: NPC 16882 {@code snailquest_snail}, varbit 16854 {@code snailquest} (the quest stage,
 * 24 when complete), varbit 16855 {@code snailquest_snail_name}, and her start coordinate from the quest's
 * dbrow ({@code 0_55_54_44_37}).
 */
@PluginDescriptor(
	name = "Shell Reborn",
	description = "Brings the juvenile snail from A Shell of a Friend back to her island",
	tags = {"snail", "quest", "shell", "snelm", "pet", "haunted woods"}
)
public class ShellRebornPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(ShellRebornPlugin.class);

	static final int SNAIL_NPC = 16882;
	static final int VARBIT_STAGE = 16854;
	static final int VARBIT_NAME = 16855;
	static final int QUEST_DONE = 24;
	/** where she stood when the player met her: the quest's start coordinate */
	static final WorldPoint HOME = new WorldPoint(3564, 3493, 0);
	/** how far from home, in tiles, she potters about */
	static final int LEASH = 3;
	/** her NPC's stand and walk animations (snail_ready, snail_walk); NPCComposition does not expose them */
	private static final int ANIM_STAND = 1276;
	private static final int ANIM_WALK = 1274;

	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ShellRebornConfig config;

	private final Random random = new Random();
	private final SnailLines lines = new SnailLines();

	private Snail snail;
	private Model model;
	private Animation stand;
	private Animation walk;
	/** this scene has been found to have no ground for her: logged, not retried until the next load */
	private boolean noGround;
	/** the state last logged, so the log shows each change once */
	private String loggedState;

	@Provides
	ShellRebornConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ShellRebornConfig.class);
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			despawn();
			model = null;
			stand = null;
			walk = null;
			loggedState = null;
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOADING:
			case LOGIN_SCREEN:
			case HOPPING:
				despawn();
				noGround = false;
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		reconcile();
	}

	/** Draws her when she should be there, and takes her away when not. */
	private void reconcile()
	{
		WorldView wv = client.getTopLevelWorldView();
		LocalPoint home = wv == null ? null : LocalPoint.fromWorld(wv, HOME);
		int stage = client.getVarbitValue(VARBIT_STAGE);
		boolean real = realSnailHere(wv);
		boolean show = config.showSnail() && client.getGameState() == GameState.LOGGED_IN && home != null
			&& (!config.onlyAfterQuest() || stage >= QUEST_DONE) && !real;
		logState(stage, home != null, real);
		if (!show)
		{
			despawn();
			return;
		}
		if (snail != null)
		{
			// the client can let go of a registered object (a scene rebuilt under it): put it back
			if (!client.isRuneLiteObjectRegistered(snail))
			{
				log.debug("[shell-reborn] the client dropped her; registering her again");
				client.registerRuneLiteObject(snail);
			}
			return;
		}
		if (!noGround)
		{
			spawn(wv, home);
		}
	}

	private void spawn(WorldView wv, LocalPoint home)
	{
		CollisionData[] maps = wv.getCollisionMaps();
		if (maps == null || maps[HOME.getPlane()] == null)
		{
			return;
		}
		Island island = new Island(HOME.getPlane(), maps[HOME.getPlane()].getFlags(), home.getSceneX(), home.getSceneY(), LEASH);
		int[] tile = island.spawnTile();
		if (tile == null)
		{
			noGround = true;
			log.debug("[shell-reborn] no open tile within {} of {} in this scene; not drawing her", LEASH, HOME);
			return;
		}
		if (!loadAssets())
		{
			return;
		}
		snail = new Snail(client, island, tile, model, stand, walk, random, wv.getId());
		client.registerRuneLiteObject(snail);
		log.debug("[shell-reborn] drawn at scene {},{} (home {},{}), name {}", tile[0], tile[1], home.getSceneX(), home.getSceneY(), name());
	}

	private void despawn()
	{
		if (snail != null)
		{
			client.removeRuneLiteObject(snail);
			snail = null;
		}
	}

	/** The real snail is in the scene (the quest is under way): she is drawn by the game, not by us. */
	private static boolean realSnailHere(WorldView wv)
	{
		if (wv == null)
		{
			return false;
		}
		for (NPC npc : wv.npcs())
		{
			if (npc != null && npc.getId() == SNAIL_NPC)
			{
				return true;
			}
		}
		return false;
	}

	/** Her model as the game builds her NPC: its models merged, recoloured, resized and lit as an actor. */
	private boolean loadAssets()
	{
		if (stand == null)
		{
			stand = client.loadAnimation(ANIM_STAND);
			walk = client.loadAnimation(ANIM_WALK);
		}
		if (model == null)
		{
			model = build();
		}
		return model != null && stand != null && walk != null;
	}

	private Model build()
	{
		NPCComposition def = client.getNpcDefinition(SNAIL_NPC);
		if (def == null || def.getModels() == null || def.getModels().length == 0)
		{
			log.debug("[shell-reborn] NPC {} has no models in this cache", SNAIL_NPC);
			return null;
		}
		int[] ids = def.getModels();
		ModelData[] parts = new ModelData[ids.length];
		for (int i = 0; i < ids.length; i++)
		{
			parts[i] = client.loadModelData(ids[i]);
			if (parts[i] == null)
			{
				return null;
			}
		}
		// copied first: recolouring and scaling write arrays shared with the cache's copy until then
		ModelData data = (parts.length == 1 ? parts[0] : client.mergeModels(parts)).cloneColors().cloneVertices();
		short[] find = def.getColorToReplace();
		short[] replace = def.getColorToReplaceWith();
		if (find != null && replace != null)
		{
			for (int i = 0; i < Math.min(find.length, replace.length); i++)
			{
				data.recolor(find[i], replace[i]);
			}
		}
		int width = def.getWidthScale();
		int height = def.getHeightScale();
		if (width != 128 || height != 128)
		{
			data.scale(width, height, width);
		}
		Model lit = data.light(64, 850, -30, -50, -30);
		if (lit != null)
		{
			lit.calculateBoundsCylinder();
		}
		return lit;
	}

	// ---- hovering and examining

	/**
	 * While the mouse is over her, the menu gets "Examine [name]" as its top entry as the client builds it (added
	 * as "Walk here" is, so anything in front of her still goes above), so hovering shows her name and a left
	 * click describes her. A RuneLite menu entry: it runs here and never reaches the server.
	 */
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (snail == null || event.getMenuEntry().getType() != MenuAction.WALK || !hovered())
		{
			return;
		}
		client.getMenu().createMenuEntry(-1)
			.setOption("Examine")
			.setTarget("<col=ffff00>" + name() + "</col>")
			.setType(MenuAction.RUNELITE)
			.onClick(e -> examine());
	}

	private boolean hovered()
	{
		Point mouse = client.getMouseCanvasPosition();
		if (mouse == null || mouse.getX() < 0 || mouse.getY() < 0)
		{
			return false;
		}
		Shape box = Perspective.getClickbox(client, client.getTopLevelWorldView(), snail.baseModel(), snail.getOrientation(),
			snail.getX(), snail.getY(), snail.getZ());
		return box != null && box.contains(mouse.getX(), mouse.getY());
	}

	private void examine()
	{
		String text = lines.next(random, name());
		if (text != null)
		{
			client.addChatMessage(ChatMessageType.NPC_EXAMINE, "", text, null);
		}
	}

	private String name()
	{
		return SnailNames.resolve(config.nameOverride(), client.getVarbitValue(VARBIT_NAME));
	}

	/**
	 * Logs what drawing her depends on whenever it changes: the quest stage, the raw name varbit and the name
	 * it maps to (the mapping is a guess until an account with a known name confirms it), whether her home is
	 * in the loaded scene, and whether the real snail is.
	 */
	private void logState(int stage, boolean homeInScene, boolean real)
	{
		int nameValue = client.getVarbitValue(VARBIT_NAME);
		String state = "stage " + stage + ", name varbit " + nameValue + " (" + SnailNames.fromVarbit(nameValue) + ")"
			+ ", override '" + config.nameOverride() + "'"
			+ ", home in scene " + homeInScene + ", real snail " + real + ", drawn " + (snail != null);
		if (!state.equals(loggedState))
		{
			loggedState = state;
			log.debug("[shell-reborn] {}", state);
		}
	}
}
