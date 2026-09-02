package yo.raidlite.mappings

import javassist.*
import java.util.jar.*

import static yo.raidlite.utils.Tools.*
import static java.nio.charset.StandardCharsets.*

@SuppressWarnings('unused')
class ApplyMap {

    static Map<String, String> readMapping(String path) {
        Map<String, String> mapping = [:]

        new File(path).eachLine(UTF_8.name()) { String line ->
            line = line.trim()

            if (line && !line.startsWith('#')) {
                def matcher = line =~ /^(.+?)\s+->\s+(.+?):$/

                if (matcher.find()) {
                    String deobfuscated = matcher.group(1).trim()
                    String obfuscated = matcher.group(2).trim()
                    mapping[obfuscated] = deobfuscated
                }
            }
            null
        }

        return mapping
    }

    static void applyClassReferences(Collection<CtClass> classes, Map<String, String> mapping) {
        classes.each { CtClass ctClass ->
            if (ctClass.isFrozen()) { ctClass.defrost() }
            mapping.each { String oldName, String newName -> if (oldName != newName) { ctClass.replaceClassName oldName, newName }}
        }
    }

    static Map<String, CtClass> renameClasses(Map<String, CtClass> classes, Map<String, String> mapping) {
        Map<String, CtClass> renamed = [:]

        classes.each { String oldName, CtClass ctClass ->
            String newName = mapping.containsKey(oldName) ? mapping[oldName] : oldName

            if (oldName != newName) {
                if (ctClass.isFrozen()) { ctClass.defrost() }
                ctClass.setName newName
            }

            renamed[newName] = ctClass
        }

        return renamed
    }

    static void apply(String inputPath, String outputPath, String mappingPath) {
        println 'Читаю маппинги...'
        Map<String, String> mapping = readMapping mappingPath
        ClassPool pool = new ClassPool(true)
        pool.appendClassPath inputPath
        Map<String, CtClass> classes = [:]

        println 'Загружаю классы...'

        new JarFile(inputPath).withCloseable { JarFile jar ->
            jar.stream()
                    .filter {
                        !it.isDirectory() && it.name.endsWith('.class')
                    }
                    .forEach {
                        String className = it.name.substring(0, it.name.length() - 6).replace('/', '.')

                        try {
                            classes[className] = pool.get className
                        } catch (Throwable throwable) {
                            println 'Не удалось загрузить ' + className + ': ' + throwable.message
                        }
                    }
        }

        applyClassReferences classes.values(), mapping
        println 'Переименовываю классы...'
        Map<String, CtClass> renamedClasses = renameClasses classes, mapping
        File outputFile = new File(outputPath)
        if (outputFile.exists()) { outputFile.delete() }
        outputFile.parentFile?.mkdirs()

        new JarFile(inputPath).withCloseable { JarFile jar ->
            outputFile.withOutputStream { OutputStream outputStream ->
                JarOutputStream jarOutput = new JarOutputStream(outputStream)

                try {
                    jar.stream().each { JarEntry entry ->
                        if (entry.isDirectory()) { return }

                        String entryName = entry.name

                        if (entryName.endsWith('.class')) {
                            String oldClassName = entryName.substring(0, entryName.length() - 6).replace '/', '.'
                            String newClassName = mapping.containsKey(oldClassName) ? mapping[oldClassName] : oldClassName
                            String newEntryName = newClassName.replace('.', '/') + '.class'
                            JarEntry newEntry = new JarEntry(newEntryName)
                            newEntry.time = entry.time
                            jarOutput.putNextEntry newEntry
                            CtClass ctClass = renamedClasses[newClassName]

                            if (ctClass != null) {
                                jarOutput.write ctClass.toBytecode()
                            } else {
                                jar.getInputStream(entry).withCloseable { jarOutput.write it.bytes }
                            }

                            jarOutput.closeEntry()
                        } else {
                            JarEntry newEntry = new JarEntry(entryName)
                            newEntry.time = entry.time
                            jarOutput.putNextEntry newEntry
                            jar.getInputStream(entry).withCloseable { jarOutput.write it.bytes }
                            jarOutput.closeEntry()
                        }
                    }
                } finally { jarOutput.close() }
            }
        }

        println 'Готово!'
    }

    static init(String[] args) {
        String input = args.length > 0 ? args[0] : GAME
        String output = args.length > 1 ? args[1] : OUTPUT
        String mapping = args.length > 2 ? args[2] : MAPPING_FILE
        apply input, output, mapping
    }
}