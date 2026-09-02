package yo.raidlite.decompiler

import org.jd.core.v1.ClassFileToJavaSourceDecompiler
import org.jd.core.v1.api.Decompiler as JavaDecompiler
import org.jd.core.v1.api.loader.Loader
import jd.core.preferences.Preferences
import jd.core.printer.PlainTextPrinter
import jd.core.process.DecompilerImpl
import java.util.jar.JarEntry
import java.util.jar.JarFile

import static yo.raidlite.utils.Tools.*

@SuppressWarnings('unused')
class Decompiler {

    static final List<String> MARKERS = [
            '// Byte code:',
            '/* monitor enter ',
            '/* monitor exit '
    ]

    static boolean fails(String str) {
        !str || MARKERS.any { String marker -> str.contains(marker) }
    }

    static int countFails(String str) {
        if (!str) return Integer.MAX_VALUE
        int total = 0
        for (String marker : MARKERS) total += str.count(marker)
        total
    }

    static void init() {
        File gameJar = new File(OUTPUT as String)
        File out = new File('libs', 'out')
        if (!gameJar.isFile()) throw new FileNotFoundException('Не найден: ' + gameJar.absolutePath)
        out.mkdirs()
        JarFile jar = new JarFile(gameJar)

        try {
            Loader loader = new JarLoader(jar)
            JavaDecompiler v1 = new ClassFileToJavaSourceDecompiler()
            DecompilerImpl v0 = new DecompilerImpl()
            Preferences preferences = new Preferences()
            preferences.realignmentLineNumber = true
            preferences.showDefaultConstructor = true
            preferences.showLineNumbers = true
            preferences.showPrefixThis = true
            preferences.unicodeEscape = false
            preferences.writeMetaData = true
            List<JarEntry> entries = jar.entries().findAll { JarEntry entry -> !entry.directory && entry.name.endsWith('.class') && entry.name.startsWith(PACKAGE as String) && !entry.name.contains('$') && !entry.name.contains('module-info') && !entry.name.contains('package-info') } as List<JarEntry>

            for (JarEntry entry : entries) {
                String cls = entry.name.substring 0, entry.name.length() - 6
                SourcePrinter printer = new SourcePrinter()

                boolean failed
                try {
                    v1.decompile loader, printer, cls
                    failed = fails printer.source
                } catch (Exception ignored) {
                    failed = true
                }

                String source = printer.source

                if (failed) {
                    try {
                        PlainTextPrinter v0Printer = new PlainTextPrinter()
                        String v0Source = v0Printer.buildDecompiledOutput loader, cls, preferences, v0
                        if (v0Source != null && countFails(v0Source) < countFails(source)) {
                            source = v0Source
                        }
                    } catch (Exception ignored) {}
                }

                File file = new File(out, cls + '.java')
                file.parentFile.mkdirs()
                file.text = source
                println "Decompiled: ${cls}${failed ? ' (fallback v0)' : ''}"
            }
        } finally {
            jar.close()
        }
    }
}