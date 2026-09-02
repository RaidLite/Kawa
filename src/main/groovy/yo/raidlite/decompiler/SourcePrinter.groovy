package yo.raidlite.decompiler

import org.jd.core.v1.api.printer.Printer

class SourcePrinter implements Printer {

    final StringBuilder buffer = new StringBuilder()
    int indentLevel = 0
    boolean lineStart = true

    String getSource() { buffer.toString() }

    @Override void start(int type, int majorVersion, int minorVersion) {}
    @Override void end() {}
    @Override void printText(String text) { write text }
    @Override void printNumericConstant(String constant) { write constant }
    @Override void printStringConstant(String constant, String owner) { write constant }
    @Override void printKeyword(String keyword) { write keyword }
    @Override void printDeclaration(int type, String internalName, String name, String descriptor) { write name }
    @Override void printReference(int type, String internalName, String name, String descriptor, String owner) { write name }
    @Override void indent() { indentLevel++ }
    @Override void unindent() { if (indentLevel > 0) { indentLevel-- }}
    @Override void startMarker(int type) {}
    @Override void endMarker(int type) {}

    @Override
    void startLine(int lineNumber) {
        if (!lineStart) {
            buffer.append '\n'
        }
        buffer.append '    ' * indentLevel
        lineStart = false
    }

    @Override
    void endLine() {
        buffer.append '\n'
        lineStart = true
    }

    @Override
    void extraLine(int count) {
        count.times { buffer.append '\n' }
        lineStart = true
    }

    void write(String text) {
        if (lineStart) {
            buffer.append '    ' * indentLevel
            lineStart = false
        }

        buffer.append text
    }
}