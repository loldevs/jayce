package com.github.loldevs;

import net.boreeas.riotapi.Shard;
import net.boreeas.riotapi.spectator.SpectatorApiHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

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
        if (!cmd.hasOption('n')) {
            new MainFrame(downloader).setVisible(true);
        }

        if (cmd.hasOption('f')) {
            for (String shardName: cmd.getOptionValues('f')) {
                Shard shard = getShardByName(shardName);

                if (shard == null) {
                    System.err.println("No shard for name: " + shardName);
                } else {
                    downloader.getFeatured(shard);
                }
            }
        }

        if (cmd.hasOption('g')) {
            for (String info: cmd.getOptionValues('g')) {
                String[] fields = info.split(" ");

                if (fields.length != 3) {
                    System.err.println("Not enough args for option -g (<region> <id> <key>): " + info);
                    continue;
                }

                Shard shard = getShardByName(fields[0]);

                if (shard == null) {
                    System.err.println("No shard for name: " + fields[0]);
                    continue;
                }

                if (!fields[1].matches("[0-9]+")) {
                    System.err.println("Illegal game id: " + fields[1]);
                    continue;
                }

                SpectatorApiHandler handler = new SpectatorApiHandler(shard);
                downloader.startDownload(shard, handler.openGame(shard, Long.parseLong(fields[1]), fields[2]));
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
}
