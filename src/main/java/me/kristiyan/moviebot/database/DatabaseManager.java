package me.kristiyan.moviebot.database;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.kristiyan.moviebot.database.entities.activity.BotUsageEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGameEntity;
import me.kristiyan.moviebot.database.entities.games.ActiveGuessEntity;
import me.kristiyan.moviebot.database.entities.leaderboards.MemberStatusEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieCategoryEntity;
import me.kristiyan.moviebot.database.entities.movie.MovieEntity;
import me.kristiyan.moviebot.database.entities.premium.PremiumUserEntity;
import me.kristiyan.moviebot.database.entities.reports.MovieReportEntity;
import me.kristiyan.moviebot.database.entities.updates.UpdateAnnouncementEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private SessionFactory sessionFactory;

    public DatabaseManager() {
        // Initialize HikariCP connection pool
        HikariConfig config = new HikariConfig();

        String dbHost = System.getenv("POSTGRES_DB_HOST");
        String dbPortStr = System.getenv("POSTGRES_DB_PORT");
        String dbUser = System.getenv("POSTGRES_DB_USERNAME");
        String dbPassword = System.getenv("POSTGRES_DB_PASSWORD");
        String dbName = System.getenv("POSTGRES_DB_NAME");

        Integer dbPort = (dbPortStr != null) ? Integer.parseInt(dbPortStr) : null;
        // Validate environment variables
        if (dbHost == null || dbPort == null || dbUser == null || dbPassword == null || dbName == null) {
            throw new RuntimeException("Missing required database environment variables. " +
                    "Required: POSTGRES_DB_HOST, POSTGRES_DB_PORT, POSTGRES_DB_USERNAME, POSTGRES_DB_PASSWORD, POSTGRES_DB_NAME");
        }

        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", dbHost, dbPort, dbName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);

        Integer hikariCPMaxPoolSize = Integer.getInteger(System.getenv("HIKARICP_MAX_POOL_SIZE"));
        Integer hikariCPMinimumIdle = Integer.getInteger(System.getenv("HIKARICP_MINIMUM_IDLE"));
        Integer hikariCPConnectionTimeout = Integer.getInteger(System.getenv("HIKARICP_CONNECTION_TIMEOUT"));
        Integer hikariCPIdleTimeout = Integer.getInteger(System.getenv("HIKARICP_IDLE_TIMEOUT"));
        Integer hikariCPMaxLifetime = Integer.getInteger(System.getenv("HIKARICP_MAX_LIFETIME"));

        // HikariCP settings
        config.setMaximumPoolSize(hikariCPMaxPoolSize == null ? 10 : hikariCPMaxPoolSize);
        config.setMinimumIdle(hikariCPMinimumIdle  == null ? 2 : hikariCPMinimumIdle);
        config.setConnectionTimeout(hikariCPConnectionTimeout == null ? 30000 : hikariCPConnectionTimeout);
        config.setIdleTimeout(hikariCPIdleTimeout == null ? 60000 : hikariCPIdleTimeout);
        config.setMaxLifetime(hikariCPMaxLifetime == null ? 1800000 : hikariCPMaxLifetime);

        dataSource = new HikariDataSource(config);

        String hibernate_hbm2ddl_auto = System.getenv("HIBERNATE_HDM2DDL_AUTO");
        String showSqlEnv = System.getenv("HIBERNATE_SHOW_SQL");
        String orderInsertsEnv = System.getenv("HIBERNATE_ORDER_INSERTS");
        String orderUpdatesEnv = System.getenv("HIBERNATE_ORDER_UPDATES");
        String batchSizeEnv = System.getenv("HIBERNATE_JDBC_BATCH_SIZE");

        setEntityManagerFactory(dataSource,
                hibernate_hbm2ddl_auto == null ? "update" : hibernate_hbm2ddl_auto,
                showSqlEnv != null && showSqlEnv.equals("true"),
                batchSizeEnv == null ? "50" : batchSizeEnv,
                orderInsertsEnv != null && orderInsertsEnv.equals("true"),
                orderUpdatesEnv != null && orderUpdatesEnv.equals("true"));
        System.out.println("Database initialized successfully!");

        logConfig();
    }

    private void setEntityManagerFactory(DataSource dataSource,
                                               String hibernate_hbm2ddl_auto,
                                               boolean showSQl,
                                         String hibernate_jdbc_batch_size,
                                         boolean hibernate_order_inserts,
                                         boolean hibernate_order_updates) {
        if (sessionFactory == null) {
            Configuration configuration = new Configuration();
            configuration.addAnnotatedClass(ActiveGameEntity.class);
            configuration.addAnnotatedClass(ActiveGuessEntity.class);

            configuration.addAnnotatedClass(MemberStatusEntity.class);

            configuration.addAnnotatedClass(MovieEntity.class);
            configuration.addAnnotatedClass(MovieCategoryEntity.class);

            configuration.addAnnotatedClass(PremiumUserEntity.class);

            configuration.addAnnotatedClass(UpdateAnnouncementEntity.class);

            configuration.addAnnotatedClass(MovieReportEntity.class);

            configuration.addAnnotatedClass(BotUsageEntity.class);

            sessionFactory = configuration.buildSessionFactory(new StandardServiceRegistryBuilder()
                    // Add standard properties
                    .applySetting("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto)
                    .applySetting("hibernate.show_sql", showSQl ? "true" : "false")
                    .applySetting("hibernate.format_sql", "true")
                    .applySetting("hibernate.jdbc.batch_size", hibernate_jdbc_batch_size)
                    .applySetting("hibernate.order_inserts", hibernate_order_inserts ? "true" : "false")
                    .applySetting("hibernate.order_updates", hibernate_order_updates ? "true" : "false")
                    .applySetting(org.hibernate.cfg.JdbcSettings.JAKARTA_NON_JTA_DATASOURCE, dataSource)
                    .build());
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }


    public void shutdown() {
        // 1. Close Hibernate first
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        // 2. Close the Pool last
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database and JPA resources shut down.");
        }
    }

    public static void logConfig() {
        // Check if we are actually using Logback to prevent the ClassCastException
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof LoggerContext context)) {
            System.err.println("Logback not found on classpath. Current factory: " + factory.getClass().getName());
            return;
        }

        context.reset();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();

        // 1. Console Appender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        // 2. Rolling File Appender (1MB limit)
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(context);
        rollingFileAppender.setName("ErrorFile");
        rollingFileAppender.setFile("logs/error_reports.log"); // Active log file

        // Policy: Roll over when file reaches 1MB
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(context);
        triggeringPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("1MB"));
        triggeringPolicy.start();

        // Policy: How to name the archived files (e.g., error_reports.log.0.gz)
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern("logs/archive/error_reports.%i.log.zip"); // Compresses old logs
        rollingPolicy.setMinIndex(1);
        rollingPolicy.start();

        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.start();

        // 3. Attach to Root
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(rollingFileAppender);
        rootLogger.setLevel(ch.qos.logback.classic.Level.INFO);

        System.out.println("Logging config initialized with 1MB rolling files!");
    }

    /*
    private static void createTables() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();

            // Table for movies
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movies (
                    id SERIAL PRIMARY KEY,
                    title TEXT NOT NULL UNIQUE,
                    gif_url TEXT NOT NULL,
                    genre VARCHAR(50) NOT NULL,
                    movie_year INT NOT NULL,
                    rating DECIMAL(3, 1) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Movie categories for flexible categorization (comedy, fantasy, family, etc.)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movie_categories (
                    movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
                    category VARCHAR(30) NOT NULL,
                    PRIMARY KEY (movie_id, category)
                )
            """);

            // Table to track current game session for each guild
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS active_games (
                    guild_id TEXT PRIMARY KEY,
                    movie_id INTEGER NOT NULL REFERENCES movies(id),
                    current_round INTEGER DEFAULT 1,
                    total_rounds INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Table to track who has already guessed in current round
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS active_guesses (
                    guild_id TEXT NOT NULL,
                    member_id TEXT NOT NULL,
                    round_number INTEGER NOT NULL,
                    guessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (guild_id, member_id, round_number)
                )
            """);

            // Table for guild leaderboard
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS guild_leaderboard (
                    guild_id TEXT PRIMARY KEY,
                    guild_name TEXT NOT NULL,
                    correct_guesses INTEGER DEFAULT 0
                )
            """);

            // Flexible member statistics table - tracks all types of guesses
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS member_stats (
                    guild_id TEXT NOT NULL,
                    member_id TEXT NOT NULL,
                    member_nickname TEXT NOT NULL,
                    stat_type VARCHAR(30) NOT NULL,
                    correct_guesses INTEGER DEFAULT 0,
                    PRIMARY KEY (guild_id, member_id, stat_type)
                )
            """);

            // Updates table for bot announcements
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS updates (
                    id SMALLSERIAL PRIMARY KEY,
                    update_released_at TIMESTAMP NOT NULL DEFAULT NOW(),
                    title VARCHAR(255) NOT NULL,
                    short_description VARCHAR(1000) NOT NULL
                )
            """);

            // Premium codes table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS premium_codes (
                    code VARCHAR(20) PRIMARY KEY
                )
            """);

            // Premium users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS premium_users (
                    user_id BIGINT PRIMARY KEY,
                    activated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                    expires_at TIMESTAMP NOT NULL,
                    activation_code VARCHAR(20)
                )
            """);

            // Create indexes for better query performance

            // Movies indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_movies_genre ON movies(genre)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_movies_year ON movies(year)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_movies_rating ON movies(rating)");

            // Movie categories indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_movie_categories_category ON movie_categories(category)");

            // Active games indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active_games_guild ON active_games(guild_id)");

            // Active guesses indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active_guesses_guild ON active_guesses(guild_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active_guesses_round ON active_guesses(guild_id, round_number)");

            // Leaderboard indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_guild_leaderboard_score ON guild_leaderboard(correct_guesses DESC)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_member_stats_guild ON member_stats(guild_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_member_stats_type ON member_stats(guild_id, stat_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_member_stats_score ON member_stats(guild_id, stat_type, correct_guesses DESC)");

            // Premium indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_premium_users_user_id ON premium_users(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_premium_users_expires ON premium_users(expires_at)");

            // Updates indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_updates_released ON updates(update_released_at DESC)");

        } catch (SQLException e) {
            System.err.println("Database table creation error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create database tables", e);
        }
    }

     */
}