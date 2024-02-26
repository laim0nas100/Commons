package lt.lb.commons.jpa;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import lt.lb.uncheckedutils.PassableException;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;
import lt.lb.uncheckedutils.func.UncheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laim0nas100
 */
public class DelegatedDataSource implements DataSource {

    private static final Logger log = LoggerFactory.getLogger(DelegatedDataSource.class);

    protected long sleepTimeMills = 3000;

    protected final UncheckedSupplier<DataSource> realSource;

    protected final UncheckedSupplier<Boolean> closing;

    protected final AtomicReference<DataSource> ref = new AtomicReference<>();

    protected final AtomicBoolean inInit = new AtomicBoolean(false);

    public DelegatedDataSource(UncheckedSupplier<DataSource> realSource, UncheckedSupplier<Boolean> closing) {
        this.realSource = Objects.requireNonNull(realSource);
        this.closing = closing;
    }

    protected DataSource init(boolean wait) throws SQLException {
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

            if (wait) {
                boolean enough = false;
                while (inInit.get() && !enough) {
                    try {
                        Thread.sleep(sleepTimeMills);
                    } catch (InterruptedException ex) {
                        enough = true;
                        throw new RuntimeException("Interrupted");
                    }

                }
            }

        }
        if (!wait) {
            return ref.get();
        }

        return Objects.requireNonNull(ref.get(), "Failed to initialize delayed DataSource");
    }

    protected DataSource tryGet() throws SQLException {
        DataSource ds = ref.get();
        if (ds != null) {
            return ds;
        }
        return init(true);
    }

    public SafeOpt<Boolean> isConnected() {

        return SafeOpt.ofGet(() -> init(false))
                .map(m -> m.getConnection())
                .map(con -> {
                    con.close();
                    return true;
                });
    }

    private static boolean exceptionEqual(Throwable a, Throwable b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass().equals(b.getClass())) {
            return a.getMessage().equals(b.getMessage());
        }
        return false;
    }

    protected <T> T patientGet(UncheckedFunction<DataSource, ? extends T> mapper) throws SQLException {
        boolean enough = false;
        Throwable savedEx = new PassableException("Default");
        int times = 50;
        while (!enough && !closing.get()) {
            try {
                SafeOpt<? extends T> result = SafeOpt.ofGet(() -> tryGet()).map(mapper);
                if (result.hasError()) {
                    SafeOpt<Throwable> error = result.getError();
                    error.throwIfErrorUnwrapping(SQLFeatureNotSupportedException.class);
                    error.throwIfErrorUnwrapping(InterruptedException.class);
                    if (error.isPresent()) {
                        Throwable get = error.get();
                        if (!exceptionEqual(savedEx, get) || times <= 0) {
                            savedEx = get;
                            times = 50;
                            log.error("Error establishing connection", get);
                        } else {
                            times--;
                        }
                    }
                    Thread.sleep(sleepTimeMills);
                } else {
                    return result.orNull();
                }

            } catch (InterruptedException ex) {
                enough = true;
                throw new RuntimeException("Interrupted");
            }
        }
        throw new IllegalStateException("Is closing");

    }

    @Override
    public Connection getConnection() throws SQLException {
        return patientGet(DataSource::getConnection);

    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return patientGet(ds -> getConnection(username, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return patientGet(DataSource::getLogWriter);
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        patientGet(ds -> {
            ds.setLogWriter(out);
            return null;
        });
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        patientGet(ds -> {
            ds.setLoginTimeout(seconds);
            return null;
        });
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return patientGet(DataSource::getLoginTimeout);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return patientGet(ds -> ds.unwrap(iface));
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return patientGet(ds -> ds.isWrapperFor(iface));
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return SafeOpt.ofGet(() -> patientGet(m -> m.getParentLogger()))
                .throwIfErrorUnwrapping(SQLFeatureNotSupportedException.class).get();
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
}
