package net.codejava.controller;
import net.codejava.Exeptions.FileException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class AppExeptionHandler {
    //private static final Logger LOGGER = LoggerFactory.getLogger(AppExeptionHandler.class);
    private final MessageSource source;

    public AppExeptionHandler(final MessageSource messageSource) {
        source = messageSource;
    }
    ModelAndView mw = new ModelAndView("error");

    @ExceptionHandler(value = FileException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ModelAndView fileError(final FileException ex) {
        mw = new ModelAndView("error");
        mw.addObject("error", "Ошибка в исходных файлах");
        mw.addObject("reason", ex.exception.getMessage());
        ex.exception.printStackTrace();
        return mw;
    }
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ModelAndView internalServerError(final Exception ex) {
        mw = new ModelAndView("error");
        mw.addObject("error", "Ошибка вычислений");

        ex.printStackTrace();

        return mw;
    }

}


