package customExceptions;

public class InvalidFileParametersException extends CustomException{
    public InvalidFileParametersException(String errorMessage) {
        super(errorMessage);
    }
}
