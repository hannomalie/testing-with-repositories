package de.hanno;

import de.hanno.note.Note;
import de.hanno.note.NoteRepository;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        var app = createApp(new NoteRepository.InMemoryNoteRepository());
        app.start(7070);
    }

    public static Javalin createApp(NoteRepository noteRepository) {
        return Javalin.create()
                .get("/notes/{id}", ctx -> {
                    var id = Integer.parseInt(ctx.pathParam("id"));
                    Optional<Note> note = noteRepository.find(id);
                    note.ifPresentOrElse(ctx::json, () -> ctx.status(404));
                })
                .get("/notes", ctx -> {
                    List<Note> notes = noteRepository.getAll();
                    ctx.json(notes);
                });
    }
}

