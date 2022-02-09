package cn.copaint.audience.model

import com.wacom.ink.rasterization.Layer

class StepStack {
    val stepList = mutableListOf<Step>()
    var currentPos = -1
    val maxCap = 16

    fun undo(): Step? {
        return if (stepList.size != 0 && currentPos > 0) {
            currentPos--
            stepList[currentPos]
        } else null
    }

    fun redo(): Step? {
        return if (stepList.size != 0 && currentPos < stepList.lastIndex) {
            currentPos++
            stepList[currentPos]
        } else null
    }

    fun addStep(stepModel: Step) {
        while (currentPos <stepList.lastIndex)stepList.removeLast()
        if (stepList.size> maxCap)stepList.removeAt(0)
        stepList.add(stepModel)
        currentPos = stepList.lastIndex
    }
}
