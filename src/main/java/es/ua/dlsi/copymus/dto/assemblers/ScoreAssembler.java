package es.ua.dlsi.copymus.dto.assemblers;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.ua.dlsi.copymus.AppProperties;
import es.ua.dlsi.copymus.dto.ScoreDto;
import es.ua.dlsi.copymus.models.Score;
import es.ua.dlsi.copymus.util.MEIUtils;
import es.ua.dlsi.copymus.util.SVGUtils;

@Service
public class ScoreAssembler {
	
	@Autowired
	AppProperties conf;
	
	public class ScoreDtoBuilder {
		
		private Logger log = LoggerFactory.getLogger(ScoreDtoBuilder.class);
		
		private Score score;
		private ScoreDto dto;
		
		public ScoreDtoBuilder(Score score) {
			this.score = score;
			dto = new ScoreDto();
		}
		
		private String encodeToBase64(byte[] data) {
			return new String(Base64.getEncoder().encode(data));
		}
		
		public ScoreDtoBuilder id() {
			dto.setId(score.getId());
			return this;
		}
		
		public ScoreDtoBuilder db() {
			dto.setDb(score.getDb());
			return this;
		}
		
		public ScoreDtoBuilder title() {
			dto.setTitle(score.getTitle());
			return this;
		}
		
		public ScoreDtoBuilder author() {
			dto.setAuthor(score.getAuthor());
			return this;
		}
		
		public ScoreDtoBuilder pdf() throws Exception {
			String svgPath = conf.getDatabasesPath() + score.getPath() + File.separator + score.getId() + ".svg";
			try {
				dto.setPdf(encodeToBase64(SVGUtils.svg2pdf(svgPath)));
			} catch (Exception e) {
				log.error("Could not get PDF for score [" + score.getId() + "]: " + e.toString());
				throw e;
			}
	
			return this;
		}
		
		public ScoreDtoBuilder png() throws Exception {
			String svgPath = conf.getDatabasesPath() + score.getPath() + File.separator + score.getId() + ".svg";
			try {
				dto.setPng(encodeToBase64(SVGUtils.svg2png(svgPath)));
			} catch (Exception e) {
				log.error("Could not get PNG for score [" + score.getId() + "]: " + e.toString());
				throw e;
			}
			
			return this;
		}
		
		public ScoreDtoBuilder midi() throws Exception {
			String meiPath = conf.getDatabasesPath() + score.getPath() + File.separator + score.getId() + ".mei";
			String midiPath = MEIUtils.mei2midi(meiPath);
			
			File midiFile = new File(midiPath); //new File(conf.getDatabasesPath() + score.getPath() + File.separator + id + ".mid");
			try {
				dto.setMidi(encodeToBase64(Files.readAllBytes(midiFile.toPath())));
			} catch (Exception e) {
				log.error("Could not get MIDI file for score [" + score.getId() + "]: " + e.toString());
				throw e;
			}
	
			return this;
		}
		
		public ScoreDto build() {
			return dto;
		}
	}
	
	public ScoreDtoBuilder getBuilder(Score score) {
		return new ScoreDtoBuilder(score);
	}
}
