package de.tudresden.inf.verdatas.xapitools.ui;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimExceptions;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;

// Heavily inspired by https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc

@ControllerAdvice
public class ErrorHandlingMavController {
    private static final String DEFAULT_ERROR_VIEW = "bootstrap/error";
    private final Logger logger = Logger.getLogger(ErrorHandlingMavController.class.getName());

    private UUID putFlowLog(Exception e) {
        UUID flowLogId = UUID.randomUUID();
        this.logger.severe("Exception has occured. Flow-ID: " + flowLogId);
        System.err.println(">>> Begin " + flowLogId);
        e.printStackTrace(System.err);
        System.err.println("<<< End " + flowLogId);
        return flowLogId;
    }

    private ModelAndView baseModelAndView(Exception e, HttpStatus status) {
        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("status", status);
        mav.addObject("exception", e);
        mav.addObject("logId", this.putFlowLog(e));
        return mav;
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView convertNotFound(Exception e) {
        return this.baseModelAndView(e, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ModelAndView convertBadRequest(Exception e) {
        return this.baseModelAndView(e, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler({DatasimExceptions.NoDatasimConnection.class, LrsExceptions.NoLrsConnection.class})
    public ModelAndView convertExternalServiceNotConnected(Exception e) {
        return this.baseModelAndView(e, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ModelAndView genericError(Exception e) {
        return this.baseModelAndView(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
