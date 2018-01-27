package inego.tinyscheduling

import javax.swing.*
import java.awt.*
import java.awt.Font.BOLD


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

        if (current == null) {
            g2.drawString("Computing...", 10, 40)
        } else {
            g2.color = TOP_ROW_BG_COLOR
            g2.fillRect(0, 0, width, TOP_ROW_HEIGHT)

            g2.color = defaultColor

            g2.font = boldFont
            g2.drawString(current.score.toString(), 70, 20)

            val devs = project.developers
            val devHeight = (height - TOP_ROW_HEIGHT) / devs.size



        }

        g2.font = font

        g2.drawString(counter.toString(), 10, 20)
    }

    fun incCounter() {
        counter++
        if (counter % 50000 == 0)
            repaint()
    }

    companion object {
        const val TOP_ROW_HEIGHT = 30
        val TOP_ROW_BG_COLOR: Color = Color.LIGHT_GRAY
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

        frame.setSize(300, 200)
        frame.isVisible = true

        val worker = ComputeWorker(chart)

        worker.execute()

    }

}