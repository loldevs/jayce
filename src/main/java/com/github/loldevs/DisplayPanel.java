package com.github.loldevs;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Malte Schütze
 */
public class DisplayPanel extends JPanel {

    private List<GameDownloadProgressDisplay> displays = new ArrayList<>();

    public DisplayPanel() {
        rebuildLayout();
    }

    public void addDisplay(@Nonnull GameDownloadProgressDisplay display) {
        displays.add(display);
        rebuildLayout();
    }

    private void rebuildLayout() {

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        GroupLayout.ParallelGroup horGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vertGroup = layout.createSequentialGroup();

        for (GameDownloadProgressDisplay display: displays) {
            horGroup.addComponent(display);

            vertGroup.addComponent(display, 30, 30, 30);
            vertGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        }

        layout.setHorizontalGroup(horGroup);
        layout.setVerticalGroup(vertGroup);
    }
}
