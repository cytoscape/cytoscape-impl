package org.cytoscape.cpath2.internal.web_service;

/**
 * Indicates Error Connecting to cPath.
 *
 * @author Ethan Cerami.
 */
public class CPathException extends Exception {
    /**
     * Error:  Canceled by User.
     */
    public final static int ERROR_CANCELED_BY_USER = 1;

    /**
     * Error:  Unknown Host.
     */
    public final static int ERROR_UNKNOWN_HOST = 2;

    /**
     * Error:  Network IO.
     */
    public final static int ERROR_NETWORK_IO = 3;

    /**
     * Error:  XML Parsing.
     */
    public final static int ERROR_XML_PARSING = 4;

    /**
     * Error:  Web Service API.
     */
    public final static int ERROR_WEB_SERVICE_API = 5;

    /**
     * Error HTTP
     */
    public final static int ERROR_HTTP = 6;

    private int errorCode;
    private String errorMessage;
    private String errorDetail;
    private String recoveryTip;

    private final static String NETWORK_RECOVERY_TIP
            = "Please check your network settings and try again.";

    private final static String SERVER_ERROR_RECOVERY_TIP
            = "Please try a different search term, or try again later.";

    /**
	 * Constructor.
     * @param errorCode Error Code.
	 * @param t Root throwable.
	 */
	public CPathException(int errorCode, Throwable t) {
        super(t);
        this.errorCode = errorCode;
        setErrorMessages(errorCode);
        if (t != null) {
            errorMessage = errorMessage + " " + t.getMessage();
        }
    }

    public CPathException(int errorCode, String errorDetail) {
        this.errorDetail = errorDetail;
        this.errorCode = errorCode;
        setErrorMessages(errorCode);
        if (errorDetail != null) {
            errorMessage = errorMessage + " " + errorDetail;
        }
    }

    /**
     * Gets the Error Code.
     * @return Error Code.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Gets Error Message.
     * @return Error Message.
     */
    public String getMessage() {
        return errorMessage;
    }

    /**
     * Gets the Recovery Tip.
     * @return Recovery Tip.
     */
    public String getRecoveryTip() {
        return recoveryTip;
    }

    private void setErrorMessages(int errorCode) {
        switch (errorCode) {
            case ERROR_CANCELED_BY_USER:
                errorMessage =  "Canceled by user.";
                break;
            case ERROR_UNKNOWN_HOST:
                errorMessage = "Network error occurred while tring to connect to "
                        + "remote web service.";
                recoveryTip = NETWORK_RECOVERY_TIP;
                break;
            case ERROR_NETWORK_IO:
                errorMessage = "Network error occurred while tring to connect to "
                        + "remote web service.";
                recoveryTip = NETWORK_RECOVERY_TIP;
                break;
            case ERROR_XML_PARSING:
                errorMessage = "Error occurred while trying to parse XML results "
                    + "retrieved from remote web service.";
                break;
            case ERROR_HTTP:
                 errorMessage = "Network error occurred while trying to connect to "
                        + "remote web service.";
                recoveryTip = SERVER_ERROR_RECOVERY_TIP;
                break;
            case ERROR_WEB_SERVICE_API:
                errorMessage = "Error occurred while trying to connect to remote web service.  ";
                recoveryTip = SERVER_ERROR_RECOVERY_TIP;
                break;
        }
    }
}
