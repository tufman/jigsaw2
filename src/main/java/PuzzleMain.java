import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PuzzleMain {

    public static void main(String[] args) throws IOException {

        // Our Test files
        //String filePath = System.getProperty("user.dir")+"\\src\\main\\resources\\Good4Pieces";     //  --- works expected
//        String filePath = "..\\jigsaw1\\src\\main\\resources\\Good16Pieces";//System.getProperty("user.dir")+"\\src\\\\main\\resources\\Good16Pieces";    //  --- works expected
        //String filePath = System.getProperty("user.dir")+"\\src\\\\main\\resources\\BadInputFile20Pieces";
        //  String filePath = System.getProperty("user.dir")+"\\src\\\\main\\resources\\SolverReturnNULL.in";  // -- works expected

        // Amir - Simple Files (*.in )
        //String filePath = System.getProperty("user.dir")+"\\src\\\\main\\resources\\simple\\test1.in";    // -- works as expected
        //String filePath = System.getProperty("user.dir")+"\\src\\\\main\\resources\\simple\\test2.in";   // -- works fine ???
        //String filePath = System.getProperty("user.dir")+"\\src\\\\main\\resources\\simple\\test3.in";   // -- works as expected
//        String filePath = System.getProperty("user.dir")+"\\src\\\\main\\resources\\simple\\test4.in";   // should work return error
        //String filePath = System.getProperty("user.dir")+"\\src\\\\main\\resources\\simple\\test5.in";
        String fileIn = System.getProperty("user.dir")+"\\src\\main\\resources\\add\\test1.in";
        String fileOut = System.getProperty("user.dir")+"\\src\\main\\resources\\add\\test1.out";

        String filePathToSave = System.getProperty("user.dir") + "\\src\\main\\resources\\result.txt";
        Map<PuzzleDirections, List<Integer>> in = new HashMap<>();
        Map<PuzzleDirections, List<Integer>> out = new HashMap<>();
        PuzzleElement[][] board = null;
        WritePuzzleStatus writePuzzleStatus = new WritePuzzleStatus(filePathToSave);
        Puzzle puzzle = new Puzzle();
        List<PuzzleElement> inputPuzzle;
        List<Integer> outputPuzzle;
//        puzzle.readInputFile(filePath);


        puzzle.readInputFile(fileIn);
        inputPuzzle = puzzle.getPuzzleElementList();
        //TODO: add readOutputFile with ids
        puzzle.readInputFile(fileOut);
        //TODO: add readOutputFile with ids
        outputPuzzle = puzzle.getIdsList();
        System.out.println("this output solution for this input " + puzzle.isSolution(inputPuzzle,outputPuzzle));

        puzzle.printErrorsFromReadingInputFile();
//        System.out.println("Get board from puzzle solution");
//        PuzzleElement[][] board = PuzzleSolver.start();

        System.out.println("#############################");
        System.out.println("#############################");
        System.out.println("##         Solution        ##");

        PuzzleSolver puzzleSolver = new PuzzleSolver(puzzle.getPuzzleElementList(), puzzle.getNumOfRowsForSolution(), puzzle.getAvailableOptionsForSolution());
        board = puzzleSolver.start();
        writePuzzleStatus.WriteResultToFile(board);
        puzzle.printSolution();


    }
}
