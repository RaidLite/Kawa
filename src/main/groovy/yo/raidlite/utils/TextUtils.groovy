package yo.raidlite.utils

import groovy.json.JsonSlurper

import static groovy.json.JsonOutput.prettyPrint
import static groovy.json.JsonOutput.toJson
import static java.lang.Integer.parseInt
import static java.lang.System.out

class TextUtils {

    static pretty(Object... args) {
        if (!args) {
            println 'null'
            return
        }

        Object target = (args.size() == 1) ? args[0] : args

        try {
            def normalize
            normalize = {
                if (it == null) return null
                if (it instanceof String) {
                    if (!(it.startsWith('{') || it.startsWith('['))) return it
                    try { return new JsonSlurper().parseText(it) } catch (ignored) { return it }
                }
                if (it instanceof Collection || it instanceof Object[]) return it.collect { normalize(it) }
                if (it instanceof Map) return it.collectEntries { k, v -> [k, normalize(v)] }
                if (it.class.name.contains('JSONObject') || it.class.name.contains('JSONArray'))
                    return new JsonSlurper().parseText(it.toString())
                it
            }

            String jsonString = prettyPrint(toJson(normalize(target))).replaceAll('\\\\u([0-9a-fA-F]{4})') { _, code ->
                ((char) parseInt(code, 16)).toString()
            }

            new PrintStream(out, true, 'UTF-8').println jsonString
        } catch (Exception ignored) {
            println target.inspect()
        }
    }
}