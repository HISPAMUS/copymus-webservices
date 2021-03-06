package es.ua.dlsi.copymus.models;

import java.sql.Timestamp;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Invalidation {
	// https://www.callicoder.com/hibernate-spring-boot-jpa-composite-primary-key-example/
	@EmbeddedId
	private AnnotationIdentity annotationIdentity;
	
	private Timestamp creationDate;
	
	public Invalidation() {
		initDate();
	}
	
	public Invalidation(AnnotationIdentity annotationIdentity) {
		initDate();
		this.annotationIdentity = annotationIdentity;
	}
	
	private void initDate() {
		this.setCreationDate(new Timestamp(System.currentTimeMillis()));
	}
	
	@Override
	public String toString() {
		return "Invalidation [user_id=" + annotationIdentity.getUserId() + ", score_id=" + annotationIdentity.getScoreId() + "]";
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
}
