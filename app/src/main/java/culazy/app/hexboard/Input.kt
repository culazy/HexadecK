package culazy.app.hexboard

import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.inputmethod.InputConnection

class Input {
    companion object {

        private val downEvents = HashMap<Long, KeyEvent>()
        private val upEvents = HashMap<Long, KeyEvent>()

        fun sendDown(connection: InputConnection, keyEventCode: Int, metaState: Int = 0) {
            val downEvent = downEvents.getOrPut(metaState.toLong().shl(32) + keyEventCode) {
                KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyEventCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, 0)
            }
            connection.sendKeyEvent(downEvent)
        }

        fun sendUp(connection: InputConnection, keyEventCode: Int, metaState: Int = 0) {
            val upEvent = upEvents.getOrPut(metaState.toLong().shl(32) + keyEventCode) {
                KeyEvent(0, 0, KeyEvent.ACTION_UP, keyEventCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, 0)
            }
            connection.sendKeyEvent(upEvent)
        }

        fun sendDownUp(connection: InputConnection, keyEventCode: Int, metaState: Int = 0) {
            sendDown(connection, keyEventCode, metaState)
            sendUp(connection, keyEventCode, metaState)
        }

        fun sendCombo(connection: InputConnection, modifierCode: Int, primaryCode: Int) {
            sendDown(connection, modifierCode)
            sendDown(connection, primaryCode)
            sendUp(connection, primaryCode)
            sendUp(connection, modifierCode)
        }
    }
}
