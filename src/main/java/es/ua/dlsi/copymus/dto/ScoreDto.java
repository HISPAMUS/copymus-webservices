package es.ua.dlsi.copymus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class ScoreDto {
	
	private String id;
	private String db;
	private String title;
	private String author;
	private String pdf;
	private String png;
	private String midi;
	
	public ScoreDto() {
		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPdf() {
		return pdf;
	}

	public void setPdf(String pdf) {
		this.pdf = pdf;
	}
	
	public String getPng() {
		return png;
	}

	public void setPng(String png) {
		this.png = png;
	}

	public String getMidi() {
		return midi;
	}

	public void setMidi(String midi) {
		this.midi = midi;
	}

}
