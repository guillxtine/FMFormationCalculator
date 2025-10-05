package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Panel extends JPanel
{
	public Dimension size = new Dimension(800, 600);
	
	private final JTextArea output = new JTextArea(16,60);

	public Panel(Dimension size, GridBagLayout layout)
	{
		this.size = size;
		setLayout(new GridBagLayout());
		setBackground(Color.decode("#173569"));
	}
	
	public Panel()
	{
		setLayout(new GridBagLayout());
		setBackground(Color.decode("#173569"));
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return size;
	}
	
	// simple add button
	public void addButton(Button button, int gridx, int gridy)
	{
		GridBagConstraints gbc = baseGbc();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		add(button, gbc);
	}
	
	// complex add button
	public void addButton(Button button, int gridx, int gridy, int gridw, int gridh, double weightx, double weighty,
			int fill, int anchor, Insets insets)
	{
		GridBagConstraints gbc = baseGbc();
		gbc.gridx = gridx; gbc.gridy = gridy;
		gbc.gridwidth = gridw; gbc.gridheight = gridh;
		gbc.weightx = weightx; gbc.weighty = weighty;
		gbc.fill = fill; gbc.anchor = anchor;
		gbc.insets = insets;
		add(button, gbc);
	}
	
	// simple add dropdown
	public void addDropdown(JComboBox<?> dropdown, int gridx, int gridy)
	{
		GridBagConstraints gbc = baseGbc();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		add(dropdown, gbc);
	}
	
	
	// complex add dropdown
	public void addDropdown(JComboBox<?> combo, int gridx, int gridy, int gridw, int gridh,
            double weightx, double weighty, int fill, int anchor, Insets insets)
	{
		GridBagConstraints gbc = baseGbc();
		gbc.gridx = gridx; gbc.gridy = gridy;
		gbc.gridwidth = gridw; gbc.gridheight = gridh;
		gbc.weightx = weightx; gbc.weighty = weighty;
		gbc.fill = fill; gbc.anchor = anchor;
		gbc.insets = insets;
		add(combo, gbc);
	}
	
	public void addLabeledDropdown(String labelText, JComboBox<?> combo, int gridy)
	{
		JLabel lbl = new JLabel(labelText);
		lbl.setForeground(Color.WHITE);
		
		combo.setBackground(Color.decode("#d1edea"));
		combo.setForeground(Color.BLACK);
		
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		row.setOpaque(false);
		row.add(lbl);
		row.add(combo);
		
		GridBagConstraints g = baseGbc();
		g.gridx = 0; g.gridy = gridy;
		g.gridwidth = 3;
		g.weightx = 1.0;
		g.weighty = 0.0;
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(12, 12, 12, 12);
		add(row, g);
	}
	
	public void setOutputText(String text)
	{
		output.setText(text == null ? "" : text);
		output.setCaretPosition(0); // scroll to top
	}
	
	public void clearOutput() { output.setText(""); }
	
	public void addHomePageButtons()
	{
		// define button(s)
		Button squadFile = new Button("Open Squad File");
		squadFile.addActionListener(e -> squadFile.openRtfChooser());
		
		Button calcTactic = new Button("Calculate Tactic");
		
		// define League Pos dropdown and label
		JComboBox<String> predictedLeaguePos = new JComboBox<>(new String[] {"Relegation", "Bottom Half", "Mid-Table", "Europa/Conference League",
				"Champions League/League Winners"});
		addLabeledDropdown("Predicted League Position", predictedLeaguePos, 0);
		
		// define Weighted or Unweighted dropdown and label
		JComboBox<String> weightOrUnweight = new JComboBox<>(new String[] {"Weighted"});
		addLabeledDropdown("Weighted or Unweighted Abilities", weightOrUnweight, 1);
		
		calcTactic.addActionListener(e -> {
			String text = calcTactic.pickFormation(predictedLeaguePos, weightOrUnweight, squadFile.getPlayers());
			setOutputText(text);
		});
		
		// add buttons
		addButton(squadFile, 0, 3, 1, 1, 0.1, 0.1, GridBagConstraints.NONE, GridBagConstraints.SOUTH, new Insets(20, 0, 60, -200));
		addButton(calcTactic, 1, 3, 1, 1, 0.1, 0.1, GridBagConstraints.NONE, GridBagConstraints.SOUTH, new Insets(20, -200, 60, 0));
		
		output.setEditable(false);
		output.setLineWrap(true);
		output.setWrapStyleWord(true);
		output.setFont(new Font("Times New Roman", Font.BOLD, 11));
		output.setBackground(Color.decode("#d1edea"));
		output.setForeground(Color.BLACK);
		
		JScrollPane scroll = new JScrollPane(output);
		GridBagConstraints go = baseGbc();
		go.gridx = 0; go.gridy = 2;
		go.gridwidth = 3;
		go.weightx = 1.0; go.weighty = 1.0;
		go.fill = GridBagConstraints.BOTH;
		go.insets = new Insets(12, 12, 12 , 12);
		add(scroll, go);
	}
	
	private GridBagConstraints baseGbc()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		return gbc;
	}
}
