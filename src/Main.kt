import java.time.LocalDate

val startingDate = LocalDate.of(2017, 1, 15)!!

fun main(args: Array<String>) {

    val p = Project(Calendar(startingDate, ::isWeekend))

    val yanis = Developer("Yanis", TaskType.BACK_END)
    val dima = Developer("Dima", TaskType.FRONT_END)

    p.addDeveloper(yanis)
    p.addDeveloper(Developer("Alex", TaskType.BACK_END, 0.8, yanis, null))
    p.addDeveloper(Developer(
            "Misha",
            TaskType.BACK_END,
            0.7,
            yanis,
            startingDate = LocalDate.of(2017, 2, 1)
    ))

    p.addDeveloper(dima)
    p.addDeveloper(Developer("Sveta", TaskType.FRONT_END, 0.8, dima))
    p.addDeveloper(Developer("Max", TaskType.FRONT_END, 0.7, dima))

    p.addFullStackTask("Admin panel", 9, 6)
    p.addFullStackTask("Reports", 5, 3)
    p.addFullStackTask("Catalog", 5, 5)
    p.addFullStackTask("Dealers", 4, 4)
    p.addFullStackTask("News", 2, 1.5)
    p.addFullStackTask("Employees", 3, 3)
    p.addFullStackTask("Partners", 6, 4)
    p.addFullStackTask("Marketing", 3, 3)
}