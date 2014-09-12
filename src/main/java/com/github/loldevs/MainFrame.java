package com.github.loldevs;

import net.boreeas.riotapi.RequestException;
import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.spectator.InProgressGame;
import net.boreeas.riotapi.spectator.SpectatorApiHandler;

import javax.swing.*;

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

    private GameDownloader downloader;

    public MainFrame(GameDownloader downloader) {
        this.downloader = downloader;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initComponents();
        setLayout();
        pack();
    }

    private void initComponents() {
        outputScrollPane.add(outputTextArea);

        loadFeaturedGame.addActionListener(e -> {
            Shard shard = (Shard) platformMenu.getSelectedItem();
            downloader.getFeatured(shard);
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
                InProgressGame game = apiHandler.openGame(shard, Long.parseLong(gameIdTextField.getText().trim()), encrytpionKeyTextField.getText().trim());
                downloader.startDownload(shard, game);
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


    private void log(Shard shard, InProgressGame game, String msg) {
        outputTextArea.append(String.format("[%4s] [%d] %s%n", shard.name, game.getGameId(), msg));
    }
}
