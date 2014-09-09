jayce
=====

A spectator tool, downloading games into zip archives (with GUI \o/)

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
