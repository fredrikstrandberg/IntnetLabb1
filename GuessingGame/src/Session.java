import java.util.Random;

public class Session {
    private int lowerBound;
    private int upperBound;
    private int numGuesses;
    private final int correctNumber;

    public Session(){
        lowerBound = 1;
        upperBound = 100;
        numGuesses = 0;
        correctNumber = new Random().nextInt(lowerBound+upperBound)+lowerBound;
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



}
