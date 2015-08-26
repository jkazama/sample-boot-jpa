package sample;

import java.io.IOException;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.boot.*;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import sample.context.orm.DefaultRepository.DefaultRepositoryConfig;
import sample.context.orm.OrmRepository.OrmNamingStrategy;

/** DDL生成ツール */
public class DdlExporter {

	private static final String packageRoot = "sample";
	private static final String packageDefault = packageRoot + ".model";
	private static final String packageSystem = packageRoot + ".context";
	private static final String ormDialect = "org.hibernate.dialect.H2Dialect";
	private static final String outputRoot = "build/";
	
	public static void main(String[] args) {
		DdlExporter exporter = new DdlExporter();
		exporter.outputDdl(packageDefault, ormDialect, "ddl-system.sql");
		exporter.outputDdl(packageSystem, ormDialect, "ddl-default.sql");
	}

	public void outputDdl(String packageName, String dialect, String fileName) {
		try {
			new SchemaExport(metadata(packageName, dialect)).setOutputFile(outputRoot + fileName).setDelimiter(";").create(false, false);
		} catch (Exception e) {
			throw new InvocationException(e);
		}
	}

	// 現状hibernate5でアノテーション制約がうまく出なくなってしまっている。(hibernate4 + Configurationだとうまくいっていた)
	// http://stackoverflow.com/questions/32090535/hibernate-5-and-spring-generate-ddl-using-schemaexport
	// low: 解決方法が判明したら直す
	private MetadataImplementor metadata(String packageName, String dialect) throws Exception {
		DefaultRepositoryConfig config = new DefaultRepositoryConfig();
		config.setShowSql(false);
		config.setCreateDrop(false);
		config.setPackageToScan(packageName);
		config.setDialect(dialect);
		LocalSessionFactoryBean sfBean = config.sessionFactory(null, null);
		try {
			sfBean.afterPropertiesSet();
		} catch (IOException e) {
			throw new InvocationException(e);
		}
		Configuration configuration = sfBean.getConfiguration();
		// MetadataImplementorを無理やり取得
		MetadataSources metadataSources = (MetadataSources) FieldUtils.readField(configuration, "metadataSources", true);
		Metadata metadata = metadataSources
		    .getMetadataBuilder(configuration.getStandardServiceRegistryBuilder().build())
		    .applyPhysicalNamingStrategy(new OrmNamingStrategy())
		    .applyImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE)
		    .build();
		return (MetadataImplementor) metadata;
	}
	
}
