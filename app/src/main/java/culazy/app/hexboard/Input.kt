package culazy.app.hexboard

import android.view.KeyEvent
import android.view.inputmethod.InputConnection

class Input {
    companion object {

        private val downEvents = HashMap<Int, KeyEvent>()
        private val upEvents = HashMap<Int, KeyEvent>()

        fun sendDown(connection: InputConnection, keyEventCode: Int) {
            val downEvent = downEvents.getOrPut(keyEventCode) {
                KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode)
            }
            connection.sendKeyEvent(downEvent)
        }

        fun sendUp(connection: InputConnection, keyEventCode: Int) {
            val upEvent = upEvents.getOrPut(keyEventCode) {
                KeyEvent(KeyEvent.ACTION_UP, keyEventCode)
            }
            connection.sendKeyEvent(upEvent)
        }

        fun sendDownUp(connection: InputConnection, keyEventCode: Int) {
            sendDown(connection, keyEventCode)
            sendUp(connection, keyEventCode)
        }

        fun sendCombo(connection: InputConnection, modifierCode: Int, primaryCode: Int) {
            sendDown(connection, modifierCode)
            sendDown(connection, primaryCode)
            sendUp(connection, primaryCode)
            sendUp(connection, modifierCode)
        }

        fun sendCombo(connection: InputConnection, firstModifierCode: Int, secondModifierCode: Int, primaryCode: Int) {
            sendDown(connection, firstModifierCode)
            sendDown(connection, secondModifierCode)
            sendDown(connection, primaryCode)
            sendUp(connection, primaryCode)
            sendUp(connection, secondModifierCode)
            sendUp(connection, firstModifierCode)
        }
    }
}
