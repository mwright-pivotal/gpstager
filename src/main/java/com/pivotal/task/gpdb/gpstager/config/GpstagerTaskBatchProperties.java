package com.pivotal.task.gpdb.gpstager.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@ConfigurationProperties("gpstager")
public class GpstagerTaskBatchProperties {
	private static final String GPDIST_PROTO="gpfdist://";
	private static final String GPDISTS_PROTO="gpfdists://";
	
	private String gpdbHost = "localhost";
	private String gpdbPort = "5432";
	private String gpdbUser = "gpadmin";
	private String gpdbPassword = "pivotal";
	
	public String getGpdbHost() {
		return gpdbHost;
	}
	public void setGpdbHost(String gpdbHost) {
		this.gpdbHost = gpdbHost;
	}
	public String getGpdbPort() {
		return gpdbPort;
	}
	public void setGpdbPort(String gpdbPort) {
		this.gpdbPort = gpdbPort;
	}
	public String getGpdbUser() {
		return gpdbUser;
	}
	public void setGpdbUser(String gpdbUser) {
		this.gpdbUser = gpdbUser;
	}
	public String getGpdbPassword() {
		return gpdbPassword;
	}
	public void setGpdbPassword(String gpdbPassword) {
		this.gpdbPassword = gpdbPassword;
	}
	private String extTableName;
	private List<String> gpfDistServerList = new ArrayList<String>();
	private Boolean secureProtocol = false;
	private List<String> relativeFilePaths = new ArrayList<String>();
	
	public String getExtTableName() {
		Assert.hasText(extTableName, "format must not be empty nor null");
		return extTableName;
	}
	public void setExtTableName(String extTableName) {
		this.extTableName = extTableName;
	}
	public List<String> getGpfDistServerList() {
		Assert.notEmpty(gpfDistServerList,"Must provide at least one gpfdist server");
		return gpfDistServerList;
	}
	public void setGpfDistServerList(ArrayList<String> gpfDistServerList) {
		this.gpfDistServerList = gpfDistServerList;
	}
	public Boolean getSecureProtocol() {
		return secureProtocol;
	}
	public void setSecureProtocol(Boolean secureProtocol) {
		this.secureProtocol = secureProtocol;
	}
	public List<String> getRelativeFilePaths() {
		Assert.notEmpty(relativeFilePaths,"Must specify at least one file path");
		return relativeFilePaths;
	}
	public void setRelativeFilePaths(ArrayList<String> relativeFilePaths) {
		this.relativeFilePaths = relativeFilePaths;
	}
	public List<String> getAttrList() {
		Assert.notEmpty(attrList,"Must specify at least one column attribute");
		return attrList;
	}
	public void setAttrList(ArrayList<String> attrList) {
		this.attrList = attrList;
	}
	public String getFileFormat() {
		return fileFormat;
	}
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	public String getDelimiter() {
		return delimiter;
	}
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	public String getNullValue() {
		return nullValue;
	}
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}
	public String getLogErrorClause() {
		return logErrorClause;
	}
	public void setLogErrorClause(String logErrorClause) {
		this.logErrorClause = logErrorClause;
	}
	public String getDimTableName() {
		return dimTableName;
	}
	public void setDimTableName(String dimTableName) {
		this.dimTableName = dimTableName;
	}
	private List<String> attrList = new ArrayList<String>();
	private String fileFormat = "TEXT";
	private String delimiter = "|";
	private String nullValue = " ";
	private String logErrorClause = "LOG ERRORS SEGMENT REJECT LIMIT 5";
	private String dimTableName;
	
}
