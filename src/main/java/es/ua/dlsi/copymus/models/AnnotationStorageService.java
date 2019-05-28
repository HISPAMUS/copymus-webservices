package es.ua.dlsi.copymus.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.ua.dlsi.copymus.AppProperties;

@Service
public class AnnotationStorageService {
	
	private static final String IMAGE = "image.png";
	private static final String INTERACTIONS = "interactions.zip";
	
	Logger log = LoggerFactory.getLogger(AnnotationStorageService.class);
	
	@Autowired
	AppProperties conf;
	
	private Path resolveAnnotationPath(Score score, User user) {
		Path path = Paths.get(conf.getAnnotationsPath());
		return path.resolve(score.getDb() + File.separator + score.getId() + File.separator + user.getId());
	}
	
	private void saveFile(Score score, User user, MultipartFile file, String filename) throws IllegalStateException, IOException {
		Path path = resolveAnnotationPath(score, user);
		path.toFile().mkdirs();
		File destination = path.resolve(filename).toFile();
		file.transferTo(destination);
		log.debug("File " + file.getOriginalFilename() + " copied to " + destination.getAbsolutePath());
	}
	
	private void saveFileFromBase64(Score score, User user, String encodedContent, String filename) throws IllegalStateException, IOException {
		Path path = resolveAnnotationPath(score, user);
		path.toFile().mkdirs();
		File destination = path.resolve(filename).toFile();
		try (OutputStream stream = new FileOutputStream(destination)) {
			stream.write(Base64.decodeBase64(encodedContent));
		}
	}
	
	public void saveImage(Score score, User user, MultipartFile file) throws IllegalStateException, IOException {
		saveFile(score, user, file, IMAGE);
	}

	public void saveImageFromBase64(Score score, User user, String encodedFile) throws IllegalStateException, IOException {
		saveFileFromBase64(score, user, encodedFile, IMAGE);
	}

	public void saveInteractions(Score score, User user, MultipartFile file) throws IllegalStateException, IOException {
		saveFile(score, user, file, INTERACTIONS);
	}
	
	public void saveInteractionsFromBase64(Score score, User user, String encodedFile) throws IllegalStateException, IOException {
		saveFileFromBase64(score, user, encodedFile, INTERACTIONS);
	}

	public File getImage(Annotation annotation) {
		Path path = resolveAnnotationPath(annotation.getScore(), annotation.getUser());
		return path.resolve(IMAGE).toFile();
	}

	public File getInteractions(Annotation annotation) {
		Path path = resolveAnnotationPath(annotation.getScore(), annotation.getUser());
		return path.resolve(INTERACTIONS).toFile();
	}
	
}
