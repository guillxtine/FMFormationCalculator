package squad;

import java.util.List;
import java.util.Map;

public record Player
(
	String name,
	Map<Attr, Integer> attributes, // 44 numeric attributes
	String leftFoot,
	String rightFoot,
	List<Positions> positions,
	Double last5Games, // nullable
	Double averageRating, // nullable
	PlayerCalculator calc
)
{
	public int get(Attr a)
	{
		return attributes.getOrDefault(a, 0);
	}
	
	public PlayerCalculator getCalc()
	{
		return this.calc;
	}
	
	public void calculateAbilties()
	{
		this.calc.calculateCurrentAbilities(this);
	}
	
	public void calculatePosAbilities()
	{
		this.calc.calculateCurrentAbilitiesPos(this);
	}
}
