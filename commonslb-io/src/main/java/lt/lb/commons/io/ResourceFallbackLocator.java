package lt.lb.commons.io;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laim0nas100
 */
public class ResourceFallbackLocator {

    static Logger logger = LoggerFactory.getLogger(ResourceFallbackLocator.class);

    public static URL loadResource(final String fileName) {
        if (fileName == null) {
            return null;
        }
        return Thread.currentThread().getContextClassLoader().getResource(fileName);
    }

    public static URL loadURL(final String s) {
        if (s == null) {
            return null;
        }
        try {
            return new URL(s);
        } catch (Exception ex) {
            logger.error("Failed to load " + s, ex);
        }
        return null;
    }

    public static URL loadPath(String path, String... more) {
        if (path == null) {
            return null;
        }

        try {
            return Paths.get(path, more).toUri().toURL();
        } catch (Exception ex) {
            StringBuilder s = new StringBuilder();
            s.append(path);
            for (String m : more) {
                s.append(File.separator).append(m);
            }

            logger.error("Failed to load " + s.toString(), ex);
        }
        return null;
    }

    public static class Loader implements Supplier<URL> {

        protected final Supplier<URL> supply;

        protected final String description;

        public Loader(String description, Supplier<URL> supply) {
            this.description = Objects.requireNonNull(description);
            this.supply = Objects.requireNonNull(supply);
        }

        @Override
        public URL get() {
            return supply.get();
        }

        public String getDescription() {
            return description;
        }

    }

    protected List<Loader> loaders;

    protected ResourceFallbackLocator(List<Loader> loaders, Loader loader) {
        this.loaders = new ArrayList<>(loaders);
        this.loaders.add(Objects.requireNonNull(loader));
    }

    public ResourceFallbackLocator() {
        this.loaders = new ArrayList<>();
    }

    public ResourceFallbackLocator withLoader(String description, Supplier<URL> loader) {
        return new ResourceFallbackLocator(loaders, new Loader(description, loader));
    }

    public ResourceFallbackLocator withSystemPropertyResource(String systemProperty) {
        Objects.requireNonNull(systemProperty);
        return withLoader("System property:" + systemProperty + " as resource", () -> {
            return loadResource(System.getProperty(systemProperty));
        });
    }

    public ResourceFallbackLocator withSystemPropertyURL(String systemProperty) {
        Objects.requireNonNull(systemProperty);
        return withLoader("System property:" + systemProperty + " as url", () -> {
            return loadURL(System.getProperty(systemProperty));
        });
    }
    
    public ResourceFallbackLocator withSystemPropertyPath(String systemProperty) {
        Objects.requireNonNull(systemProperty);
        return withLoader("System property:" + systemProperty + " as path", () -> {
            return loadPath(System.getProperty(systemProperty));
        });
    }

    public ResourceFallbackLocator withResource(String resource) {
        Objects.requireNonNull(resource);
        return withLoader("Resource:" + resource, () -> {
            return loadResource(resource);
        });
    }

    public ResourceFallbackLocator withURL(String url) {
        Objects.requireNonNull(url);
        return withLoader("URL:" + url, () -> {
            return loadURL(url);
        });
    }

    public ResourceFallbackLocator withPath(String path, String... more) {
        Objects.requireNonNull(path);
        return withLoader("Path:" + path, () -> {
            return loadPath(path, more);
        });
    }

    public List<Loader> getLoaders() {
        return Collections.unmodifiableList(loaders);
    }

    public Loader getFirstSuccessfulLoader() {
        for (Loader loader : loaders) {
            URL get = loader.get();
            if (get != null) {
                return loader;
            }
        }
        return null;
    }

    public URL load() {
        for (Loader loader : loaders) {
            URL get = loader.get();
            if (get != null) {
                return get;
            }
        }
        return null;
    }

}
