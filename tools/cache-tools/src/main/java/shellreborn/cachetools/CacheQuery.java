package shellreborn.cachetools;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import net.runelite.cache.EntityOpsDefinition;
import net.runelite.cache.ItemManager;
import net.runelite.cache.NpcManager;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.fs.Store;

/**
 * Looks things up in a copy of the game cache:
 * <pre>
 *   --cache DIR newest (npcs|items|objects) N   the N highest ids, which is where a new quest's content lands
 *   --cache DIR find (npcs|items|objects) REGEX  definitions whose name matches (case-insensitive)
 *   --cache DIR show (npc|item|object) ID...     every field of those definitions that is not at its default
 * </pre>
 * Each command opens its own Store: RuneLite's Archive mutates on decompress.
 */
public final class CacheQuery
{
	public static void main(String[] args) throws Exception
	{
		File cache = null;
		List<String> rest = new ArrayList<>();
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--cache"))
			{
				cache = new File(args[++i]);
			}
			else
			{
				rest.add(args[i]);
			}
		}
		if (cache == null || rest.size() < 3)
		{
			System.err.println("usage: --cache DIR (newest|find|show) (npcs|items|objects) ARG...");
			System.exit(1);
		}
		String command = rest.get(0);
		String kind = rest.get(1).replaceAll("s$", "");
		try (Store store = new Store(cache))
		{
			store.load();
			Collection<?> defs = load(store, kind);
			switch (command)
			{
				case "newest" -> newest(defs, Integer.parseInt(rest.get(2)));
				case "find" -> find(defs, Pattern.compile(rest.get(2), Pattern.CASE_INSENSITIVE));
				case "show" ->
				{
					for (String id : rest.subList(2, rest.size()))
					{
						show(defs, Integer.parseInt(id));
					}
				}
				default -> throw new IllegalArgumentException(command);
			}
		}
	}

	private static Collection<?> load(Store store, String kind) throws Exception
	{
		switch (kind)
		{
			case "npc":
			{
				NpcManager m = new NpcManager(store);
				m.load();
				return m.getNpcs();
			}
			case "item":
			{
				ItemManager m = new ItemManager(store);
				m.load();
				return m.getItems();
			}
			case "object":
			{
				ObjectManager m = new ObjectManager(store);
				m.load();
				return m.getObjects();
			}
			default:
				throw new IllegalArgumentException(kind);
		}
	}

	private static void newest(Collection<?> defs, int n)
	{
		List<Object> sorted = new ArrayList<>(defs);
		sorted.sort(Comparator.comparingInt((ToIntFunction<Object>) CacheQuery::id).reversed());
		List<Object> top = sorted.subList(0, Math.min(n, sorted.size()));
		for (int i = top.size() - 1; i >= 0; i--)
		{
			System.out.println(summary(top.get(i)));
		}
	}

	private static void find(Collection<?> defs, Pattern pattern)
	{
		defs.stream()
			.filter(d -> name(d) != null && pattern.matcher(name(d)).find())
			.sorted(Comparator.comparingInt(CacheQuery::id))
			.forEach(d -> System.out.println(summary(d)));
	}

	private static void show(Collection<?> defs, int id)
	{
		Object def = defs.stream().filter(d -> id(d) == id).findFirst().orElse(null);
		if (def == null)
		{
			System.out.println(id + ": not found");
			return;
		}
		System.out.println("== " + def.getClass().getSimpleName() + " " + id);
		for (Field f : def.getClass().getFields())
		{
			if (Modifier.isStatic(f.getModifiers()))
			{
				continue;
			}
			try
			{
				String value = render(f.get(def));
				if (value != null)
				{
					System.out.println("  " + f.getName() + " = " + value);
				}
			}
			catch (IllegalAccessException e)
			{
				// public fields only
			}
		}
	}

	/** A field's value, or null when it is at an uninteresting default (null, -1, 0, false). */
	private static String render(Object v)
	{
		if (v == null)
		{
			return null;
		}
		if (v.getClass().isArray())
		{
			int n = Array.getLength(v);
			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < n; i++)
			{
				sb.append(i == 0 ? "" : ", ").append(String.valueOf(Array.get(v, i)));
			}
			return sb.append("]").toString();
		}
		if (v instanceof Number && (((Number) v).longValue() == -1 || ((Number) v).longValue() == 0))
		{
			return null;
		}
		if (v instanceof Boolean && !((Boolean) v))
		{
			return null;
		}
		if (v instanceof EntityOpsDefinition)
		{
			return ops((EntityOpsDefinition) v);
		}
		return String.valueOf(v);
	}

	/** An NPC's or object's right-click options by slot, conditional ones with the var they hang on. */
	private static String ops(EntityOpsDefinition d)
	{
		List<String> out = new ArrayList<>();
		List<EntityOpsDefinition.Op> ops = d.getOps();
		for (int i = 0; ops != null && i < ops.size(); i++)
		{
			if (ops.get(i) != null)
			{
				out.add(i + ":" + ops.get(i).text);
			}
		}
		List<List<EntityOpsDefinition.ConditionalOp>> cond = d.getConditionalOps();
		for (int i = 0; cond != null && i < cond.size(); i++)
		{
			for (EntityOpsDefinition.ConditionalOp c : cond.get(i) == null ? List.<EntityOpsDefinition.ConditionalOp>of() : cond.get(i))
			{
				out.add(i + ":" + c.text + "{varp " + c.varpID + " varbit " + c.varbitID + " in " + c.minValue + ".." + c.maxValue + "}");
			}
		}
		return out.isEmpty() ? null : out.toString();
	}

	private static String summary(Object d)
	{
		return id(d) + "\t" + name(d) + "\t" + extra(d);
	}

	private static String extra(Object d)
	{
		List<String> parts = new ArrayList<>();
		for (String field : new String[]{"models", "objectModels", "inventoryModel", "standingAnimation", "walkingAnimation",
			"animationID", "configs", "varbitID", "varbitId", "varpID", "varpIndex", "configChangeDest", "ops", "actions",
			"interfaceOptions", "options", "category"})
		{
			Object v = field(d, field);
			String r = v == null ? null : render(v);
			if (r != null)
			{
				parts.add(field + "=" + r);
			}
		}
		return String.join("  ", parts);
	}

	private static int id(Object d)
	{
		Object v = field(d, "id");
		if (v == null)
		{
			v = field(d, "objectID");
		}
		return v == null ? -1 : ((Number) v).intValue();
	}

	private static String name(Object d)
	{
		Object v = field(d, "name");
		return v == null ? null : v.toString();
	}

	private static Object field(Object d, String name)
	{
		try
		{
			return d.getClass().getField(name).get(d);
		}
		catch (ReflectiveOperationException e)
		{
			return null;
		}
	}
}
