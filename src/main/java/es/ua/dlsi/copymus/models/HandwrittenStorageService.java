package es.ua.dlsi.copymus.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.ua.dlsi.copymus.AppProperties;

@Service
public class HandwrittenStorageService {
	
	Logger log = LoggerFactory.getLogger(HandwrittenStorageService.class);
	
	@Autowired
	AppProperties conf;
	
	private Path resolveHandwrittenPath(Score score) {
		Path path = Paths.get(conf.getHandwrittenPath());
		return path.resolve(score.getDb());
	}
	
	private void saveFileFromBase64(Score score, String encodedContent, String filename) throws IllegalStateException, IOException {
		Path path = resolveHandwrittenPath(score);
		path.toFile().mkdirs();
		File destination = path.resolve(filename).toFile();
		try (OutputStream stream = new FileOutputStream(destination)) {
			stream.write(Base64.decodeBase64(encodedContent));
		}
	}
	
	public void saveImageFromBase64(Score score, String encodedFile) throws IllegalStateException, IOException {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		String filename = score.getId() + "." + timestamp + ".png";
		saveFileFromBase64(score, encodedFile, filename);
	}
}
