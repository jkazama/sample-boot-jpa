package sample;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.boot.orm.jpa.hibernate.*;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 * Generate DDL by Entity definition.
 * <p>Use it at time when you want to develop it in the model first.
 * <p>Set the next if you want to output the column definition appropriately.
 * <pre>
 *  e.g. {@literal @}Column(length = 32, nullable = true)
 * </pre>
 */
public class DdlExporter {

    private static final String PackageRoot = "sample";
    private static final String PackageDefault = PackageRoot + ".model";
    private static final String PackageSystem = PackageRoot + ".context";
    private static final String OrmDialect = "org.hibernate.dialect.H2Dialect";
    private static final String OutputRoot = "build/";
    private static final boolean FormatSql = false;

    public static void main(String[] args) {
        DdlExporter exporter = new DdlExporter();
        exporter.outputDdl(PackageSystem, OrmDialect, "ddl-system.sql");
        exporter.outputDdl(PackageDefault, OrmDialect, "ddl-default.sql");
    }

    private void outputDdl(String packageName, String dialect, String fileName) {
        LocalSessionFactoryBean sfBean = sfBean(packageName, dialect);
        StandardServiceRegistry serviceRegistry = sfBean.getConfiguration().getStandardServiceRegistryBuilder().build();
        try {
            String outputFile = OutputRoot + fileName;
            Files.deleteIfExists(Paths.get(outputFile));
            MetadataImplementor metadata = metadata(sfBean, serviceRegistry);
            
            SchemaExport export = new SchemaExport();
            export.setDelimiter(";");
            export.setFormat(FormatSql);
            export.setOutputFile(outputFile);
            export.create(EnumSet.of(TargetType.SCRIPT), metadata);
        } catch (Exception e) {
            throw new InvocationException(e);
        } finally {
            StandardServiceRegistryBuilder.destroy( serviceRegistry );
        }
    }

    private LocalSessionFactoryBean sfBean(String packageName, String dialect) {
        LocalSessionFactoryBean sfBean = new LocalSessionFactoryBean();
        try {
            sfBean.setPackagesToScan(packageName);
            sfBean.setHibernateProperties(hibernateProperties(dialect));
            sfBean.afterPropertiesSet();
        } catch (IOException e) {
            throw new InvocationException(e);
        }
        return sfBean;
    }
    
    private Properties hibernateProperties(String dialect) {
        Properties props = new Properties();
        props.put("hibernate.dialect", dialect);
        props.put("hibernate.show_sql", false);
        props.put("hibernate.hbm2ddl.auto", "none");
        return props;
    }

    private MetadataImplementor metadata(LocalSessionFactoryBean sfBean, StandardServiceRegistry registry) throws Exception {
        MetadataSources metadataSources = sfBean.getMetadataSources();
        Metadata metadata = metadataSources
                .getMetadataBuilder(registry)
                .applyPhysicalNamingStrategy(new SpringPhysicalNamingStrategy())
                .applyImplicitNamingStrategy(new SpringImplicitNamingStrategy())
                .build();
        return (MetadataImplementor) metadata;
    }

}
