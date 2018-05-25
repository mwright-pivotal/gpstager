package com.pivotal.task.gpdb.gpstager.config;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

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
	private JdbcTemplate gpdb;

	@Bean
	public Job job1() {
		return jobBuilderFactory.get("generateExternalTable")
				.start(stepBuilderFactory.get("job1step1")
						.tasklet(new Tasklet() {
							@Override
							public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
									throws Exception {
								gpdb.execute(generateExtDDL());
								return RepeatStatus.FINISHED;
							}
						})
						.build())
				.build();
	}

	@Bean
	public Job job2() {
		return jobBuilderFactory.get("loadIntoStagingTable")
				.start(stepBuilderFactory.get("job2step1")
						.tasklet(new Tasklet() {
							@Override
							public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
									throws Exception {
								gpdb.execute(generateLoadDML());
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
		
		for (String attr: config.getAttrList()) {
			sb.append(attr + "TEXT, ");
		}
		ddl += sb.toString();
		logger.info("Generated external table ddl: " + ddl);
		return ddl;
	}
	
	private String generateLoadDML() {
		String ddl = "CREATE TABLE " + config.getDimTableName() + " AS SELECT * FROM " + config.getExtTableName() + ";";
		logger.info("Generated load table dml: " + ddl);
		return ddl;
	}
}
