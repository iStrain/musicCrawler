
/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Dictionary;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MLUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int V_SCROLL = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
    private static final int H_SCROLL = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;

    private MusicLibrary ml;
    private DefaultListCellRenderer cr;
    private JPanel jc;
    private JMenuBar menuBar;
    private JPanel startPanel;
    private JPanel centrePanel;
    private JTabbedPane artistPane;
    private JList<String>[] artistList;
    private MyScrollPane[] artistScroll;
    private MyScrollPane albumPane;
    private JList<String> albumList;
    private MyScrollPane songPane;
    private JList<String> songList;
    private static JPanel endPanel;
    private JFXPanel fxPanel;
    private static MediaPlayer mp;
    private JLabel locationLabel;

    private int playerChoice;
    private String player;

    private String[][] artists;
    private String artistItem;
    private String albumItem;
    private String songItem;

    // Create a 3-tiered JList linking Artists, Albums and Songs
    // Use Java MediaPlayer or VLC to play selected Songs
    public MLUI(MusicLibrary ml) {
	this.ml = ml;
	this.cr = new DefaultListCellRenderer();
	this.cr.setBackground(new Color(0, 0, 0, 0));
	this.cr.setOpaque(false);

	// Change the 0 to 1 if you prefer to use VLC rather than the Java MediaPlayer
	this.playerChoice = 0;
	if (this.playerChoice == 1) {
	    this.player = "\"C:\\Program Files\\VideoLAN\\VLC\\vlc.exe\"";
	}
	this.fxPanel = new JFXPanel();

	this.setTitle("Music Library");
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.menuBar = createMenu();
	this.setJMenuBar(menuBar);
	this.jc = (JPanel) (this.getContentPane());
	this.startPanel = createStartPanel();
	this.jc.add(startPanel, BorderLayout.PAGE_START);
	this.centrePanel = createCentrePanel();
	this.jc.add(centrePanel, BorderLayout.CENTER);
	endPanel = createEndPanel();
	this.jc.add(endPanel, BorderLayout.PAGE_END);

	this.artistPane.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		int selection = artistPane.getSelectedIndex();
		artistItem = artistList[selection].getSelectedValue();
		if (artists[selection].length == 0) {
		    artistItem = null;
		    albumList.setListData(new String[0]);
		    albumItem = null;
		} else {
		    artistItem = artists[selection][0];
		    artistList[selection].setSelectedIndex(0);
		    albumList.setListData(ml.getArtistByName(artistItem).getAlbumArray());
		    albumList.setSelectedIndex(0);
		    albumItem = albumList.getModel().getElementAt(0);
		}
	    }
	});
	this.pack();
	this.setVisible(true);
    }

    /*
     * Return input array, split into 27 sub-arrays (one for each tab on the
     * artistPane). Split ignores case (ASCII chars < #65 ('A') are allocated to tab
     * 0). Handles empty tabs - returns a zero-length array for that tab.
     */
    public String[][] getTabArray(String[] artistArray) {
	String[][] str = new String[27][];

	// Find the first Artist whose name begins with "A" or "a"
	int tabNum = 0; // Number of the current tab (0 to 26)
	char tabChar = 'A'; // Initial character for a tab
	int walker = 0; // Steps through the input array one element at a time
	int base = 0; // First element of the current tab (in input array)
	int i; // Steps through a sub-array of the output array (when copying)

	// Note: subtracting 32 from the ASCII code for a lower case character, switches
	// it to upper case eg. #97 ('a') - 32 = #65 ('A') - so masking bit 6 is
	// equivalent to "toUpper()" ... and yes, this was the source of THE most
	// intractable bug so far - took 2 days to find, 5 seconds to fix
	while (walker < artistArray.length && (artistArray[walker].charAt(0) & (255 - 32)) != tabChar) {
	    walker++;
	}
	str[0] = new String[walker];
	while (base < walker) {
	    str[0][base] = artistArray[base];
	    base++;
	}
	tabNum++;

	// Find the first Artist whose name does not begin with this tab's initial
	// letter (again, search ignores case)
	while (tabNum < 27) {
	    while (walker < artistArray.length && (artistArray[walker].charAt(0) & (255 - 32)) == tabChar) {
		walker++;
	    }
	    str[tabNum] = new String[walker - base]; // Instantiate the sub-array for current tab
	    i = 0;
	    while (base < walker) {
		str[tabNum][i] = artistArray[base];
		i++;
		base++;
	    }
	    tabNum++;
	    tabChar++;
	}
	return str;
    }

    // Clean a string for use as a URI
    // Replaces instances of " " with "%20"
    // Replaces instances of "\" with "/"
    public String clean(String str) {
	str = str.replaceAll(" ", "%20");
	str = str.replaceAll("\\\\", "/");
	return str;
    }

    // Adjust Font size for a JLabel Object
    // + values -> bigger, - values -> smaller
    private void adjustFont(JLabel label, int change) {
	Font f = label.getFont();
	label.setFont(new Font(f.getFontName(), f.getStyle(), f.getSize() + change));
    }

    // MediaPlayer object mp will not be cleaned away while playMusic() holds a
    // reference to it!
    public static void playMusic() {
	mp.setAutoPlay(true);
    }

    // Instantiate components for main user interface
    // Create menu bar
    @SuppressWarnings({ "serial" })
    private JMenuBar createMenu() {
	JMenuBar menuBar = new JMenuBar();

	// "File" menu contains "Load", "Save"
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic(KeyEvent.VK_F);
	menuBar.add(fileMenu);

	Action action = new AbstractAction("Load") {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("Load");
		// Call method to implement action;
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
	fileMenu.add(action);

	action = new AbstractAction("Save") {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("Save");
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
	fileMenu.add(action);

	// "Search" menu contains "Search", "Sort"
	JMenu searchMenu = new JMenu("Search");
	searchMenu.setMnemonic(KeyEvent.VK_S);
	menuBar.add(searchMenu);

	action = new AbstractAction("Search") {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("Search");
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
	searchMenu.add(action);

	action = new AbstractAction("Sort") {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("Sort");
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
	searchMenu.add(action);

	// "Window" menu is EMPTY
	JMenu windowMenu = new JMenu("Window");
	windowMenu.setMnemonic(KeyEvent.VK_W);
	menuBar.add(windowMenu);

	// "Help" menu contains "Help", "About"
	JMenu helpMenu = new JMenu("Help");
	helpMenu.setMnemonic(KeyEvent.VK_H);
	menuBar.add(helpMenu);

	action = new AbstractAction("Help") {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("Help");
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
	action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
	helpMenu.add(action);

	action = new AbstractAction("About") {
	    public void actionPerformed(ActionEvent e) {
		System.out.println("About");
	    }
	};
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
	helpMenu.add(action);
	return menuBar;
    }

    // Create JTabbedPane for artistArray
    // Create and add JList & JScrollPane for each tab
    @SuppressWarnings("unchecked")
    private JTabbedPane createArtistPane() {
	this.artistList = new JList[27];
	this.artistScroll = new MyScrollPane[27];
	this.artistPane = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
	this.artistPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
	this.artistPane.setOpaque(false);
	this.artists = getTabArray(this.ml.getArtistArray());
	String tabChar = "?";
	byte i = 0;
	while (i < 27) {
	    if (i > 0) {
		tabChar = String.valueOf((char) (i + 64));
	    }
	    artistList[i] = new JList<String>(artists[i]);
	    artistList[i].setBackground(new Color(0, 0, 0, 0));
	    artistList[i].setOpaque(false);
	    artistList[i].setCellRenderer(cr);
	    artistList[i].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    artistList[i].addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    if (!e.getValueIsAdjusting()) {
			artistItem = ((JList<String>) e.getSource()).getSelectedValue();
			if (artistItem != null && albumList != null) {
			    albumList.setListData(ml.getArtistByName(artistItem).getAlbumArray());
			    albumList.setSelectedIndex(0);
			    albumItem = albumList.getModel().getElementAt(0);
			}
		    }
		}
	    });

	    artistList[i].setVisibleRowCount(32);
	    artistScroll[i] = new MyScrollPane(artistList[i], V_SCROLL, H_SCROLL);
	    artistPane.add(tabChar, artistScroll[i]);
	    i++;
	}

	// Select the first non-empty alphabetical tab (or 'A' if all empty)
	i = 1;
	while (i < 27 && artists[i].length == 0) {
	    i++;
	}
	if (i == 27) {
	    i = 1;
	}
	artistPane.setSelectedIndex(i);
	artistList[i].setSelectedIndex(0);
	artistItem = artistList[i].getModel().getElementAt(0);
	return artistPane;
    }

    // Create JList and JScrollPane for albumArray[]
    private MyScrollPane createAlbumPane() {
	albumList = new JList<String>(this.ml.getArtistByName(artistItem).getAlbumArray());
	albumList.setBackground(new Color(0, 0, 0, 0));
	albumList.setOpaque(false);
	albumList.setCellRenderer(cr);
	albumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	albumList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    albumItem = albumList.getSelectedValue();
		    if (albumItem == null) {
			songList.setListData(new String[0]);
			songItem = null;
		    } else {
			songList.setListData(ml.getArtistByName(artistItem).getAlbumByName(albumItem).getSongArray());
			songItem = songList.getModel().getElementAt(0);
			if (songItem != null) {
			    locationLabel.setText(ml.getArtistByName(artistItem).getAlbumByName(albumItem)
				    .getSongByTitle(songItem).getLocation());
			}
		    }
		}
	    }
	});
	albumItem = albumList.getModel().getElementAt(0);
	albumPane = new MyScrollPane(albumList, V_SCROLL, H_SCROLL);
	albumPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
	return albumPane;
    }

    // Create JList and JScrollPane for songArray[]
    private MyScrollPane createSongPane() {
	songList = new JList<String>(this.ml.getArtistByName(artistItem).getAlbumByName(albumItem).getSongArray());
	songList.setBackground(new Color(0, 0, 0, 0));
	songList.setOpaque(false);
	songList.setCellRenderer(cr);
	songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	songList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
		    songItem = songList.getSelectedValue();
		    if (songItem != null) {
			String location = ml.getArtistByName(artistItem).getAlbumByName(albumItem)
				.getSongByTitle(songItem).getLocation();
			locationLabel.setText(location);
			if (playerChoice == 0) {
			    Media m = new Media(new File(location).toURI().toASCIIString());
			    mp = new MediaPlayer(m);
			    createControls(mp);
			    playMusic();
			} else if (playerChoice == 1) {
			    try {
				Runtime.getRuntime().exec(new String[] { player, "--qt-start-minimized",
					"\"" + location + "\"", "vlc://quit" });
			    } catch (IOException IOe) {
				IOe.getMessage();
			    }
			}
		    }
		}
	    }
	});
	songItem = songList.getModel().getElementAt(0);
	songPane = new MyScrollPane(songList, V_SCROLL, H_SCROLL);
	songPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
	return songPane;
    }

    // Create slider for MediaPlayer time control
    private JSlider createTimeSlider() {
	JSlider slider = new JSlider(0, 100, 0);
	Double start = mp.getStartTime().toSeconds();
	Double stop = mp.getStopTime().toSeconds();
	int duration = ((int) (stop - start));
	int durationMins = duration / 60;
	int durationSecs = duration % 60;

	slider.setMaximum(duration);
	slider.setMajorTickSpacing(60);
	slider.setMinorTickSpacing(15);
	slider.setPaintTicks(true);
	Dictionary<Integer, JComponent> table = slider.createStandardLabels(60);
	int i = 0;
	while (i <= durationMins) {
	    JLabel label = new JLabel(String.format("%d.00", i));
	    adjustFont(label, -2);
	    table.put(i * 60, label);
	    i++;
	}
	if (durationSecs > 45) {
	    JLabel label = new JLabel(String.format("%d.%d", durationMins, durationSecs));
	    adjustFont(label, -2);
	    table.put(duration, label);
	}
	slider.setLabelTable(table);
	slider.setPaintLabels(true);

	Timer t = new Timer(15, new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (!slider.getValueIsAdjusting()) {
		    slider.setValue((int) (mp.getCurrentTime().toSeconds()));
		}
	    }
	});
	t.start();

	slider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		if (slider.getValueIsAdjusting()) {
		    mp.seek(Duration.seconds((double) (slider.getValue())));
		}
	    }
	});
	return slider;
    }

    // Create Play/Pause button for MediaPlayer
    private JButton createPlayButton(ImageIcon playIcon, ImageIcon pauseIcon) {
	JButton button = new JButton(pauseIcon);
	button.setActionCommand("Pause");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Pause")) {
		    mp.pause();
		    button.setActionCommand("Play");
		    button.setIcon(playIcon);
		} else {
		    mp.play();
		    button.setActionCommand("Pause");
		    button.setIcon(pauseIcon);
		}
	    }
	});
	return button;
    }

    // Create Stop button for MediaPlayer
    private JButton createStopButton(ImageIcon stopIcon, JButton play, ImageIcon playIcon) {
	JButton button = new JButton(stopIcon);
	button.setActionCommand("Stop");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		mp.stop();
		play.setActionCommand("Play");
		play.setIcon(playIcon);
	    }
	});
	return button;
    }

    // Create Rewind button for MediaPlayer
    private JButton createRewButton(ImageIcon rewIcon) {
	JButton button = new JButton(rewIcon);
	button.setActionCommand("Rewind");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		mp.seek(mp.getCurrentTime().subtract(new Duration(10000.0d)));
	    }
	});
	return button;
    }

    // Create Fast Forward button for MediaPlayer
    private JButton createFwdButton(ImageIcon fwdIcon) {
	JButton button = new JButton(fwdIcon);
	button.setActionCommand("Fwd");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		mp.seek(mp.getCurrentTime().add(new Duration(10000.0d)));
	    }
	});
	return button;
    }

    // Create Start button for MediaPlayer
    private JButton createStartButton(ImageIcon startIcon) {
	JButton button = new JButton(startIcon);
	button.setActionCommand("Start");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		mp.seek(mp.getStartTime());
	    }
	});
	return button;
    }

    // Create End button for MediaPlayer
    private JButton createEndButton(ImageIcon endIcon, JButton play, ImageIcon playIcon) {
	JButton button = new JButton(endIcon);
	button.setActionCommand("End");
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		mp.stop();
		mp.seek(mp.getStartTime());
		play.setActionCommand("Play");
		play.setIcon(playIcon);
	    }
	});
	return button;
    }

    // Create slider for MediaPlayer volume control
    private JSlider createVolSlider() {
	JSlider slider = new JSlider(0, 100);
	slider.setMajorTickSpacing(25);
	slider.setMinorTickSpacing(5);
	slider.setPaintTicks(true);
	Dictionary<Integer, JComponent> table = slider.createStandardLabels(25);
	int i = 0;
	while (i <= 4) {
	    JLabel label = new JLabel(String.format("%d%%", i * 25));
	    adjustFont(label, -2);
	    table.put(i * 25, label);
	    i++;
	}
	slider.setLabelTable(table);
	slider.setPaintLabels(true);

	slider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		if (slider.getValueIsAdjusting()) {
		    mp.setVolume(slider.getValue() / 100.0);
		}
	    }
	});
	return slider;
    }

    // Create controls for Media Player object
    private void createControls(MediaPlayer mp) {
	ImageIcon startIcon = new ImageIcon("sm_media_skip_backward.png");
	ImageIcon rewIcon = new ImageIcon("sm_media_seek_backward.png");
	ImageIcon playIcon = new ImageIcon("sm_media_playback_start.png");
	ImageIcon pauseIcon = new ImageIcon("sm_media_playback_pause.png");
	ImageIcon stopIcon = new ImageIcon("sm_media_playback_stop.png");
	ImageIcon fwdIcon = new ImageIcon("sm_media_seek_forward.png");
	ImageIcon endIcon = new ImageIcon("sm_media_skip_forward.png");

	mp.setOnReady(new Runnable() {
	    @Override
	    public void run() {
		JFrame mpFrame = new JFrame(mp.getMedia().getSource());
		JPanel mpContents = ((JPanel) mpFrame.getContentPane());
		JButton play = createPlayButton(playIcon, pauseIcon);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(createTimeSlider());

		JPanel midPanel = new JPanel();
		midPanel.setLayout(new GridLayout(1, 6));
		midPanel.add(createStartButton(startIcon));
		midPanel.add(createRewButton(rewIcon));
		midPanel.add(play);
		midPanel.add(createStopButton(stopIcon, play, playIcon));
		midPanel.add(createFwdButton(fwdIcon));
		midPanel.add(createEndButton(endIcon, play, playIcon));

		JPanel endPanel = new JPanel();
		endPanel.setLayout(new BoxLayout(endPanel, BoxLayout.X_AXIS));
		endPanel.add(createVolSlider());

		mpContents.setLayout(new FlowLayout());
		mpContents.add(topPanel);
		mpContents.add(new JLabel("TIME", SwingConstants.CENTER));
		mpContents.add(midPanel);
		mpContents.add(new JLabel("VOLUME", SwingConstants.CENTER));
		mpContents.add(endPanel);
		mpFrame.pack();
		mpFrame.setLocationRelativeTo(MLUI.endPanel);
		mpFrame.setVisible(true);
		
		mp.setOnEndOfMedia(new Runnable() {
		    public void run() {
			mpFrame.dispose();
			mp.dispose();
		    }
		});
	    }
	});
    }

    // Create JLabel for song location
    private JLabel createLocationLabel() {
	JLabel rLabel = new JLabel("", SwingConstants.TRAILING);
	adjustFont(rLabel, -2);
	rLabel.setText(ml.getArtistByName(artistItem).getAlbumByName(albumItem).getSongByTitle(songItem).getLocation());
	return rLabel;
    }

    // Create Start Panel for JList/JScrollPane panel JLabel titles
    private JPanel createStartPanel() {
	JPanel panel = new JPanel(new GridLayout(1, 3));
	panel.setBorder(new EmptyBorder(5, 5, 5, 5));

	JLabel leftTitle = new JLabel("Artists", SwingConstants.CENTER);
	adjustFont(leftTitle, 1);
	panel.add(leftTitle);

	JLabel centreTitle = new JLabel("Albums", SwingConstants.CENTER);
	adjustFont(centreTitle, 1);
	panel.add(centreTitle);

	JLabel rightTitle = new JLabel("Songs", SwingConstants.CENTER);
	adjustFont(rightTitle, 1);
	panel.add(rightTitle);
	return panel;
    }

    // Create Centre Panel for JTabbedPane and 2 x JList/JScrollPane combos
    private JPanel createCentrePanel() {
	JPanel panel = new JPanel(new GridLayout(1, 3));
	this.artistPane = createArtistPane();
	panel.add(this.artistPane);
	this.albumPane = createAlbumPane();
	panel.add(this.albumPane);
	this.songPane = createSongPane();
	panel.add(this.songPane);
	return panel;
    }

    // Create End Panel for JLabel Song location
    private JPanel createEndPanel() {
	JPanel panel = new JPanel(new GridLayout(1, 3));
	panel.setBorder(new EmptyBorder(5, 5, 5, 5));
	panel.add(fxPanel);
	panel.add(new JLabel(""));
	this.locationLabel = createLocationLabel();
	panel.add(this.locationLabel);
	return panel;
    }

}
