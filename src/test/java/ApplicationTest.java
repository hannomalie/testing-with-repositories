import de.hanno.note.Note;
import de.hanno.note.NoteRepository;
import de.hanno.note.NoteRepository.InMemoryNoteRepository;
import de.hanno.note.PostgresNoteRepository;
import io.javalin.json.JavalinJackson;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static de.hanno.Main.createApp;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(NoteRepositoryExtension.class)
public class ApplicationTest {

    @Test
    void existingNoteIsFound(NoteRepository repository) {
        repository.addAll(new Note(0, "asd"), new Note(1, "ftz"), new Note(2, "jek"));

        JavalinTest.test(createApp(repository), (server, client) -> {
            assertThat(client.get("/notes/2").code()).isEqualTo(200);
        });
    }

    @Test
    void absentNoteIsNotFound(NoteRepository repository) {
        JavalinTest.test(createApp(repository), (server, client) -> {
            assertThat(client.get("/notes/2").code()).isEqualTo(404);
        });

    }

    @Test
    void existingNoteIsFound(InMemoryNoteRepository repository) {
        repository.addAll(new Note(0, "asd"), new Note(1, "ftz"), new Note(2, "jek"));

        JavalinTest.test(createApp(repository), (server, client) -> {
            assertThat(client.get("/notes/2").code()).isEqualTo(200);
        });
    }

    @Test
    void allNotesAreFound(NoteRepository repository) {
        var notes = List.of(new Note(0, "asd"), new Note(1, "ftz"), new Note(2, "jek"));
        notes.forEach(repository::add);

        JavalinTest.test(createApp(repository), (server, client) -> {
            String allNotesJson = new JavalinJackson().toJsonString(notes, List.class);
            assertThat(client.get("/notes").body().string()).isEqualTo(allNotesJson);
        });
    }

    @Test
    void existingNoteIsFound(PostgresNoteRepository repository) {
        repository.add(new Note(0, "asd"));
        repository.add(new Note(1, "ftz"));
        repository.add(new Note(2, "jek"));

        JavalinTest.test(createApp(repository), (server, client) -> {
            assertThat(client.get("/notes/2").code()).isEqualTo(200);
        });
    }
}
