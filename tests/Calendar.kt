import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CalendarTest {
    private val startingDate = LocalDate.of(2017, 1, 15)!!

    lateinit var calendar: Calendar

    @BeforeEach
    fun beforeEach() {
        calendar = Calendar(startingDate, ::isWeekend)
    }

    @Test
    fun testNew() {
        Assertions.assertEquals(startingDate, calendar.intToDate(0))
        Assertions.assertEquals(0, calendar.dateToInt(startingDate))
    }

    @Test
    fun testNextDateFromInt() {
        // Assuming it's Monday
        Assertions.assertEquals(LocalDate.of(2017, 1, 16), calendar.intToDate(1))
    }

    @Test
    fun testNextDateFromDate() {

    }



}