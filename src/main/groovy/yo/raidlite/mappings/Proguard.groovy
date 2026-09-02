package yo.raidlite.mappings

import com.google.common.collect.ListMultimap
import javassist.ClassPool
import javassist.CtClass
import java.util.jar.JarFile

import static yo.raidlite.utils.Tools.*
import static com.google.common.collect.MultimapBuilder.hashKeys
import static java.nio.charset.StandardCharsets.UTF_8

@SuppressWarnings('unused')
class Proguard {

    static int classWeight(CtClass ctClass) {
        int weight = 0

        ctClass.declaredBehaviors.each {
            weight += it.methodInfo.codeAttribute?.codeLength ?: 0
        }

        weight
    }

    static String packageOf(String name) {
        name.contains('.') ? name.substring(0, name.lastIndexOf('.')) : ''
    }

    static String simpleFromSourceFile(String source) {
        source ? source.replaceFirst(/\.java$/, '') : ''
    }

    static init(String[] args) {
        String path = args.length > 0 ? args[0] : GAME
        println 'Загружаю...'
        ClassPool pool = new ClassPool(true)
        pool.appendClassPath path
        Map<String, CtClass> loadedClasses = [:]

        new JarFile(path).withCloseable { jar ->
            jar.stream()
                    .filter {
                        it.name.endsWith('.class') &&
                                it.name.startsWith(PACKAGE)
                    }
                    .map {
                        it.name.substring(0, it.name.length() - 6).replace '/', '.'
                    }
                    .forEach {
                        loadedClasses[it] = pool.get it
                    }
        }

        ListMultimap<SourceKey, String> groups = hashKeys().arrayListValues().build()
        Set<String> groupedClasses = []

        loadedClasses.each { name, ctClass ->
            String src = ctClass.classFile.sourceFile

            if (src) {
                groups.put new SourceKey(packageOf(name), src), name
                groupedClasses << name
            }

            null
        }

        Map<String, String> resolved = [:]

        groups.asMap().each { key, Collection<String> names ->
            String pkg = key.pkg()
            String source = key.source()
            String root = names.max {
                classWeight loadedClasses[it]
            }

            String simpleName = simpleFromSourceFile source
            String rootName = pkg ? pkg + '.' + simpleName : simpleName
            resolved[root] = rootName

            (names - root).eachWithIndex { String child, int i ->
                resolved[child] = (rootName + '$' + String.valueOf(i + 1)).toString()
            }
        }

        loadedClasses.keySet()
                .findAll { !groupedClasses.contains(it) }
                .each { resolved[it] = it }

        new File(MAPPING_FILE).withWriter(UTF_8.name()) { writer ->
            resolved.each { name, mapped ->
                if (name != mapped) {
                    writer << mapped + ' -> ' + name + ':\n'
                }

                null
            }
        }

        println 'Готово...'
    }
}