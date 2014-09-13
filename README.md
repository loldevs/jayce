jayce
=====

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
------------------------------------------------
-n | --no-gui | | Do not show a GUI |
-f | --featured | region | Download featured games from the specified region | Yes
-g | --game | "[region] [game id] [encryption key]" | Download the specified game | Yes
-i | --infile | file | Download games as declared in the file. Each line in the file should be in the same format as the -g option. Empty lines tolerated. Comments start with '#'
