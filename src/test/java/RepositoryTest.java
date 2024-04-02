import de.hanno.note.Note;
import de.hanno.note.NoteRepository.InMemoryNoteRepository;
import de.hanno.note.PostgresNoteRepository;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@ExtendWith(NoteRepositoryExtension.class)
public class RepositoryTest {

    @TestFactory
    List<DynamicTest> absentNoteCanTBeRetrieved(
            InMemoryNoteRepository inMemoryNoteRepository,
            PostgresNoteRepository postgresNoteRepository
    ) {
        return Stream.of(inMemoryNoteRepository, postgresNoteRepository).map(repository -> {
                    repository.add(new Note(5, "asd"));
                    return dynamicTest(repository.getClass().getSimpleName(), () -> {
                        assertThat(repository.find(4)).isEmpty();
                    });
                }
        ).toList();
    }

    @TestFactory
    List<DynamicTest> addedNoteCanBeRetrieved(
            InMemoryNoteRepository inMemoryNoteRepository,
            PostgresNoteRepository postgresNoteRepository
    ) {
        return Stream.of(inMemoryNoteRepository, postgresNoteRepository).map(repository -> {
                    repository.add(new Note(5, "asd"));
                    return dynamicTest(repository.getClass().getSimpleName(), () -> {
                        assertThat(repository.find(5)).isNotEmpty();
                    });
                }
        ).toList();
    }
}
