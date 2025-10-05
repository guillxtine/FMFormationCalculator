package logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import squad.Player;
import squad.PosAbilityLinker;
import squad.Positions;
import squad.WeightedPosAbilityLinker;

public class FormationRecommender
{
	// Public API
	
	public static List<Result> chooseTop(List<Player> players, String predictedFinish, boolean weighted, int k)
	{
		PredictedFinish pf = PredictedFinish.fromLabel(predictedFinish);
	    List<Formation> pool = forFinish(pf);

	    // Deduplicate by formation name (preserves order)
	    Map<String, Formation> uniq = new LinkedHashMap<>();
	    for (Formation f : pool)
	    	{
	    		uniq.putIfAbsent(f.name(), f);
	    	}
	    pool = new ArrayList<>(uniq.values());

	    Map<Player, Map<Positions, Double>> abilityMap = buildAbilityMap(players, weighted);

	    List<Result> scored = new ArrayList<>(pool.size());
	    for (Formation f : pool)
	    	{
	    		scored.add(scoreFormation(f, players, abilityMap, pf));
	    	}

	    scored.sort(
	        Comparator.comparingDouble(Result::totalScore).reversed()
	                  .thenComparing((Result r) -> isFourBack(r.formation()) ? 0 : 1)
	                  .thenComparing(r -> r.formation().name())
	    );

	    return scored.subList(0, Math.min(k, scored.size()));
	}
	
	public static Result chooseBest(List<Player> players, String predictedFinish, boolean weighted)
	{
		return chooseTop(players, predictedFinish, weighted, 1).get(0);
	}
	
	// Scoring / Matching
	private static Result scoreFormation(Formation f, List<Player> players, Map<Player, Map<Positions, Double>> abilityMap, PredictedFinish pf)
	{
		// Greedy assignment: fill "scarce / specific" slots first
		List<RoleSlot> slots = new ArrayList<>(f.slots());
		slots.sort(Comparator.comparingInt(FormationRecommender::slotSpecificity).reversed());
		
		Map<RoleSlot, Player> pick = new LinkedHashMap<>();
		Set<Player> used = new HashSet<>();
		double fitScore = 0.0;
		
		for (RoleSlot slot : slots)
			{
				Player bestP = null;
				double bestV = -1;
				
				for (Player p : players)
					{
						if (used.contains(p))
							{
								continue;
							}
						double v = bestAbilityForSlot(abilityMap.getOrDefault(p, Collections.emptyMap()), slot);
						if (v > bestV)
							{
								bestV = v;
								bestP = p;
							}
					}
				
				// allow a tiny value for out of position
				if (bestP != null && bestV > 0)
					{
						pick.put(slot, bestP);
						used.add(bestP);
						fitScore += bestV;
					}
				else
					{
						// no good candidate -> small penalty
						fitScore -= 25;
					}
			}
		
		// Style score: prefer formations whose attacking index matches the finish expectation
		double styleTarget = pf.styleTarget();
		double styleDelta = Math.abs(f.attackingIndex() - styleTarget);
		double styleScore = 100 - styleDelta * 25; // 0-100 ish, MULTIPLIER IS TUNABLE
		
		// Star bonus: keep the top 3 players in roles where they excel
		double starsBonus = computeStarsBonus(players, abilityMap, pick);
		
		// slight preference for a back four
		double fourBackBonus = 1.0;
		
		switch (pf)
		{
			case RELEGATION:
				fourBackBonus = 1.0;
				break;
			case BOTTOM_HALF:
				fourBackBonus = 1.0;
				break;
			case MIDTABLE:
				fourBackBonus = 1.01;
				break;
			case SECONDARY_EUROPE:
				fourBackBonus = 1.015;
				break;
			case CHAMPIONS:
				fourBackBonus = 1.02;
				break;
			default:
				break;
		}
		
		double shapeBonus = isFourBack(f) ? fourBackBonus : 1.0;
		
		double total = (fitScore + styleScore + starsBonus) * shapeBonus;
		
		return new Result(f, pick, total, styleScore, fitScore, starsBonus);
	}
	
	private static int slotSpecificity(RoleSlot s)
	{
		int opts = s.options().length;
		boolean isGK = Arrays.asList(s.options()).contains(Positions.GK);
		boolean isWide = containsAny(s.options(), Positions.WBL, Positions.WBR, Positions.AML, Positions.AMR,
				Positions.ML, Positions.MR, Positions.DL, Positions.DR);
		boolean isCB = containsAny(s.options(), Positions.DC);
		
		// GK highest, then very specific (1 option), then wide, then CB, then others (TUNEABLE)
		if (isGK) return 1000;
		if (opts == 1) return 900;
		if (isWide) return 700;
		if (isCB) return 600;
		return 500 - opts; // fewer options = earlier
	}
	
	private static boolean containsAny(Positions[] arr, Positions... ps)
	{
		for (Positions a : arr)
			{
				for (Positions p : ps)
					{
						if (a == p)
							{
								return true;
							}
					}
			}
		return false;
	}
	
	private static double bestAbilityForSlot(Map<Positions, Double> map, RoleSlot s)
	{
		double best = 0.0;
		for (Positions pos : s.options())
			{
				best = Math.max(best,  map.getOrDefault(pos, 0.0));
			}
		return best;
	}
	
	private static double computeStarsBonus(List<Player> players, Map<Player, Map<Positions, Double>> abilityMap, Map<RoleSlot, Player> pick)
	{
		// top 3 players by max ability anywhere
		List<Player> stars = players.stream()
				.sorted(Comparator.<Player>comparingDouble(p -> maxAbility(abilityMap.getOrDefault(p, Map.of()))).reversed())
				.limit(3).toList();
		
		double bonus = 0.0;
		for (Player star : stars)
			{
				double starMax = maxAbility(abilityMap.getOrDefault(star, Map.of()));
				
				// check to assign star to (one of) their best roles (within 95% of the max)?
				for (var e : pick.entrySet())
					{
						if (e.getValue() != star)
							{
								continue;
							}
						double v = bestAbilityForSlot(abilityMap.get(star), e.getKey());
						if (v >= 0.95 * starMax)
							{
								bonus += 25.0;
							}
						else if (v >= 0.85 * starMax)
							{
								bonus += 10.0;
							}
						break;
					}
			}
		return bonus;
	}
	
	private static boolean isFourBack(Formation f)
	{
		// 1) try by name
		String n = f.name();
		int dash = n.indexOf('-');
		if (dash > 0)
			{
				try
				{
					int defenders = Integer.parseInt(n.substring(0, dash));
					if (defenders == 4)
						{
							return true;
						}
				}
				catch (NumberFormatException ignored)
				{
					// Fall through to fallback
				}
			}
		
		// 2) fallback by slots (only 2 DC + a DL + a DR)
		int dc = 0;
		boolean hasDL = false;
		boolean hasDR = false;
		for (RoleSlot s : f.slots())
			{
				var opts = Arrays.asList(s.options());
				if (opts.contains(Positions.DC)) { dc++; }
				if (opts.contains(Positions.DL)) { hasDL = true; }
				if (opts.contains(Positions.DR)) { hasDR = true; }
			}
		return dc == 2 && hasDL && hasDR;
	}
	
	private static double maxAbility(Map<Positions, Double> m)
	{
		double best = 0;
		for (double v : m.values())
			{
				best = Math.max(best, v);
			}
		return best;
	}
	
	private static Map<Player, Map<Positions, Double>> buildAbilityMap(List<Player> players, boolean weighted)
	{
		Map<Player, Map<Positions, Double>> out = new HashMap<>();
		
		for (Player p : players)
			{
				Map<Positions, Double> m = new EnumMap<>(Positions.class);
				
				if (weighted)
					{
						WeightedPosAbilityLinker wl = new WeightedPosAbilityLinker(p);
						// use linker's map (recomputed for this player)
						Map<Positions, Double> byPos = wl.LinkPosAbility(p);
						m.putAll(byPos);
					}
				else
					{
						PosAbilityLinker pl = new PosAbilityLinker(p);
						Map<Positions, Integer> byPos = pl.LinkPosAbility(p);
						// normalize to Double so callers can treat both paths uniformly
						for (var e : byPos.entrySet())
							{
								m.put(e.getKey(), e.getValue().doubleValue());
							}
					}
				
				out.put(p, m);
			}
		
		return out;
	}
	
	// Models
	
	public enum PredictedFinish
	{
		RELEGATION(-2), BOTTOM_HALF(-1), MIDTABLE(0), SECONDARY_EUROPE(+1), CHAMPIONS(+2);
		
		private final int styleTarget;
		PredictedFinish(int t)
		{
			this.styleTarget = t;
		}
		public int styleTarget()
		{
			return styleTarget;
		}
		
		public static PredictedFinish fromLabel(String s)
		{
			if (s == null)
				{
					return MIDTABLE;
				}
			String x = s.trim().toLowerCase(Locale.ROOT);
			if (x.contains("releg")) return RELEGATION;
			if (x.contains("bott")) return BOTTOM_HALF;
			if (x.contains("mid")) return MIDTABLE;
			if (x.contains("euro")) return SECONDARY_EUROPE;
			if (x.contains("champ")) return CHAMPIONS;
			return MIDTABLE;
		}
	}
	
	public record Result(Formation formation,
			Map<RoleSlot, Player> assignments,
			double totalScore,
			double styleScore,
			double fitScore,
			double starsBonus) {}
	
	public static final class Formation
	{
		private final String name;
		private final List<RoleSlot> slots;
		private final int attackingIndex;
		
		public Formation(String name, int attackingIndex, List<RoleSlot> slots)
		{
			this.name = name;
			this.slots = List.copyOf(slots);
			this.attackingIndex = attackingIndex;
		}
		
		public String name()
		{
			return name;
		}
		
		public List<RoleSlot> slots()
		{
			return slots;
		}
		
		public int attackingIndex()
		{
			return attackingIndex;
		}
	}
	
	public static final class RoleSlot
	{
		private final String label;
		private final Positions[] options;
		
		public RoleSlot(String label, Positions... options)
		{
			this.label = label;
			this.options = options;
		}
		
		public String label()
		{
			return label;
		}
		
		public Positions[] options()
		{
			return options;
		}
	}
	
	// Formations Library
	
	// Helpers to keep defs tidy
	private static RoleSlot GK(String n) { return new RoleSlot(n, Positions.GK); }
	private static RoleSlot DC(String n) { return new RoleSlot(n, Positions.DC); }
	private static RoleSlot DL(String n) { return new RoleSlot(n, Positions.DL); }
	private static RoleSlot DR(String n) { return new RoleSlot(n, Positions.DR); }
	private static RoleSlot WBL(String n) { return new RoleSlot(n, Positions.WBL); }
	private static RoleSlot WBR(String n) { return new RoleSlot(n, Positions.WBR); }
	private static RoleSlot DM(String n) { return new RoleSlot(n, Positions.DM); }
	private static RoleSlot MC(String n) { return new RoleSlot(n, Positions.MC); }
	private static RoleSlot ML(String n) { return new RoleSlot(n, Positions.ML); }
	private static RoleSlot MR(String n) { return new RoleSlot(n, Positions.MR); }
	private static RoleSlot AMC(String n) { return new RoleSlot(n, Positions.AMC); }
	private static RoleSlot AML(String n) { return new RoleSlot(n, Positions.AML); }
	private static RoleSlot AMR(String n) { return new RoleSlot(n, Positions.AMR); }
	private static RoleSlot ST(String n) { return new RoleSlot(n, Positions.ST); }
	
	static List<Formation> forFinish(PredictedFinish pf)
	{
		// defensive pool for relegation/bottom half, balanced for mid-table, attacking for europe and champions
		List<Formation> defensive = List.of(
					f442(),
					f442_2dm(),
					f442_diamond_wide(),
					f442_diamond_narrow(),
					f4222_dm(),
					f433_dm(),
					f433_2dm(),
					f433_3dm(),
					f4231_narrow(),
					f4231_dm_am(),
					f4411(),
					f4411_2dm(),
					f4321_narrow_2dm(),
					f4321_narrow_3dm(),
					f4321_dm(),
					f4141(),
					f451(),
					f451_2dm(),
					f451_3dm(),
					f4132_narrow(),
					f4132(),
					f352(),
					f523_wb(),
					f523_dm_wb(),
					f5221_dm_am_wb(),
					f5221_dm_wb(),
					f5212_dm_am_wb(),
					f532_dm_wb(),
					f532_2dm_wb(),
					f532_wb(),
					f541_wb(),
					f541_2dm_wb(),
					f523(),
					f523_dm(),
					f5221_dm_am(),
					f5221_dm(),
					f5221(),
					f5212_dm_am(),
					f5212(),
					f532_dm(),
					f532_2dm(),
					f532(),
					f541(),
					f541_2dm()
				);
		
		List<Formation> balanced = List.of(
					f442(),
					f442_2dm(),
					f442_diamond_wide(),
					f442_diamond_narrow(),
					f424_dm(),
					f4222_dm_am(),
					f4222_dm(),
					f433_dm(),
					f433_2dm(),
					f433_wide(),
					f4231_narrow(),
					f4231_dm_am(),
					f4231(),
					f4411(),
					f4411_2dm(),
					f4321_narrow(),
					f4321_narrow_dm(),
					f4321_narrow_2dm(),
					f4321_dm(),
					f4321(),
					f4141(),
					f451(),
					f451_2dm(),
					f4132_narrow(),
					f4132(),
					f3142(),
					f352(),
					f352_2dm(),
					f3412(),
					f3412_2dm(),
					f343_wide(),
					f343_wide_2dm(),
					f523_wb(),
					f523_dm_wb(),
					f5221_dm_am_wb(),
					f5221_wb(),
					f5212_dm_am_wb(),
					f5212_wb(),
					f532_dm_wb(),
					f532_wb(),
					f541_wb(),
					f523(),
					f5221_dm_am(),
					f5221(),
					f5212(),
					f532(),
					f541()
				);
		
		List<Formation> attacking = List.of(
					f442(),
					f442_diamond_wide(),
					f442_diamond_narrow(),
					f424_dm(),
					f424(),
					f4222_dm_am(),
					f4222(),
					f433_dm(),
					f433_wide(),
					f433_narrow(),
					f433_dm_narrow(),
					f4213_dm_am(),
					f4231_narrow(),
					f4231_dm_am(),
					f4231(),
					f4411(),
					f4321_narrow(),
					f451(),
					f4132_narrow(),
					f4132(),
					f3142(),
					f352(),
					f3412(),
					f3412_2dm(),
					f343_wide(),
					f343_wide_2dm(),
					f343_2dm(),
					f343(),
					f523_wb(),
					f5221_dm_am_wb(),
					f5221_wb(),
					f5212_dm_am_wb(),
					f5212_wb(),
					f532_wb()
				);
		
		return switch (pf)
				{
					case RELEGATION -> defensive;
					case BOTTOM_HALF -> concat(defensive, balanced);
					case MIDTABLE -> balanced;
					case SECONDARY_EUROPE -> concat(balanced, attacking);
					case CHAMPIONS -> attacking;
				};
	}
	
	private static List<Formation> concat(List<Formation> a, List<Formation> b)
	{
		List<Formation> out = new ArrayList<>(a);
		for (Formation f : b)
			{
				if (!out.contains(f))
					{
						out.add(f);
					}
			}
		return out;
	}
	
	private static Formation f442()
	{
		return new Formation("4-4-2", 0, List.of(
					ST("STCL"), ST("STCR"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f442_2dm()
	{
		return new Formation("4-4-2 2DM", -1, List.of(
					ST("STCL"), ST("STCR"),
					ML("ML"), MR("MR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f442_diamond_wide()
	{
		return new Formation("4-4-2 Diamond Wide", 0, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					ML("ML"), MR("MR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f442_diamond_narrow()
	{
		return new Formation("4-4-2 Diamond Narrow", 0, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					MC("MCL"), MC("MCR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f424_dm()
	{
		return new Formation("4-2-4 DM", +1, List.of(
					ST("STCL"), ST("STCR"),
					AML("AML"), AMR("AMR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f424()
	{
		return new Formation("4-2-4", +2, List.of(
					ST("STCL"), ST("STCR"),
					AML("AML"), AMR("AMR"),
					MC("MCL"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4222_dm_am()
	{
		return new Formation("4-2-2-2 DM AM", +1, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMCL"), AMC("AMCR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4222_dm()
	{
		return new Formation("4-2-2-2 DM", -1, List.of(
					ST("STCL"), ST("STCR"),
					MC("MCL"), MC("MCR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4222()
	{
		return new Formation("4-2-2-2", +2, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMCL"), AMC("AMCR"),
					MC("MCL"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f433_dm()
	{
		return new Formation("4-3-3 DM", 0, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					MC("MCL"), MC("MCR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f433_2dm()
	{
		return new Formation("4-3-3 2DM", -1, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					MC("MC"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f433_3dm()
	{
		return new Formation("4-3-3 3DM", -2, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					DM("DMCL"), DM("DM"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f433_wide()
	{
		return new Formation("4-3-3 Wide", +1, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					MC("MCL"), MC("MC"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f433_narrow()
	{
		return new Formation("4-3-3 Narrow", +2, List.of(
					ST("STCL"), ST("STC"), ST("STCR"),
					MC("MCL"), MC("MC"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f433_dm_narrow()
	{
		return new Formation("4-3-3 DM Narrow", +2, List.of(
					ST("STCL"), ST("STC"), ST("STCR"),
					MC("MCL"), MC("MCR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4213_dm_am()
	{
		return new Formation("4-2-1-3 DM AM", +2, List.of(
					ST("STCL"), ST("STC"), ST("STCR"),
					AMC("AMC"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4231_narrow()
	{
		return new Formation("4-2-3-1 Narrow", 0, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMC"), AMC("AMCR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4231_dm_am()
	{
		return new Formation("4-2-3-1 DM AM", 0, List.of(
					ST("STC"),
					AML("AML"), AMC("AMC"), AMR("AMR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4231()
	{
		return new Formation("4-2-3-1", +1, List.of(
					ST("STC"),
					AML("AML"), AMC("AMC"), AMR("AMR"),
					MC("MCL"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4411()
	{
		return new Formation("4-4-1-1", 0, List.of(
					ST("STC"),
					AMC("AMC"), 
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4411_2dm()
	{
		return new Formation("4-4-1-1 2DM", -1, List.of(
					ST("STC"),
					AMC("AMC"),
					ML("ML"), MR("MR"),
					DM("DMCL"), DM("DMCR"), 
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4321_narrow()
	{
		return new Formation("4-3-2-1 Narrow", +1, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					MC("MCL"), MC("MC"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4321_narrow_dm()
	{
		return new Formation("4-3-2-1 Narrow DM", 0, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					MC("MCL"), MC("MCR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4321_narrow_2dm()
	{
		return new Formation("4-3-2-1 Narrow 2DM", -1, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					MC("MC"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4321_narrow_3dm()
	{
		return new Formation("4-3-2-1 Narrow 3DM", -2, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					DM("DMCL"), DM("DM"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4321_dm()
	{
		return new Formation("4-3-2-1 DM", -1, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					ML("ML"), MR("MR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4321()
	{
		return new Formation("4-3-2-1", 0, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					ML("ML"), MC("MC"), MR("MR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4141()
	{
		return new Formation("4-1-4-1", -1, List.of(
					ST("STC"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f451()
	{
		return new Formation("4-5-1", 0, List.of(
					ST("STC"),
					ML("ML"), MC("MCL"), MC("MC"), MC("MCR"), MR("MR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f451_2dm()
	{
		return new Formation("4-5-1 2DM", -1, List.of(
					ST("STC"),
					ML("ML"), MC("MC"), MR("MR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f451_3dm()
	{
		return new Formation("4-5-1 3DM", -2, List.of(
					ST("STC"),
					ML("ML"), MR("MR"),
					DM("DMCL"), DM("DM"), DM("DMCR"), 
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4132_narrow()
	{
		return new Formation("4-1-3-2 Narrow", 0, List.of(
					ST("STCL"), ST("STCR"),
					MC("MCL"), MC("MC"), MC("MCR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f4132()
	{
		return new Formation("4-1-3-2", 0, List.of(
					ST("STCL"), ST("STCR"),
					ML("ML"), MC("MC"), MR("MR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f3142()
	{
		return new Formation("3-1-4-2", +1, List.of(
					ST("STCL"), ST("STCR"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DM("DM"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f352()
	{
		return new Formation("3-5-2", 0, List.of(
					ST("STCL"), ST("STCR"),
					ML("ML"), MC("MCL"), MC("MC"), MC("MCR"), MR("MR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f352_2dm()
	{
		return new Formation("3-5-2 2DM", 0, List.of(
					ST("STCL"), ST("STCR"),
					ML("ML"), MC("MC"), MR("MR"),
					DM("DMCL"), DM("DMCR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f3412()
	{
		return new Formation("3-4-1-2", +1, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f3412_2dm()
	{
		return new Formation("3-4-1-2 2DM", +1, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					ML("ML"), MR("MR"),
					DM("DMCL"), DM("DMCR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f343_wide()
	{
		return new Formation("3-4-3 Wide", +1, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f343_wide_2dm()
	{
		return new Formation("3-4-3 2DM Wide", +1, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					ML("ML"), MR("MR"),
					DM("DMCL"), DM("DMCR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f343_2dm()
	{
		return new Formation("3-4-3 2DM", +2, List.of(
					ST("STCL"), ST("STC"), ST("STCR"),
					ML("ML"), MR("MR"),
					DM("DMCL"), DM("DMCR"), 
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f343()
	{
		return new Formation("3-4-3", +2, List.of(
					ST("STCL"), ST("STC"), ST("STCR"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f523_wb()
	{
		return new Formation("5-2-3 WB", 0, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					MC("MCL"), MC("MCR"),
					WBL("WBL"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f523_dm_wb()
	{
		return new Formation("5-2-3 DM WB", -1, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					WBL("WBL"), DM("DMCL"), DM("DMCR"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f5221_dm_am_wb()
	{
		return new Formation("5-2-2-1 DM AM WB", 0, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					WBL("WBL"), DM("DMCL"), DM("DMCR"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f5221_dm_wb()
	{
		return new Formation("5-2-2-1 DM WB", -2, List.of(
					ST("STC"),
					MC("MCL"), MC("MCR"),
					WBL("WBL"), DM("DMCL"), DM("DMCR"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f5221_wb()
	{
		return new Formation("5-2-2-1 WB", +1, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					MC("MCL"), MC("MCR"),
					WBL("WBL"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f5212_dm_am_wb()
	{
		return new Formation("5-2-1-2 DM AM WB", 0, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					WBL("WBL"), DM("DMCL"), DM("DMCR"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f5212_wb()
	{
		return new Formation("5-2-1-2 WB", +1, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					MC("MCL"), MC("MCR"),
					WBL("WBL"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f532_dm_wb()
	{
		return new Formation("5-3-2 DM WB", -1, List.of(
					ST("STCL"), ST("STCR"),
					MC("MCL"), MC("MCR"),
					WBL("WBL"), DM("DM"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f532_2dm_wb()
	{
		return new Formation("5-3-2 2DM WB", -2, List.of(
					ST("STCL"), ST("STCR"),
					MC("MC"),
					WBL("WBL"), DM("DMCL"), DM("DMCR"),WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f532_wb()
	{
		return new Formation("5-3-2 WB", 0, List.of(
					ST("STCL"), ST("STCR"),
					MC("MCL"), MC("MC"), MC("MCR"),
					WBL("WBL"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f541_wb()
	{
		return new Formation("5-4-1 WB", -1, List.of(
					ST("STC"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					WBL("WBL"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f541_2dm_wb()
	{
		return new Formation("5-4-1 2DM WB", -2, List.of(
					ST("STC"),
					ML("ML"), MR("MR"),
					WBL("WBL"), DM("DMCL"), DM("DMCR"), WBR("WBR"),
					DC("DCL"), DC("DC"), DC("DCR"),
					GK("GK")
				));
	}
	
	private static Formation f523()
	{
		return new Formation("5-2-3", -1, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					MC("MCL"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f523_dm()
	{
		return new Formation("5-2-3 DM", -2, List.of(
					ST("STC"),
					AML("AML"), AMR("AMR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f5221_dm_am()
	{
		return new Formation("5-2-2-1 DM AM", -1, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f5221_dm()
	{
		return new Formation("5-2-2-1 DM", -2, List.of(
					ST("STC"),
					MC("MCL"), MC("MCR"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f5221()
	{
		return new Formation("5-2-2-1", -1, List.of(
					ST("STC"),
					AMC("AMCL"), AMC("AMCR"),
					MC("MCL"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f5212_dm_am()
	{
		return new Formation("5-2-1-2 DM AM", -2, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f5212()
	{
		return new Formation("5-2-1-2", -1, List.of(
					ST("STCL"), ST("STCR"),
					AMC("AMC"),
					MC("MCL"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f532_dm()
	{
		return new Formation("5-3-2 DM", -2, List.of(
					ST("STCL"), ST("STCR"),
					MC("MCL"), MC("MCR"),
					DM("DM"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f532_2dm()
	{
		return new Formation("5-3-2 2DM", -2, List.of(
					ST("STCL"), ST("STCR"),
					MC("MC"),
					DM("DMCL"), DM("DMCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f532()
	{
		return new Formation("5-3-2", -1, List.of(
					ST("STCL"), ST("STCR"),
					MC("MCL"), MC("MC"), MC("MCR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f541()
	{
		return new Formation("5-4-1", -1, List.of(
					ST("STC"),
					ML("ML"), MC("MCL"), MC("MCR"), MR("MR"),
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
	
	private static Formation f541_2dm()
	{
		return new Formation("5-4-1 2DM", -2, List.of(
					ST("STC"),
					ML("ML"), MR("MR"),
					DM("DMCL"), DM("DMCR"), 
					DL("DL"), DC("DCL"), DC("DC"), DC("DCR"), DR("DR"),
					GK("GK")
				));
	}
}
	

