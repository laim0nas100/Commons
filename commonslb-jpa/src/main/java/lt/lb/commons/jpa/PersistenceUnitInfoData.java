package lt.lb.commons.jpa;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoData implements PersistenceUnitInfo {

    protected String persistenceXMLSchemaVersion = "2.1";
    protected String persistenceProviderClassName = "org.hibernate.jpa.HibernatePersistenceProvider";
    protected String persistenceUnitName;
    protected PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
    protected List<String> managedClassNames = new ArrayList<>();
    protected List<String> mappingFileNames = new ArrayList<>();
    protected List<URL> jarFileUrls = new ArrayList<>();
    protected Properties properties;
    protected DataSource jtaDataSource;
    protected DataSource nonJtaDataSource;
    protected URL persistenceUnitRootUrl;
    protected List<ClassTransformer> transformers = new ArrayList<>();
    protected boolean excludeUnlistedClasses = false;
    protected SharedCacheMode sharedCacheMode = SharedCacheMode.UNSPECIFIED;
    protected ValidationMode validationMode = ValidationMode.AUTO;
    protected Supplier<ClassLoader> classloader = () -> Thread.currentThread().getContextClassLoader();
    protected Supplier<ClassLoader> newTempClassLoader = () -> null;

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    public void setJtaDataSource(DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
        this.nonJtaDataSource = null;
        transactionType = PersistenceUnitTransactionType.JTA;
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
        this.jtaDataSource = null;
        transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    @Override
    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    @Override
    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return sharedCacheMode;
    }

    @Override
    public ValidationMode getValidationMode() {
        return validationMode;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return persistenceXMLSchemaVersion;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classloader.get();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        transformers.add(transformer);
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return this.newTempClassLoader.get();
    }

    public Supplier<ClassLoader> getClassloader() {
        return classloader;
    }

    public List<ClassTransformer> getTransformers() {
        return transformers;
    }

    public void setTransformers(List<ClassTransformer> transformers) {
        this.transformers = transformers;
    }

    public boolean isExcludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion) {
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }

    public void setPersistenceProviderClassName(String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public void setTransactionType(PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setManagedClassNames(List<String> managedClassNames) {
        this.managedClassNames = managedClassNames;
    }

    public void setMappingFileNames(List<String> mappingFileNames) {
        this.mappingFileNames = mappingFileNames;
    }

    public void setJarFileUrls(List<URL> jarFileUrls) {
        this.jarFileUrls = jarFileUrls;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    public void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public void setClassloader(Supplier<ClassLoader> classloader) {
        this.classloader = classloader;
    }

    public void setNewTempClassLoader(Supplier<ClassLoader> newTempClassLoader) {
        this.newTempClassLoader = newTempClassLoader;
    }
    
    
    
    
    
    

}
