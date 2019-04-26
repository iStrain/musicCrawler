/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.io.BufferedReader;
import java.io.IOException;

public class Song {
    private String title;
    private String location;

    // Constructor for Song objects
    // Requires Song.title and Song.location
    public Song(String title, String location) {
	this.title = title;
	this.location = location;
    }

    // Constructor for Song objects - data is read from file
    public Song(BufferedReader inFile) throws IOException {
	this.title = inFile.readLine();
	this.location = inFile.readLine();
    }

    // Accessor method for Song title
    public String getTitle() {
	return this.title;
    }

    // Accessor method for Song location
    public String getLocation() {
	return this.location;
    }

    // Overrides java.lang.Object.toString() so we can print a Song
    public String toString() {
	String str = this.title + "\n";
	str += this.location + "\n";
	return str;
    }

}
