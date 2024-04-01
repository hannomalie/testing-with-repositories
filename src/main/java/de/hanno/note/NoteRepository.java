package de.hanno.note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface NoteRepository {
    Optional<Note> find(Integer id);
    void add(Note note);
    List<Note> getAll();

    class InMemoryNoteRepository implements NoteRepository {
        private final ArrayList<Note> notes = new ArrayList<>();
        @Override
        public Optional<Note> find(Integer id) {
            return notes.stream().filter(it -> it.id() == id).findFirst();
        }

        @Override
        public void add(Note note) {
            notes.add(note);
        }

        @Override
        public List<Note> getAll() {
            return new ArrayList<>(notes);
        }

        public void addAll(Note ...notes) {
            Arrays.stream(notes).forEach(this::add);
        }
    }
}
