import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

@SuppressWarnings("serial")
public class MP3Player extends JFrame {
    private JButton playButton, pauseButton, nextButton, prevButton, loadButton, addSongButton, removeSongButton, createPlaylistButton, deletePlaylistButton, saveButton;
    private JLabel songLabel;
    private int currSongIndex = -1;
    private List<Song> songs = new ArrayList<>();
    private AdvancedPlayer player;
    private Thread playerThread;
    private String currentFilePath;
    private int currentFrame;
    private boolean isPaused;

    public MP3Player() {
        setTitle("MP3 Player");
        setSize(700, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        songLabel = new JLabel("No song playing", SwingConstants.CENTER);
        add(songLabel, BorderLayout.CENTER);
        
        JPanel bottomControlPanel = new JPanel(); //bot
        bottomControlPanel.setLayout(new GridLayout(1, 4));
        
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        nextButton = new JButton("Next");
        prevButton = new JButton("Prev");
        
        bottomControlPanel.add(prevButton);
        bottomControlPanel.add(playButton);
        bottomControlPanel.add(pauseButton);
        bottomControlPanel.add(nextButton);

        add(bottomControlPanel, BorderLayout.SOUTH);
        
        JPanel topControlPanel = new JPanel(); //top
        topControlPanel.setLayout(new GridLayout(1, 6));
        
        
        loadButton = new JButton("Load Playlist");
        addSongButton = new JButton("Add Song");
        removeSongButton = new JButton("Remove Song");
        createPlaylistButton = new JButton("Create Playlist");
        deletePlaylistButton = new JButton("Delete Playlist");
        saveButton = new JButton("Save Playlist");
        
        topControlPanel.add(loadButton);
        topControlPanel.add(saveButton);
        topControlPanel.add(addSongButton);
        topControlPanel.add(removeSongButton);
        topControlPanel.add(createPlaylistButton);
        topControlPanel.add(deletePlaylistButton);
        
        add(topControlPanel, BorderLayout.NORTH);

        
        createPlaylistButton.addActionListener(new ActionListener() { ////////////@Overrides
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewPlaylist();
            }
        });

        deletePlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePlaylist();
            }
        });
        
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSongToPlaylist();
            }
        });
        
        removeSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	removeSongFromPlaylist();
            }
        });
        
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSong();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseSong();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextSong();
            }
        });

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prevSong();
            }
        });

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSongsFromFile();
            }
        });
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSongsToFile();
            }
        });
    }

    private void playSong() { //func's
        if (currSongIndex >= 0 && currSongIndex < songs.size()) {
            Song song = songs.get(currSongIndex);
            currentFilePath = song.getFilePath();
            songLabel.setText("Playing: " + song.getName() + " by " + song.getAuthor());

            stopSong();
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(currentFilePath));
                Bitstream bitstream = new Bitstream(fileInputStream);
                int skipFrames = isPaused ? currentFrame : 0;
                for (int i = 0; i < skipFrames; i++) {
                    bitstream.readFrame();
                }
                player = new AdvancedPlayer(fileInputStream);
                playerThread = new Thread(() -> {
                    try {
                        player.setPlayBackListener(new PlaybackListener() {
                            @Override
                            public void playbackFinished(PlaybackEvent evt) {
                                currentFrame = evt.getFrame();
                                nextSong();
                            }
                        });
                        player.play();
                    } catch (JavaLayerException e) {
                        e.printStackTrace();
                        nextSong();
                    }
                });
                playerThread.start();
                isPaused = false;
            } catch (IOException | JavaLayerException e) {
                e.printStackTrace();
            }
        } else {
            songLabel.setText("No song loaded");
        }
    }

    private void pauseSong() {
        if (player != null) {
            isPaused = true;
            player.close();
            songLabel.setText("Paused: " + songs.get(currSongIndex).getName() + " by " + songs.get(currSongIndex).getAuthor());
        } else {
            songLabel.setText("No song loaded");
        }
    }

    private void nextSong() {
        if (!songs.isEmpty()) {
            currSongIndex = (currSongIndex + 1) % songs.size();
            playSong();
        }
    }

    private void prevSong() {
        if (!songs.isEmpty()) {
            currSongIndex = (currSongIndex - 1 + songs.size()) % songs.size();
            playSong();
        }
    }

    private void loadSongsFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                songs.clear();
                currSongIndex = -1;

                String line;
                while ((line = br.readLine()) != null) {
                    String[] songData = line.split(", ");
                    if (songData.length == 4) {
                        int duration = Integer.parseInt(songData[0].trim());
                        String songName = songData[1].trim();
                        String authorName = songData[2].trim();
                        String filePath = songData[3].trim();
                        songs.add(new Song(duration, songName, authorName, filePath));
                    }
                }

                if (!songs.isEmpty()) {
                    currSongIndex = 0;
                    playSong();
                } else {
                    songLabel.setText("No songs found in the file");
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading songs from file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveSongsToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                for (Song song : songs) {
                    String line = song.getDuration() + ", " + song.getName() + ", " + song.getAuthor() + ", " + song.getFilePath();
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
                JOptionPane.showMessageDialog(this, "Playlist saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving playlist to file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void stopSong() {
        if (player != null) {
            player.close();
            playerThread.interrupt();
        }
    }
    
	private Song createSongFromMP3File(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
        
            String songName = tag.getFirst(FieldKey.TITLE);
            String artist = tag.getFirst(FieldKey.ARTIST);
        
            return new Song(0, songName, artist, file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

	private void addSongToPlaylist() {
	    JFileChooser fileChooser = new JFileChooser();
	    int result = fileChooser.showOpenDialog(this);

	    if (result == JFileChooser.APPROVE_OPTION) {
	        try {
	            File selectedFile = fileChooser.getSelectedFile();
	            Song newSong = createSongFromMP3File(selectedFile);
	            if (newSong != null) {
	                songs.add(newSong);
	                if (currSongIndex == -1) {
	                    currSongIndex = 0;
	                }
	                songLabel.setText("Song added: " + newSong.getName() + " by " + newSong.getAuthor());
	            } else {
	                JOptionPane.showMessageDialog(this, "Error loading the selected song", "Error", JOptionPane.ERROR_MESSAGE);
	            }
	        } catch (Exception e) {
	            JOptionPane.showMessageDialog(this, "Error loading songs from file", "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }
	}
	
	private void removeSongFromPlaylist() {
		if (currSongIndex >= 0 && currSongIndex < songs.size()) {
	        songs.remove(currSongIndex);
	        stopSong();
	        if (!songs.isEmpty()) {
	            currSongIndex = (currSongIndex == songs.size()) ? 0 : currSongIndex;
	            playSong();
	        } else {
	            songLabel.setText("No songs in the playlist");
	        }
	    } else {
	        songLabel.setText("No current song to remove");
	    }
	}
	
	private void createNewPlaylist() {
	    songs.clear();
	    currSongIndex = -1;
	    stopSong();
	    songLabel.setText("New playlist created");
	}
	
	private void deletePlaylist() {
	    songs.clear();
	    currSongIndex = -1;
	    stopSong();
	    songLabel.setText("Playlist deleted");
	}
    
    public static void main(String[] args) { //main
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MP3Player().setVisible(true);
            }
        });
    }
}