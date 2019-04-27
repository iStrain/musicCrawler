
/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

@SuppressWarnings("serial")
class MyScrollPane extends JScrollPane {
    private Image image;
    private int imageWidth;
    private int imageHeight;

    public MyScrollPane(Component view, int a, int b) {
	super(view, a, b);
	this.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.5f));
	this.setOpaque(true);
	this.getViewport().setBackground(new Color(0, 0, 0, 0));
	this.getViewport().setOpaque(false);
	this.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
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
	    Rectangle rect = this.getViewport().getBounds();
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
