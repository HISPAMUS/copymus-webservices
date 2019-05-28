package es.ua.dlsi.copymus;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "copymus")
public class AppProperties {

	private String appPath;
	private String dbPath;
	private String databasesPath;
	private String annotationsPath;
	private String handwrittenPath;
	
	public String getAppPath() {
		return appPath;
	}
	public void setAppPath(String path) {
		this.appPath = path;
	}
	public String getDbPath() {
		return dbPath;
	}
	public void setDbPath(String db) {
		this.dbPath = db;
	}
	public String getDatabasesPath() {
		return databasesPath;
	}
	public void setDatabasesPath(String databases) {
		this.databasesPath = databases;
	}
	public String getAnnotationsPath() {
		return annotationsPath;
	}
	public void setAnnotationsPath(String annotations) {
		this.annotationsPath = annotations;
	}
	public String getHandwrittenPath() {
		return handwrittenPath;
	}
	public void setHandwrittenPath(String handwrittenPath) {
		this.handwrittenPath = handwrittenPath;
	}

}
