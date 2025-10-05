package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import logic.FormationRecommender;
import logic.RTFParser;
import logic.SquadTextParser;
import squad.Player;
import squad.PlayerMapper;
import squad.PosAbilityLinker;
import squad.Positions;
import squad.WeightedPosAbilityLinker;

public class Button extends JButton
{
	public String text = "";
	int x = 150;
	int y = 50;
	public Dimension size = new Dimension(x, y);
	boolean focus = true;
	boolean border = true;
	boolean transparent = true;
	
	List<Player> players;

	public Button(String text, Dimension size, Color background, Color foreground, boolean focus, boolean border, boolean transparent)
	{
		super(text);
		
		if (size != null)
			{
				this.size = size;
			}
		setPreferredSize(this.size);
		
		if (background != null)
			{
				setBackground(background);
			}
		
		if (foreground != null)
			{
				setForeground(foreground);
			}
		
		setFocusPainted(focus);
		setBorderPainted(border);
		setContentAreaFilled(transparent);
	}
	
	public Button(String text)
	{
		this(text, null, Color.decode("#d1edea"), Color.BLACK, true, true, true);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return size;
	}
	
	public void openRtfChooser()
	{
	    JFileChooser chooser = new JFileChooser();
	    chooser.setFileFilter(new FileNameExtensionFilter("Rich Text Format (*.rtf)", "rtf"));

	    System.out.println("[DEBUG] Opening chooser...");
	    int result = chooser.showOpenDialog(null);
	    System.out.println("[DEBUG] chooser result = " + result);

	    if (result == JFileChooser.APPROVE_OPTION)
	    	{
	        File selectedFile = chooser.getSelectedFile();
	        System.out.println("[DEBUG] Selected: " + selectedFile.getAbsolutePath());
	        try
	        {
	            String plain = RTFParser.parseRtfOrPlain(selectedFile);
	            System.out.println("[DEBUG] plain length = " + (plain == null ? -1 : plain.length()));

	            String cleaned = RTFParser.keepAlnumPipesAndLines(plain);
	            
	            System.out.println("[DEBUG] Parsing to table...");
	            List<List<String>> table = SquadTextParser.parsePipeTable(cleaned);
	            
	            List<Player> players = new ArrayList<>();
	            for (int i = 1; i < table.size(); i++)
	            	{
	            		players.add(PlayerMapper.fromRow(table.get(i)));
	            	}
	            
	            this.players = players;
	            
	            playerDetailer(players);
	            
	        }
	        catch (Exception ex)
	        {
	            ex.printStackTrace();
	        }
	    }
	    else
	    	{
	        System.out.println("[DEBUG] chooser cancelled");
	    	}
	}
	
	public void playerDetailer(List<Player> players)
	{
		List<Map<Positions, Double>> weightedPosAbilities = new ArrayList<>();
		List<Map<Positions, Integer>> posAbilities = new ArrayList<>();
		
		System.out.println("[DEBUG] Calculating positional abilities...");
		
		System.out.println("[DEBUG] Building ability table...");
		
		for (Player player : players)
        	{
        		WeightedPosAbilityLinker wpal = new WeightedPosAbilityLinker(player);
        		PosAbilityLinker pal = new PosAbilityLinker(player);
        		Map<Positions, Double> byPos = wpal.LinkPosAbility(player);
        		weightedPosAbilities.add(byPos);
        		
        		StringBuilder sb = new StringBuilder();
        		sb.append(player.name()).append(", ");

        		boolean first = true;
        		for (Map.Entry<Positions, Double> e : byPos.entrySet()) {   // iterates all roles the player can play
        		    if (!first) sb.append(" | ");
        		    sb.append(e.getKey())
        		      .append(": ")
        		      .append(String.format(Locale.US, "%.1f", e.getValue()));
        		    first = false;
        		}
        	}
		
		System.out.println("[DEBUG] Built ability table");
	}
	
	public String pickFormation(JComboBox<?> predFin, JComboBox<?> weight, List<Player> players)
	{
		if (players == null || players.isEmpty())
			{
				JOptionPane.showMessageDialog(null, "No players loaded. Please open a squad file first.", "Missing Squad", JOptionPane.WARNING_MESSAGE);
				return "";
			}
		
		String finish = (String) predFin.getSelectedItem();
		boolean useWeighted = (weight.getSelectedItem().equals("Weighted"));
		
		System.out.println("[DEBUG] Selecting formation...");
		
		var results = FormationRecommender.chooseTop(players, finish, useWeighted, 3);
		
		var abilityMap = buildAbilityMap(players, useWeighted);

	    StringBuilder out = new StringBuilder(1024);
	    for (int i = 0; i < results.size(); i++)
	    	{
	    		var r = results.get(i);

	    		out.append(String.format("Option %d — %s%n%n", i + 1, r.formation().name()));

	    		// partition: non-GK first, then GK (reuses your previous approach)
	    		var gk   = new ArrayList<Map.Entry<FormationRecommender.RoleSlot, squad.Player>>();
	    		var rest = new ArrayList<Map.Entry<FormationRecommender.RoleSlot, squad.Player>>();

	    		for (var e : r.assignments().entrySet())
	    			{
	    				boolean isGK =
	    						Arrays.stream(e.getKey().options()).anyMatch(p -> p == squad.Positions.GK)
	    						|| "GK".equalsIgnoreCase(e.getKey().label());
	    				(isGK ? gk : rest).add(e);
	    			}

	    		for (var e : rest)
	    			{
	    				var slot   = e.getKey();
	    	            var player = e.getValue();
	    	            double abil = bestAbilityForSlot(abilityMap.getOrDefault(player, Collections.emptyMap()), slot);
	    				out.append(String.format("%-6s -> %s (%.1f)%n%n", slot.label(), player.name(), abil));
	    			}
	    		for (var e : gk)
	    			{
	    				var slot   = e.getKey();
	    	            var player = e.getValue();
	    	            double abil = bestAbilityForSlot(abilityMap.getOrDefault(player, Collections.emptyMap()), slot);
	    	            out.append(String.format("%-6s -> %s (%.1f)%n%n", slot.label(), player.name(), abil));
	    			}

	    		out.append(String.format("Score: %.1f%n", r.totalScore()));

	    		if (i < results.size() - 1) out.append('\n'); // blank line between options
	    	}
	    return out.toString();
	}
	
	// Build a map of each player's ability by Positions (weighted or unweighted)
	private static Map<Player, Map<Positions, Double>> buildAbilityMap(List<Player> players, boolean weighted)
	{
		var out = new HashMap<Player, Map<squad.Positions, Double>>();
	    for (Player p : players)
	    	{
	    		if (weighted)
	    			{
	    				var wl = new WeightedPosAbilityLinker(p);
	    				out.put(p, wl.LinkPosAbility(p)); // Map<Positions, Double>
	    			}
	    		else
	    			{
	    				var pl = new PosAbilityLinker(p);
	    				var ints = pl.LinkPosAbility(p);  // Map<Positions, Integer>
	    				var m = new EnumMap<squad.Positions, Double>(squad.Positions.class);
	    				for (var e : ints.entrySet()) { m.put(e.getKey(), e.getValue().doubleValue()); }
	    				out.put(p, m);
	    			}
	    }
	    return out;
	}
	
	// Return the best ability for any of the slot’s acceptable positions
	private static double bestAbilityForSlot(Map<Positions, Double> map, FormationRecommender.RoleSlot slot)
	{
	    double best = 0.0;
	    for (squad.Positions pos : slot.options())
	    	{
	    		Double v = map.get(pos);
	    		if (v != null && v > best) { best = v; }
	    	}
	    return best;
	}
	
	public List<Player> getPlayers()
	{
		return players;
	}
}
