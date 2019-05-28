package es.ua.dlsi.copymus.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.ua.dlsi.copymus.dto.AnnotationDto;
import es.ua.dlsi.copymus.dto.InvalidationDto;
import es.ua.dlsi.copymus.dto.ScoreDto;
import es.ua.dlsi.copymus.dto.assemblers.AnnotationAssembler;
import es.ua.dlsi.copymus.dto.assemblers.ScoreAssembler;
import es.ua.dlsi.copymus.models.Annotation;
import es.ua.dlsi.copymus.models.AnnotationIdentity;
import es.ua.dlsi.copymus.models.AnnotationRepository;
import es.ua.dlsi.copymus.models.AnnotationStorageService;
import es.ua.dlsi.copymus.models.Invalidation;
import es.ua.dlsi.copymus.models.InvalidationRepository;
import es.ua.dlsi.copymus.models.Score;
import es.ua.dlsi.copymus.models.ScoreRepository;
import es.ua.dlsi.copymus.models.User;
import es.ua.dlsi.copymus.models.UserRepository;

@RestController
@RequestMapping("/scores")
public class ScoreController {

	private final Logger log = LoggerFactory.getLogger(ScoreController.class);
	
	private static final String SCORE_ID_NOT_FOUND = "Score with id [%s] not found in database [%s]";
	//private static final String USERNAME_NOT_FOUND = "User with username [%s] not found";
	private static final String USER_ID_NOT_FOUND = "User with id [%d] not found";
	private static final String RANDOM_SCORE_ERROR = "There are no pending scores for this user";
	private static final String REPRESENTATION_ERROR = "An error occurred while creating a representation for score [%s]";
	private static final String SAVE_ANNOTATION_ERROR = "Error while saving annotation files for score [%s]";
	private static final String SCORE_REPRESENTATION_ERROR = "An error occurred while creating a representation for score [%s]";
	private static final String ANNOTATION_FILES_ERROR = "An error occurred while reading the annotation files";
	private static final String ANNOTATION_NOT_FOUND = "There are no annotations for user [%d] and score [%s]";
	private static final String ANNOTATION_EXISTS = "Score [%s] is already annotated by user [%d]";
	
	@Autowired
	AnnotationAssembler annotationAssembler;
	
	@Autowired
	ScoreAssembler scoreAssembler;
	
	@Autowired
	ScoreRepository scoreRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AnnotationRepository annotationRepository;
	
	@Autowired
	InvalidationRepository invalidationRepository;
	
	@Autowired
	AnnotationStorageService storageService;
	
	@GetMapping("/{db}/{scoreId}")
	@ResponseStatus(HttpStatus.OK)
	public ScoreDto getScore(@PathVariable("db") String db, @PathVariable("scoreId") String scoreId) throws NotFoundException, ErrorException {
		Optional<Score> score = scoreRepository.findByDbAndId(db, scoreId);
		if (!score.isPresent())
			throw new NotFoundException(String.format(SCORE_ID_NOT_FOUND, scoreId, db));
		
		try {
			return scoreAssembler.getBuilder(score.get())
					.id().title().author().pdf().midi().build();
		}
		catch (Exception e) {
			throw new ErrorException(String.format(SCORE_REPRESENTATION_ERROR, scoreId));
		}
	}

	@PostMapping("/{db}/{scoreId}/annotate")
	@ResponseStatus(HttpStatus.CREATED)
	public void createAnnotation(@PathVariable("db") String db,
			@PathVariable("scoreId") String scoreId,
			@RequestBody AnnotationDto data) throws ErrorException, NotFoundException {
		
		Optional<Annotation> a = annotationRepository.findById(new AnnotationIdentity(data.getUserId(), scoreId));
		if (a.isPresent())
			throw new ErrorException(String.format(ANNOTATION_EXISTS, scoreId, data.getUserId()));
		
		Optional<Score> score = scoreRepository.findByDbAndId(db, scoreId);
		if (!score.isPresent()) {
			throw new NotFoundException(String.format(SCORE_ID_NOT_FOUND, scoreId, db));
		}
		
		if (score.get().isInvalid())
			throw new ErrorException("Score [" + scoreId + "] is flagged as not valid");
		
		Long userId = data.getUserId();
		Optional<User> user = userRepository.findById(userId);
		if (!user.isPresent())
			throw new NotFoundException(String.format(USER_ID_NOT_FOUND, userId));
		
		try {
			storageService.saveImageFromBase64(score.get(), user.get(), data.getImage());
			storageService.saveInteractionsFromBase64(score.get(), user.get(), data.getInteractions());
		} catch (Exception e) {
			throw new ErrorException(String.format(SAVE_ANNOTATION_ERROR, scoreId));
		}
		
		Annotation annotation = new Annotation(new AnnotationIdentity(user.get().getId(), score.get().getId()));
		annotationRepository.save(annotation);
		log.info("Stored annotations for score " + scoreId + "[" + db + "] by user " + user.get().getUsername());
	}

	@PostMapping("/{db}/{scoreId}/invalidate")
	@ResponseStatus(HttpStatus.OK)
	public void setScoreInvalid(@PathVariable("db") String db,
		@PathVariable("scoreId") String scoreId,
		@RequestBody InvalidationDto data) throws NotFoundException {
		
		Optional<Score> optScore = scoreRepository.findByDbAndId(db, scoreId);
		if (!optScore.isPresent())
			throw new NotFoundException(String.format(SCORE_ID_NOT_FOUND, scoreId, db));
		
		Long userId = data.getUserId();
		Optional<User> user = userRepository.findById(userId);
		if (!user.isPresent())
			throw new NotFoundException(String.format(USER_ID_NOT_FOUND, userId));
		
		Score score = optScore.get();
		score.setInvalid(true);
		scoreRepository.save(score);
		
		invalidationRepository.save(new Invalidation(new AnnotationIdentity(userId, scoreId)));
	}
	
	@GetMapping("/{db}/{userId}/pending")
	@ResponseStatus(HttpStatus.OK)
	public ScoreDto getNotAnnotatedScoreForUser(@PathVariable("db") String db, @PathVariable("userId") Long userId) throws NotFoundException, ErrorException {
		Optional<User> user = userRepository.findById(userId);
		if (!user.isPresent())
			throw new NotFoundException(String.format(USER_ID_NOT_FOUND, userId));
		
		Optional<Score> score = scoreRepository.getRandomScore(db);
		if (!score.isPresent())
			throw new NotFoundException(RANDOM_SCORE_ERROR);
			
		try {
			return scoreAssembler.getBuilder(score.get())
					.id().title().author().pdf().midi().build();
		}
		catch (Exception e) {
			throw new ErrorException(String.format(REPRESENTATION_ERROR, score.get().getId()));
		}
	}
	
	@GetMapping("/{db}/{scoreId}/{userId}")
	@ResponseStatus(HttpStatus.OK)
	public AnnotationDto getAnnotation(@PathVariable("db") String db,
			@PathVariable("scoreId") String scoreId,
			@PathVariable("userId") Long userId) throws NotFoundException, ErrorException {
		
		Optional<Annotation> annotation = annotationRepository.findById(new AnnotationIdentity(userId, scoreId));
		if (!annotation.isPresent())
			throw new NotFoundException(String.format(ANNOTATION_NOT_FOUND, userId, scoreId));
		
		try {
			return annotationAssembler.getAnnotationDto(annotation.get());
		} catch (Exception e) {
			throw new ErrorException(ANNOTATION_FILES_ERROR);
		}
		
//		File file = storageService.getInteractions(annotation.get());
//		ByteArrayResource resource;
//		try {
//			resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));
//		} catch (IOException e) {
//			throw new NotFoundException(READ_ANNOTATION_ERROR);
//		}
//
//	    return ResponseEntity.ok()
//	    		.header("Content-Disposition", "attachment; filename="+file.getName())
//	            .contentLength(file.length())
//	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
//	            .body(resource);
	}
	
	
	@ExceptionHandler({NotFoundException.class})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ExceptionResponse handleUserNotFoundException(NotFoundException e) {
		log.info(e.getCustomMessage());
		return new ExceptionResponse(e);
	}

	@ExceptionHandler({ErrorException.class})
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse handleUserNotFoundException(ErrorException e) {
		log.error(e.getCustomMessage());
		return new ExceptionResponse(e);
	}

	
/*	@GetMapping(
			value = "/{id}.mid",
			produces = "audio/midi"
	)
	public @ResponseBody byte[] getMidi(@PathVariable("id") String scoreId) throws ScoreNotFoundException, IOException {
		Optional<Score> score = scoreRepository.findById(scoreId);
		if (!score.isPresent())
			throw new ScoreNotFoundException();
		
		File file = new File(score.get().getPath() + File.separator + scoreId + ".mid");
		log.trace(file.getAbsolutePath() + " exists: " + file.exists());
		return Files.readAllBytes(file.toPath());
	}	
*/
}
