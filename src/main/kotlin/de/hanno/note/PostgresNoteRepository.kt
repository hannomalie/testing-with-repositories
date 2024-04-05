package de.hanno.note

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import javax.sql.DataSource

class PostgresNoteRepository(dataSource: DataSource?) : NoteRepository {
    private val jdbi = Jdbi.create(dataSource).apply {
        installPlugin(SqlObjectPlugin())
        installPlugin(KotlinPlugin())
        withExtension<Any?, NoteDao, RuntimeException>(NoteDao::class.java) { dao: NoteDao ->
            dao.createNotesTable()
        }
    }

    override fun find(id: Int) = jdbi.withNoteDao { find(id) }
    override fun add(note: Note): Unit = jdbi.withNoteDao { add(note) }
    override fun getAll(): List<Note> = jdbi.withNoteDao { getAll() }
    override fun addAll(vararg notes: Note): Unit = jdbi.withNoteDao { notes.forEach(::add) }

    private fun <T> Jdbi.withNoteDao(block: NoteDao.() -> T): T = withExtension<T, NoteDao, RuntimeException>(NoteDao::class.java) {
        block(it)
    }

    private interface NoteDao {
        @SqlUpdate("CREATE TABLE IF NOT EXISTS note(id Integer, text Text)")
        fun createNotesTable()

        @SqlQuery("SELECT * FROM \"note\" WHERE id=:id ORDER BY \"id\"")
        fun findById(@Bind("id") id: Int?): List<Note?>

        @SqlUpdate("INSERT INTO \"note\" (id, \"text\") VALUES (:id, :text)")
        fun add(@BindBean note: Note?)

        @SqlQuery("SELECT * FROM \"note\" ORDER BY \"id\"")
        fun getAll(): List<Note>
    }
    private fun NoteDao.find(id: Int?) = findById(id).firstOrNull()
}
