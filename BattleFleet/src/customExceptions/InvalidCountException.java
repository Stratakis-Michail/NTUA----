package customExceptions;

public class InvalidCountException extends CustomException{
    public InvalidCountException(String errorMessage) {
        super(errorMessage);
    }
}
