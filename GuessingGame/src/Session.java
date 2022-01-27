import java.util.Random;

public class Session {
    private int lowerBound;
    private int upperBound;
    private int numGuesses;
    private final int correctNumber;
    private boolean correctGuess = false;
    private boolean outOfBounds = false;
    private String SessionID;

    public Session(String cookie){
        SessionID = cookie;
        lowerBound = 1;
        upperBound = 100;
        numGuesses = 0;
        correctNumber = new Random().nextInt(upperBound-(1+lowerBound))+lowerBound+1;  //random 2 to 99
    }

    public void increaseGuesses(){
        numGuesses++;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    public int getCorrectNumber() {
        return correctNumber;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getNumGuesses() {
        return numGuesses;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public void setCorrectGuess() {
        correctGuess = true;
    }

    public boolean getCorrectGuess(){
        return correctGuess;
    }

    public void setOutOfBounds(boolean OB){
        outOfBounds = OB;
    }

    public boolean getOutOfBounds(){
        return outOfBounds;
    }

    public String getCookie(){
        return SessionID;
    }


}
