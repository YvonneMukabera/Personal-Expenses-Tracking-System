package org.first.personalexpensetrackingsystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DatabaseSchemaGuard implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSchemaGuard.class);

    private final JdbcTemplate jdbcTemplate;
    private final Set<String> managedTables;
    private final List<LegacyTableMigration> legacyTableMigrations;
    private final boolean archiveUnmanagedTables;
    private final String archiveSchema;
    private final boolean failOnUnmanagedTables;

    public DatabaseSchemaGuard(
            JdbcTemplate jdbcTemplate,
            @Value("${app.database.managed-tables}") String managedTables,
            @Value("${app.database.legacy-table-migrations:}") String legacyTableMigrations,
            @Value("${app.database.archive-unmanaged-tables:false}") boolean archiveUnmanagedTables,
            @Value("${app.database.archive-schema:}") String archiveSchema,
            @Value("${app.database.fail-on-unmanaged-tables:false}") boolean failOnUnmanagedTables
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.managedTables = Arrays.stream(managedTables.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.legacyTableMigrations = parseLegacyTableMigrations(legacyTableMigrations);
        this.archiveUnmanagedTables = archiveUnmanagedTables;
        this.archiveSchema = archiveSchema;
        this.failOnUnmanagedTables = failOnUnmanagedTables;
    }

    @Override
    public void run(ApplicationArguments args) {
        legacyTableMigrations.forEach(this::migrateAndArchiveLegacyTable);

        List<String> unmanagedTables = jdbcTemplate.queryForList(
                        """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = DATABASE()
                          AND table_type = 'BASE TABLE'
                        ORDER BY table_name
                        """,
                        String.class
                )
                .stream()
                .filter(tableName -> !managedTables.contains(tableName.toLowerCase()))
                .toList();

        if (unmanagedTables.isEmpty()) {
            return;
        }

        if (archiveUnmanagedTables) {
            unmanagedTables.forEach(this::archiveTable);
            return;
        }

        String message = "Database contains tables that are not mapped by project models: "
                + unmanagedTables
                + ". Managed model tables are: "
                + managedTables
                + ". Review src/main/resources/db/archive-unmanaged-tables.sql before moving archive tables.";

        if (failOnUnmanagedTables) {
            throw new IllegalStateException(message);
        }

        LOGGER.warn(message);
    }

    private void migrateAndArchiveLegacyTable(LegacyTableMigration migration) {
        if (!tableExists(migration.sourceTable()) || !tableExists(migration.targetTable())) {
            return;
        }

        copySharedRows(migration.sourceTable(), migration.targetTable());
        archiveTable(migration.sourceTable());
    }

    private void copySharedRows(String sourceTable, String targetTable) {
        List<String> sourceColumns = getColumns(sourceTable);
        Set<String> targetColumns = getColumns(targetTable).stream()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> sharedColumns = sourceColumns.stream()
                .filter(targetColumns::contains)
                .filter(column -> !"id".equalsIgnoreCase(column))
                .toList();

        if (sharedColumns.isEmpty()) {
            LOGGER.warn("Skipping data copy from {} to {} because no shared non-id columns were found.",
                    sourceTable, targetTable);
            return;
        }

        String columns = sharedColumns.stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));
        String sourceSelect = sharedColumns.stream()
                .map(column -> "source." + quoteIdentifier(column))
                .collect(Collectors.joining(", "));
        String duplicateCheck = sharedColumns.stream()
                .map(column -> "target." + quoteIdentifier(column) + " <=> source." + quoteIdentifier(column))
                .collect(Collectors.joining(" AND "));

        int copiedRows = jdbcTemplate.update("""
                INSERT INTO %s (%s)
                SELECT %s
                FROM %s source
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM %s target
                    WHERE %s
                )
                """.formatted(
                quoteIdentifier(targetTable),
                columns,
                sourceSelect,
                quoteIdentifier(sourceTable),
                quoteIdentifier(targetTable),
                duplicateCheck
        ));

        LOGGER.info("Copied {} row(s) from legacy table {} into {}.", copiedRows, sourceTable, targetTable);
    }

    private void archiveTable(String tableName) {
        if (!tableExists(tableName)) {
            return;
        }

        String currentSchema = currentSchema();
        String targetSchema = archiveSchema == null || archiveSchema.isBlank()
                ? currentSchema
                : archiveSchema;

        if (!targetSchema.equalsIgnoreCase(currentSchema)) {
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + quoteIdentifier(targetSchema));
        }

        String archiveName = getAvailableArchiveName(targetSchema, tableName);
        jdbcTemplate.execute("RENAME TABLE "
                + quoteIdentifier(currentSchema) + "." + quoteIdentifier(tableName)
                + " TO "
                + quoteIdentifier(targetSchema) + "." + quoteIdentifier(archiveName));
        LOGGER.warn("Moved unmanaged table {}.{} to {}.{}. No data was deleted.",
                currentSchema, tableName, targetSchema, archiveName);
    }

    private boolean tableExists(String tableName) {
        return tableExists(currentSchema(), tableName);
    }

    private boolean tableExists(String schema, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = ?
                  AND table_type = 'BASE TABLE'
                  AND LOWER(table_name) = LOWER(?)
                """,
                Integer.class,
                schema,
                tableName
        );
        return count != null && count > 0;
    }

    private List<String> getColumns(String tableName) {
        return jdbcTemplate.queryForList(
                """
                SELECT column_name
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND LOWER(table_name) = LOWER(?)
                ORDER BY ordinal_position
                """,
                String.class,
                tableName
        );
    }

    private String getAvailableArchiveName(String schema, String tableName) {
        String baseName = tableName.toLowerCase().startsWith("archived_")
                ? tableName
                : "archived_" + tableName + "_" + System.currentTimeMillis();
        String archiveName = baseName;
        int suffix = 1;

        while (tableExists(schema, archiveName)) {
            archiveName = baseName + "_" + suffix;
            suffix++;
        }

        return archiveName;
    }

    private String currentSchema() {
        return jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
    }

    private String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private List<LegacyTableMigration> parseLegacyTableMigrations(String migrations) {
        if (migrations == null || migrations.isBlank()) {
            return List.of();
        }

        return Arrays.stream(migrations.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.split(":"))
                .filter(parts -> parts.length == 2)
                .map(parts -> new LegacyTableMigration(parts[0].trim(), parts[1].trim()))
                .toList();
    }

    private record LegacyTableMigration(String sourceTable, String targetTable) {
    }
}
