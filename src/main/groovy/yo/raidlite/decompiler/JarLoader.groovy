package yo.raidlite.decompiler

import org.jd.core.v1.api.loader.Loader
import java.util.jar.JarFile

class JarLoader implements Loader {

    final JarFile jar

    JarLoader(JarFile jar) {
        this.jar = jar
    }

    @Override
    boolean canLoad(String internalName) {
        jar.getJarEntry(internalName + '.class') != null
    }

    @Override
    byte[] load(String internalName) throws IOException {
        def entry = jar.getJarEntry internalName + '.class'

        if (entry == null) {
            throw new FileNotFoundException('Class not found: ' + internalName)
        }

        jar.getInputStream(entry).bytes
    }
}