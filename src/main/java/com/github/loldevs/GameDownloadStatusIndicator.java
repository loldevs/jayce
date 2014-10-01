package com.github.loldevs;

import javax.swing.*;
import java.awt.*;

/**
 * @author Malte Schütze
 */
public class GameDownloadStatusIndicator extends JComponent {
    public static final int SIZE = 32;
    private Status status;

    public GameDownloadStatusIndicator(Status status) {
        this.status = status;
    }

    @Override
    public void paint(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        g.drawImage(status.icon, clipBounds.x + (clipBounds.width / 2) - (SIZE / 2), clipBounds.y + (clipBounds.height / 2) - (SIZE / 2), SIZE, SIZE, null);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
