jayce
=====
![gui](https://github.com/loldevs/jayce/blob/master/jayce.png)

A spectator tool, downloading games into zip archives (with GUI and command line)

Spectator file format:
```
<platform>-<gameid>.7z
|
+-metadata.json
+-chunks
| |
| +-1.bin
| +-2.bin
| +-...
|
+-keyframes
  |
  +-1.bin
  +-2.bin
  +-...
```

##### Command line options

Short | Long | Params | Description | Repeatable
------|------|--------|-------------|-----------
`-n` | `--no-gui` | | Do not show a GUI |
`-f` | `--featured` | `region` | Download featured games from the specified region | Yes
`-g` | `--game` | `"[region] [game id] [encryption key]"` | Download the specified game | Yes
`-i` | `--infile` | `file` | Download games as declared in the file. Each line in the file should be in the same format as the -g option. Empty lines tolerated. Comments start with '#' |



##### Example Usage (Command Line)
`java -jar jayce.jar` - Start Jayce in GUI mode
`java -jar jayce.jar --featured euw` - Download featured EUW games and open a GUI showing the progress
`java -jar jayce.jar --game euw 973114380 SA/2JHiyArOG0TaKI01mSZKcvV6Elctd --no-gui` - Download game 973114380 from euw and surpress the GUI


##### Example Usage (GUI)
- The empty part on top fills with progress displays for each downloaded game. Each progress display consists of the id of the game (a combination of platform and game id), two rows for downloaded chunks and keyframes and a status icon which turns green when the game has been saved or red if an error occurred
- Under this is the region selector
- Enter game id and encryption key in the specified boxes and press the "Load Game" button to load the specified game.
- Or press the "Load Featured Games" buttons and load all the featured games in this region
