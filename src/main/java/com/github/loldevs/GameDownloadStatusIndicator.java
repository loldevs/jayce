package com.github.loldevs;

import javax.swing.*;
import java.awt.*;

/**
 * @author Malte Schütze
 */
public class GameDownloadStatusIndicator extends JComponent {
    private Status status;

    public GameDownloadStatusIndicator(Status status) {
        this.status = status;
    }

    @Override
    public void paint(Graphics g) {
        Rectangle clipBounds = g.getClipBounds();
        g.drawImage(status.icon, clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height, null);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
