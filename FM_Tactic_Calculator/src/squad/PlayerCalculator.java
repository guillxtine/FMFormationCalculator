package squad;

import java.util.ArrayList;
import java.util.List;

public class PlayerCalculator
{
	static final int NEW_MAX = 200;
	static final int NEW_MIN = 1;
	
	public int gkAbility;
	public int cbAbility;
	public int fbAbility;
	public int dmAbility;
	public int wbAbility;
	public int cmAbility;
	public int wmAbility;
	public int amAbility;
	public int wfAbility;
	public int stAbility;
	
	List<Integer> abilities;
	List<Double> weightedAbilities;
	
	public void calculateCurrentAbilities(Player p)
	{			
		List<Integer> abilities = new ArrayList<>();
		
		// set attribute abilities
		gkAbility = calculateCurrentAbilityGK(p);
		cbAbility = calculateCurrentAbilityCB(p);
		fbAbility = calculateCurrentAbilityFB(p);
		dmAbility = calculateCurrentAbilityDM(p);
		wbAbility = calculateCurrentAbilityWB(p);
		cmAbility = calculateCurrentAbilityCM(p);
		wmAbility = calculateCurrentAbilityWM(p);
		amAbility = calculateCurrentAbilityAM(p);
		wfAbility = calculateCurrentAbilityWF(p);
		stAbility = calculateCurrentAbilityST(p);
		
		// add abilities to list
		abilities.add(gkAbility);
		abilities.add(cbAbility);
		abilities.add(fbAbility);
		abilities.add(dmAbility);
		abilities.add(wbAbility);
		abilities.add(cmAbility);
		abilities.add(wmAbility);
		abilities.add(amAbility);
		abilities.add(wfAbility);
		abilities.add(stAbility);
		
		this.abilities = abilities;
	}
	
	public List<Integer> calculateCurrentAbilitiesPos(Player p)
	{
		List<Integer> abilities = new ArrayList<>();
		
		if (p.positions().contains(Positions.GK))
			{
				gkAbility = calculateCurrentAbilityGK(p);
				abilities.add(gkAbility);
			}
		if (p.positions().contains(Positions.DC))
			{
				cbAbility = calculateCurrentAbilityCB(p);
				abilities.add(cbAbility);
			}
		if (p.positions().contains(Positions.DL) || p.positions().contains(Positions.DR))
			{
				fbAbility = calculateCurrentAbilityFB(p);
				abilities.add(fbAbility);
			}
		if (p.positions().contains(Positions.DM))
			{
				dmAbility = calculateCurrentAbilityDM(p);
				abilities.add(dmAbility);
			}
		if (p.positions().contains(Positions.WBL) || p.positions().contains(Positions.WBR))
			{
				wbAbility = calculateCurrentAbilityWB(p);
				abilities.add(wbAbility);
			}
		if (p.positions().contains(Positions.MC))
			{
				cmAbility = calculateCurrentAbilityCM(p);
				abilities.add(cmAbility);
			}
		if (p.positions().contains(Positions.ML) || p.positions().contains(Positions.MR))
			{
				wmAbility = calculateCurrentAbilityWM(p);
				abilities.add(wmAbility);
			}
		if (p.positions().contains(Positions.AMC))
			{
				amAbility = calculateCurrentAbilityAM(p);
				abilities.add(amAbility);
			}
		if (p.positions().contains(Positions.AML) || p.positions().contains(Positions.AMR))
			{
				wfAbility = calculateCurrentAbilityWF(p);
				abilities.add(wfAbility);
			}
		if (p.positions().contains(Positions.ST))
			{
				stAbility = calculateCurrentAbilityST(p);
				abilities.add(stAbility);
			}
		
		this.abilities = abilities;
		
		return abilities;
	}
	
	public List<Double> calculateWeightedAbilitiesByPos(Player p)
	{
		List<Double> weightedAbilities = new ArrayList<>();
		
		calculateCurrentAbilitiesPos(p);
		List<Integer> calcAbils = p.getCalc().getAbilities();
		Double avgRating = p.averageRating();
		double weightedAvgRating;
		
		if (avgRating == null)
			{
				weightedAvgRating = 1.0;
			}
		else
			{
				if (avgRating < 6.0)
					{
						weightedAvgRating = 0.88;
					}
				else if (avgRating < 6.2 && avgRating >= 6.0)
					{
						weightedAvgRating = 0.91;
					}
				else if (avgRating < 6.4 && avgRating >= 6.2)
					{
						weightedAvgRating = 0.94;
					}
				else if (avgRating < 6.6 && avgRating >= 6.4)
					{
						weightedAvgRating = 0.97;
					}
				else if (avgRating < 6.8 && avgRating >= 6.6)
					{
						weightedAvgRating = 1.0;
					}
				else if (avgRating < 7.0 && avgRating >= 6.8)
					{
						weightedAvgRating = 1.03;
					}
				else if (avgRating < 7.3 && avgRating >= 7.0)
					{
						weightedAvgRating = 1.06;
					}
				else if (avgRating < 7.6 && avgRating >= 7.3)
					{
						weightedAvgRating = 1.09;
					}
				else
					{
						weightedAvgRating = 1.15;
					}
			}
		
		for (int abil : calcAbils)
			{
				double newAbil = abil * weightedAvgRating;
				weightedAbilities.add(newAbil);
			}
		
		this.weightedAbilities = weightedAbilities;
		return weightedAbilities;
	}

	public int calculateCurrentAbilityGK(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 29;
		final double MIN_SCORE = 114 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 2280 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (10 * p.get(Attr.DECISIONS)) + (8 * p.get(Attr.AGILITY)) + (8 * p.get(Attr.REFLEXES)) + (8 * p.get(Attr.HANDLING))
				+ (6 * p.get(Attr.CONCENTRATION)) + (6 * p.get(Attr.BRAVERY)) + (6 * p.get(Attr.ACCELERATION)) + (6 * p.get(Attr.COMMAND_OF_AREA))
				+ (6 * p.get(Attr.AERIAL_REACH)) + (5 * p.get(Attr.POSITIONING)) + (5 * p.get(Attr.KICKING)) + (5 * p.get(Attr.COMMUNICATION))
				+ (4 * p.get(Attr.STRENGTH)) + (4 * p.get(Attr.ONE_VS_ONES)) + (3 * p.get(Attr.PACE)) + (3 * p.get(Attr.ANTICIPATION))
				+ (3 * p.get(Attr.THROWING)) + (3 * p.get(Attr.PASSING)) + (2 * p.get(Attr.COMPOSURE)) + (2 * p.get(Attr.BALANCE))
				+ (2 * p.get(Attr.LEADERSHIP)) + (2 * p.get(Attr.TEAMWORK)) + (p.get(Attr.HEADING)) + (p.get(Attr.TECHNIQUE)) + (p.get(Attr.VISION))
				+ (p.get(Attr.WORK_RATE)) + (p.get(Attr.JUMPING_REACH)) + (p.get(Attr.STAMINA)) + (p.get(Attr.FIRST_TOUCH));
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.GK))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityCB(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 101 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 2020 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (10 * p.get(Attr.DECISIONS)) + (8 * p.get(Attr.POSITIONING)) + (8 * p.get(Attr.MARKING)) + (6 * p.get(Attr.AGILITY))
				+ (6 * p.get(Attr.JUMPING_REACH)) + (6 * p.get(Attr.STRENGTH)) + (6 * p.get(Attr.ACCELERATION)) + (5 * p.get(Attr.PACE))
				+ (5 * p.get(Attr.ANTICIPATION)) + (5 * p.get(Attr.TACKLING)) + (5 * p.get(Attr.HEADING)) + (4 * p.get(Attr.CONCENTRATION))
				+ (3 * p.get(Attr.STAMINA)) + (2 * p.get(Attr.COMPOSURE)) + (2 * p.get(Attr.BRAVERY)) + (2 * p.get(Attr.BALANCE))
				+ (2 * p.get(Attr.LEADERSHIP)) + (2 * p.get(Attr.WORK_RATE)) + (2 * p.get(Attr.FIRST_TOUCH)) + (2 * p.get(Attr.PASSING))
				+ p.get(Attr.CORNERS) + p.get(Attr.DRIBBLING) + p.get(Attr.FINISHING) + p.get(Attr.FREE_KICK_TAKING) + p.get(Attr.LONG_SHOTS)
				+ p.get(Attr.LONG_THROWS) + p.get(Attr.PENALTY_TAKING) + p.get(Attr.TECHNIQUE) + p.get(Attr.OFF_THE_BALL) + p.get(Attr.TEAMWORK)
				+ p.get(Attr.VISION) + p.get(Attr.CROSSING);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.DC))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityFB(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 87 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 1740 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (7 * p.get(Attr.ACCELERATION)) + (7 * p.get(Attr.DECISIONS)) + (6 * p.get(Attr.AGILITY)) + (6 * p.get(Attr.STAMINA))
				+ (5 * p.get(Attr.PACE)) + (4 * p.get(Attr.CONCENTRATION)) + (4 * p.get(Attr.STRENGTH)) + (4 * p.get(Attr.POSITIONING))
				+ (4 * p.get(Attr.TACKLING)) + (3 * p.get(Attr.FIRST_TOUCH)) + (3 * p.get(Attr.ANTICIPATION)) + (3 * p.get(Attr.MARKING))
				+ (2 * p.get(Attr.COMPOSURE)) + (2 * p.get(Attr.BRAVERY)) + (2 * p.get(Attr.BALANCE)) + (2 * p.get(Attr.JUMPING_REACH))
				+ (2 * p.get(Attr.WORK_RATE)) + (2 * p.get(Attr.TEAMWORK)) + (2 * p.get(Attr.TECHNIQUE)) + (2 * p.get(Attr.VISION))
				+ (2 * p.get(Attr.PASSING)) + (2 * p.get(Attr.HEADING)) + (2 * p.get(Attr.CROSSING)) + p.get(Attr.CORNERS)
				+ p.get(Attr.DRIBBLING) + p.get(Attr.FINISHING) + p.get(Attr.FREE_KICK_TAKING) + p.get(Attr.LONG_SHOTS) + p.get(Attr.LONG_THROWS)
				+ p.get(Attr.PENALTY_TAKING) + p.get(Attr.LEADERSHIP) + p.get(Attr.OFF_THE_BALL);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.DL) && !p.positions().contains(Positions.DR))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityDM(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 96 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 1920 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (8 * p.get(Attr.DECISIONS)) + (7 * p.get(Attr.DECISIONS)) + (6 * p.get(Attr.AGILITY)) + (6 * p.get(Attr.ACCELERATION))
				+ (5 * p.get(Attr.STRENGTH)) + (5 * p.get(Attr.POSITIONING)) + (5 * p.get(Attr.ANTICIPATION)) + (4 * p.get(Attr.PACE))
				+ (4 * p.get(Attr.STAMINA)) + (4 * p.get(Attr.WORK_RATE)) + (4 * p.get(Attr.FIRST_TOUCH)) + (4 * p.get(Attr.VISION))
				+ (4 * p.get(Attr.PASSING)) + (3 * p.get(Attr.CONCENTRATION)) + (3 * p.get(Attr.TECHNIQUE)) + (3 * p.get(Attr.MARKING))
				+ (3 * p.get(Attr.LONG_SHOTS)) + (2 * p.get(Attr.COMPOSURE)) + (2 * p.get(Attr.BALANCE)) + (2 * p.get(Attr.TEAMWORK))
				+ (2 * p.get(Attr.VISION)) + (2 * p.get(Attr.OFF_THE_BALL)) + (2 * p.get(Attr.MARKING)) + (2 * p.get(Attr.DRIBBLING))
				+ p.get(Attr.CORNERS) + p.get(Attr.CROSSING) + p.get(Attr.FREE_KICK_TAKING) + p.get(Attr.HEADING) + p.get(Attr.LONG_THROWS)
				+ p.get(Attr.PENALTY_TAKING) + p.get(Attr.LEADERSHIP) + p.get(Attr.JUMPING_REACH);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.DM))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityWB(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 85 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 1700 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (8 * p.get(Attr.ACCELERATION)) + (7 * p.get(Attr.STAMINA)) + (6 * p.get(Attr.PACE)) + (5 * p.get(Attr.AGILITY))
				+ (5 * p.get(Attr.DECISIONS)) + (4 * p.get(Attr.STRENGTH)) + (3 * p.get(Attr.CONCENTRATION)) + (3 * p.get(Attr.TECHNIQUE))
				+ (3 * p.get(Attr.FIRST_TOUCH)) + (3 * p.get(Attr.POSITIONING)) + (3 * p.get(Attr.ANTICIPATION)) + (3 * p.get(Attr.TACKLING))
				+ (3 * p.get(Attr.PASSING)) + (3 * p.get(Attr.CROSSING)) + (2 * p.get(Attr.COMPOSURE)) + (2 * p.get(Attr.BALANCE))
				+ (2 * p.get(Attr.WORK_RATE)) + (2 * p.get(Attr.TEAMWORK)) + (2 * p.get(Attr.VISION)) + (2 * p.get(Attr.OFF_THE_BALL))
				+ (2 * p.get(Attr.MARKING)) + (2 * p.get(Attr.DRIBBLING)) + p.get(Attr.CORNERS) + p.get(Attr.FINISHING) + p.get(Attr.FREE_KICK_TAKING)
				+ p.get(Attr.HEADING) + p.get(Attr.LONG_SHOTS) + p.get(Attr.LONG_THROWS) + p.get(Attr.PENALTY_TAKING) + p.get(Attr.BRAVERY)
				+ p.get(Attr.LEADERSHIP) + p.get(Attr.JUMPING_REACH);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.WBL) && !p.positions().contains(Positions.WBR))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityCM(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 99 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 1980 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (7 * p.get(Attr.DECISIONS)) + (6 * p.get(Attr.AGILITY)) + (6 * p.get(Attr.STAMINA)) + (6 * p.get(Attr.ACCELERATION))
				+ (6 * p.get(Attr.FIRST_TOUCH)) + (6 * p.get(Attr.VISION)) + (6 * p.get(Attr.PASSING)) + (5 * p.get(Attr.PACE))
				+ (4 * p.get(Attr.STRENGTH)) + (4 * p.get(Attr.TECHNIQUE)) + (3 * p.get(Attr.COMPOSURE)) + (3 * p.get(Attr.WORK_RATE))
				+ (3 * p.get(Attr.POSITIONING)) + (3 * p.get(Attr.ANTICIPATION)) + (3 * p.get(Attr.TACKLING)) + (3 * p.get(Attr.OFF_THE_BALL))
				+ (3 * p.get(Attr.MARKING)) + (3 * p.get(Attr.LONG_SHOTS)) + (2 * p.get(Attr.CONCENTRATION)) + (2 * p.get(Attr.BALANCE))
				+ (2 * p.get(Attr.TEAMWORK)) + (2 * p.get(Attr.FINISHING)) + (2 * p.get(Attr.DRIBBLING)) + p.get(Attr.CORNERS)
				+ p.get(Attr.CROSSING) + p.get(Attr.FREE_KICK_TAKING) + p.get(Attr.HEADING) + p.get(Attr.LONG_THROWS) + p.get(Attr.PENALTY_TAKING)
				+ p.get(Attr.BRAVERY) + p.get(Attr.LEADERSHIP) + p.get(Attr.JUMPING_REACH);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.MC))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityWM(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 88 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 1760 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (8 * p.get(Attr.ACCELERATION)) + (6 * p.get(Attr.AGILITY)) + (6 * p.get(Attr.PACE)) + (5 * p.get(Attr.STAMINA))
				+ (5 * p.get(Attr.DECISIONS)) + (5 * p.get(Attr.CROSSING)) + (4 * p.get(Attr.TECHNIQUE)) + (4 * p.get(Attr.FIRST_TOUCH))
				+ (3 * p.get(Attr.COMPOSURE)) + (3 * p.get(Attr.STRENGTH)) + (3 * p.get(Attr.WORK_RATE)) + (3 * p.get(Attr.ANTICIPATION))
				+ (3 * p.get(Attr.VISION)) + (3 * p.get(Attr.PASSING)) + (3 * p.get(Attr.DRIBBLING)) + (2 * p.get(Attr.CONCENTRATION))
				+ (2 * p.get(Attr.BALANCE)) + (2 * p.get(Attr.TEAMWORK)) + (2 * p.get(Attr.TACKLING)) + (2 * p.get(Attr.OFF_THE_BALL))
				+ (2 * p.get(Attr.LONG_SHOTS)) + (2 * p.get(Attr.FINISHING)) + p.get(Attr.CORNERS) + p.get(Attr.FREE_KICK_TAKING)
				+ p.get(Attr.HEADING) + p.get(Attr.LONG_THROWS) + p.get(Attr.MARKING) + p.get(Attr.PENALTY_TAKING) + p.get(Attr.LEADERSHIP)
				+ p.get(Attr.POSITIONING) + p.get(Attr.JUMPING_REACH) + p.get(Attr.BRAVERY);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.ML) && !p.positions().contains(Positions.MR))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityAM(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 98 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 1960 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (9 * p.get(Attr.ACCELERATION)) + (7 * p.get(Attr.PACE)) + (6 * p.get(Attr.AGILITY)) + (6 * p.get(Attr.STAMINA))
				+ (6 * p.get(Attr.DECISIONS)) + (6 * p.get(Attr.VISION)) + (5 * p.get(Attr.TECHNIQUE)) + (5 * p.get(Attr.FIRST_TOUCH))
				+ (4 * p.get(Attr.PASSING)) + (3 * p.get(Attr.COMPOSURE)) + (3 * p.get(Attr.STRENGTH)) + (3 * p.get(Attr.WORK_RATE))
				+ (3 * p.get(Attr.ANTICIPATION)) + (3 * p.get(Attr.OFF_THE_BALL)) + (3 * p.get(Attr.LONG_SHOTS)) + (3 * p.get(Attr.FINISHING))
				+ (3 * p.get(Attr.DRIBBLING)) + (2 * p.get(Attr.CONCENTRATION)) + (2 * p.get(Attr.BALANCE)) + (2 * p.get(Attr.TEAMWORK))
				+ (2 * p.get(Attr.POSITIONING)) + (2 * p.get(Attr.TACKLING)) + p.get(Attr.CORNERS) + p.get(Attr.CROSSING)
				+ p.get(Attr.FREE_KICK_TAKING) + p.get(Attr.HEADING) + p.get(Attr.LONG_THROWS) + p.get(Attr.MARKING) + p.get(Attr.PENALTY_TAKING)
				+ p.get(Attr.BRAVERY) + p.get(Attr.LEADERSHIP) + p.get(Attr.JUMPING_REACH);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.AMC))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityWF(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 98 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 1960 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (10 * p.get(Attr.PACE)) + (10 * p.get(Attr.ACCELERATION)) + (7 * p.get(Attr.STAMINA)) + (6 * p.get(Attr.AGILITY))
				+ (5 * p.get(Attr.FIRST_TOUCH)) + (5 * p.get(Attr.DECISIONS)) + (5 * p.get(Attr.DRIBBLING)) + (5 * p.get(Attr.CROSSING))
				+ (4 * p.get(Attr.TECHNIQUE)) + (3 * p.get(Attr.COMPOSURE)) + (3 * p.get(Attr.STRENGTH)) + (3 * p.get(Attr.WORK_RATE))
				+ (3 * p.get(Attr.ANTICIPATION)) + (3 * p.get(Attr.VISION)) + (2 * p.get(Attr.CONCENTRATION)) + (2 * p.get(Attr.BALANCE))
				+ (2 * p.get(Attr.TEAMWORK)) + (2 * p.get(Attr.TACKLING)) + (2 * p.get(Attr.PASSING)) + (2 * p.get(Attr.OFF_THE_BALL))
				+ (2 * p.get(Attr.LONG_SHOTS)) + (2 * p.get(Attr.FINISHING)) + p.get(Attr.CORNERS) + p.get(Attr.FREE_KICK_TAKING)
				+ p.get(Attr.HEADING) + p.get(Attr.LONG_THROWS) + p.get(Attr.MARKING) + p.get(Attr.PENALTY_TAKING) + p.get(Attr.BRAVERY)
				+ p.get(Attr.POSITIONING) + p.get(Attr.LEADERSHIP) + p.get(Attr.JUMPING_REACH);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.AML) && !p.positions().contains(Positions.AMR))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int calculateCurrentAbilityST(Player p)
	{
		int ability;
		
		final int NUM_OF_ATTRIBUTES = 32;
		final double MIN_SCORE = 116 / NUM_OF_ATTRIBUTES;
		final double MAX_SCORE = 2320 / NUM_OF_ATTRIBUTES;
		
		double weightedTotal = (10 * p.get(Attr.ACCELERATION)) + (8 * p.get(Attr.FINISHING)) + (7 * p.get(Attr.PACE)) + (6 * p.get(Attr.COMPOSURE))
				+ (6 * p.get(Attr.AGILITY)) + (6 * p.get(Attr.STAMINA)) + (6 * p.get(Attr.STRENGTH)) + (6 * p.get(Attr.FIRST_TOUCH))
				+ (6 * p.get(Attr.OFF_THE_BALL)) + (6 * p.get(Attr.HEADING)) + (5 * p.get(Attr.JUMPING_REACH)) + (5 * p.get(Attr.DECISIONS))
				+ (5 * p.get(Attr.ANTICIPATION)) + (5 * p.get(Attr.DRIBBLING)) + (4 * p.get(Attr.TECHNIQUE)) + (2 * p.get(Attr.CONCENTRATION))
				+ (2 * p.get(Attr.BALANCE)) + (2 * p.get(Attr.WORK_RATE)) + (2 * p.get(Attr.POSITIONING)) + (2 * p.get(Attr.VISION))
				+ (2 * p.get(Attr.PASSING)) + (2 * p.get(Attr.LONG_SHOTS)) + (2 * p.get(Attr.CROSSING)) + p.get(Attr.CORNERS)
				+ p.get(Attr.FREE_KICK_TAKING) + p.get(Attr.LONG_THROWS) + p.get(Attr.MARKING) + p.get(Attr.PENALTY_TAKING) + p.get(Attr.TACKLING)
				+ p.get(Attr.BRAVERY) + p.get(Attr.LEADERSHIP) + p.get(Attr.TEAMWORK);
		
		double weightedAvg = weightedTotal / NUM_OF_ATTRIBUTES;
		
		double abilityUnrounded = ((weightedAvg - MIN_SCORE) / (MAX_SCORE - MIN_SCORE)) * (NEW_MAX - NEW_MIN) + 1;
		
		if (!p.positions().contains(Positions.ST))
			{
				abilityUnrounded = abilityUnrounded / 2;
			}
		
		ability = (int) Math.floor(abilityUnrounded);
		
		return ability;
	}
	
	public int getGkAbility()
	{
		return gkAbility;
	}

	public int getCbAbility()
	{
		return cbAbility;
	}

	public int getFbAbility()
	{
		return fbAbility;
	}

	public int getDmAbility()
	{
		return dmAbility;
	}

	public int getWbAbility()
	{
		return wbAbility;
	}

	public int getCmAbility()
	{
		return cmAbility;
	}

	public int getWmAbility()
	{
		return wmAbility;
	}

	public int getAmAbility()
	{
		return amAbility;
	}

	public int getWfAbility()
	{
		return wfAbility;
	}

	public int getStAbility()
	{
		return stAbility;
	}
	
	public List<Integer> getAbilities()
	{
		return abilities;
	}
	
	public List<Double> getWeightedAbilities()
	{
		return weightedAbilities;
	}
}
