package yo.raidlite.inspector

@SuppressWarnings('unused')
class Value {

    String type
    Object value
    List<Value> items = []
    LinkedHashMap<String, Value> fields = new LinkedHashMap<>()

    static Value unknown() {
        new Value(type: 'unknown')
    }

    static Value constant(Object value) {
        new Value(type: 'constant', value: value)
    }

    static Value object() {
        new Value(type: 'object')
    }

    static Value array() {
        new Value(type: 'array')
    }

    Value copy() {
        Value result = new Value(
                type: type,
                value: value
        )

        fields.each { key, child -> result.fields[key] = child?.copy() ?: unknown() }
        items.each { result.items << (it?.copy() ?: unknown()) }

        result
    }
}