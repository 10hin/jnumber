package number.algebra;

public class AlgebraicException extends RuntimeException {

    private static final long serialVersionUID = 1340413177518114556L;

    public AlgebraicException() {
        super();
    }

    public AlgebraicException(final String msg) {
        super(msg);
    }

    public AlgebraicException(final Throwable cause) {
        super(cause);
    }

    public AlgebraicException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    protected AlgebraicException(final String msg, final Throwable cause, final boolean enableSupression, final boolean writableStackTrace) {
        super(msg, cause, enableSupression, writableStackTrace);
    }

}
