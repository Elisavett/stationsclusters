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
        mw = new ModelAndView("error");
        mw.addObject("error", "Ошибка в исходных файлах");
        printStackTrace(ex);

        return mw;
    }
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ModelAndView internalServerError(final Exception ex) {
        mw = new ModelAndView("error");
        mw.addObject("error", "Ошибка вычислений");

        printStackTrace(ex);

        return mw;
    }
    private void printStackTrace (Exception ex){
        System.out.print(ex.getMessage() + "\n");
        for(StackTraceElement element : ex.getStackTrace())
        {
            System.out.print(element.toString() + "\n");
        }
    }


}


