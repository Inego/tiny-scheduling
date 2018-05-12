package inego.tinyscheduling

import javax.swing.*
import java.awt.*
import java.awt.Font.BOLD
import javax.swing.JFrame


class MyGanttChart(val project: Project) : JPanel() {

    var interimResult: InterimResult? = null

    private var counter = 0

    private val boldFont: Font = font.deriveFont(BOLD)

    override fun paintComponent(g: Graphics) {

        super.paintComponent(g)

        val g2 = g as Graphics2D

        paintGanttChart(g2, width, height)
    }

    private fun paintGanttChart(g2: Graphics2D, width: Int, height: Int) {

        val current = interimResult

        val defaultColor = g2.color

        val taskColor = Color(200, 240, 255)

        val dashed = BasicStroke(
                1F,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0F,
                floatArrayOf(3f),
                0F
        )

        if (current == null) {
            g2.drawString("Computing...", 10, 40)
        } else {
            val hours = current.score

            val defaultStroke = g2.stroke

            g2.color = TOP_ROW_BG_COLOR

            g2.fillRect(0, 0, width, TOP_ROW_HEIGHT)

            g2.color = defaultColor

            g2.font = boldFont
            g2.drawString(hours.toString(), 70, TOP_ROW_BASE)
            g2.font = font

            val devs = project.developers

            val devIndices = devs.withIndex().associate { Pair(it.value, it.index) }

            val metrics = g2.fontMetrics

            val fontHeight = metrics.ascent + metrics.descent

            val chartWidth = width - DEV_WIDTH
            val chartHeight = height - TOP_ROW_HEIGHT

            fun hourToScreenX(hour: Int) = DEV_WIDTH + chartWidth * hour / hours
            fun devToScreenY(devIdx: Int) = TOP_ROW_HEIGHT + chartHeight * devIdx / devs.size

            val devHeight = chartHeight / devs.size

            var hour = 0

            g2.stroke = dashed
            g2.color = Color.LIGHT_GRAY

            while (hour <= hours) {
                val dayX = hourToScreenX(hour)
                g2.drawLine(dayX, TOP_ROW_HEIGHT, dayX, height)
                hour += 8
            }

            g2.stroke = defaultStroke

            // Draw lines and names of developers
            for (dev in devs.withIndex()) {
                val devIdx = dev.index
                val top = devToScreenY(devIdx)
                val next = devToScreenY(devIdx + 1)

                g2.color = Color.LIGHT_GRAY
                g2.drawRect(0, top, width, next - top)

                val base = top + (devHeight + fontHeight) / 2

                g2.color = defaultColor
                g2.drawString(dev.value.toString(), H_MARGIN, base)
            }

            // Draw assignments
            for (assignment in current.solution) {
                val developer = assignment.developer
                val devIdx = devIndices.getValue(developer)

                val xStart = hourToScreenX(assignment.start)
                val xEnd = hourToScreenX(assignment.end)

                val yStart = devToScreenY(devIdx)
                val yEnd = devToScreenY(devIdx + 1)

                g2.color = taskColor
                g2.fillRect(xStart + TASK_INSET, yStart + TASK_INSET, xEnd - xStart - 2 * TASK_INSET, yEnd - yStart - 2 * TASK_INSET)
                g2.color = defaultColor
                g2.drawRect(xStart + TASK_INSET, yStart + TASK_INSET, xEnd - xStart - 2 * TASK_INSET, yEnd - yStart - 2 * TASK_INSET)

                // Task name
                g2.font = boldFont
                val taskNameBase = yStart + 2 * TASK_INSET + metrics.height
                val taskX = xStart + 3 * TASK_INSET
                g2.drawString(assignment.task.toString(), taskX, taskNameBase)
                g2.font = font
                val taskStartBase = taskNameBase + metrics.height
                g2.drawString(project.calendar.hoursToString(assignment.start), taskX, taskStartBase)
            }

            // Time of ending
            val endDate = project.calendar.hoursToString(current.score)
            val endDateWidth = metrics.stringWidth(endDate)
            g2.drawString(endDate, width - TASK_INSET - endDateWidth, TOP_ROW_BASE)
        }

        g2.font = font
        g2.drawString(counter.toString(), 10, TOP_ROW_BASE)
    }

    fun incCounter() {
        counter++
        if (counter % 50000 == 0)
            repaint()
    }

    companion object {
        const val TOP_ROW_HEIGHT = 30
        const val TOP_ROW_BASE = 20
        const val DEV_WIDTH = 100
        const val H_MARGIN = 5
        val TOP_ROW_BG_COLOR: Color = Color.LIGHT_GRAY
        const val TASK_INSET = 3
    }
}


class InterimResult(val project: Project, val solution: BranchAndBoundSolution, val score: Int)


class ComputeWorker(private val chart: MyGanttChart) : SwingWorker<Void?, InterimResult>() {

    override fun doInBackground(): Void? {

        val tree = BbTree(chart.project) { solution, score ->
            publish(InterimResult(chart.project, solution, score))
        }

        while (!isCancelled) {
            chart.incCounter()
            tree.playout()
        }

        return null
    }

    override fun process(chunks: MutableList<InterimResult>?) {
        chunks?.apply {
            if (isNotEmpty()) {
                chart.interimResult = last()

                chart.repaint()
            }
        }
    }
}


fun main(args: Array<String>) {

    SwingUtilities.invokeLater {
        val frame = JFrame("Live computation")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        val pane = frame.contentPane

        val stopBtn = JButton("Stop and dump")

        pane.add(stopBtn, BorderLayout.PAGE_START)

        val project = createSampleProject()

        val chart = MyGanttChart(project)

        pane.add(chart, BorderLayout.CENTER)

        val dim = Toolkit.getDefaultToolkit().screenSize

        frame.setSize(dim.width * 2 / 3, dim.height * 2 / 3)
        frame.setLocation((dim.width - frame.width) / 2, (dim.height - frame.height) / 2)

        frame.isVisible = true

        val worker = ComputeWorker(chart)

        worker.execute()

        stopBtn.addActionListener {
            worker.cancel(true)
        }

    }

}