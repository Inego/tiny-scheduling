package inego.tinyscheduling

import java.time.LocalDate

fun createSampleProject(): Project {
    val calendar = Calendar(startingDate, ::isWeekend)

    val p = Project(calendar)

    val yanis = Developer("Yanis", TaskType.BACK_END)
    val alex = Developer("Alex", TaskType.BACK_END, 0.7, leader = yanis)
    val dima = Developer("Dima", TaskType.FRONT_END, efficiency = 0.8)
    val sveta = Developer("Sveta", TaskType.FRONT_END, efficiency = 0.8)

    val misha = Developer(
            "Misha",
            TaskType.BACK_END,
            0.7,
            startingDate = 1,
            leader = yanis
    )
    val max = Developer(
            "Max",
            TaskType.FRONT_END,
            efficiency = 0.8
            //startingDate = calendar.dateToInt(LocalDate.of(2018, 2, 12))
    )

    p.addDeveloper(yanis)
    p.addDeveloper(alex)
    p.addDeveloper(misha)

    p.addDeveloper(dima)
    p.addDeveloper(sveta)
    p.addDeveloper(max)

    p.addFullStackTask("Admin panel", 0.0, 3.0, backOnlyBy = yanis, frontOnlyBy = dima)
    p.addFullStackTask("Reports", 5.0, 3.0)
    p.addFullStackTask("Catalog", 0.0, 4.0, frontOnlyBy=sveta, backOnlyBy = yanis, first = true)
    p.addFullStackTask("Product documents", 3.0, 5.0)
    p.addFullStackTask("Dealers", 0.5, 0.5, frontOnlyBy = dima, backOnlyBy = alex, first = true)
    p.addFullStackTask("News", 2.0, 1.5, frontOnlyBy = dima, backOnlyBy = misha)
    p.addFullStackTask("Employees", 3.0, 3.0)
    p.addFullStackTask("Partners", 6.0, 4.0)
    p.addFullStackTask("Marketing", 3.0, 3.0, backOnlyBy = yanis)
    p.addFullStackTask("Closing", 3.0, 2.0, backOnlyBy=alex)

    return p
}

val startingDate = LocalDate.of(2018, 2, 9)!!