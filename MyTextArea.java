
/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class MyTextArea extends JTextArea {
    private Image image;
    private int imageWidth;
    private int imageHeight;

    public MyTextArea(String str, int a, int b) {
	super(str, a, b);
	this.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.5f));
	this.setOpaque(true);
	Toolkit tk = Toolkit.getDefaultToolkit();
	MediaTracker mt = new MediaTracker(this);
	this.image = tk.getImage(MusicLibrary.class.getResource("background.jpg"));
	mt.addImage(image, 0);
	try {
	    mt.waitForAll();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	this.imageWidth = image.getWidth(null);
	this.imageHeight = image.getHeight(null);
    }

    public void paintComponent(Graphics g) {
	if (this.image != null) {
	    Rectangle rect = this.getBounds();
	    if (rect.height > imageHeight) {
		g.clearRect(rect.x, rect.y, rect.width, rect.height - imageHeight);
	    }
	    if (rect.width > imageWidth) {
		g.clearRect(rect.x, rect.y, rect.width - imageWidth, rect.height);
	    }
	    g.drawImage(image, rect.width - imageWidth, rect.height - imageHeight, null);
	}
	super.paintComponent(g);
    }
}
