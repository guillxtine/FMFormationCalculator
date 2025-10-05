package squad;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PosAbilityLinker
{
	List<Integer> abilities;
	List<Positions> positions;
	Map<Positions, Integer> posAbilityMap;
	
	public PosAbilityLinker(Player p)
	{
		this.abilities = p.getCalc().calculateCurrentAbilitiesPos(p);
		this.positions = p.positions();
	}
	
	public Map<Positions, Integer> LinkPosAbility(Player p)
	{
	    // make sure the list is (re)computed for this player & filtered to his positions
	    List<Integer> vals = p.getCalc().calculateCurrentAbilitiesPos(p); // returns in GK..ST group order
	    Map<Positions, Integer> out = new LinkedHashMap<>();
	    int i = 0;

	    if (p.positions().contains(Positions.GK))  out.put(Positions.GK, vals.get(i++));
	    if (p.positions().contains(Positions.DC))  out.put(Positions.DC, vals.get(i++));

	    if (p.positions().contains(Positions.DL) || p.positions().contains(Positions.DR)) {
	        int v = vals.get(i++);                        // FB (one value)
	        if (p.positions().contains(Positions.DL)) out.put(Positions.DL, v);
	        if (p.positions().contains(Positions.DR)) out.put(Positions.DR, v);
	    }

	    if (p.positions().contains(Positions.DM))  out.put(Positions.DM, vals.get(i++));

	    if (p.positions().contains(Positions.WBL) || p.positions().contains(Positions.WBR)) {
	        int v = vals.get(i++);                        // WB (one value)
	        if (p.positions().contains(Positions.WBL)) out.put(Positions.WBL, v);
	        if (p.positions().contains(Positions.WBR)) out.put(Positions.WBR, v);
	    }

	    if (p.positions().contains(Positions.MC))  out.put(Positions.MC,  vals.get(i++));

	    if (p.positions().contains(Positions.ML) || p.positions().contains(Positions.MR)) {
	        int v = vals.get(i++);                        // WM (one value)
	        if (p.positions().contains(Positions.ML)) out.put(Positions.ML, v);
	        if (p.positions().contains(Positions.MR)) out.put(Positions.MR, v);
	    }

	    if (p.positions().contains(Positions.AMC)) out.put(Positions.AMC, vals.get(i++));

	    if (p.positions().contains(Positions.AML) || p.positions().contains(Positions.AMR)) {
	        int v = vals.get(i++);                        // WF (one value)
	        if (p.positions().contains(Positions.AML)) out.put(Positions.AML, v);
	        if (p.positions().contains(Positions.AMR)) out.put(Positions.AMR, v);
	    }

	    if (p.positions().contains(Positions.ST))  out.put(Positions.ST, vals.get(i++));
	    
	    return out;
	}

	public Map<Positions, Integer> getPosAbilityMap()
	{
		return posAbilityMap;
	}
}
