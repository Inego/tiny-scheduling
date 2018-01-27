package inego.tinyscheduling

import java.time.LocalDate

fun createSampleProject(): Project {
    val calendar = Calendar(startingDate, ::isWeekend)

    val p = Project(calendar)

    val yanis = Developer("Yanis", TaskType.BACK_END, startingDate = 2)
    val alex = Developer("Alex", TaskType.BACK_END, 0.6, leader = yanis)
    val dima = Developer("Dima", TaskType.FRONT_END, efficiency = 0.8)
    val sveta = Developer("Sveta", TaskType.FRONT_END, efficiency = 0.8)

    val misha = Developer(
            "Misha",
            TaskType.BACK_END,
            0.7,
            startingDate = calendar.dateToInt(LocalDate.of(2018, 2, 5)),
            leader = yanis
    )
    p.addDeveloper(yanis)
    p.addDeveloper(alex)
    p.addDeveloper(misha)

    p.addDeveloper(dima)
    p.addDeveloper(sveta)
    p.addDeveloper(Developer(
            "Max",
            TaskType.FRONT_END,
            efficiency = 0.7,
            startingDate = calendar.dateToInt(LocalDate.of(2018, 2, 12))
    ))

    p.addFullStackTask("Admin panel", 9, 6)
    p.addFullStackTask("Reports", 5, 3)
    p.addFullStackTask("Catalog", 5, 5, frontOnlyBy=sveta, backOnlyBy = alex)
    p.addFullStackTask("Dealers", 4, 4)
    p.addFullStackTask("News", 2, 1.5)
    p.addFullStackTask("Employees", 3, 3)
    p.addFullStackTask("Partners", 6, 4)
    p.addFullStackTask("Marketing", 3, 3, backOnlyBy = yanis)
    p.addFullStackTask("Closing", 3, 2, backOnlyBy=alex)

    return p
}