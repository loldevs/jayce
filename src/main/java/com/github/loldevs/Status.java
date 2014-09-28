package com.github.loldevs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

/**
* @author Malte Schütze
*/
public enum Status {
    IN_PROGRESS("/status_icon_inprogress.png"),
    SAVED("/status_icon_saved.png"),
    ERROR("/status_icon_error.png");

    public final Image icon;

    Status(String imgLocation) {

        Image icon;
        try {
            icon = ImageIO.read(Status.class.getResourceAsStream(imgLocation));
        } catch (IOException e) {
            icon = null;
        }

        this.icon = icon;
    }
}
