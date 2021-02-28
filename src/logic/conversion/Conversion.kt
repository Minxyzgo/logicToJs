package logic.conversion

import arc.scene.ui.*
import arc.scene.ui.layout.*

import java.util.StringJoiner

import mindustry.logic.*

fun readLogic(
    command: String, 
    table: Table? = null
    ): String {
    //Maybe there is a simpler way to write
    with(StringJoiner("\n")) {
        var inLineString: String? = null
    
        var line: Int? = null
        var isInFunction = false
        var list: List<String>? = null
        command.split("\n").apply{ 
            line = this.count() 
            list = this
        }.forEachIndexed { index: Int, cmd: String ->
            
            fun addLine(arg: CharSequence): StringJoiner {
                return add("${if(isInFunction) "    " else ""}$arg".apply {
                    inLineString = this
                })
            }
            
            when(cmd.getSpace()) {
                "set" -> addLine("var ${cmd.getSpace(1)} = ${cmd.getSpace(2)}")
                "op" -> addLine(opCommandToJs(cmd.getSpace(2), cmd.getSpace(3), cmd.getSpace(4), LogicOp.valueOf(cmd.getSpace(1))))
                "jump" -> jumpParser(cmd, list!!.get(index + 1), index, this, list!!, ::addLine).apply { isInFunction = this }
                "end" -> {
                    if(isInFunction) add("}") else add("return")
                    isInFunction = false
                }
                
                else -> getMet(this, cmd, ::addLine)
            }
            
            table?.add(inLineString!!)!!.left().pad(3f).padLeft(6f).padRight(6f)
            table?.row()
        }
        
        return toString()
    }
}

fun opCommandToJs(arg: String, a: String, b: String, op: LogicOp): String {
    fun add(str: String) = "$arg = $str"
    
    return when(op) {
        LogicOp.idiv -> add("Math.floor($a / $b)")
        LogicOp.pow -> add("Math.pow($a, $b)")
        LogicOp.equal -> add("Math.abs($a - $b) < 0.000001")
        LogicOp.notEqual -> add("Math.abs($a - $b) < 0.000001")
        LogicOp.land -> add("$a != 0 && $b != 0")
        LogicOp.or -> add("$a | $b")
        LogicOp.and -> add("$a & $b")
        LogicOp.xor -> add("$a ^ $b")
        LogicOp.not -> add("~$a")
        LogicOp.max -> add("Math.max($a, $b)")
        LogicOp.min -> add("Math.min($a, $b)")
        LogicOp.strictEqual -> add("false")
        LogicOp.angle -> add("Angles.angle($a, $b)")
        LogicOp.len -> add("Mathf.dst($a, $b)")
        LogicOp.noise -> add("LExecutor.noise.rawNoise2D($a, $b)")
        LogicOp.abs -> add("Math.abs($a)")
        LogicOp.log -> add("Math.log($a, $b)")
        LogicOp.log10 -> add("Math.log10($a, $b)")
        LogicOp.floor -> add("Math.floor($a, $b)")
        LogicOp.ceil -> add("Math.ceil($a, $b)")
        LogicOp.sqrt -> add("Math.sqrt($a, $b)")
        LogicOp.rand -> add("Mathf.rand.nextDouble() * $a")
        LogicOp.sin, LogicOp.cos, LogicOp.tan -> add("Math.${op.symbol}($a * 0.017453292519943295)")
        
        else -> add("$a ${op.symbol} $b")
    }
}

/*
This is very imperfect and can only support part of the syntax
For example:
    set arg 0
    ...
    jump ... lessThan arg 1 |
    end                     |
    read sth cell1 0        ←
will be:
    var arg = 0
    ...
    if(arg < 1) {
        read(sth, cell1, 0)
    } else {
        return
    }
*/
fun jumpParser(arg: String, next: String, line: Int, joiner: StringJoiner, list: List<String>, add: (CharSequence) -> StringJoiner = joiner::add): Boolean {
    var lastLine: Int? = null
    fun getBooleanSymbol(): String {
        val x = arg.getSpace(3)
        val y = arg.getSpace(4)
        val op = ConditionOp.valueOf(arg.getSpace(2))
        return when(arg.getSpace(2)) {
            "notEqual" -> "$x != $y"
            "always" -> "true"
            else -> "$x ${op.symbol} $y"
        }
    }
    
    fun getSymbolLine() {
        list.slice(0..line).forEachIndexed { index: Int, sym: String ->
            if(sym.getSpace() == "end")
                lastLine = index + line
        }
    }
    
    add("if(${getBooleanSymbol()}) {")
    if(next == "end") {
        add("} else {")
        add("    return")
        add("}")
        return false
    } else if(next.getSpace() == "jump" && next.getSpace(2) == "always") {
        // TODO
    }
    return true
}

fun String.getSpace(index: Int = 0): String {
    var list = split(" ")
    return if(list.count() >= index) list[index] else "0"
}

fun getMet(joiner: StringJoiner, args: String, add: (CharSequence) -> StringJoiner = joiner::add) = add("${args.getSpace()}(${args.getSpaceList().mergeList()})")

fun String.getSpaceList(): List<String> = split(" ").drop(1)

fun List<String>.mergeList(): String {
    return toString().replace("[", "").replace("]", "")
}
object ReadTool {
    val joiner = StringJoiner("\n")
    fun end() = joiner.add("end")
    
    fun read(args: List<String>) = joiner.add("read ${args[0]} ${args[1]} ${args[2]}")
    
    fun ubind(unit: List<String>) = joiner.add("ubind ${unit[0]}")
}

//data class FunSymbol() {}