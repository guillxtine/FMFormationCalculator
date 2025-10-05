package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SquadTextParser
{
	private static final String PARTICLES =
		    "(?:de|del|della|der|den|van|von|da|di|dos|das|du|le|la|el|al|ter|ten|te)";
	
	private static final String SHORT_PREV = "(?:al|el|de|di|da|du|le|la)";
	
	private static final Pattern P_GLUED_TO_NEXT_CAP =
		    Pattern.compile("(?u)\\b(?=\\p{Ll})(?:" + PARTICLES + ")(?=\\p{Lu})");
	
	private static final Pattern P_GLUED_TO_PREV_CHAINHEAD_SPACED =
		    Pattern.compile("(?u)(?<=\\p{Ll}{2})(?=(?:" + PARTICLES + ")\\b(?=\\s+(?:" + PARTICLES + ")\\b))");
	
	private static final Pattern P_GLUED_TO_PREV_CHAINHEAD_GLUE =
		    Pattern.compile("(?u)(?<=\\p{Ll}{2})(?=(?:(?:" + PARTICLES + "){2,})\\b)");
	
	private static final Pattern P_GLUED_TO_PREV_CHAIN =
		    Pattern.compile(
		        "(?u)(?<=\\p{Ll}{2})"                                  // two lowercase before
		      + "(?=(?=\\p{Ll})"                                       // particle starts lowercase
		      + "(?:(?!(?:" + SHORT_PREV + ")\\b)(?:" + PARTICLES + "))\\b" // but NOT a short 2-letter particle
		      + "(?=\\s*(?:\\p{Lu}|(?:" + PARTICLES + ")\\b)))"        // next = Uppercase word OR another particle
		    );
	
	private static final Pattern P_ACRONYM_SPLIT =
		    Pattern.compile("(?<=[\\p{Lu}])(?=[\\p{Lu}][\\p{Ll}])");
	
	public static List<List<String>> parsePipeTable(String text)
	{
		// Normalize the line endings and trim the trailing/leading whitespace per line
		String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");
		
		List<List<String>> table = new ArrayList<>();
		for (String line : lines)
			{
				String trimmed = line.trim();
				if (trimmed.isEmpty())
					{
						continue; // skip blank lines
					}
				if (trimmed.matches("^-+$|^\\|?-+\\|?-+$"))
					{
						continue; // skip "----" dividers
					}
				
				String[] rawCells = trimmed.split("\\|", -1);
				
				List<String> row = new ArrayList<>(rawCells.length);
				for (String c : rawCells)
					{
						row.add(c.trim());
					}
				
				while (!row.isEmpty() && row.get(0).isEmpty())
					{
						row.remove(0);
					}
				
				while (!row.isEmpty() && row.get(row.size() - 1).isEmpty())
					{
						row.remove(row.size() - 1);
					}
				
				if (!row.isEmpty())
					{
						table.add(row);
					}
			}

			for (List<String> r : table.subList(1, table.size()))
				{
					for (int i = 0; i < r.size(); i++)
						{
							r.set(i, splitCamelCase(r.get(i)));
						}
				}
		return table;
	}
	
	private static String splitCamelCase(String s)
	{
		return s.replaceAll("(?<=[\\p{Ll}])(?=[\\p{Lu}])|(?<=[\\p{Lu}])(?=[\\p{Lu}][\\p{Ll}])", " ");
	}
	
	// Normalize name with both rules
	public static String normalizeName(String raw) {
	    if (raw == null) return null;
	    String s = raw.trim();

	    // NEW: split before a chain head (spaced or glued)
	    s = P_GLUED_TO_PREV_CHAINHEAD_GLUE.matcher(s).replaceAll(" ");
	    s = P_GLUED_TO_PREV_CHAINHEAD_SPACED.matcher(s).replaceAll(" ");

	    // YOUR EXISTING RULES (unchanged order/content)
	    s = P_GLUED_TO_PREV_CHAIN.matcher(s).replaceAll(" ");
	    s = P_GLUED_TO_NEXT_CAP.matcher(s).replaceAll("$0 ");
	    s = P_ACRONYM_SPLIT.matcher(s).replaceAll(" ");

	    return s.replaceAll("\\s{2,}", " ").trim();
	}
}
