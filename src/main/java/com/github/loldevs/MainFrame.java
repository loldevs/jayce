package com.github.loldevs;

import net.boreeas.riotapi.RequestException;
import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.spectator.GameUpdateTask;
import net.boreeas.riotapi.spectator.InProgressGame;
import net.boreeas.riotapi.spectator.SpectatorApiHandler;

import javax.lang.model.type.ErrorType;
import javax.swing.*;

/**
 * @author Malte Schütze
 */
public class MainFrame extends JFrame {

    // ---- Components

    private JScrollPane outputScrollPane = new JScrollPane();

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

        loadFeaturedGame.addActionListener(e -> {
            Shard shard = (Shard) platformMenu.getSelectedItem();
            for (GameUpdateTask task: downloader.getFeatured(shard)) {
                addProgressDisplay(task);
            }
        });

        loadGameBtn.addActionListener(e -> {
            if (gameIdTextField.getText().trim().isEmpty()) {

                return;
            }

            if (encrytpionKeyTextField.getText().trim().isEmpty()) {
                return;
            }

            Shard shard = (Shard) platformMenu.getSelectedItem();
            SpectatorApiHandler apiHandler = new SpectatorApiHandler(shard);

            try {
                InProgressGame game = apiHandler.openGame(shard, Long.parseLong(gameIdTextField.getText().trim()), encrytpionKeyTextField.getText().trim());
                addProgressDisplay(downloader.startDownload(shard, game));
                gameIdTextField.setText("");
                encrytpionKeyTextField.setText("");
            } catch (NumberFormatException ex) {
                //outputTextArea.append("Invalid game id: Not a number: " + gameIdTextField.getText().trim());
            } catch (RequestException ex) {
                //outputTextArea.append("Game could not be opened: " + ex);
            }
        });
    }

    public void addProgressDisplay(GameUpdateTask task) {
        GameDownloadProgressDisplay display = new GameDownloadProgressDisplay();

        task.addOnFinished(() -> display.setStatus(Status.SAVED));
        task.addOnError(ex -> {
            if (ex instanceof RequestException) {
                RequestException.ErrorType errorType = ((RequestException) ex).getErrorType();
                if (errorType.code >= RequestException.ErrorType.CLOUDFLARE_GENERIC.code && errorType.code <= RequestException.ErrorType.CLOUDFLARE_SSL_HANDSHAKE_FAILED.code) {
                    return;
                }
            }

            display.setStatus(Status.ERROR);
            display.setHover(ex.getMessage());
        });
        task.addOnChunkPulled(i -> display.setChunk(i, Status.SAVED));
        task.addOnKeyframePulled(i -> display.setKeyframe(i, Status.SAVED));
        task.addOnChunkFailed(i -> display.setChunk(i, Status.ERROR));
        task.setOnKeyframeFailed(i -> display.setKeyframe(i, Status.ERROR));
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
}
