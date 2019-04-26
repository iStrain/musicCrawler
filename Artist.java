
/*
Simple Music Library program
by Duncan Baxter & Jennifer Kennedy
DunJen Software
*/

import java.io.BufferedReader;
import java.io.IOException;

public class Artist implements Comparable<Artist> {
    private String name; // name of Artist
    private Album[] albumArray; // array of Albums by Artist
    private int numAlbums; // number of Albums in Artist.albumArray[]

    // Constructor method for Artist objects
    // Requires Artist.name and Artist.maxAlbums
    public Artist(String name) {
        this.name = name;
        this.numAlbums = 0;
        this.albumArray = new Album[numAlbums];
    }

    // Constructor for Artist objects - data is read from file
    public Artist(BufferedReader inFile) throws IOException {
        // Name of Artist
        this.name = inFile.readLine();

        // Number of Albums by that Artist in our MusicLibrary
        this.numAlbums = Integer.parseInt(inFile.readLine());
        this.albumArray = new Album[this.numAlbums];
        MusicLibrary.totalAlbums += this.numAlbums;

        // For each Album ...
        int countAlbums = 0;
        while (countAlbums < this.numAlbums) {
            this.albumArray[countAlbums++] = new Album(inFile);
        }
    }

    public int compareTo(Artist a) {
        if (a != null) {
            return this.name.compareToIgnoreCase(a.name);
        } else {
            return 0;
        }
    }

    // Accessor method for Artist name
    public String getName() {
        return this.name;
    }

    // Accessor method to get all the Album names for an Artist
    public String[] getAlbumArray() {
        String[] s = new String[this.numAlbums];
        int i = 0;
        while (i < this.numAlbums) {
            s[i] = this.albumArray[i].getName();
            i++;
        }
        return s;
    }

    // Mutator method for Artist name
    public void setName(String name) {
        this.name = name;
    }

    // Find an Album by an Artist given its name
    // Returns null if Album not found in Artist.albumArray[]
    public Album getAlbumByName(String name) {
        boolean found = false;
        int i = 0;
        while (i < this.numAlbums && !found) {
            found = this.albumArray[i++].getName().equalsIgnoreCase(name);
        }
        if (found) {
            return this.albumArray[i - 1];
        } else {
            return null;
        }
    }

    // Add an Album to an existing Artist
    // If Artist.albumArray[] is full, increment the size of the array
    public Album addAlbum(String name) {
        if (this.numAlbums == this.albumArray.length) {
            Album[] newArray = new Album[this.albumArray.length + 1];
            int i = 0;
            while (i < this.numAlbums) {
                newArray[i] = this.albumArray[i];
                i++;
            }
            this.albumArray = newArray;
        }
        this.albumArray[this.numAlbums] = new Album(name);
        return this.albumArray[this.numAlbums++];
    }

    // Overrides java.lang.Object.toString() so we can print an Artist
    public String toString() {
        String str = this.name + "\n";
        str += this.numAlbums + "\n";
        int countAlbums = 0;
        while (countAlbums < this.numAlbums) {
            str += this.albumArray[countAlbums++].toString();
        }
        return str;
    }
}
