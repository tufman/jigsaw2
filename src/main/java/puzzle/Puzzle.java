package puzzle;

/*
@Author Shay Tufman
 */

import java.io.*;
import java.util.*;

public class Puzzle {

    private int expectedNumOfElementsFromFirstLine;
    private List<PuzzleElement> puzzleElementList = new ArrayList<>();

    public List<String> getErrorsReadingInputFile() {
        return errorsReadingInputFile;
    }

    private List<String> errorsReadingInputFile = new ArrayList<>();
    private Map<PuzzleDirections, List<Integer>> availableOptionsForSolution = new HashMap<>();
    private List<Integer> idsForErrorsNotInRange = new ArrayList<>();
    private ArrayList<Integer> splittedLineToInt = new ArrayList<>();
    private Map<Integer,List<Integer>> puzzleOutput = new HashMap<>();

    private boolean[] puzzleElementIDs;
    private PuzzleElement[][] board = null;
    private ArrayList<Integer> numOfRowsForSolution;
    private PuzzleMapper puzzleMapper = new PuzzleMapper();

    private Properties prop = null;

    public Puzzle() {
    }


    public boolean readInputFile(String filePath) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
        } catch (IOException e) {
            e.getMessage();
            System.out.println("Puzzle Fail to Init");
        }
        if (fis == null) {
            return false;
        }
        try (InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            initConfiguration();
            readDataFromFile(br);
        }
        return errorsReadingInputFile.isEmpty();
    }


    private void readDataFromFile(BufferedReader br) throws IOException {
        System.out.println("--------------------------------------------");
        System.out.println("---         Data from  input file        ---");
        System.out.println("--------------------------------------------");
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println("line: " + line);
            if (line.trim().length() == 0) {
                continue;
            }

            if (line.charAt(0) == '#') {
                continue;
            }

            if (line.contains("NumElements")) {
                extractNumOfElements(line);
                continue;
            }


            parseLineFromFileToIntArr(line);


            int id = splittedLineToInt.get(0);
            if(id <1) {
                addErrorWrongElementFormat(id,line);
                continue;
            }



            if (splittedLineToInt.size() == 5) {
                if (verifyIdInRange(id)) {
                    if (verifyAllEdgesInRange(splittedLineToInt)) {
                        PuzzleElement element = new PuzzleElement(splittedLineToInt);
                        puzzleElementList.add(element);
                        puzzleMapper.mapElementToSolutionList(element, element.getId());//puzzleElementList.size());
                        markExistElement(id);
                        continue;
                    }
                    // left, top, right and bottom between -1 to 1
                    else {
                        addErrorWrongElementFormat(id, line);
                    }
                }
                //ID is not in range
                else {
                    addIDToNotInRangeList(id);
                }

            }
            //Num of edges is not 4 (id + 4 edges)
            else {
                addErrorWrongElementFormat(id, line);
            }
        }


        if (idsForErrorsNotInRange.size() > 0) {
            addErrorForIDsNotInRange();
        }

        if (expectedNumOfElementsFromFirstLine != puzzleElementList.size()) {
            addErrorMissingPuzzleElements();
        }


        this.availableOptionsForSolution = puzzleMapper.getSolutionMap();

        verifyAtLeastOneLineAvailable();
        verifyAllCornersExist();
        verifySumZero();

        ArrayList<Integer> numOfAvailableRowsForSolution = puzzleMapper.getNumOfRowsForSolution();

        if (errorsReadingInputFile.size() == 0 && numOfAvailableRowsForSolution != null && puzzleElementList != null && availableOptionsForSolution.get(PuzzleDirections.TOP_LEFT_CORNER).size() > 0) {

        } else if (errorsReadingInputFile.size() > 0) {

        }

    }

    private void addErrorMissingPuzzleElements() {
        String allIDs = "";
        for (int i = 0; i < puzzleElementIDs.length; i++) {
            if (!(puzzleElementIDs[i])) {
                allIDs += (i + 1) + ",";
            }
        }
        String errorToAdd = (prop.getProperty("missingPuzzleElements"));
        allIDs = allIDs.substring(0, allIDs.length() - 1);
        errorToAdd += allIDs ;
        errorsReadingInputFile.add(errorToAdd);
    }

    private void addErrorForIDsNotInRange() {
        String wrongElementID = prop.getProperty("wrongElementIDs");
        wrongElementID = wrongElementID.replace("SIZE", String.valueOf(expectedNumOfElementsFromFirstLine));
        for (int i = 0; i < idsForErrorsNotInRange.size(); i++) {
            if (i == (idsForErrorsNotInRange.size() -1)){
                wrongElementID += idsForErrorsNotInRange.get(i);
                continue;
            }
            wrongElementID += idsForErrorsNotInRange.get(i) + ",";
        }
        errorsReadingInputFile.add(wrongElementID);
    }

    private void parseLineFromFileToIntArr(String line) {
        line = line.trim();
        String[] lineToArray = line.split(" ");
        splittedLineToInt.clear();
        for (String str : lineToArray) {
            if (str.length() == 0) {
                continue;
            }
            try {
                splittedLineToInt.add(Integer.parseInt(str));
            } catch (NumberFormatException e) {
                //TODO - make it more elegant ...
                addErrorWrongElementFormat(-9999, line);
            }
        }
    }

    private void extractNumOfElements(String line) {
        String[] numElementArr = line.split("=");
        try {
            expectedNumOfElementsFromFirstLine = Integer.parseInt(numElementArr[1].trim());
            puzzleElementIDs = new boolean[expectedNumOfElementsFromFirstLine];
        } catch (NumberFormatException e) {
            addErrorWrongFirstLine(line);
        }
    }

    private void verifySumZero() {
        int leftPlus = availableOptionsForSolution.get(PuzzleDirections.LEFT_PLUS).size() * 1;
        int leftMinus = availableOptionsForSolution.get(PuzzleDirections.LEFT_MINUS).size() * (-1);
        int rightPlus = availableOptionsForSolution.get(PuzzleDirections.RIGHT_PLUS).size() * 1;
        int rightMinus = availableOptionsForSolution.get(PuzzleDirections.RIGHT_MINUS).size() * (-1);
        int topPlus = availableOptionsForSolution.get(PuzzleDirections.TOP_PLUS).size() * 1;
        int topMinus = availableOptionsForSolution.get(PuzzleDirections.TOP_MINUS).size() * (-1);
        int bottomPlus = availableOptionsForSolution.get(PuzzleDirections.BOTTOM_PLUS).size() * 1;
        int bottomMinus = availableOptionsForSolution.get(PuzzleDirections.BOTTOM_MINUS).size() * (-1);

        if (!((leftPlus + leftMinus + rightPlus + rightMinus + topPlus +topMinus + bottomPlus + bottomMinus) == 0)){
            errorsReadingInputFile.add(prop.getProperty("sumOfAllEdgesIsNotZero"));
        }
    }

    public ArrayList<Integer> getNumOfRowsForSolution() {
        numOfRowsForSolution = puzzleMapper.getNumOfRowsForSolution();
        return numOfRowsForSolution;
    }

    public List<PuzzleElement> getPuzzleElementList() {
        return puzzleElementList;
    }

    public Map<PuzzleDirections, List<Integer>> getAvailableOptionsForSolution() {
        return availableOptionsForSolution;
    }

    public void printSolution() {
        if (board != null) {

            for (int ii = 0; ii <= board.length - 1; ii++) {
                for (int jj = 0; jj <= board[0].length - 1; jj++) {
                    System.out.print(board[ii][jj].getId() + " ");
                }
                System.out.println();
            }
        }
    }

    private void verifyAtLeastOneLineAvailable() {
        String error = prop.getProperty("wrongNumberOfStraighEdges");
        if ((this.availableOptionsForSolution.get(PuzzleDirections.LEFT_ZERO).size() == 0) ||
                (this.availableOptionsForSolution.get(PuzzleDirections.RIGHT_ZERO).size() == 0)) {
            errorsReadingInputFile.add(error);
        }
    }

    private void verifyAllCornersExist() {
        String error = prop.getProperty("missingCorner");
        if (this.availableOptionsForSolution.get(PuzzleDirections.TOP_LEFT_CORNER).size() == 0) {
            String errorTopLeftCorner = error.replace("<>", "TL");
            errorsReadingInputFile.add(errorTopLeftCorner);
        }
        if (this.availableOptionsForSolution.get(PuzzleDirections.TOP_RIGHT_CORNER).size() == 0) {
            String errorTopRightCorner = error.replace("<>", "TR");
            errorsReadingInputFile.add(errorTopRightCorner);
        }
        if (this.availableOptionsForSolution.get(PuzzleDirections.BOTTOM_LEFT_CORNER).size() == 0) {
            String errorBottomLeftCorner = error.replace("<>", "BL");
            errorsReadingInputFile.add(errorBottomLeftCorner);
        }
        if (this.availableOptionsForSolution.get(PuzzleDirections.BOTTOM_RIGHT_CORNER).size() == 0) {
            String errorBottomRight = error.replace("<>", "BR");
            errorsReadingInputFile.add(errorBottomRight);
        }

    }

    private void addErrorWrongFirstLine(String line) {
        String errMsg = prop.getProperty("wrongFirstLineFormat");
        errorsReadingInputFile.add(errMsg + line);
    }

    private void addIDToNotInRangeList(int id) {
        idsForErrorsNotInRange.add(id);
    }

    private void addErrorWrongElementFormat(int id, String line) {
        String errorToAdd = (prop.getProperty("wrongElementsFormat"));
        if (!(id == -9999)) {
            String errorToAdd1 = errorToAdd.replace("<id>", String.valueOf(id));
            errorsReadingInputFile.add(errorToAdd1 + line);
        } else {
            errorsReadingInputFile.add(errorToAdd + line);
        }


    }

    private void markExistElement(int id) {
        puzzleElementIDs[id - 1] = true;
    }

    private boolean verifyIdInRange(Integer idToCheck) {
        return idToCheck <= expectedNumOfElementsFromFirstLine;
    }

    public void printErrorsFromReadingInputFile() {
        System.out.println("----------------------------------");
        System.out.println("--- All Errors from Input File ---");
        System.out.println("----------------------------------");
        if (errorsReadingInputFile.size() > 0) {
            for (String error : errorsReadingInputFile) {
                System.out.println(error);
            }
        } else {
            System.out.println("NO ERRORS");
        }
    }


    private boolean verifyAllEdgesInRange(ArrayList<Integer> numFromLine) {
        for (int i = 1; i < numFromLine.size(); i++) {
            if (!(numFromLine.get(i) >= -1 && numFromLine.get(i) <= 1)) {
                return false;
            }
        }
        return true;
    }


    public void printListOfElements() {
        System.out.println("----------------------------------");
        System.out.println("----   printListOfElements   ----");
        System.out.println("----------------------------------");
        for (PuzzleElement element : puzzleElementList) {
            System.out.println(element);
        }
    }

    private void initConfiguration() throws IOException {
        GetPuzzleErrors properties = new GetPuzzleErrors();
        prop = properties.getPropValues();

        System.out.println("--------------------------------------------");
        System.out.println("--- Existing Errors in config.properties ---");
        System.out.println("--------------------------------------------");
        prop.forEach((key, value) -> System.out.println(key + " : " + value));
    }

    public int getNumOfElementsFromFirstLine() {
        return expectedNumOfElementsFromFirstLine;
    }


    public boolean verifyErrorExistInList(String error) {
        return errorsReadingInputFile.contains(error);
    }


    public int getActualNumOfElementsReadFromInputFile() {
        return puzzleElementList.size();
    }


//reade output file with ids only
    public void readOutputFile(String filePath) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
        } catch (IOException e) {
            e.getMessage();
            System.out.println("Puzzle Fail to Init");
        }
        if (fis == null) {
            return;
        }
        try (InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
//            initConfiguration();
            readDataFromOutputFile(br);
        }
    }


    private void readDataFromOutputFile(BufferedReader br) throws IOException {

        String line;
        Integer lineIndex = 0;
        List<Integer>list = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            System.out.println("line: " + line);
            if (line.trim().length() == 0) {
                continue;
            }
            line = line.trim();
            String[] lineToArray = line.split(" ");
            list = new ArrayList<>();
            for (String str : lineToArray) {
                if (str.length() == 0) {
                    continue;
                }
                try {
                    list.add(Integer.parseInt(str));
                    puzzleOutput.put(lineIndex,list);
                } catch (NumberFormatException e) {
                    //TODO - make it more elegant ...
                    addErrorWrongElementFormat(-9999, line);
                }
            }
            puzzleOutput.put(lineIndex,list);
            lineIndex++;
        }
    }


//check if output file is solution for input
    public boolean isIOSolvable() {
        PuzzleElement elm;
        int index=0;
        boolean isSolution = false;
        PuzzleElement[][] board = null;
        int row = puzzleOutput.size();
        int col = 0;
        int i = 0, j=0;
        if(row != 0) {
            col = expectedNumOfElementsFromFirstLine / row;
            board = new PuzzleElement[row][col];
        }

        for (Map.Entry<Integer, List<Integer>> ids: puzzleOutput.entrySet()){
            for(Integer id :ids.getValue()) {
                elm = getById(id);
//                if(i>=0 && i<=row && j>=0 && j<=col && (board[i][j-1].getRight()+elm.getLeft()==0)&&(board[i-1][j].getBottom()+elm.getTop()==0)){
                    if (i == 0 && j == 0) { // first TOP_LEFT_CORNER
                        board[i][j++] = elm;
                    }
                    else if (i == row-1 && j == 0) { // first BOTTOM_LEFT_CORNER
                        board[i][j++] = elm;
                    }
                    else if (i == row -1 && j==col-1) { // last BOTTOM_RIGHT_CORNER
                        board[i][j++] = elm;
                    }
                    else if (i == 0 && j==col-1) { // last TOP_RIGHT_CORNER
                        board[i][j++] = elm;
                    }
                    //check if edge
                    else if (i == 0) { // first row
                        if ((board[i][j-1].getRight()+elm.getLeft() == 0))board[i][j++] = elm;
                    }
                    else if (j == 0) { // first column
                        if ((board[i-1][j].getBottom()+elm.getTop() == 0))board[i][j++] = elm;
                    }
                    else if (i == row -1) { // last row
                        if ((board[i-1][j].getBottom()+elm.getTop() == 0)&& (board[i][j-1].getRight()+elm.getLeft() == 0)) board[i][j++] = elm;
                    }
                    else if (j==col-1) { // last column
                        if ((board[i-1][j].getBottom()+elm.getTop() == 0)&& (board[i][j-1].getRight()+elm.getLeft() == 0)) board[i][j++] = elm;
                    }
                    else { // middle element
                        if ((board[i-1][j].getBottom()+elm.getTop() == 0)&& (board[i][j-1].getRight()+elm.getLeft() == 0)) board[i][j++] = elm;
                    }
//                    board[i][j++] = elm;
                    System.out.print(id);
                }
//            }
            i++;
            j=0;
        }

        return isProperPuzzle(board);
    }

    //get puzzle element by id from output file
    public PuzzleElement getById(int id){
        PuzzleElement puzzleElement = null;
        int count = puzzleElementList.size()-1;
        while (count>=0) {
            puzzleElement = puzzleElementList.get(count);
            if(puzzleElement.getId() == id)
                return puzzleElement;
            count--;
        }
        return puzzleElement;
    }

    //check if puzzle have straight edges
    private boolean isProperPuzzle(PuzzleElement[][] board){
        for (int i=0;i<board[0].length; i++){
            for(int j=0;j<board.length; j++) {
                if (i == 0) {
                    if (!(board[i][j].getTop() == 0)) {
                        return false;
                    }
                }
                if (i == board[0].length-1) {
                    if (!(board[i][j].getBottom() == 0)) {
                        return false;
                    }
                }
                if (j == 0) {
                    if (!(board[i][j].getLeft() == 0)) {
                        return false;
                    }
                }
                if (j == board[0].length-1) {
                    if (!(board[i][j].getRight() == 0)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
//return list of ids from output file


    public Map<PuzzleDirections,List<Integer>> getSolutionMap() {
        return puzzleMapper.getSolutionMap();
    }
}
