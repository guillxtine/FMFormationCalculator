package main;

import java.awt.Dimension;

import graphics.Panel;
import graphics.Window;

public class Main {

	public static void main(String[] args)
	{
		Panel panel = new Panel();
		panel.addHomePageButtons();
		Window window = new Window("FM24 Formation Calculator", panel);
	}

}
