/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.io.BufferedReader;
import java.io.IOException;

public class Album {
    private String name; // name of Album
    private Song[] songArray; // array of Songs in Album
    private int numSongs; // number of Songs in Album.songArray[]

    // Constructor method for Album objects
    // Requires Album.name
    public Album(String name) {
	this.name = name;
	this.numSongs = 0;
	this.songArray = new Song[this.numSongs];
    }

    // Constructor for Album objects - all data is read from file
    public Album(BufferedReader inFile) throws IOException {
	// Name of Album
	this.name = inFile.readLine();

	// Number of Songs on that Album in our MusicLibrary
	this.numSongs = Integer.parseInt(inFile.readLine());
	this.songArray = new Song[numSongs];
	MusicLibrary.totalSongs += this.numSongs;

	// For each Song ...
	int countSongs = 0;
	while (countSongs < numSongs) {
	    songArray[countSongs++] = new Song(inFile);
	}
    }

    // Accessor method for Album name
    public String getName() {
	return this.name;
    }

    // Accessor method to get all the Song titles for an Album
    public String[] getSongArray() {
	String[] s = new String[this.numSongs];
	int i = 0;
	while (i < this.numSongs) {
	    s[i] = this.songArray[i].getTitle();
	    i++;
	}
	return s;
    }

    // Mutator method for Album name
    public void setName(String name) {
	this.name = name;
    }

    // Accessor method for Album numSongs
    public int getNumSongs() {
	return this.numSongs;
    }

    // Find a Song on an Album given its title
    // Returns null if Song not found in Album.songArray[]
    public Song getSongByTitle(String title) {
	boolean found = false;
	int i = 0;
	while (i < numSongs && !found) {
	    found = songArray[i].getTitle().equalsIgnoreCase(title);
	    i++;
	}
	if (found) {
	    return songArray[i - 1];
	} else {
	    return null;
	}
    }

    // Add a Song to an existing Album
    // If Album.songArray[] is full, increment the size of the array
    public Song addSong(String title, String location) {
	if (this.numSongs == this.songArray.length) {
	    Song[] newArray = new Song[this.songArray.length + 1];
	    int i = 0;
	    while (i < this.numSongs) {
		newArray[i] = this.songArray[i];
		i++;
	    }
	    this.songArray = newArray;
	}
	this.songArray[this.numSongs] = new Song(title, location);
	return this.songArray[this.numSongs++];
    }

    // Overrides java.lang.Object.toString() so we can print an Album
    public String toString() {
	String str = this.name + "\n";
	str += this.numSongs + "\n";
	int countSongs = 0;
	while (countSongs < this.numSongs) {
	    str += this.songArray[countSongs++].toString();
	}
	return str;
    }
}
