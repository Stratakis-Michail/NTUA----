package customExceptions;

public abstract class CustomException extends Throwable{
    public CustomException(String errorMessage){super(errorMessage);}
}
