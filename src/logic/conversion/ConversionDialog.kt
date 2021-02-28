package logic.conversion

import arc.*
import arc.func.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import arc.scene.ui.TextButton.*
import mindustry.gen.*
import mindustry.logic.*
import mindustry.logic.LStatements.*
import mindustry.ui.*
import mindustry.ui.dialogs.*

class ConversionDialog(val command: String) : BaseDialog("logicToJs") {

    init {
        cont.pane {
            it.table { table ->
                readLogic(command, table)
            }.size(320f, 400f)
        }.size(Math.min(Core.graphics.getWidth(), Core.graphics.getHeight()) - 100f).grow().row()
        
        addCloseButton()
        buttons.defaults().size(160f, 64f)
        buttons.button("@schematic.copy", Icon.copy, Styles.cleart) {
            hide()
            Core.app.setClipboardText(readLogic(command))
        }
    }
    
    class BuildLogicDialog : LogicDialog() {
        init {
            buttons.button("LogicToJs") { ConversionDialog(canvas.save()).show() }
        }
    }
}