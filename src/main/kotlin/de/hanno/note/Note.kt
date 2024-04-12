package de.hanno.note

data class Note(val id: Int, val text: String)

interface NoteRepository {
    fun find(id: Int): Note?
    fun add(note: Note)
    fun getAll(): List<Note>
    fun addAll(vararg notes: Note)
}
