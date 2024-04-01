package de.hanno.note;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class PostgresNoteRepository implements NoteRepository {

    private final Jdbi jdbi;

    public PostgresNoteRepository(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.registerRowMapper(new RecordAndAnnotatedConstructorMapper());
        jdbi.withExtension(NoteDao.class, dao -> {
                dao.createNotesTable();
                return null;
            });
    }

    @Override
    public Optional<Note> find(Integer id) {
        return jdbi.withExtension(NoteDao.class, dao -> dao.find(id));
    }

    @Override
    public void add(Note note) {
        jdbi.withExtension(NoteDao.class, dao -> {
            dao.add(note);
            return null;
        });
    }

    @Override
    public List<Note> getAll() {
        return jdbi.withExtension(NoteDao.class, NoteDao::getAll);
    }

    // https://github.com/jdbi/jdbi/issues/1822
    private static class RecordAndAnnotatedConstructorMapper
            implements RowMapperFactory
    {
        @Override
        public Optional<RowMapper<?>> build(Type type, ConfigRegistry config)
        {
            if ((type instanceof Class<?> clazz) && clazz.isRecord()) {
                return ConstructorMapper.factory(clazz).build(type, config);
            }
            return Optional.empty();
        }
    }
    private interface NoteDao {
        @SqlUpdate("CREATE TABLE IF NOT EXISTS note(id Integer, text Text)")
        void createNotesTable();

        @SqlQuery("SELECT * FROM \"note\" WHERE id=:id ORDER BY \"id\"")
        List<Note> findById(@Bind("id") Integer id);

        default Optional<Note> find(Integer id) {
            return findById(id).stream().findFirst();
        }

        @SqlUpdate("INSERT INTO \"note\" (id, \"text\") VALUES (:id, :text)")
        void add(@BindMethods Note note);

        @SqlQuery("SELECT * FROM \"note\" ORDER BY \"id\"")
        List<Note> getAll();
    }
}
