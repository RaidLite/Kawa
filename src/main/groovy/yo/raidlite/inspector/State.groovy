package yo.raidlite.inspector

import static yo.raidlite.inspector.Value.unknown

class State {

    List<Value> stack = []
    Map<Integer, Value> locals = [:]

    Value pop() {
        stack ? stack.remove(stack.size() - 1) : unknown()
    }

    Value load(int index) {
        locals[index] ?: unknown()
    }

    void push(Value value) {
        stack << (value ?: unknown())
    }

    void store(int index, Value value) {
        locals[index] = value ?: unknown()
    }
}