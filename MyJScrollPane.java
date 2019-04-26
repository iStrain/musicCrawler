
/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

@SuppressWarnings("serial")
class MyJScrollPane extends JScrollPane {
    private Image image;
    private int imageWidth;
    private int imageHeight;

    public MyJScrollPane() {
	this(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public MyJScrollPane(int vsbPolicy, int hsbPolicy) {
	this(null, vsbPolicy, hsbPolicy);
    }

    public MyJScrollPane(Component view) {
	this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public MyJScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
	super(view, vsbPolicy, hsbPolicy);
	if (view instanceof JComponent) {
	    ((JComponent) view).setBackground(new Color(0, 0, 0, 0));
//	    ((JComponent) view).setOpaque(false);
	}
	this.setBackground(new Color(0, 0, 0, 0));
	this.setOpaque(false);
	this.getViewport().setBackground(new Color(1.0f, 0.0f, 1.0f, 1.0f));
	this.getViewport().setOpaque(true);
	this.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
    }

    public void setBackgroundImage(Image image) {
	this.image = image;
	this.imageWidth = image.getWidth(null);
	this.imageHeight = image.getHeight(null);
    }

    public void paint(Graphics g) {
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
	super.paint(g);
    }
}
