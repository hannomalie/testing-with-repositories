import de.hanno.note.Note
import de.hanno.note.NoteRepository
import de.hanno.note.InMemoryNoteRepository
import de.hanno.note.PostgresNoteRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(NoteRepositoryExtension::class)
class RepositoryTest {
    @TestFactory
    fun absentNoteCanTBeRetrieved(
        inMemoryNoteRepository: InMemoryNoteRepository,
        postgresNoteRepository: PostgresNoteRepository
    ): List<DynamicTest> = listOf(inMemoryNoteRepository, postgresNoteRepository).map { repository: NoteRepository ->
        repository.add(Note(5, "asd"))

        dynamicTest(repository.javaClass.getSimpleName()) {
            assertThat(repository.find(4)).isNull()
        }
    }

    @TestFactory
    fun addedNoteCanBeRetrieved(
        inMemoryNoteRepository: InMemoryNoteRepository,
        postgresNoteRepository: PostgresNoteRepository
    ): List<DynamicTest> = listOf(inMemoryNoteRepository, postgresNoteRepository).map { repository: NoteRepository ->
        repository.add(Note(5, "asd"))

        dynamicTest(repository.javaClass.getSimpleName()) {
            assertThat(repository.find(5)).isNotNull
        }
    }
}
