package com.pivotal.task.gpdb.gpstager.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.pivotal.task.gpdb.database.GreenplumDatabase;

@EnableTask
@EnableBatchProcessing
@Configuration
@EnableConfigurationProperties({ GpstagerTaskBatchProperties.class })
public class GpdbJobConfiguration {
	private static final Log logger = LogFactory.getLog(GpdbJobConfiguration.class);

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private GpstagerTaskBatchProperties config;
	
	@Autowired
	private GreenplumDatabase greenplum;

	@Bean
	public Job job1() {
		return jobBuilderFactory.get("generateExternalTable").incrementer(new RunIdIncrementer())
				.start(stepBuilderFactory.get("job1step1")
						.tasklet(new Tasklet() {
							@Override
							public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
									throws Exception {
								greenplum.executeUpdate("DROP EXTERNAL TABLE IF EXISTS " + config.getExtTableName());
								greenplum.executeUpdate(generateExtDDL());
								return RepeatStatus.FINISHED;
							}
						})
						.build())
				.build();
	}

	@Bean
	public Job job2() {
		return jobBuilderFactory.get("loadIntoStagingTable").incrementer(new RunIdIncrementer())
				.start(stepBuilderFactory.get("job2step1")
						.tasklet(new Tasklet() {
							@Override
							public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
									throws Exception {
								greenplum.executeUpdate("DROP TABLE IF EXISTS " + config.getDimTableName());
								greenplum.executeUpdate(generateLoadDML());
								return RepeatStatus.FINISHED;
							}
						})
						.build())
				.build();
	}

	/**
	 * An example looks like this:
	 * CREATE EXTERNAL TABLE ext_expenses ( name text, 
   			date date, amount float4, category text, desc1 text ) 
   			LOCATION ('gpfdist://etlhost-1:8081/*.txt', 
             'gpfdist://etlhost-2:8082/*.txt')
   			FORMAT 'TEXT' ( DELIMITER '|' NULL ' ')
   			LOG ERRORS SEGMENT REJECT LIMIT 5;
	 * @return
	 */
	private String generateExtDDL() {
		String ddl = "CREATE EXTERNAL TABLE " + config.getExtTableName() + " (";
		
		StringBuilder sb = new StringBuilder();
		
		for (int i=0;i<config.getAttrList().length-1;i++) {
			sb.append(config.getAttrList()[i] + " TEXT, ");
		}
		sb.append(config.getAttrList()[config.getAttrList().length-1] + " TEXT) ");
		
		String proto;
		if (config.getSecureProtocol())
			proto = "gpfdists";
		else
			proto = "gpfdist";
		
		ddl += sb.toString();
		ddl += " LOCATION ('" + proto + "://" + config.getGpfDistServerList()[0] + config.getRelativeFilePaths()[0] + 
				"') FORMAT '" + config.getFileFormat() + "' (DELIMITER '"+config.getDelimiter()+
				"' NULL '"+config.getNullValue()+"') "+
				config.getLogErrorClause();
		logger.info("Generated external table ddl: " + ddl);
		return ddl;
	}
	
	private String generateLoadDML() {
		String ddl = "CREATE TABLE " + config.getDimTableName() + " AS SELECT * FROM " + config.getExtTableName() + ";";
		logger.info("Generated load table dml: " + ddl);
		return ddl;
	}
}
