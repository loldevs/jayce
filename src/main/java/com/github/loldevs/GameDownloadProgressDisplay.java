package com.github.loldevs;

import javax.swing.*;

/**
 * @author Malte Schütze
 */
public class GameDownloadProgressDisplay extends JPanel {
    private GameDownloadStatusIndicator statusIndicator = new GameDownloadStatusIndicator(Status.IN_PROGRESS);
    private GameDownloadProgressBar progressBar = new GameDownloadProgressBar();
    private JLabel nameLbl;

    public GameDownloadProgressDisplay(String name) {
        this.nameLbl = new JLabel(name);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(createHorizontalGroup(layout));
        layout.setVerticalGroup(createVerticalGroup(layout));
    }

    private GroupLayout.Group createHorizontalGroup(GroupLayout layout) {
        GroupLayout.SequentialGroup group = layout.createSequentialGroup();

        group.addComponent(nameLbl);
        group.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group.addComponent(progressBar);
        group.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group.addComponent(statusIndicator);

        return group;
    }

    private GroupLayout.Group createVerticalGroup(GroupLayout layout) {
        GroupLayout.ParallelGroup group = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);

        group.addComponent(statusIndicator);
        group.addComponent(progressBar);
        group.addComponent(nameLbl);

        return group;
    }

    public void setChunk(int i, Status status) {
        progressBar.setChunk(i, status);
    }

    public void setKeyframe(int i, Status status) {
        progressBar.setKeyframe(i, status);
    }


    public void setStatus(Status status) {
        this.statusIndicator.setStatus(status);
    }

    public void setHover(String txt) {
        this.statusIndicator.setToolTipText(txt);
    }
}
