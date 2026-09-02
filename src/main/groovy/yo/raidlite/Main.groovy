package yo.raidlite

@SuppressWarnings('unused')
class Main {

    //
    // For using Kawa, you need download game to C drive (on steam) like this:
    // C:/Program Files (x86)/Steam/steamapps/common/Mafia Online/out-windows/MafiaOnline.jar
    //
    // And you should download Python (tested on 3.11.0) and download google_play_scraper
    // with command pip install google_play_scraper
    // Because its only variant to get always working version (steam and iOS have magic version)
    //
    // Run croupier (like Spigot core for Minecraft, but for MafiaOnline):
    // Croupier.init(args)
    // Also you can make plugins in /scripts/ but only in groovy :(
    //
    // Inspect emits:
    // Inspector.inspect()
    //
    // Connecting to server
    // je socket = MafSocket.createSocket()
    // MafSocket.catchAll socket
    // MafSocket.connect socket
    //
    // Example of sending methods
    // Simple.fastStart socket
    //
    // Steps to decompile (or just deobfuscate jar)
    // 1. Proguard.init() <- made /dump/mapping.txt
    // 2. ApplyMap.init() <- gets /dump/mapping.txt and apply mappings and makes /libs/game.jar
    // 3. Decompiler.init() <- gets /libs/game.jar and decompiles, out files save to /libs/out
    //

    static main(args) {}
}