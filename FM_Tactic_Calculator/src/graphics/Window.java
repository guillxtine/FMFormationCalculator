package graphics;

import java.awt.Image;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Window extends JFrame
{	
	public String title = "";
	
	public Window(String title, JPanel panel)
	{
		this.title = title;
		
		// add panel to 
		add(panel);
		pack();
		
		setIconImages(loadAppIcons());
		
		// set attributes for the window
		setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private static List<Image> loadAppIcons()
	{
		try
		{
			Class<?> anchor = Window.class;
			return List.of(
						ImageIO.read(Objects.requireNonNull(anchor.getResource("/resources/icons/app-16.png"))),
						ImageIO.read(Objects.requireNonNull(anchor.getResource("/resources/icons/app-32.png"))),
						ImageIO.read(Objects.requireNonNull(anchor.getResource("/resources/icons/app-48.png"))),
						ImageIO.read(Objects.requireNonNull(anchor.getResource("/resources/icons/app-256.png")))
					);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not load app icons", e);
		}
	}
}
