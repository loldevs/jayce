package com.github.loldevs;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Malte Schütze
 */
public class GameDownloadProgressBar extends Component {
    private List<Status> chunks = new ArrayList<>();
    private List<Status> keyframes = new ArrayList<>();

    public synchronized void setKeyframe(int idx, Status status) {
        if (idx >= keyframes.size()) {
            for (int i = keyframes.size(); i < idx; i++) {
                keyframes.add(Status.IN_PROGRESS);
            }
        }

        keyframes.add(status);
    }

    public synchronized void setChunk(int idx, Status status) {
        if (idx >= chunks.size()) {
            for (int i = chunks.size(); i < idx; i++) {
                chunks.add(Status.IN_PROGRESS);
            }
        }

        chunks.add(status);
    }

    @Override
    public void paint(Graphics g) {
        Rectangle clip = g.getClipBounds();
        int normalizedChunkCount = ((chunks.size() + 1) / 2) * 2; // is `size` if size is even, else `size + 1`
        int count = Math.max(keyframes.size(), normalizedChunkCount);

        int xscale = clip.width / count;
        int yscale = clip.height / 2;

        drawStatusBar(g, chunks, clip.x, clip.y, xscale, yscale);
        drawStatusBar(g, keyframes, clip.x, clip.y + yscale, 2 * xscale, yscale);
    }

    private void drawStatusBar(Graphics g, List<Status> stati, int x, int y, int width, int height) {

        for (int i = 0; i < stati.size(); i++) {
            Color color;
            switch (stati.get(i)) {
                case SAVED:
                    color = Color.green.darker();
                    break;
                case ERROR:
                    color = Color.red.darker();
                    break;
                case IN_PROGRESS:
                default:
                    color = Color.darkGray;
                    break;

            }

            g.setColor(color);
            g.fillRect(x + i*width, y, width, height);
        }
    }
}
