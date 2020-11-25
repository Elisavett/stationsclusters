package net.codejava.controller;
import lombok.AllArgsConstructor;
import lombok.Data;
import motif.errormessage.ErrorMessage;
import net.codejava.Exeptions.FileException;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.logging.Logger;
@ControllerAdvice
public class AppExeptionHandler {
    private static final String INCOMING_REQUEST_FAILED = "Incoming request failed:";
    //private static final Logger LOGGER = LoggerFactory.getLogger(AppExeptionHandler.class);
    private final MessageSource source;

    public AppExeptionHandler(final MessageSource messageSource) {
        source = messageSource;
    }
    ModelAndView mw = new ModelAndView("error");

    @ExceptionHandler(value = FileException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ModelAndView fileError(final Exception ex) {
        mw.addObject("error", "Ошибка в исходных файлах");
        System.out.print(ex.getMessage() + "\n");
        for(StackTraceElement element : ex.getStackTrace())
        {
            System.out.print(element.toString() + "\n");
        }

        return mw;
    }
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ModelAndView internalServerError(final Exception ex) {
        mw.addObject("error", "Ошибка вычислений");
        System.out.print(ex.getMessage() + "\n");
        for(StackTraceElement element : ex.getStackTrace())
        {
            System.out.print(element.toString() + "\n");
        }

        return mw;
    }


}


