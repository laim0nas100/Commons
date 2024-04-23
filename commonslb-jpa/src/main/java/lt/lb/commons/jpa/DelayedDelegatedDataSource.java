package lt.lb.commons.jpa;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedSupplier;

/**
 *
 * @author laim0nas100
 */
@Deprecated
public class DelayedDelegatedDataSource implements DataSource {

    protected long sleepTimeMills = 500;
    protected int sleepTries = 10;

    protected final UncheckedSupplier<DataSource> realSource;

    protected final AtomicReference<DataSource> ref = new AtomicReference<>();

    protected final AtomicBoolean inInit = new AtomicBoolean(false);

    public DelayedDelegatedDataSource(UncheckedSupplier<DataSource> realSource) {
        this.realSource = Objects.requireNonNull(realSource);
    }
    
    protected DataSource init() throws SQLException{
        if (inInit.compareAndSet(false, true)) { // try init
            SafeOpt<DataSource> safe = realSource.getSafe();
            safe.ifPresent(dataSource -> {
                ref.set(dataSource);
            });
            inInit.set(false);

            if (safe.hasError()) {
                safe.throwIfErrorUnwrapping(SQLException.class);
            }
            if (safe.isEmpty()) {
                throw new SQLException("Failed to initialize delayed DataSource");
            }
        } else {

            int tries = sleepTries;
            while (inInit.get() && tries > 0) {
                try {
                    tries--;
                    Thread.sleep(sleepTimeMills);
                } catch (InterruptedException ex) {
                    tries = 0;
                }

            }
        }

        return Objects.requireNonNull(ref.get(), "Failed to initialize delayed DataSource");
    }

    protected DataSource tryGet() throws SQLException {
        DataSource ds = ref.get();
        if (ds != null) {
            return ds;
        }
        return init();
    }

    protected SafeOpt<DataSource> tryGetSafe() {
        return SafeOpt.ofGet(() -> tryGet());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return tryGet().getConnection();

    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return tryGet().getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return tryGet().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        tryGet().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        tryGet().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return tryGet().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return tryGetSafe().map(m -> m.getParentLogger())
                .throwIfErrorUnwrapping(SQLFeatureNotSupportedException.class).get();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return tryGet().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return tryGet().isWrapperFor(iface);
    }

    public long getSleepTimeMills() {
        return sleepTimeMills;
    }

    public void setSleepTimeMills(long sleepTimeMills) {
        if (sleepTimeMills <= 0) {
            throw new IllegalArgumentException("Must be positive");
        }
        this.sleepTimeMills = sleepTimeMills;
    }

    public int getSleepTries() {
        return sleepTries;
    }

    public void setSleepTries(int sleepTries) {
        if (sleepTimeMills <= 0) {
            throw new IllegalArgumentException("Must be positive");
        }
        this.sleepTries = sleepTries;
    }

}
