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

import es.ua.dlsi.copymus.dto.ScannedImageDto;
import es.ua.dlsi.copymus.dto.ScoreDto;
import es.ua.dlsi.copymus.dto.assemblers.ScoreAssembler;
import es.ua.dlsi.copymus.models.HandwrittenStorageService;
import es.ua.dlsi.copymus.models.Score;
import es.ua.dlsi.copymus.models.ScoreRepository;

@RestController
@RequestMapping("/api/scanner")
public class ScannerController {

	private final Logger log = LoggerFactory.getLogger(ScoreController.class);
	
	private static final String RANDOM_SCORE_ERROR = "Could not find any score for database [%s]";
	private static final String REPRESENTATION_ERROR = "An error occurred while creating a representation for score [%s]";
	private static final String SCORE_ID_NOT_FOUND = "Score with id [%s] not found in database [%s]";
	private static final String SAVE_IMAGE_ERROR = "Error while saving handwritten image for score [%s]";

	@Autowired
	ScoreRepository scoreRepository;
	
	@Autowired
	ScoreAssembler scoreAssembler;
	
	@Autowired
	HandwrittenStorageService storageService;

	// Returns a random score for the smartphone upload form
	@GetMapping("/random/{db}")
	@ResponseStatus(HttpStatus.OK)
	public ScoreDto getRandomScore(@PathVariable("db") String db) throws NotFoundException, ErrorException {
		Optional<Score> score = scoreRepository.getRandomScore(db);
		if (!score.isPresent())
			throw new NotFoundException(String.format(RANDOM_SCORE_ERROR, db));
		
		try {
			return scoreAssembler.getBuilder(score.get())
					.id().db().title().author().png().midi().build();
		}
		catch (Exception e) {
			throw new ErrorException(String.format(REPRESENTATION_ERROR, score.get().getId()));
		}
	}

	// Returns a random score for the smartphone upload form
	@GetMapping("/{db}/{id}")
	@ResponseStatus(HttpStatus.OK)
	public ScoreDto getScore(@PathVariable("db") String db, @PathVariable("id") String id) throws NotFoundException, ErrorException {
		Optional<Score> score = scoreRepository.findByDbAndId(db, id);
		if (!score.isPresent())
			throw new NotFoundException(RANDOM_SCORE_ERROR);
		
		try {
			return scoreAssembler.getBuilder(score.get())
					.id().db().title().author().png().midi().build();
		}
		catch (Exception e) {
			throw new ErrorException(String.format(REPRESENTATION_ERROR, score.get().getId()));
		}
	}
	
	@PostMapping("/upload")
	@ResponseStatus(HttpStatus.CREATED)
	public void uploadImage(@RequestBody ScannedImageDto data) throws NotFoundException, ErrorException {
		Optional<Score> score = scoreRepository.findByDbAndId(data.getDb(), data.getId());

		if (!score.isPresent())
			throw new NotFoundException(String.format(SCORE_ID_NOT_FOUND, data.getDb(), data.getId()));
		
		try {
			storageService.saveImageFromBase64(score.get(), data.getPng());
			log.info("Saved handwritten image for score " + data.getDb() + "/" + data.getId());
		} catch (Exception e) {
			throw new ErrorException(String.format(SAVE_IMAGE_ERROR, data.getId()));
		}
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

}
