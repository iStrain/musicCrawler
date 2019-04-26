
/*
 * Based on a code example that is (c) 2009, Oracle and/or its affiliates.
 * All rights with respect to the example remain reserved by them.
 */

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Finds files that match the specified glob pattern. For more information on
 * what constitutes a glob pattern, see:
 * http://docs.oracle.com/javase/javatutorials/tutorial/essential/io/fileOps.html#glob
 *
 * Test ignores files < 2 megabytes in size because they are unlikely to be
 * genuine "songs".
 *
 * The artist, album and title metadata (if present) from matching files are
 * used to add those files to the MusicLibrary. If the metadata for a file are
 * incomplete, the grandparent directory, parent directory and/or filename are
 * used as defaults for artist, album and/or title (as required).
 * 
 * The number of matches is also printed.
 */

public class MetaSongCrawler {
    private MusicLibrary ml;
    private Iterable<Path> dirs;
    private Finder finder;
    private int numMatches;
    private int numAdded;

    public MetaSongCrawler(MusicLibrary ml, String pattern) throws IOException {
	@SuppressWarnings("unused")
	final JFXPanel fxPanel = new JFXPanel(); // Needed to initialise Media toolkit
	this.ml = ml;
	this.dirs = FileSystems.getDefault().getRootDirectories();
	this.finder = new Finder(pattern);
	this.numMatches = 0;
	this.numAdded = 0;
	for (Path name : this.dirs) {
	    Files.walkFileTree(name, this.finder);
	}
	System.out.println("Matched: " + this.numMatches);
	System.out.println("Added: " + this.numAdded);
    }

    /**
     * A {@code FileVisitor} that finds all files that match the specified pattern.
     */
    public class Finder extends SimpleFileVisitor<Path> {
	private final int MIN_BYTES;
	private final PathMatcher matcher;

	Finder(String pattern) {
	    this.MIN_BYTES = 2 << 20;
	    this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
	}

	String clean(String str) {
	    str = str.trim();
	    return str;
	}

	// Ignores files shorter than 2 megabytes (unlikely to be genuine "songs")
	// Compares the glob pattern against the file name
	// Extracts metadata, uses default/metadata per rules above
	// Adds new matches to the MusicLibrary
	void find(Path file) {
	    try {
		if (file.toFile().length() > MIN_BYTES) {
		    Path name = file.getFileName();
		    if (name != null && matcher.matches(name)) {
			Media m = new Media(file.toUri().toString());
			MediaPlayer mp = new MediaPlayer(m);
			mp.setOnReady(new Runnable() {

			    @Override
			    public void run() {
				String artist, album, title, data;
				Map<String, Object> map = m.getMetadata();

				artist = file.getName(file.getNameCount() - 3).toString();
				if (map.containsKey("artist")) {
				    data = (String) (map.get("artist"));
				    if (!data.equals("Unknown Artist")) {
					artist = data;
				    }
				}
				artist = clean(artist);
				Artist ar = ml.getArtistByName(artist);
				if (ar == null) {
				    ar = ml.addArtist(artist);
				    numAdded++;
				}
				if (map.containsKey("album")) {
				    album = (String) (map.get("album"));
				} else {
				    album = file.getName(file.getNameCount() - 2).toString();
				}
				album = clean(album);
				Album al = ar.getAlbumByName(album);
				if (al == null) {
				    al = ar.addAlbum(album);
				}
				if (map.containsKey("title")) {
				    title = (String) (map.get("title"));
				} else {
				    title = name.toString();
				}
				title = clean(title);
				Song s = al.getSongByTitle(title);
				if (s == null) {
				    s = al.addSong(title, file.toString());
				}
				mp.dispose();
			    }
			});
			numMatches++;
		    }
		}
	    } catch (Exception e) {
		e.getMessage();
	    }
	}

	// Invoke the pattern matching method on each file
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
	    find(file);
	    return FileVisitResult.CONTINUE;
	}

	// Invoke the pattern matching method on each directory.
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
	    find(dir);
	    return CONTINUE;
	}

	// IOException! Print the IOException information and move on ...
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException IOe) {
	    System.err.println(IOe);
	    return FileVisitResult.CONTINUE;
	}
    }
}