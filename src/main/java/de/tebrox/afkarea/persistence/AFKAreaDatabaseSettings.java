package de.tebrox.afkarea.persistence;

import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.vertexCore.database.DatabaseSettings;

public class AFKAreaDatabaseSettings implements DatabaseSettings {
    private final String backend;
    private final boolean useQueue;
    private final long timeoutMillis;
    private final int poolSize;
    private final String tablePrefix;
    private final String mysqlUrl;
    private final String mysqlUser;
    private final String mysqlPassword;

    public AFKAreaDatabaseSettings(AFKAreaConfig config) {
        this.backend = config.databaseBackend;
        this.useQueue = config.databaseUseQueue;
        this.timeoutMillis = config.databaseTimeoutMillis;
        this.poolSize = config.databasePoolSize;
        this.tablePrefix = config.databaseTablePrefix;
        this.mysqlUrl = config.databaseMysqlUrl;
        this.mysqlUser = config.databaseMysqlUser;
        this.mysqlPassword = config.databaseMysqlPassword;
    }

    @Override
    public String backend() {
        return backend;
    }

    @Override
    public boolean useQueue() {
        return useQueue;
    }

    @Override
    public long timeoutMillis() {
        return timeoutMillis;
    }

    @Override
    public int poolSize() {
        return poolSize;
    }

    @Override
    public String mysqlUrl() {
        return mysqlUrl;
    }

    @Override
    public String mysqlUser() {
        return mysqlUser;
    }

    @Override
    public String mysqlPassword() {
        return mysqlPassword;
    }

    @Override
    public String tablePrefix() {
        return tablePrefix;
    }
}
