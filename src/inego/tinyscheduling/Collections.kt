package inego.tinyscheduling

import java.util.*

fun <E> List<E>.getRandomElement() = this[Random().nextInt(this.size)]
