package CompileError;

public class CompileException extends Exception {
    char typeCode;
    int lineNumber;

    public CompileException(int lineNumber) {
        typeCode = '?';
        this.lineNumber = lineNumber;
    }

    public CompileException(char typeCode,int lineNumber) {
        this.typeCode = typeCode;
        this.lineNumber = lineNumber;
    }

}
