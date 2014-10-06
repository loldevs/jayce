package com.github.loldevs;

import com.google.gson.Gson;
import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.spectator.*;
import net.boreeas.riotapi.spectator.rest.FeaturedGame;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Malte Schütze
 */
public class GameDownloader {

    private Gson gson = new Gson();
    private GamePool gamePool = new GamePool();
    private ExecutorService keepAlive = Executors.newFixedThreadPool(1);

    public Set<GameUpdateTask> getFeatured(Shard shard) {
        SpectatorApiHandler handler = new SpectatorApiHandler(shard);

        Set<GameUpdateTask> result = new HashSet<>();

        for (FeaturedGame game: handler.getFeaturedGames()) {
            result.add(startDownload(shard, handler.openFeaturedGame(game)));
        }

        return result;
    }

    public GameUpdateTask startDownload(Shard shard, InProgressGame game) {
        GameUpdateTask task = gamePool.submit(game/*, ex -> log(shard, game, "Error: " + ex)*/);

        task.addOnFinished(() -> saveGame(shard, game));
        keepAlive.submit(game::waitForEndOfGame); // Make sure we don't exit before all games are downloaded

        return task;
    }


    private void saveGame(Shard shard, InProgressGame game) {
        String outputName = shard.name + "-" + game.getGameId() + ".7z";
        //log(shard, game, "Finished, saving to " + outputName);

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
                    //log(shard, game, "Chunk #" + i + " is not available");
                }
            }

            SevenZArchiveEntry keyframes = new SevenZArchiveEntry();
            keyframes.setName("keyframes");
            keyframes.setDirectory(true);
            out.putArchiveEntry(keyframes);
            out.closeArchiveEntry();

            for (int i = game.getFirstAvailableKeyFrame(); i <= game.getLastAvailableKeyFrame(); i++) {
                try {

                    Chunk chunk = game.getFutureChunk(i).get(1, TimeUnit.SECONDS);

                    SevenZArchiveEntry chunkEntry = new SevenZArchiveEntry();
                    chunkEntry.setName("keyframes/" + i + ".bin");
                    chunkEntry.setDirectory(false);

                    out.putArchiveEntry(chunkEntry);
                    out.write(chunk.getBuffer());
                    out.closeArchiveEntry();
                } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                    //log(shard, game, "Keyframe #" + i + " is not available");
                }
            }

            out.close();
        } catch (IOException ex) {
            //log(shard, game, "Error saving archive: " + ex);
        }
    }
}
