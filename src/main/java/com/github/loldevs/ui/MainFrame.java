package com.github.loldevs.ui;

import com.google.gson.Gson;
import net.boreeas.riotapi.RequestException;
import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.spectator.*;
import net.boreeas.riotapi.spectator.rest.FeaturedGame;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Malte Schütze
 */
public class MainFrame extends JFrame {

    // ---- Components

    private JScrollPane outputScrollPane = new JScrollPane();
    private JTextArea outputTextArea = new JTextArea();

    private JComboBox<Shard> platformMenu = new JComboBox<Shard>(Shard.values());
    private JTextField gameIdTextField = new JTextField("Game Id");
    private JTextField encrytpionKeyTextField = new JTextField("Encryption Key");
    private JButton loadGameBtn = new JButton("Load Game");
    private JButton loadFeaturedGame = new JButton("Load Featured Games");

    // ---- Fields

    private GamePool gamePool = new GamePool();
    private Gson gson = new Gson();

    public MainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initComponents();
        setLayout();
        pack();
    }

    private void initComponents() {
        outputScrollPane.add(outputTextArea);

        loadFeaturedGame.addActionListener(e -> {
            Shard shard = (Shard) platformMenu.getSelectedItem();
            SpectatorApiHandler apiHandler = new SpectatorApiHandler(shard);

            for (FeaturedGame featured: apiHandler.getFeaturedGames()) {
                InProgressGame game = apiHandler.openFeaturedGame(featured);
                startDownload(shard, game);
            }
        });

        loadGameBtn.addActionListener(e -> {
            if (gameIdTextField.getText().trim().isEmpty()) {
                outputTextArea.append("Missing game id");
                return;
            }

            if (encrytpionKeyTextField.getText().trim().isEmpty()) {
                outputTextArea.append("Missing encryption key");
                return;
            }

            Shard shard = (Shard) platformMenu.getSelectedItem();
            SpectatorApiHandler apiHandler = new SpectatorApiHandler(shard);

            try {
                startDownload(shard, apiHandler.openGame(shard, Long.parseLong(gameIdTextField.getText().trim()), encrytpionKeyTextField.getText().trim()));
                gameIdTextField.setText("");
                encrytpionKeyTextField.setText("");
            } catch (NumberFormatException ex) {
                outputTextArea.append("Invalid game id: Not a number: " + gameIdTextField.getText().trim());
            } catch (RequestException ex) {
                outputTextArea.append("Game could not be opened: " + ex);
            }
        });
    }

    private void setLayout() {
        GroupLayout layout = new GroupLayout(getContentPane());
        this.setLayout(layout);

        layout.setHorizontalGroup(createHorizontalGroup(layout));
        layout.setVerticalGroup(createVerticalGroup(layout));
    }

    private GroupLayout.Group createHorizontalGroup(GroupLayout layout) {

        GroupLayout.ParallelGroup group = layout.createParallelGroup();

        GroupLayout.SequentialGroup gameDataGroup = layout.createSequentialGroup();
        gameDataGroup.addComponent(gameIdTextField);
        gameDataGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        gameDataGroup.addComponent(encrytpionKeyTextField);
        gameDataGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        gameDataGroup.addComponent(loadGameBtn);


        group.addComponent(outputScrollPane, 0, 800, Integer.MAX_VALUE);
        group.addComponent(platformMenu);
        group.addGroup(gameDataGroup);
        group.addComponent(loadFeaturedGame, GroupLayout.Alignment.TRAILING);

        return group;
    }

    private GroupLayout.Group createVerticalGroup(GroupLayout layout) {

        GroupLayout.SequentialGroup group = layout.createSequentialGroup();

        GroupLayout.ParallelGroup gameDataGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        gameDataGroup.addComponent(gameIdTextField);
        gameDataGroup.addComponent(encrytpionKeyTextField);
        gameDataGroup.addComponent(loadGameBtn);


        group.addComponent(outputScrollPane, 0, 600, Integer.MAX_VALUE);
        group.addGap(20);
        group.addComponent(platformMenu, 20, 20, 20);
        group.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group.addGroup(gameDataGroup);
        group.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group.addComponent(loadFeaturedGame);

        return group;
    }

    private void startDownload(Shard shard, InProgressGame game) {
        GameUpdateTask task = gamePool.submit(game, ex -> log(shard, game, "Error: " + ex));

        task.setOnFinished(() -> saveGame(shard, game));
    }

    private void saveGame(Shard shard, InProgressGame game) {
        String outputName = shard.name + "-" + game.getGameId() + ".7z";
        log(shard, game, "Finished, saving to " + outputName);

        try {
            File file = new File(outputName);
            SevenZOutputFile out = new SevenZOutputFile(file);

            SevenZArchiveEntry metadata = new SevenZArchiveEntry();
            metadata.setName("metadata.json");
            metadata.setDirectory(false);
            out.putArchiveEntry(metadata);
            out.write(gson.toJson(game.getMetaData()).getBytes("UTF-8"));
            out.closeArchiveEntry();

            SevenZArchiveEntry chunks = new SevenZArchiveEntry();
            chunks.setName("chunks");
            chunks.setDirectory(true);
            out.putArchiveEntry(chunks);
            out.closeArchiveEntry();

            for (int i = game.getFirstAvailableChunk(); i <= game.getLastAvailableChunk(); i++) {
                try {
                    Chunk chunk = game.getFutureChunk(i).get(1, TimeUnit.SECONDS);

                    SevenZArchiveEntry chunkEntry = new SevenZArchiveEntry();
                    chunkEntry.setName("chunks/" + i + ".bin");
                    chunkEntry.setDirectory(false);

                    out.putArchiveEntry(chunkEntry);
                    out.write(chunk.getBuffer());
                    out.closeArchiveEntry();
                } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                    log(shard, game, "Chunk #" + i + " is not available");
                }
            }

            SevenZArchiveEntry keyframes = new SevenZArchiveEntry();
            keyframes.setName("keyframes");
            keyframes.setDirectory(false);
            out.putArchiveEntry(keyframes);
            out.closeArchiveEntry();

            for (int i = game.getFirstAvailableKeyFrame(); i <= game.getLastAvailableKeyFrame(); i++) {
                try {

                    Chunk chunk = game.getFutureChunk(i).get(1, TimeUnit.SECONDS);

                    SevenZArchiveEntry chunkEntry = new SevenZArchiveEntry();
                    chunkEntry.setName("keyframe/" + i + ".bin");
                    chunkEntry.setDirectory(false);

                    out.putArchiveEntry(chunkEntry);
                    out.write(chunk.getBuffer());
                    out.closeArchiveEntry();
                } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                    log(shard, game, "Keyframe #" + i + " is not available");
                }
            }

            out.close();
        } catch (IOException ex) {
            log(shard, game, "Error saving archive: " + ex);
        }
    }

    private void log(Shard shard, InProgressGame game, String msg) {
        outputTextArea.append(String.format("[%4s] [%d] %s%n", shard.name, game.getGameId(), msg));
    }
}
