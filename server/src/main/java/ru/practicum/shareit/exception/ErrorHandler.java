package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNotFound(NotFoundException e) {
		log.warn(e.getMessage());
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleConflict(ConflictException e) {
		log.warn(e.getMessage());
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleDataIntegrity(DataIntegrityViolationException e) {
		String message = "Ошибка записи данных (DataIntegrityViolationException)";
		log.warn(message, e);
		return new ErrorResponse(message);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleForbidden(ForbiddenException e) {
		log.warn(e.getMessage());
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleBadRequest(BadRequestException e) {
		log.warn(e.getMessage());
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.filter(Objects::nonNull)
				.collect(Collectors.joining("; "));
		if (message.isBlank()) {
			message = "Ошибка валидации (MethodArgumentNotValidException)";
		}
		log.warn("Ошибка валидации: {}", message);
		return new ErrorResponse(message);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleConstraintViolation(ConstraintViolationException e) {
		String message = e.getConstraintViolations().stream()
				.map(ConstraintViolation::getMessage)
				.filter(Objects::nonNull)
				.collect(Collectors.joining("; "));
		if (message.isBlank()) {
			message = "Ошибка валидации (ConstraintViolationException)";
		}
		log.warn("Ошибка валидации: {}", message);
		return new ErrorResponse(message);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMissingHeader(MissingRequestHeaderException e) {
		String message = "Не передан заголовок " + e.getHeaderName();
		log.warn(message);
		return new ErrorResponse(message);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		String message = "Некорректное значение параметра " + e.getName() + ": " + e.getValue();
		log.warn(message);
		return new ErrorResponse(message);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleNotReadable(HttpMessageNotReadableException e) {
		String message = "Некорректный формат запроса";
		log.warn(message, e);
		return new ErrorResponse(message);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleOther(Throwable e) {
		log.error("Непредвиденная ошибка", e);
		return new ErrorResponse("Произошла непредвиденная ошибка");
	}
}
