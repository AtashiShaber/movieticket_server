package com.shaber.movieticket.handler;

import com.shaber.movieticket.exception.*;
import com.shaber.movieticket.resp.RV;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BaseHandler {

    @ExceptionHandler(value = {
            UserServiceException.class,
            AdminServiceException.class,
            MovieServiceException.class,
            CinemaServiceException.class,
            ScreeningServiceException.class,
            ScreenRoomServiceException.class,
            TicketServiceException.class,
            OrderServiceException.class})
    public RV<?> handlerLogin(RuntimeException e) {
        return RV.fail(e.getLocalizedMessage());
    }

}
