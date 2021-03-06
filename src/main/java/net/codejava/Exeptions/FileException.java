package net.codejava.Exeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Ошибка в исходных файлах")
public class FileException extends NumberFormatException {
    public Exception exception;
    public FileException(Exception exception)
    {
        this.exception = exception;
    }
}

