package yo.raidlite.socket

import io.socket.client.IO
import io.socket.emitter.Emitter
import org.json.JSONObject
import com.kartuzov.mafiaonline.je
import io.socket.client.Socket
import yo.raidlite.utils.SocketOptionsBuilder
import java.lang.reflect.Field

import static io.socket.client.IO.socket
import static java.lang.Integer.MAX_VALUE
import static yo.raidlite.utils.TextUtils.pretty
import static yo.raidlite.utils.Tools.serverUrl

@SuppressWarnings('unused')
class MafSocket {

    static IO.Options buildOptions() {
        new SocketOptionsBuilder()
                .timeout(30000L)
                .path('/new')
                .upgrade(false)
                .forceNew(false)
                .multiplex(true)
                .reconnection(true)
                .reconnectionAttempts(MAX_VALUE)
                .reconnectionDelay(2000L)
                .reconnectionDelayMax(10000L)
                .randomizationFactor(0.5)
                .build()
    }

    static Socket catchAll(je server) {
        onAnyIncoming server, { pretty it }
    }

    static Socket onAnyIncoming(je wrapper, Emitter.Listener listener) {
        Field field = je.getDeclaredField 'a'
        field.accessible = true
        Socket socket = (Socket) field.get(wrapper)
        socket.onAnyIncoming listener
        socket
    }

    static createSocket() {
        new je(socket(serverUrl, buildOptions()))
    }

    static Socket connect(je socket) {
        socket.b()
    }

    static smartEmit(je socket, String name, Object... args) {
        if (args.length > 0 && args[0] instanceof JSONObject && ((JSONObject) args[0]).length() == 0) {
            args = args.length > 1 ? args[1..-1] as Object[] : [] as Object[]
        }

        socket.a name, args
        sleep 500
    }
}