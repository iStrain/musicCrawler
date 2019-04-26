import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public class SongList extends JFrame {
    private static final long serialVersionUID = 1L;
    public MusicLibrary ml;

    private JComboBox<String> artistComboBox;
    private JComboBox<String> albumComboBox;
    private JComboBox<String> songComboBox;

    private String artistItem;
    private String albumItem;
    private String songItem;

    // Create a 3-tiered JComboBox linking Artists, Albums and Songs
    // Use VLC to play selected Songs
    public SongList(MusicLibrary ml) {
	this.ml = ml;
	artistComboBox = new JComboBox<String>(ml.getArtistArray());
	artistItem = artistComboBox.getItemAt(0);
	// prevent action events from being fired when the up/down arrow keys are used
	artistComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
	getContentPane().add(artistComboBox, BorderLayout.NORTH);
	artistComboBox.addActionListener(new ArtistAL());

	albumComboBox = new JComboBox<String>(ml.getArtistByName(artistItem).getAlbumArray());
	albumItem = albumComboBox.getItemAt(0);
	getContentPane().add(albumComboBox, BorderLayout.CENTER);
	albumComboBox.addActionListener(new AlbumAL());

	songComboBox = new JComboBox<String>(ml.getArtistByName(artistItem).getAlbumByName(albumItem).getSongArray());
	getContentPane().add(songComboBox, BorderLayout.SOUTH);
	songComboBox.addActionListener(new SongAL());
    }

    // User selected an Artist:
    // Update albumComboBox to list the Albums by that Artist
    // Update songComboBox to list the Songs on the first listed Album
    public class ArtistAL implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    artistItem = (String) artistComboBox.getSelectedItem();
	    albumComboBox.setModel(new DefaultComboBoxModel<String>(ml.getArtistByName(artistItem).getAlbumArray()));
	    albumItem = albumComboBox.getItemAt(0);
	    songComboBox.setModel(new DefaultComboBoxModel<String>(
		    ml.getArtistByName(artistItem).getAlbumByName(albumComboBox.getItemAt(0)).getSongArray()));
	}
    }

    // User selected an Album:
    // Update songComboBox to list the Songs on that Album
    public class AlbumAL implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    albumItem = (String) albumComboBox.getSelectedItem();
	    songComboBox.setModel(new DefaultComboBoxModel<String>(
		    ml.getArtistByName(artistItem).getAlbumByName(albumItem).getSongArray()));
	}
    }

    // User selected a Song:
    // Use VLC to play that Song
    public class SongAL implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    String player = "\"C:\\Program Files\\VideoLAN\\VLC\\vlc.exe\"";
	    songItem = (String) songComboBox.getSelectedItem();
	    String location = ml.getArtistByName(artistItem).getAlbumByName(albumItem).getSongByTitle(songItem)
		    .getLocation();
	    try {
		Runtime.getRuntime()
			.exec(new String[] { player, "--qt-start-minimized", "\"" + location + "\"", "vlc://quit" });
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}
    }
}
