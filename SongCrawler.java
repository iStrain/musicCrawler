/*
 * Simple Music Library program
 * by Duncan Baxter & Jennifer Kennedy
 * DunJen Software
 *
 * This code based on an example that is (c) 2009, Oracle and/or its affiliates.
 * All rights with respect to the example code remain reserved by them.
 * ... and, of course, we appreciate its assistance when writing this program
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

import javax.swing.JTextArea;

/**
 * Finds files that match the specified glob pattern. For more information on
 * what constitutes a glob pattern, see:
 * http://docs.oracle.com/javase/javatutorials/tutorial/essential/io/fileOps.html#glob
 *
 * The grandparent directory, parent directory and filename from matching files
 * are used as defaults for artist, album and title to add files to the
 * MusicLibrary.
 * 
 * The number of matches is also printed.
 */

public class SongCrawler {
    private MusicLibrary ml;
    private JTextArea text;
    private Iterable<Path> dirs;
    private Finder finder;
    private int numMatches;
    private int newArtists;
    private int newAlbums;
    private int newSongs;

    public SongCrawler(MusicLibrary ml, JTextArea text, String pattern) throws IOException {
	this.ml = ml;
	this.text = text;
	this.dirs = FileSystems.getDefault().getRootDirectories();
	this.finder = new Finder(pattern);
	this.numMatches = 0;
	this.newArtists = 0;
	this.newAlbums = 0;
	this.newSongs = 0;

	for (Path name : this.dirs) {
	    Files.walkFileTree(name, this.finder);
	}
	this.text.append("\nFinished scanning your system for music files.\n");
	this.text.append("Found " + this.numMatches+" music files.\n");
	this.text.append(
		"Added " + this.newArtists + " artists, " + this.newAlbums + " albums and " + this.newSongs + " songs to your Music Library.\n");
    }

    /**
     * A {@code FileVisitor} that finds all files that match the specified pattern.
     */
    public class Finder extends SimpleFileVisitor<Path> {
	private final PathMatcher matcher;

	Finder(String pattern) {
	    this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
	}

	// Remove leading and trailing spaces from names
	// Convert "The <name>" to "<name>, The" (except for "The The")
	String clean(String str) {
	    str = str.trim();
	    if (str.startsWith("The ") && !str.equals("The The")) {
		str = str.substring(4) + ", The";
	    }
	    return str;
	}

	// Compares the glob pattern against the file name
	// Adds new matches to the MusicLibrary
	// Derives artist, album and title from grandparent and parent directory, and
	// filename
	void find(Path file) {
	    String artist, album, title;
	    String[] split;
	    try {
		Path name = file.getFileName();
		if (matcher.matches(name)) {
		    // Derive artist, album and title from directory and file names
		    artist = clean(file.getName(file.getNameCount() - 3).toString());
		    album = clean(file.getName(file.getNameCount() - 2).toString());
		    title = name.toString();
		    title = title.substring(0, title.lastIndexOf("."));

		    if (artist.contains("CDs") || artist.contains("Musicals") || artist.contains("Miscellaneous") || artist.contains("Soundtracks")
			    || artist.contains("Unknown") || artist.contains("Various")) {
			artist = album;
		    }
		    if (album.equals("Tracks") || album.endsWith("- Tracks")) {
			split = title.split(" - ");
			try {
			    Integer.parseInt(clean(split[0]));
			    artist = clean(split[1]);
			} catch (NumberFormatException nfe) {
			    artist = clean(split[0]);
			}
			album = artist + " [Other]";
			title = clean(split[split.length - 1]);
		    }
		    Artist ar = ml.getArtistByName(artist);
		    if (ar == null) {
			ar = ml.addArtist(artist);
			newArtists++;
		    }
		    Album al = ar.getAlbumByName(album);
		    if (al == null) {
			al = ar.addAlbum(album);
			newAlbums++;
		    }
		    Song s = al.getSongByTitle(title);
		    if (s == null) {
			s = al.addSong(title, file.toString());
			newSongs++;
		    }
		    numMatches++;
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