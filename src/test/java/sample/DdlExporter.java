package sample;

import java.io.IOException;
import java.nio.file.*;

import org.hibernate.boot.*;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.*;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import sample.context.orm.DefaultRepository.*;
import sample.context.orm.OrmRepository.OrmNamingStrategy;

/**
 * Entity 定義を元に DDL を生成します。  
 * <p>モデルファーストで開発していきたいときなどに利用して下さい。
 * <p>現状 Hibernate 5 で Bean Validation をソース元としたカラム定義がうまく出力されなくなってしまっています。( Hibernate 4 + Configuration だとうまくいっていた)<br>
 * http://stackoverflow.com/questions/32090535/hibernate-5-and-spring-generate-ddl-using-schemaexport
 * <p>前述のやり取りを見る限りでは Bean Validation よりも JPA のアノテーションで明示すべきとのことなので、適切な DDL を自動生成したいなら以下のような JPA 列定義で
 * 桁数等の指定を明示的におこなうようにしてください。 ( DDL を自動生成しないのであれば気にしないで OK )
 * <pre>
 *  e.g. @Column(length = 32, nullable = true)
 * </pre>
 */
public class DdlExporter {

    private static final String PackageRoot = "sample";
    private static final String PackageDefault = PackageRoot + ".model";
    private static final String PackageSystem = PackageRoot + ".context";
    private static final String OrmDialect = "org.hibernate.dialect.H2Dialect";
    private static final String OutputRoot = "build/";

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
            MetadataImplementor metadata = metadata(sfBean);
            
            SchemaExport export = new SchemaExport(serviceRegistry, metadata, false);
            export.setDelimiter(";");
            export.setOutputFile(outputFile);
            export.create(true, false);
        } catch (Exception e) {
            throw new InvocationException(e);
        } finally {
            StandardServiceRegistryBuilder.destroy( serviceRegistry );
        }
    }

    private LocalSessionFactoryBean sfBean(String packageName, String dialect) {
        DefaultRepositoryConfig config = new DefaultRepositoryConfig();
        config.setShowSql(false);
        config.setPackageToScan(packageName);
        config.setDialect(dialect);
        config.getProperties().put("hibernate.hbm2ddl.auto", "none");
        LocalSessionFactoryBean sfBean = config.sessionFactory(null, null);
        try {
            sfBean.afterPropertiesSet();
        } catch (IOException e) {
            throw new InvocationException(e);
        }
        return sfBean;
    }
    
    private MetadataImplementor metadata(LocalSessionFactoryBean sfBean) throws Exception {
        MetadataSources metadataSources = sfBean.getMetadataSources();
        Metadata metadata = metadataSources
                .getMetadataBuilder()
                .applyPhysicalNamingStrategy(new OrmNamingStrategy())
                .applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE)
                .build();
        return (MetadataImplementor) metadata;
    }

}
