package de.hanno.note

import java.util.ArrayList

class InMemoryNoteRepository : NoteRepository {
    private val notes = ArrayList<Note>()
    override fun find(id: Int) = notes.firstOrNull { it.id == id }

    override fun add(note: Note) {
        notes.add(note)
    }

    override fun getAll(): List<Note> = notes

    override fun addAll(vararg notes: Note): Unit = notes.forEach(::add)
}