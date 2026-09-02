package yo.raidlite.emits

import com.kartuzov.mafiaonline.je
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject

import java.security.SecureRandom

import static yo.raidlite.socket.MafSocket.smartEmit
import static yo.raidlite.utils.Tools.*

@SuppressWarnings('unused')
class Simple {

    static SecureRandom RANDOM = new SecureRandom()

    static Emitter getNumberOnline(je socket) {
        smartEmit socket, 'NumberOnline', new JSONObject()
    }

    static Emitter getFindGroupRows(je socket) {
        smartEmit socket, 'getFindGroupRows', new JSONObject()
    }

    static Emitter getGangsFightResult(je socket) {
        smartEmit socket, 'getGangsFightResult', new JSONObject()
    }

    static Emitter enterRegionChat(je socket) {
        smartEmit socket, 'EnterRegionChat', new JSONObject()
    }

    static Emitter exitRegionChat(je socket) {
        smartEmit socket, 'ExitRegionChat', new JSONObject()
    }

    static Emitter getTop(je socket) {
        smartEmit socket, 'Top', new JSONObject()
    }

    static Emitter leaveTown(je socket) {
        smartEmit socket, 'LeaveTown', new JSONObject()
    }

    static Emitter getListOfServers(je socket) {
        smartEmit socket, 'ListOfServers', new JSONObject()
    }

    static Emitter getRegions(je socket) {
        smartEmit socket, 'getRegions', new JSONObject()
    }

    static Emitter getClansTop(je socket) {
        smartEmit socket, 'getClansTop', new JSONObject()
    }

    static Emitter getOnlinePeople(je socket, String name) {
        smartEmit socket, 'OnlineFriend', new JSONArray().put(name)
    }

    static Emitter sendPasswordToMail(je socket, String name) {
        def payload = json(name: name)
        smartEmit socket, 'SendPasswordToMail', payload
    }

    static Emitter fastStart(je socket) {
        def payload = json(
                role: 2, version: getGameVersion(),
                skinRole: 'mirn.jpg', skinShirt: 'Cart.jpg'
        )
        smartEmit socket, 'FastStart', payload
    }

    static Emitter login(je socket, String login, String password) {
        def payload = json(
                login: login, password: password,
                version: getGameVersion(),
                isUsePassword: isUsePassword,
                i: id, o: check
        )
        smartEmit socket, 'Login', payload
    }

    static Emitter register(je socket, String login, String password) {
        def payload = json(login: login, password: password, i: id)
        smartEmit socket, 'Createlogin', payload
    }
}