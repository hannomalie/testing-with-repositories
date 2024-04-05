import de.hanno.Main.createApp
import de.hanno.note.Note
import de.hanno.note.NoteRepository
import de.hanno.note.InMemoryNoteRepository
import de.hanno.note.PostgresNoteRepository
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.testtools.HttpClient
import io.javalin.testtools.JavalinTest.test
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

@ExtendWith(NoteRepositoryExtension::class)
class ApplicationTest {
    @Test
    fun existingNoteIsFound(repository: NoteRepository) {
        repository.addAll(Note(0, "asd"), Note(1, "ftz"), Note(2, "jek"))

        test(createApp(repository)) { _: Javalin, client: HttpClient ->
            Assertions.assertThat(client.get("/notes/2").code).isEqualTo(200)
        }
    }

    @Test
    fun existingNoteIsFound(repository: InMemoryNoteRepository) {
        repository.addAll(Note(0, "asd"), Note(1, "ftz"), Note(2, "jek"))

        test(createApp(repository)) { _: Javalin, client: HttpClient ->
            Assertions.assertThat(client.get("/notes/2").code).isEqualTo(200)
        }
    }

    @Test
    fun existingNoteIsFound() {
        val repository = Mockito.mock(NoteRepository::class.java)
        Mockito.`when`(repository.find(2)).thenReturn(Note(2, "jek"))

        test(createApp(repository)) { _: Javalin, client: HttpClient ->
            Assertions.assertThat(client.get("/notes/2").code).isEqualTo(200)
        }
    }

    @Test
    fun absentNoteIsNotFound(repository: NoteRepository) {
        test(createApp(repository)) { _: Javalin, client: HttpClient ->
            Assertions.assertThat(client.get("/notes/2").code).isEqualTo(404)
        }
    }

    @Test
    fun allNotesAreFound(repository: NoteRepository) {
        val notes = listOf(Note(0, "asd"), Note(1, "ftz"), Note(2, "jek"))
        notes.forEach(repository::add)

        test(createApp(repository)) { _: Javalin, client: HttpClient ->
            val allNotesJson = JavalinJackson().toJsonString(notes, MutableList::class.java)
            Assertions.assertThat(client.get("/notes").body!!.string()).isEqualTo(allNotesJson)
        }
    }

    @Test
    fun existingNoteIsFound(repository: PostgresNoteRepository) {
        repository.add(Note(0, "asd"))
        repository.add(Note(1, "ftz"))
        repository.add(Note(2, "jek"))

        test(createApp(repository)) { _: Javalin, client: HttpClient ->
            Assertions.assertThat(client.get("/notes/2").code).isEqualTo(200)
        }
    }
}
