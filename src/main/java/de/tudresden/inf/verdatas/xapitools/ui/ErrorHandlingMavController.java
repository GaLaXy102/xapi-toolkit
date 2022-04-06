package de.tudresden.inf.verdatas.xapitools.ui;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimExceptions;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveExceptions;
import de.tudresden.inf.verdatas.xapitools.dave.analysis.AnalysisExceptions;
import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DashboardExceptions;
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

/**
 * Error Handlers in accordance with <a href="https://datatracker.ietf.org/doc/html/rfc7231#section-6">Section 6 of RFC7231</a>.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@ControllerAdvice
public class ErrorHandlingMavController {
    // Path to Template
    private static final String DEFAULT_ERROR_VIEW = "bootstrap/error";
    private final Logger logger = Logger.getLogger(ErrorHandlingMavController.class.getName());

    /**
     * Print exception details to STDERR including markers
     *
     * @param e Exception to be handled
     * @return Used marker code
     */
    private UUID putFlowLog(Exception e) {
        UUID flowLogId = UUID.randomUUID();
        this.logger.severe("Exception has occured. Flow-ID: " + flowLogId);
        System.err.println(">>> Begin " + flowLogId);
        e.printStackTrace(System.err);
        System.err.println("<<< End " + flowLogId);
        return flowLogId;
    }

    /**
     * Base error view
     *
     * @param e      Exception be included
     * @param status Status Code for this exception
     * @return ModelAndView with the given details incorporated
     */
    private ModelAndView baseModelAndView(Exception e, HttpStatus status) {
        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("status", status);
        mav.addObject("exception", e);
        mav.addObject("logId", this.putFlowLog(e));
        return mav;
    }

    /**
     * Handler for {@link NoSuchElementException}.
     * The {@link HttpStatus} raised for this Exception class is 404 (Not Found).
     *
     * @param e Exception to be handled
     * @return ModelAndView for this Exception
     */
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView convertNotFound(Exception e) {
        return this.baseModelAndView(e, HttpStatus.NOT_FOUND);
    }

    /**
     * Handler for {@link IllegalArgumentException}, {@link IllegalStateException} (manually raised from code) and {@link ConstraintViolationException} (automatically raised from Validators).
     * The {@link HttpStatus} raised for this Exception class is 400 (Bad Request).
     *
     * @param e Exception to be handled
     * @return ModelAndView for this Exception
     */
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class, IllegalStateException.class})
    public ModelAndView convertBadRequest(Exception e) {
        return this.baseModelAndView(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handler for {@link AnalysisExceptions.ConfigurationConflict}, {@link AnalysisExceptions.SideEffectsError} and {@link DashboardExceptions.ConfigurationConflict} (all manually raised from code).
     * The {@link HttpStatus} raised for this Exception class is 409 (Conflict).
     *
     * @param e Exception to be handled
     * @return ModelAndView for this Exception
     */
    @ResponseStatus(code = HttpStatus.CONFLICT)
    @ExceptionHandler({AnalysisExceptions.ConfigurationConflict.class, AnalysisExceptions.SideEffectsError.class, DashboardExceptions.ConfigurationConflict.class})
    public ModelAndView convertConfigurationConflicts(Exception e) {
        return this.baseModelAndView(e, HttpStatus.CONFLICT);
    }

    /**
     * Handler for manually created ConnectionErrors (i.e. {@link DatasimExceptions.NoDatasimConnection}, {@link LrsExceptions.NoLrsConnection} and {@link DaveExceptions.NoDaveConnection}).
     * The {@link HttpStatus} raised for this Exception class is 503 (Service Unavailable).
     *
     * @param e Exception to be handled
     * @return ModelAndView for this Exception
     */
    @ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler({DatasimExceptions.NoDatasimConnection.class, LrsExceptions.NoLrsConnection.class,
            DaveExceptions.NoDaveConnection.class})
    public ModelAndView convertExternalServiceNotConnected(Exception e) {
        return this.baseModelAndView(e, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handler for all other Exceptions.
     * The {@link HttpStatus} raised for this Exception class is 500 (Internal Server Error).
     * Consider using any handled Exception class or creating your own ExceptionHandler when encountering this while developing.
     *
     * @param e Exception to be handled
     * @return ModelAndView for this Exception
     */
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ModelAndView genericError(Exception e) {
        return this.baseModelAndView(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
