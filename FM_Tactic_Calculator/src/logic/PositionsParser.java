package logic;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import squad.Positions;

public final class PositionsParser
{
    // e.g. "D (RC), WB (R)" or "M (L), AM (RL), ST (C)"
    private static final Pattern BASE_WITH_SIDES =
        Pattern.compile("(GK|ST|WB|AM|DM|D|M)\\s*(?:\\(([^)]+)\\))?");

    public static List<Positions> parse(String raw)
    {
        if (raw == null || raw.isBlank())
        	{
        		return List.of();
        	}
        String s = raw.trim();

        // If it still has separators, use the structured parser
        if (s.matches(".*[()/, ].*"))
        	{
        		return parseStructured(s);
        	}
        // Otherwise, fall back to compact parser (e.g., "DRLC", "DRCWBR", "GK")
        return parseCompact(s);
    }

    /** Handles: "D (RC), WB (R)" or "D/M/AM (R)" */
    private static List<Positions> parseStructured(String s)
    {
        s = s.toUpperCase(Locale.ROOT);
        LinkedHashSet<Positions> out = new LinkedHashSet<>();
        Matcher m = BASE_WITH_SIDES.matcher(s);

        // capture each base with its sides if present
        List<String> bases  = new ArrayList<>();
        List<String> sidesL = new ArrayList<>();
        while (m.find())
        	{
        		bases.add(m.group(1));
        		sidesL.add(m.group(2)); // may be null
        	}

        // If only the last has sides (e.g. "D/M/AM (R)"), use those sides for prior side-able bases
        String lastSides = null;
        for (int i = sidesL.size() - 1; i >= 0; i--)
        	{
        		if (sidesL.get(i) != null)
        			{
        				lastSides = sidesL.get(i).replaceAll("\\s+", "");
        				break;
        			}
        	}

        for (int i = 0; i < bases.size(); i++)
        	{
        		String base  = bases.get(i);
        		String sides = sidesL.get(i);
        		if (sides == null)
        			{
        				sides = lastSides;
        			}
        		addPositions(out, base, sides);
        	}
        return List.copyOf(out);
    }

    private static List<Positions> parseCompact(String compact) {
        String u = compact.toUpperCase(java.util.Locale.ROOT);
        java.util.LinkedHashSet<Positions> out = new java.util.LinkedHashSet<>();

        int i = 0;
        while (i < u.length()) {
            // 1) collect one or more base tokens until we hit a side char (R/L/C) or end
            java.util.List<String> bases = new java.util.ArrayList<>();

            while (i < u.length()) {
                String base = null;
                // two-letter bases first
                if      (u.startsWith("GK", i)) { base = "GK"; i += 2; }
                else if (u.startsWith("ST", i)) { base = "ST"; i += 2; }
                else if (u.startsWith("WB", i)) { base = "WB"; i += 2; }
                else if (u.startsWith("DM", i)) { base = "DM"; i += 2; }
                else if (u.startsWith("AM", i)) { base = "AM"; i += 2; }
                // one-letter bases
                else if (u.charAt(i) == 'D')    { base = "D";  i += 1; }
                else if (u.charAt(i) == 'M')    { base = "M";  i += 1; }

                if (base == null) break;  // not a base token
                bases.add(base);

                // peek: next char starts a side group? then stop collecting bases
                if (i < u.length()) {
                    char c = u.charAt(i);
                    if (c == 'R' || c == 'L' || c == 'C') break;
                }
            }

            if (bases.isEmpty()) {
                // skip any non-base char and continue
                i++;
                continue;
            }

            // 2) collect sides (zero or more of R/L/C)
            int j = i;
            while (j < u.length()) {
                char c = u.charAt(j);
                if (c == 'R' || c == 'L' || c == 'C') j++;
                else break;
            }
            String sides = (j > i) ? u.substring(i, j) : null;
            i = j; // advance

            // 3) apply sides to ALL bases we collected in this segment
            for (String base : bases) {
                addPositions(out, base, sides);
            }
        }
        return List.copyOf(out);
    }


    private static void addPositions(Set<Positions> out, String base, String sides) {
        switch (base) {
            case "GK" -> out.add(Positions.GK);
            case "D" ->
            {
                if (sides == null) { out.add(Positions.DC); return; }
                if (sides.indexOf('L') >= 0) out.add(Positions.DL);
                if (sides.indexOf('C') >= 0) out.add(Positions.DC);
                if (sides.indexOf('R') >= 0) out.add(Positions.DR);
            }
            case "WB" ->
            {
                if (sides == null) return; // no WBC
                if (sides.indexOf('L') >= 0) out.add(Positions.WBL);
                if (sides.indexOf('R') >= 0) out.add(Positions.WBR);
            }
            case "DM" -> out.add(Positions.DM); // ignore sides for DM
            case "M" ->
            {
                if (sides == null) { out.add(Positions.MC); return; }
                if (sides.indexOf('L') >= 0) out.add(Positions.ML);
                if (sides.indexOf('C') >= 0) out.add(Positions.MC);
                if (sides.indexOf('R') >= 0) out.add(Positions.MR);
            }
            case "AM" ->
            {
                if (sides == null) { out.add(Positions.AMC); return; }
                if (sides.indexOf('L') >= 0) out.add(Positions.AML);
                if (sides.indexOf('C') >= 0) out.add(Positions.AMC);
                if (sides.indexOf('R') >= 0) out.add(Positions.AMR);
            }
            case "ST" -> out.add(Positions.ST);
        }
    }
}

