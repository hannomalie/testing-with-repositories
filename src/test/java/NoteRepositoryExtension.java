import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.hanno.note.NoteRepository;
import de.hanno.note.NoteRepository.InMemoryNoteRepository;
import de.hanno.note.PostgresNoteRepository;
import org.junit.jupiter.api.extension.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class NoteRepositoryExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback {
    boolean preferInMemory = Boolean.parseBoolean((String) System.getProperties().getOrDefault("test.prefer-in-memory", "true"));
    private final Map<Method, PostgreSQLContainer<?>> containers = new HashMap<>();

    private HikariDataSource createDataSourceForContainer(PostgreSQLContainer<?> container) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return type.equals(NoteRepository.class) || type.equals(InMemoryNoteRepository.class) || type.equals(PostgresNoteRepository.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        if (type.equals(InMemoryNoteRepository.class)) {
            return new InMemoryNoteRepository();
        } else if (type.equals(PostgresNoteRepository.class)) {
            var container = containers.get(extensionContext.getTestMethod().get());
            return new PostgresNoteRepository(createDataSourceForContainer(container));
        } else if (type.equals(NoteRepository.class)) {
            if (preferInMemory) {
                return new InMemoryNoteRepository();
            } else {
                var container = containers.get(extensionContext.getTestMethod().get());
                return new PostgresNoteRepository(createDataSourceForContainer(container));
            }
        }
        throw new IllegalStateException("Cannot resolve parameter of type " + type);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getTestMethod().ifPresent(it -> {
            var container = containers.get(it);
            if (container != null) {
                container.stop();
            }
        });
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestMethod().ifPresent(it -> {
            for (Parameter param : it.getParameters()) {
                if (param.getType().equals(PostgresNoteRepository.class)) {
                    startPostgresContainer(it);
                } else if (!preferInMemory && param.getType().equals(NoteRepository.class)) {
                    startPostgresContainer(it);
                }
            }
        });
    }

    private void startPostgresContainer(Method method) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
        postgres.start();
        containers.put(method, postgres);
    }
}
