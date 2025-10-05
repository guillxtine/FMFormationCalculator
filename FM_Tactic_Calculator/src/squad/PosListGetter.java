package squad;

import java.util.ArrayList;
import java.util.List;

public class PosListGetter
{
	List<Player> players;
	
	public PosListGetter(List<Player> players)
	{
		this.players = players;
	}
	
	public List<Player> getGks()
	{
		List<Player> gks = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.GK))
					{
						gks.add(p);
					}
			}
		
		return gks;
	}
	
	public List<Player> getCbs()
	{
		List<Player> cbs = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.DC))
					{
						cbs.add(p);
					}
			}
		
		return cbs;
	}
	
	public List<Player> getLbs()
	{
		List<Player> lbs = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.DL))
					{
						lbs.add(p);
					}
			}
		
		return lbs;
	}
	
	public List<Player> getRbs()
	{
		List<Player> rbs = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.DR))
					{
						rbs.add(p);
					}
			}
		
		return rbs;
	}
	
	public List<Player> getDms()
	{
		List<Player> dms = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.DM))
					{
						dms.add(p);
					}
			}
		
		return dms;
	}
	
	public List<Player> getLwbs()
	{
		List<Player> lwbs = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.WBL))
					{
						lwbs.add(p);
					}
			}
		
		return lwbs;
	}
	
	public List<Player> getRwbs()
	{
		List<Player> rwbs = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.WBR))
					{
						rwbs.add(p);
					}
			}
		
		return rwbs;
	}
	
	public List<Player> getCms()
	{
		List<Player> cms = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.MC))
					{
						cms.add(p);
					}
			}
		
		return cms;
	}
	
	public List<Player> getLms()
	{
		List<Player> lms = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.ML))
					{
						lms.add(p);
					}
			}
		
		return lms;
	}
	
	public List<Player> getRms()
	{
		List<Player> rms = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.MR))
					{
						rms.add(p);
					}
			}
		
		return rms;
	}
	
	public List<Player> getCams()
	{
		List<Player> cams = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.AMC))
					{
						cams.add(p);
					}
			}
		
		return cams;
	}
	
	public List<Player> getLws()
	{
		List<Player> lws = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.AML))
					{
						lws.add(p);
					}
			}
		
		return lws;
	}
	
	public List<Player> getRws()
	{
		List<Player> rws = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.AMR))
					{
						rws.add(p);
					}
			}
		
		return rws;
	}
	
	public List<Player> getSts()
	{
		List<Player> sts = new ArrayList<>();
		
		for (Player p : this.players)
			{
				if (p.positions().contains(Positions.ST))
					{
						sts.add(p);
					}
			}
		
		return sts;
	}
}
