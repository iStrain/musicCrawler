
/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class MusicLibrary extends JFrame {
    private String fileName; // Filename for save file (required parameter for
			     // Constructor)
    private Artist[] artistArray; // Array of Artist objects
    private int numArtists; // Current number of Artist objects in artistArray[]

    private JDialog frame; // Top-level Container for initialisation code
    private JPanel contents; // New Contents Pane for JDialog frame
    private MyTextArea text; // extended JTextArea for JPanel.CENTER contents
			     // pane - enables watermark
    private JPanel endPanel; // JPanel for JPanel.PAGE_END contents pane
    private JProgressBar bar; // JProgressBar for endPanel (left)
    private JSlider slider; // JSlider for watermark fade
    private JButton button; // JButton OK button for endPanel (center)
    private JLabel cLabel; // JLabel copyright label for endPanel (right)

    static int totalArtists; // Total number of artists loaded, scanned, saved,
			     // etc
    static int totalAlbums; // Total number of albums loaded, scanned, saved,
			    // etc
    static int totalSongs; // Total number of songs loaded, scanned, saved, etc

    // Constructor method for MusicLibrary objects
    public MusicLibrary(String fileName) {
	this.fileName = fileName;

	// Use the Nimbus vector graphics Look & Feel
	try {
	    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		if ("Nimbus".equals(info.getName())) {
		    UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	} catch (Exception e) {
	    System.out.println(e);
	}
	// Create new JDialog for setup tasks
	frame = new JDialog(null, "Music Library v 0.50", Dialog.ModalityType.MODELESS);
	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	// Create new ContentPane for JDialog
	contents = new JPanel(new BorderLayout());
	contents.setBorder(new EmptyBorder(5, 5, 5, 5));

	this.text = createTextArea();
	contents.add(text, BorderLayout.CENTER);
	this.endPanel = createEndPanel();
	contents.add(endPanel, BorderLayout.PAGE_END);

	// Add ContentPane to JDialog and display
	frame.setContentPane(contents);
	frame.pack();
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);

	// Load MusicLibrary from file (if one exists)
	text.append("Looking for library file: " + this.fileName + ".\n");
	if (loadLibrary()) {
	    text.append("Found library file: " + this.fileName + ".\n");
	    text.append("Loading artist, album and song information from library file: " + this.fileName + ".\n");
	    text.append("Loaded " + totalArtists + " artists, " + totalAlbums + " albums and " + totalSongs
		    + " songs from library file: " + this.fileName + ".\n");

	    // Otherwise, scan the user's system for music files
	} else {
	    this.numArtists = 0;
	    this.artistArray = new Artist[this.numArtists];

	    text.append("Did not find library file: " + this.fileName + ".\n");
	    text.append(
		    "If this is the first time you've run the program, don't worry: we're not expecting a library file.\n");
	    text.append("We'll scan your system and create a new library file for you.\n");
	    text.append("Scanning for mp3 and wav files.");
	    try {
		new SongCrawler(this, text, "*.{mp3,wav}");
	    } catch (Exception e) {
		text.append(e.getMessage() + "\n");
	    }
	    // Sort the list of artists
	    text.append("Sorting your Music Library.\n");
	    sortLibrary();
	    text.append("Finished sorting your Music Library.\n");
	    if (saveLibrary()) {
		text.append("Saved " + numArtists + " artists to library file: " + this.fileName + ".\n");
	    }
	}
	text.append("Press the \"OK\" button, or close this window, to see your music.\n");

	// Wait until the user pushes the "OK" button
	frame.setVisible(false);
	frame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	frame.setVisible(true);

	// Display the main user interface (UI)
	new MLUI(this);
    }

    // Create new TextArea to display results of setup tasks
    private MyTextArea createTextArea() {
	text = new MyTextArea("", 27, 45);
	text.setEditable(false);
	return text;
    }

    // Create "OK" button for user
    private JButton createButton() {
	button = new JButton("OK");
	button.setMnemonic(KeyEvent.VK_O);
	button.setActionCommand("OK_OPTION");
	button.setBorder(new EmptyBorder(5, 5, 5, 5));
	button.setToolTipText("Press OK to continue");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("OK_OPTION")) {
		    frame.setVisible(false);
		    frame.dispose();
		}
	    }
	});
	button.setPreferredSize(new Dimension(80, 40));
	return button;
    }

    // Create progress bar for load, save, sort
    @SuppressWarnings("unused")
    private JProgressBar createBar() {
	bar = new JProgressBar(0, 100);
	bar.setValue(0);
	bar.setStringPainted(true);
	bar.setIndeterminate(true);
//	bar.setMaximum(newLength);
//	bar.setValue(newValue);
//	bar.setIndeterminate(false);
	return bar;
    }

    // Create slider for watermark fade
    private JSlider createSlider() {
	slider = new JSlider(0, 100, text.getBackground().getAlpha() * 100 / 255);
	Hashtable<Integer, JComponent> table = slider.createStandardLabels(25);
	int i = 0;
	while (i < 5) {
	    table.put(i * 25, new JLabel(Float.toString(((float) (i)) / 4.0f)));
	    i++;
	}
	slider.setLabelTable(table);
	slider.setPaintLabels(true);
	slider.setMajorTickSpacing(25);
	slider.setMinorTickSpacing(5);
	slider.setPaintTicks(true);
	slider.setSnapToTicks(true);
	slider.setBorder(new EmptyBorder(5, 10, 5, 10));
	slider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		text.setBackground(new Color(1.0f, 1.0f, 1.0f, ((float) (source.getValue())) / 100.0f));
	    }
	});
	return slider;
    }

    private JLabel createCopyrightLabel() {
	cLabel = new JLabel(
		"<html><center>&copy 2018 DunJen Software</center><center>All Rights Reserved</center></html>",
		SwingConstants.CENTER);
	Font f = cLabel.getFont();
	cLabel.setFont(new Font(f.getFontName(), f.getStyle(), f.getSize() - 2));
	return cLabel;
    }

    // Create End Panel for Progress Bar, OK Button and Copyright Label
    private JPanel createEndPanel() {
	this.endPanel = new JPanel(new GridLayout(1, 3));
//	this.bar = createBar();
//	endPanel.add(this.bar);
	this.slider = createSlider();
	endPanel.add(this.slider);
	this.button = createButton();
	endPanel.add(this.button);
	this.cLabel = createCopyrightLabel();
	endPanel.add(cLabel);
	return endPanel;
    }

    // Accessor method to get all the Artist names for a MusicLibrary
    // Returns a String array containing the names
    public String[] getArtistArray() {
	String[] s = new String[numArtists];
	int i = 0;
	while (i < numArtists) {
	    s[i] = artistArray[i].getName();
	    i++;
	}
	return s;
    }

    // Accessor method to get complete Song array for an Album
    // Ignores Artist - so Songs can be spread across multiple Albums
    // ... incomplete ...
    public String[] getCompleteSongArray(String albumName) {
	Album a = new Album(albumName); // Create a new Album to hold the
					// results
	int i = 0;
	while (i < this.numArtists) {
	    if (artistArray[i].getAlbumByName(albumName) != null) {
		Album b = artistArray[i].getAlbumByName(albumName);
		int j = 0;
		while (j < b.getNumSongs()) {
		}
	    }
	}
	return a.getSongArray();
    }

    // Find an Artist given their name
    // Returns the Artist object if the name was found in
    // MusicLibrary.artistArray[]
    // Returns null otherwise
    public Artist getArtistByName(String name) {
	boolean found = false;
	int i = 0;
	while (i < numArtists && !found) {
	    found = artistArray[i++].getName().equalsIgnoreCase(name);
	}
	if (found) {
	    return artistArray[i - 1];
	} else {
	    return null;
	}
    }

    // Add an Artist to an existing MusicLibrary
    // If MusicLibrary.artistArray[] is full, double the size and add 1
    // Returns the new Artist object
    public Artist addArtist(String name) {
	if (this.numArtists == this.artistArray.length) {
	    Artist[] newArray = new Artist[this.artistArray.length * 2 + 1];
	    int i = 0;
	    while (i < this.numArtists) {
		newArray[i] = this.artistArray[i];
		i++;
	    }
	    this.artistArray = newArray;
	}
	this.artistArray[this.numArtists] = new Artist(name);
	return this.artistArray[this.numArtists++];
    }

    // Sort the MusicLibrary by Artist.name
    public void sortLibrary() {
	Arrays.sort(this.artistArray, 0, numArtists);
    }

    // Load a MusicLibrary from file
    public boolean loadLibrary() {
	BufferedReader inFile = null;
	try {
	    inFile = new BufferedReader(new FileReader(this.fileName));

	    // Get number of Artists in our MusicLibrary
	    this.numArtists = Integer.parseInt(inFile.readLine());
	    this.artistArray = new Artist[this.numArtists];

	    // For each Artist ...
	    int countArtists = 0;
	    while (countArtists < this.numArtists) {
		this.artistArray[countArtists++] = new Artist(inFile);
	    }
	    inFile.close();
	    totalArtists = this.numArtists;
	    return true;
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return false;
	}
    }

    // Save a MusicLibrary to file
    public boolean saveLibrary() {
	BufferedWriter outFile = null;
	try {
	    outFile = new BufferedWriter(new FileWriter(this.fileName));
	    outFile.write(this.toString());
	    outFile.close();
	    return true;
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    return false;
	}
    }

    // Overrides java.lang.Object.toString() so we can print a MusicLibrary
    public String toString() {
	String str = this.numArtists + "\n";
	int countArtists = 0;
	while (countArtists < this.numArtists) {
	    str += this.artistArray[countArtists++].toString();
	}
	return str;
    }

    public static void main(String[] args) {
	new MusicLibrary("Library.csv");
    }
}
