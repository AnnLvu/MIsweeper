package com.javarush.games.minesweeper;

import com.javarush.engine.cell.Color;
import com.javarush.engine.cell.Game;
import java.util.ArrayList;
import java.util.List;

public class MinesweeperGame extends Game {
    private static final int SIDE = 9;
    private final GameObject[][] gameField = new GameObject[SIDE][SIDE];
    private int countMinesOnField;
    private static final String MINE = "\uD83D\uDCA3";
    private static final String FLAG = "\uD83D\uDEA9";
    private int countFlags;
    private boolean isGameStopped;
    private int countClosedTiles = SIDE * SIDE;
    private int score = 0;

    @Override
    public void initialize() {
        setScreenSize(SIDE, SIDE);
        createGame();
    }

    private void createGame() {

        countClosedTiles = SIDE * SIDE;

        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                boolean isMine = getRandomNumber(10) == 0;
                if (isMine) {
                    countMinesOnField++;
                }
                gameField[y][x] = new GameObject(x, y, isMine);
                setCellColor(x, y, Color.ORANGE);
                setCellValue(x, y, "");
            }
        }
        countMineNeighbors();
        countFlags = countMinesOnField;
    }

    private void win() {
        isGameStopped = true;
        showMessageDialog(Color.BLACK, "You Win!", Color.GREEN, 24);
    }

    private void gameOver() {
        isGameStopped = true;
        showMessageDialog(Color.BLACK, "Game Over", Color.RED, 24);
    }


    private List<GameObject> getNeighbors(GameObject gameObject) {
        List<GameObject> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1; y++) { //"up and down neighbor"
            for (int x = gameObject.x - 1; x <= gameObject.x + 1; x++) { //"right and left"
                if (y < 0 || y >= SIDE) { //for y = 0 or reached the last
                    continue;
                }
                if (x < 0 || x >= SIDE) { //for x = 0 or reached the last
                    continue;
                }
                if (gameField[y][x] == gameObject) { //an element cannot be its own neighbor
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    }

    private void countMineNeighbors() {
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                GameObject gameObject = gameField[y][x]; //I take the cell

                if (!gameObject.isMine) { //checking it's not a mine
                    for (GameObject neighbor : getNeighbors(gameObject)) { //I'm going to the neighbors
                        if (neighbor.isMine) { //if the neighbor is a mine, then +1
                            gameObject.countMineNeighbors++;
                        }
                    }
                }
            }
        }
    }

    private void openTile(int x, int y) {
        GameObject gameObject = gameField[y][x];
        if (isGameStopped || gameObject.isOpen || gameObject.isFlag) {
            return; //if the game is stopped, the element is already open or flagged - I do nothing
        }
        countClosedTiles--;
        gameObject.isOpen = true;
        setCellColor(x, y, Color.GREEN); //cell in green
        if (gameObject.isMine) {
            setCellValueEx(x, y, Color.RED, MINE); //I draw a mine on a red background
            gameOver(); // вызываю метод gameOver
        } else {
            if (gameObject.countMineNeighbors == 0) { //if the symbol is not a mine and the neighbors of the mine = 0, then I recursively call the openTile(neighbor.x, neighbor.y) method
                for (GameObject neighbor : getNeighbors(gameObject)) {
                    if (!neighbor.isOpen) {
                        openTile(neighbor.x, neighbor.y);
                    }
                }
                setCellValue(x, y, ""); //output an empty line if mine neighbors = 0
            } else {
                setCellNumber(x, y, gameObject.countMineNeighbors); //I draw the number of neighbors min
            }

            score += 5;
            setScore(score);
        }
        if (countClosedTiles == countMinesOnField && !gameObject.isMine) {
            win();
        }
    }

    private void markTile(int x, int y) {
        GameObject gameObject = gameField[y][x];

        if (gameObject.isOpen) {
            return; //I don't do anything if the element is already open
        }

        if (countFlags == 0 && !gameObject.isFlag) {
            return; //do nothing if the number of unused flags is zero and the current element is not a flag
        }

        if (gameObject.isFlag) {
            gameObject.isFlag = false;
            countFlags++;
            setCellValue(x, y, "");
            setCellColor(x, y, Color.ORANGE); //I use the original cell color
        } else {
            gameObject.isFlag = true; //set the value of the isFlag field to true, reduce the number of unused flags by one, draw a FLAG sign on the field if the current element is not a flag (use the setCellValue(int, int, String) method) and change the background of a cell in the field using the setCellColor(int) method , int, Color)
            countFlags--;
            setCellValue(x, y, FLAG);
            setCellColor(x, y, Color.YELLOW);
        }
    }

    private void restart() {
        isGameStopped = false;
        countClosedTiles = SIDE * SIDE;
        score = 0;
        countMinesOnField = 0;
        setScore(score);
        createGame();
    }

    @Override
    public void onMouseRightClick(int x, int y) { //кликаю правой мышкой
        markTile(x, y);
    }

    @Override
    public void onMouseLeftClick(int x, int y) {
        if (isGameStopped) {
            restart();
        } else {
            openTile(x, y);
        }


    }
}
