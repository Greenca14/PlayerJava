import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    
    @BeforeEach
    public void setup() {
        Player.playlists.clear();
        Player.downPlaylist = null;
        Player.currSong = -1;
    }
    
    @Test
    public void testCreatePlaylist() {
        String input = "Test Playlist\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Player.createPlaylist();
        assertNotNull(Player.downPlaylist, "Playlist should have been created");
    }

    @Test
    public void testChangePlaylist() {
        Playlist testPlaylist = new Playlist("Test Playlist");
        Player.playlists.add(testPlaylist);
        String input = "1\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Player.changePlaylist();
        assertNotNull(Player.downPlaylist, "Playlist should have been changed");
    }

    @Test
    public void testSavePlaylist() {
        Playlist testPlaylist = new Playlist("Test Playlist");
        Player.downPlaylist = testPlaylist;
        Player.savePlaylist();
        assertTrue(Player.playlists.contains(testPlaylist), "Playlist should have been saved");
    }
  
    @Test
    public void testAddSongToPlaylist() {
        Playlist testPlaylist = new Playlist("Test Playlist");
        Player.playlists.add(testPlaylist);
        Song testSong = new Song(100, "Test Song", "Test Artist");
        Player.addSong(testSong, testPlaylist);
        assertTrue(testPlaylist.getSongs().contains(testSong), "Song should have been added to the playlist");
    }
  
    @Test
    public void testDeletePlaylist() {
        Playlist testPlaylist = new Playlist("Test Playlist");
        Player.playlists.add(testPlaylist);
        String input = "1\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Player.delPlaylist();
        assertFalse(Player.playlists.contains(testPlaylist), "Playlist should have been deleted");
    }
  
    @Test
    public void testDeleteSongFromPlaylist() {
        Playlist testPlaylist = new Playlist("Test Playlist");
        Song testSong = new Song(100, "Test Song", "Test Artist");
        testPlaylist.addSong(testSong);
        Player.downPlaylist = testPlaylist;
        Player.delSong();
        assertFalse(testPlaylist.getSongs().contains(testSong), "Song should have been deleted from the playlist");
    }
  
    @Test
    public void testShowPlaylists() {
        String input = "1\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        assertDoesNotThrow(() -> Player.showPlaylists(), "showPlaylists should not throw an exception");
    }
  
    @Test
    public void testPlayNextSong() {
        Player.downPlaylist = null;
        Player.currSong = -1;
        Playlist testPlaylist = new Playlist("Test Playlist");
        testPlaylist.addSong(new Song(100, "Song 1", "Artist 1"));
        testPlaylist.addSong(new Song(200, "Song 2", "Artist 2"));
        Player.downPlaylist = testPlaylist;
        Player.nextSong();
        assertEquals(0, Player.currSong, "Next song should be played");
    }
    
    @Test
    public void testPlayPrevSong() {
        Player.downPlaylist = null;
        Player.currSong = -1;
        Playlist testPlaylist = new Playlist("Test Playlist");
        testPlaylist.addSong(new Song(100, "Song 1", "Artist 1"));
        testPlaylist.addSong(new Song(200, "Song 2", "Artist 2"));
        Player.downPlaylist = testPlaylist;
        Player.prevSong();
        assertEquals(0, Player.currSong, "Prev song should be played");
    }
  
    @Test
    public void testRepeatCurrentSong() {
        Playlist testPlaylist = new Playlist("Test Playlist");
        Song testSong = new Song(100, "Song 1", "Artist 1");
        testPlaylist.addSong(testSong);
        Player.downPlaylist = testPlaylist;
        Player.currSong = 0;
        Player.repeatSong();
        assertEquals(0, Player.currSong, "Current song should be repeated");
    }
  
    @Test
    public void testUploadFilePlaylist() {
        String input = "test.txt\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        assertDoesNotThrow(() -> Player.uploadFilePlaylist(), "uploadFilePlaylist should not throw an exception");
        assertNotNull(Player.downPlaylist, "Playlist should have been uploaded from file");
    }
  
    @Test
    public void testSaveFilePlaylist() {
        Player.downPlaylist = new Playlist("Test Playlist");
        assertDoesNotThrow(() -> Player.saveFilePlaylist(), "saveFilePlaylist should not throw an exception");
    }
}