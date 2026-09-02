package yo.raidlite.utils

import io.socket.client.SocketOptionBuilder
import io.socket.client.IO

import static io.socket.client.SocketOptionBuilder.*

class SocketOptionsBuilder {

    final SocketOptionBuilder delegate = builder()

    SocketOptionsBuilder timeout(long value) {
        delegate.timeout = value
        this
    }

    SocketOptionsBuilder path(String value) {
        delegate.path = value
        this
    }

    SocketOptionsBuilder upgrade(boolean value) {
        delegate.upgrade = value
        this
    }

    SocketOptionsBuilder forceNew(boolean value) {
        delegate.forceNew = value
        this
    }

    SocketOptionsBuilder multiplex(boolean value) {
        delegate.multiplex = value
        this
    }

    SocketOptionsBuilder reconnection(boolean value) {
        delegate.reconnection = value
        this
    }

    SocketOptionsBuilder reconnectionAttempts(int value) {
        delegate.reconnectionAttempts = value
        this
    }

    SocketOptionsBuilder reconnectionDelay(long value) {
        delegate.reconnectionDelay = value
        this
    }

    SocketOptionsBuilder reconnectionDelayMax(long value) {
        delegate.reconnectionDelayMax = value
        this
    }

    SocketOptionsBuilder randomizationFactor(double value) {
        delegate.randomizationFactor = value
        this
    }

    IO.Options build() {
        delegate.build()
    }
}