package yo.raidlite

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.kartuzov.mafiaonline.desktop.a
import com.kartuzov.mafiaonline.jf
import yo.raidlite.api.Plugin
import yo.raidlite.utils.Activity

import static java.io.File.separator
import static java.lang.System.exit
import static java.lang.management.ManagementFactory.runtimeMXBean
import static yo.raidlite.utils.Tools.getGameVersion

@SuppressWarnings('unused')
class Croupier {

    static jf game
    static List<Plugin> plugins = []

    static loadPlugins() {
        File folder = new File('./scripts/')

        if (!folder.exists()) {
            folder.mkdirs()
            return
        }

        File[] files = folder.listFiles({ dir, String name -> name.endsWith '.groovy' } as FilenameFilter)
        if (!files) return
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Croupier.class.getClassLoader())

        files.each {
            try {
                Class scriptClass = groovyClassLoader.parseClass it
                Plugin instance = (Plugin) scriptClass.getDeclaredConstructor().newInstance()

                if (instance instanceof Plugin) {
                    plugins << instance
                    instance.onEnable()
                    println 'Загружен плагин: ' + it.name
                } else {
                    println it.name + ' не реализует Plugin'
                }

            } catch (Throwable throwable) {
                println 'Ошибка загрузки ' + it.name + ': ' + throwable.message
                throwable.printStackTrace()
            }
        }
    }

    static init(args) {
        if (!runtimeMXBean.inputArguments.any { it.contains 'add-opens' }) {
            String javaBin = System.getProperty('java.home') + separator + 'bin' + separator + 'java'
            String classpath = System.getProperty 'java.class.path'
            String mainClass = 'yo.raidlite.Main'
            List<String> command = [
                    javaBin, '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
                    '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
                    '-Dfile.encoding=UTF-8', '-cp', classpath, mainClass
            ] + (args as List)

            Process process = new ProcessBuilder(command).inheritIO().start()
            process.waitFor()
            exit process.exitValue()
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration()
        config.setWindowedMode 800, 480
        config.useVsync false
        config.setTitle 'Мафия Онлайн'
        loadPlugins()
        Activity activity = new Activity(new a(game))
        game = new jf(getGameVersion(), '12', activity)
        activity.b game
        new Lwjgl3Application(game, config)
    }
}