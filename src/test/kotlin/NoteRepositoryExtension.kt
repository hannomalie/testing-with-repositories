import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.hanno.note.NoteRepository
import de.hanno.note.InMemoryNoteRepository
import de.hanno.note.PostgresNoteRepository
import org.junit.jupiter.api.extension.*
import org.testcontainers.containers.PostgreSQLContainer
import java.lang.reflect.Method

class NoteRepositoryExtension : ParameterResolver, BeforeEachCallback, AfterEachCallback {
    private var preferInMemory = (System.getProperties().getOrDefault("test.prefer-in-memory", "true") as String).toBoolean()
    private val containers: MutableMap<Method, PostgreSQLContainer<*>> = HashMap()
    private fun createDataSourceForContainer(container: PostgreSQLContainer<*>) = HikariDataSource(HikariConfig().apply {
        setJdbcUrl(container.getJdbcUrl())
        username = container.username
        password = container.password
        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    })

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val type = parameterContext.parameter.getType()
        return type == NoteRepository::class.java || type == InMemoryNoteRepository::class.java || type == PostgresNoteRepository::class.java
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ) = when (val type = parameterContext.parameter.getType()) {
        InMemoryNoteRepository::class.java -> InMemoryNoteRepository()
        PostgresNoteRepository::class.java -> {
            val container = containers[extensionContext.testMethod.get()]!!
            PostgresNoteRepository(createDataSourceForContainer(container))
        }

        NoteRepository::class.java -> if (preferInMemory) {
            InMemoryNoteRepository()
        } else {
            val container = containers[extensionContext.testMethod.get()]!!
            PostgresNoteRepository(createDataSourceForContainer(container))
        }

        else -> throw IllegalStateException("Cannot resolve parameter of type $type")
    }

    override fun afterEach(context: ExtensionContext) {
        context.testMethod.ifPresent { it: Method -> containers[it]?.stop() }
    }

    override fun beforeEach(context: ExtensionContext) {
        context.testMethod.ifPresent { it: Method ->
            for (param in it.parameters) {
                if (param.getType() == PostgresNoteRepository::class.java) {
                    startPostgresContainer(it)
                } else if (!preferInMemory && param.getType() == NoteRepository::class.java) {
                    startPostgresContainer(it)
                }
            }
        }
    }

    private fun startPostgresContainer(method: Method) {
        containers[method] = PostgreSQLContainer("postgres:15-alpine").apply {
            start()
        }
    }
}
