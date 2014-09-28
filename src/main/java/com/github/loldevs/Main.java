package com.github.loldevs;

import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.spectator.GameUpdateTask;
import net.boreeas.riotapi.spectator.SpectatorApiHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import java.io.*;

/**
 * @author Malte Schütze
 */
public class Main {
    public static void main(String... args) throws Exception {
        Options options = new Options();
        options.addOption("n", "no-gui", false, "hides the gui");
        options.addOption("i", "infile", true, "read game keys from the target file (each line should be in the format <region> <id> <key>)");
        options.addOption("f", "featured", true, "download featured games for the specified region (can be repeated)");
        options.addOption("g", "game", true, "download the specified game (format: <region> <id> <key>) (can be repeated)");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);

        GameDownloader downloader = new GameDownloader();
        MainFrame frame = null;
        if (!cmd.hasOption('n')) {
            frame = new MainFrame(downloader);
            frame.setVisible(true);
        }

        if (cmd.hasOption('f')) {
            for (String shardName: cmd.getOptionValues('f')) {
                Shard shard = getShardByName(shardName);

                if (shard == null) {
                    System.err.println("No shard for name: " + shardName);
                } else {
                    for (GameUpdateTask gameUpdateTask: downloader.getFeatured(shard)) {
                        if (frame == null) break;
                        frame.addProgressDisplay(gameUpdateTask);
                    }
                }
            }
        }

        if (cmd.hasOption('g')) {
            for (String info: cmd.getOptionValues('g')) {

                ValidatedGameInfo validatedGameInfo = new ValidatedGameInfo(info).validate();
                if (validatedGameInfo.isBad()) continue;

                Shard shard = validatedGameInfo.getShard();

                SpectatorApiHandler handler = new SpectatorApiHandler(shard);
                GameUpdateTask task = downloader.startDownload(shard, handler.openGame(shard, validatedGameInfo.getGameId(), validatedGameInfo.getEncryptionKey()));
                if (frame != null) frame.addProgressDisplay(task);
            }
        }

        if (cmd.hasOption('i')) {
            File file = new File(cmd.getOptionValue('i'));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {

                String in;
                while ((in = reader.readLine()) != null) {
                    if ((in = in.trim()).isEmpty() || in.charAt(0) == '#') {
                        continue;
                    }

                    ValidatedGameInfo validatedGameInfo = new ValidatedGameInfo(in).validate();
                    if (validatedGameInfo.isBad()) continue;

                    Shard shard = validatedGameInfo.getShard();

                    SpectatorApiHandler handler = new SpectatorApiHandler(shard);
                    GameUpdateTask task = downloader.startDownload(shard, handler.openGame(shard, validatedGameInfo.getGameId(), validatedGameInfo.getEncryptionKey()));
                    if (frame != null) frame.addProgressDisplay(task);
                }
            } catch (IOException ex) {
                System.err.println("Error reading from file: " + ex);
            }
        }
    }

    private static Shard getShardByName(String name) {
        for (Shard shard: Shard.values()) {
            if (shard.name.equalsIgnoreCase(name)) {
                return shard;
            }

            if (shard.name().equalsIgnoreCase(name)) {
                return shard;
            }
        }

        return Shard.getBySpectatorPlatform(name);
    }

    private static class ValidatedGameInfo {
        private boolean isBad;
        private String info;
        private String[] fields;
        private Shard shard;
        private long gameId;
        private String encryptionKey;

        public ValidatedGameInfo(String info) {
            this.info = info;
        }

        boolean isBad() {
            return isBad;
        }

        public String[] getFields() {
            return fields;
        }

        public Shard getShard() {
            return shard;
        }

        public long getGameId() {
            return gameId;
        }

        public String getEncryptionKey() {
            return encryptionKey;
        }

        public ValidatedGameInfo validate() {
            fields = info.split(" ");

            if (fields.length != 3) {
                System.err.println("Not enough args for option -g (<region> <id> <key>): " + info);
                isBad = true;
                return this;
            }

            shard = getShardByName(fields[0]);

            if (shard == null) {
                System.err.println("No shard for name: " + fields[0]);
                isBad = true;
                return this;
            }

            if (!fields[1].matches("[0-9]+")) {
                System.err.println("Illegal game id: " + fields[1]);
                isBad = true;
                return this;
            }

            isBad = false;
            gameId = Long.parseLong(fields[1]);
            encryptionKey = fields[2];
            return this;
        }
    }
}
