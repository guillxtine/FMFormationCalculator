package squad;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import logic.PositionsParser;
import logic.SquadTextParser;

public final class PlayerMapper
{
	private static final Attr[] ORDER =
		{
				Attr.CORNERS, Attr.CROSSING, Attr.DRIBBLING, Attr.FINISHING, Attr.FIRST_TOUCH, Attr.FREE_KICK_TAKING,
				Attr.HEADING, Attr.LONG_SHOTS, Attr.LONG_THROWS, Attr.MARKING, Attr.PASSING, Attr.PENALTY_TAKING,
				Attr.TACKLING, Attr.TECHNIQUE, Attr.AGGRESSION, Attr.ANTICIPATION, Attr.BRAVERY, Attr.COMPOSURE,
				Attr.CONCENTRATION, Attr.DECISIONS, Attr.DETERMINATION, Attr.FLAIR, Attr.LEADERSHIP, Attr.OFF_THE_BALL,
				Attr.POSITIONING, Attr.TEAMWORK, Attr.VISION, Attr.WORK_RATE, Attr.ACCELERATION, Attr.AGILITY,
				Attr.BALANCE, Attr.JUMPING_REACH, Attr.NATURAL_FITNESS, Attr.PACE, Attr.STAMINA, Attr.STRENGTH,
				Attr.AERIAL_REACH, Attr.COMMAND_OF_AREA, Attr.COMMUNICATION, Attr.ECCENTRICITY, Attr.HANDLING,
				Attr.KICKING, Attr.ONE_VS_ONES, Attr.PUNCHING_TENDENCY, Attr.REFLEXES, Attr.RUSHING_OUT_TENDENCY, Attr.THROWING
		};
	
	public static Player fromRow(List<String> r)
	{
		// safety checks
		if (r == null || r.isEmpty()) throw new IllegalArgumentException("Empty row");
		
		String name = SquadTextParser.normalizeName(safe(r, 0));
		
		// Map all 45 numeric attributes
		Map<Attr, Integer> attrs = new EnumMap<>(Attr.class);
		for (int i = 0; i < ORDER.length; i++)
			{
				int col = 1 + i; // attributes start at column 1
				attrs.put(ORDER[i], parseIntSafe(safe(r, col)));
			}
		
		// Strings after the 45 attributes
		int idxLeft = 1 + ORDER.length; // 48th column
		int idxRight = idxLeft + 1; // 49th column
		int idxPosition = idxRight + 1; // 50th column
		
		String positionRaw = safe(r, idxPosition);
		List<Positions> positions = PositionsParser.parse(positionRaw);
		
		String leftFoot = safe(r, idxLeft);
		String rightFoot = safe(r, idxRight);
		String position = safe(r, idxPosition);
		
		PlayerCalculator calc = new PlayerCalculator();
		
		// optional doubles, may not exist
		if (r.size() >= 52)
			{
				int idxLast5 = idxPosition + 1; // 51
				int idxAvg = idxLast5 + 1; // 52
				
				Double last5 = parseDoubleOrNull(safeOrNull(r, idxLast5));
				Double avg = parseDoubleOrNull(safeOrNull(r, idxAvg));
				
				return new Player(name, attrs, leftFoot, rightFoot, positions, last5, avg, calc);
			}
		else
			{
				return new Player(name, attrs, leftFoot, rightFoot, positions, null, null, calc);
			}
	}
	
	private static String safe(List<String> r, int i)
	{
		String v = safeOrNull(r, i);
		return (v == null) ? "" : v.trim();
	}
	
	private static String safeOrNull(List<String> r, int i)
	{
		return (i >= 0 && i < r.size()) ? r.get(i) : null;
	}
	
	private static int parseIntSafe(String s)
	{
		if (s == null || s.isBlank())
			{
				return 0;
			}
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}
	
	private static Double parseDoubleOrNull(String s)
	{
		if (s == null || s.isBlank())
			{
				return null;
			}
		try
		{
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
}
