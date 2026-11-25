package yo.raidlite.utils

import org.json.JSONObject

class Tools {

    public static final id = '719a8f523b3dc106 ! com.simejikeyboard/com.baidu.simeji.SimejiIME ! null ! http://www.google.com http://www.google.co.uk ! SP1A.210812.016 ! 21081111RG ! unknown'
    public static final check = '2068136186'
    public static final serverUrl = 'http://mafiaonline.jcloud.kz'
    public static final isUsePassword = true
    public static final OUTPUT = 'libs/game.jar'
    public static final GAME = /C:\Program Files (x86)\Steam\steamapps\common\Mafia Online\out-windows\MafiaOnline.jar/
    public static final MAPPING_FILE = /dump\mapping.txt/
    public static final PACKAGE = 'com/kartuzov/mafiaonline/'
    public static final JE = 'com/kartuzov/mafiaonline/je'
    public static final EMITS_OUTPUT = 'emits.txt'

    static <T> JSONObject json(LinkedHashMap<String, T> values) {
        new JSONObject().tap { values.each { key, value -> it.put key, value } }
    }

    static getGameVersion() {
        new ProcessBuilder('python', '-c', 'from google_play_scraper import app; print(app(\'com.kartuzov.mafiaonline\')[\'version\'])').start().inputStream.text.trim().toInteger()
    }
}
