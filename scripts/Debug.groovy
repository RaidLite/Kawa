import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.LoaderClassPath
import yo.raidlite.api.Plugin
import com.kartuzov.mafiaonline.desktop.a

import static javassist.ClassPool.getDefault

@SuppressWarnings('unused')
class Debug implements Plugin {

    void onEnable() {
        println 'Устанавливаем хук...'

        try {
            ClassLoader gameClassLoader = a.classLoader
            ClassPool pool = getDefault()
            pool.insertClassPath new LoaderClassPath(gameClassLoader)
            CtClass obf = pool.get 'com.kartuzov.mafiaonline.je'
            CtClass stringCt = pool.get String.class.name
            CtClass objectCt = pool.get Object[].class.name
            CtClass socket = pool.get 'io.socket.client.Socket'
            CtClass packetCt = pool.get 'io.socket.parser.Packet'
            CtMethod emit = obf.getDeclaredMethod 'a', [stringCt, objectCt] as CtClass[]
            CtMethod onEvent = socket.getDeclaredMethod 'onevent', [packetCt] as CtClass[]

            onEvent.insertBefore '''
                if ($1.data instanceof org.json.JSONArray) {
                   org.json.JSONArray packet = (org.json.JSONArray)$1.data;
                   System.out.println(packet.toString(4));
                } else {
                    System.out.println($1.data);
                }
            '''

            emit.insertBefore '''
                System.out.println("[EMIT] event: " + $1);

                if ($2 != null) {
                    for (int i = 0; i < $2.length; i++) {
                        Object arg = $2[i];

                        if (arg instanceof org.json.JSONObject) {
                            System.out.println(((org.json.JSONObject)arg).toString(4));
                        } else if (arg instanceof org.json.JSONArray) {
                            System.out.println(((org.json.JSONArray)arg).toString(4));
                        } else {
                            System.out.println(arg);
                        }
                    }
                }
            '''

            socket.toClass gameClassLoader, null
            obf.toClass gameClassLoader, null
        } catch (Throwable throwable) {
            println 'ОШИБКА: ' + throwable.class.name + ': ' + throwable.message
            throwable.printStackTrace()
        }
    }
}