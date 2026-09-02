package yo.raidlite.inspector

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.jar.JarFile

import static yo.raidlite.utils.Tools.*
import static yo.raidlite.inspector.Value.*
import static java.io.File.pathSeparator
import static org.objectweb.asm.ClassReader.*
import static org.objectweb.asm.Opcodes.*
import static org.objectweb.asm.Type.*

@SuppressWarnings('unused')
class Inspector {

    static void inspect() {
        Set<String> emits = new LinkedHashSet<>()
        System.getProperty('java.class.path').split(pathSeparator).each {
            File file = new File(it)
            if (!file.isFile() || !file.name.endsWith('.jar')) {
                return
            }
            scanJar file, emits
        }

        writeOutput emits
    }

    static void scanJar(File file, Set<String> emits) {
        JarFile jar

        try {
            jar = new JarFile(file)
        } catch (Throwable ignored) {
            return
        }

        try {
            jar.entries().each {
                if (it.directory || !it.name.endsWith('.class') || !it.name.startsWith(PACKAGE)) {
                    return
                }

                try {
                    jar.getInputStream(it).withCloseable {
                        ClassReader reader = new ClassReader(it)
                        scanClass reader, emits
                    }
                } catch (Throwable ignored) {}
            }
        } finally {
            jar.close()
        }
    }

    static void scanClass(ClassReader reader, Set<String> emits) {
        ClassNode node = new ClassNode()
        reader.accept node, SKIP_DEBUG
        node.methods.each { scanMethod it, emits }
    }

    static void scanMethod(MethodNode method, Set<String> emits) {
        State state = new State()

        for (int i = 0; i < method.instructions.size(); i++) {
            AbstractInsnNode abstractInsnNode = method.instructions[i]
            if (abstractInsnNode instanceof LabelNode || abstractInsnNode instanceof LineNumberNode || abstractInsnNode instanceof FrameNode) { continue }

            if (abstractInsnNode instanceof LdcInsnNode) {
                state.push constant(abstractInsnNode.cst)
                continue
            }

            if (abstractInsnNode instanceof IntInsnNode) {
                handleIntInsn abstractInsnNode, state
                continue
            }

            if (abstractInsnNode instanceof VarInsnNode) {
                handleVarInsn abstractInsnNode, state
                continue
            }

            if (abstractInsnNode instanceof TypeInsnNode) {
                handleTypeInsn abstractInsnNode, state
                continue
            }

            if (abstractInsnNode instanceof FieldInsnNode) {
                handleFieldInsn abstractInsnNode, state
                continue
            }

            if (abstractInsnNode instanceof InsnNode) {
                handleInsn abstractInsnNode, state
                continue
            }

            if (abstractInsnNode instanceof MethodInsnNode) { handleMethodInsn abstractInsnNode, state, emits }
        }
    }

    static void handleIntInsn(IntInsnNode insn, State state) {
        switch (insn.opcode) {
            case BIPUSH:
            case SIPUSH:
                state.push constant(insn.operand)
                break

            case NEWARRAY:
                state.pop()
                state.push unknown()
                break
        }
    }

    static void handleVarInsn(VarInsnNode insn, State state) {
        switch (insn.opcode) {
            case ALOAD:
            case ILOAD:
            case FLOAD:
            case LLOAD:
            case DLOAD:
                state.push state.load(insn.var)
                break

            case ASTORE:
            case ISTORE:
            case FSTORE:
            case LSTORE:
            case DSTORE:
                state.store insn.var, state.pop()
                break
        }
    }

    static void handleTypeInsn(TypeInsnNode insn, State state) {
        switch (insn.opcode) {
            case NEW:
                state.push unknown()
                break

            case ANEWARRAY:
                state.pop()
                state.push unknown()
                break

            case CHECKCAST:
                break
        }
    }

    static void handleFieldInsn(FieldInsnNode insn, State state) {
        switch (insn.opcode) {
            case GETSTATIC:
                state.push unknown()
                break

            case PUTSTATIC:
                state.pop()
                break

            case GETFIELD:
                state.pop()
                state.push unknown()
                break

            case PUTFIELD:
                state.pop()
                state.pop()
                break
        }
    }

    static void handleInsn(InsnNode insn, State state) {
        switch (insn.opcode) {
            case NOP:
                return

            case ACONST_NULL:
                state.push constant(null)
                return

            case ICONST_M1:
                state.push constant(-1)
                return

            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                state.push constant(insn.opcode - ICONST_0)
                return

            case LCONST_0:
                state.push constant(0L)
                return

            case LCONST_1:
                state.push constant(1L)
                return

            case FCONST_0:
                state.push constant(0F)
                return

            case FCONST_1:
                state.push constant(1F)
                return

            case FCONST_2:
                state.push constant(2F)
                return

            case DCONST_0:
                state.push constant(0D)
                return

            case DCONST_1:
                state.push constant(1D)
                return

            case POP:
                state.pop()
                return

            case POP2:
                state.pop()
                return

            case DUP: {
                Value value = state.pop()

                state.push value
                state.push value

                return
            }

            case DUP_X1: {
                Value a = state.pop()
                Value b = state.pop()
                state.push a
                state.push b
                state.push a
                return
            }

            case DUP_X2: {
                Value a = state.pop()
                Value b = state.pop()
                Value c = state.pop()
                state.push a
                state.push c
                state.push b
                state.push a
                return
            }

            case SWAP: {
                Value a = state.pop()
                Value b = state.pop()
                state.push a
                state.push b
                return
            }

            case AALOAD:
                state.pop()
                state.pop()
                state.push unknown()
                return

            case AASTORE:
                state.pop()
                state.pop()
                state.pop()
                return

            case IADD:
            case ISUB:
            case IMUL:
            case IDIV:
            case IREM:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                state.pop()
                state.pop()
                state.push unknown()
                return
        }
    }

    static void handleMethodInsn(MethodInsnNode insn, State state, Set<String> emits) {
        Type[] argumentTypes = getArgumentTypes insn.desc
        Value[] arguments = new Value[argumentTypes.length]
        for (int i = arguments.length - 1; i >= 0; i--) { arguments[i] = state.pop() }
        if (insn.opcode != INVOKESTATIC) { state.pop() }

        if (insn.owner == JE && insn.name in ['a', 'b'] && isEmitMethod(insn.desc)) {
            if (arguments.length >= 2) {
                String event = constantString arguments[0]
                if (event != null) { emits << event }
            }

            pushUnknownReturn insn, state
            return
        }

        pushUnknownReturn insn, state
    }

    static void pushUnknownReturn(MethodInsnNode insn, State state) {
        Type returnType = getReturnType insn.desc
        if (returnType.sort != VOID) { state.push unknown() }
    }

    static boolean isEmitMethod(String desc) {
        Type[] args = getArgumentTypes desc
        if (args.length != 2) { return false }
        if (args[0].sort != OBJECT || args[0].className != 'java.lang.String') { return false }
        if (args[1].sort != ARRAY || args[1].elementType.sort != OBJECT || args[1].elementType.className != 'java.lang.Object') { return false }
        getReturnType(desc).descriptor == 'Lio/socket/emitter/Emitter;'
    }

    static String constantString(Value value) {
        if (value?.type == 'constant' && value.value instanceof String) { return value.value }
        null
    }

    static void writeOutput(Set<String> emits) {
        List<String> result = emits.toList()
        result.sort()

        new File(EMITS_OUTPUT).withWriter('UTF-8') {
            result.each { event ->
                it << event
                it << '\n'
            }
        }
    }
}