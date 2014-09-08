package com.github.loldevs.ui;

import com.google.gson.Gson;
import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.spectator.GamePool;
import net.boreeas.riotapi.spectator.GameUpdateTask;
import net.boreeas.riotapi.spectator.InProgressGame;
import net.boreeas.riotapi.spectator.SpectatorApiHandler;
import net.boreeas.riotapi.spectator.rest.FeaturedGame;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

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
        GameUpdateTask task = gamePool.submit(game, ex -> outputTextArea.append("[" + shard.name() + "] [" + game.getGameId() + "] Cancelled: " + ex + "\n"));

        task.setOnFinished(() -> saveGame(shard, game));
    }

    private void saveGame(Shard shard, InProgressGame game) {
        String outputName = shard.name + "-" + game.getGameId() + ".7z";
        outputTextArea.append("[" + shard.name() + "] [" + game.getGameId() + "] Finished, saving to " + outputName + "\n");

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

            for (int i = game.getFirstAvailableChunk(); i <= game.getLastAvailableChunk(); i++) {
                SevenZArchiveEntry chunkEntry = new SevenZArchiveEntry();
                chunkEntry.setName(Integer.toString(i));
            }

        } catch (IOException ex) {
            outputTextArea.append("[" + shard.name() + "] [" + game.getGameId() + "] Error saving archive: " + ex + "\n");
        }
    }
}
