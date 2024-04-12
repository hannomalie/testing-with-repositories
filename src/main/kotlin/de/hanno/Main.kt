package de.hanno

import de.hanno.note.NoteRepository
import de.hanno.note.InMemoryNoteRepository
import io.javalin.Javalin
import io.javalin.http.Context

object Main {
    fun main(args: Array<String>) {
        val app = createApp(InMemoryNoteRepository())
        app.start(7070)
    }

    fun createApp(noteRepository: NoteRepository): Javalin = Javalin.create()
        .get("/notes/{id}") { ctx: Context ->
            val id = ctx.pathParam("id").toInt()
            when (val note = noteRepository.find(id)) {
                null -> ctx.status(404)
                else -> ctx.json(note)
            }
        }
        .get("/notes") { ctx: Context ->
            val notes = noteRepository.getAll()
            ctx.json(notes)
        }
}
