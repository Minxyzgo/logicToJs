package logic.conversion;

import arc.*

import mindustry.*
import mindustry.game.*
import mindustry.mod.*

class Main : Mod() {

    override fun loadContent() {
        Events.on(EventType.ClientLoadEvent::class.java) {
            Vars.ui.logic = ConversionDialog.BuildLogicDialog()
        }
    }
}