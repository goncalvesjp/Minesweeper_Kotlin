package minesweeper

import java.util.Stack
import kotlin.random.Random
import kotlin.system.exitProcess

class Cell(
    var isMarked: Boolean = false, var hasMine: Boolean = false, var neighbours: Int = 0, var explored: Boolean = false
)

class Minesweeper(var nbMines: Int) {

    lateinit var minefield: Array<Array<Cell>>
    var firstCellIsExplored = false
    val mineslist = mutableListOf<Pair<Int, Int>>()
    val stack = Stack<Pair<Int, Int>>()

    fun init() {
        minefield = arrayOf(
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
            arrayOf<Cell>(Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell(), Cell()),
        )

        printMinefield()
    }

    private fun playMine(x: Int, y: Int) {
        if (minefield[x][y].isMarked) {
            minefield[x][y].isMarked = false
        } else if (!minefield[x][y].isMarked) {
            minefield[x][y].isMarked = true
        }
    }

    private fun initNeighbours() {
        for (y in 0..8) {
            for (x in 0..8) {
                if (!(minefield[x][y]).hasMine) {
                    if (getNbMinesNearCell(x, y) != 0) {
                        minefield[x][y].neighbours = getNbMinesNearCell(x, y)
                    }
                }
            }
        }
    }


    private fun initMines(nbMines: Int, x0: Int, y0: Int) {

        for (m in mineslist) {
            minefield[m.first][m.second].hasMine = true
        }

        val v = x0 + y0 * 9
        val l = mutableSetOf<Int>()

        while (l.size < nbMines) {
            l.addAll(List(nbMines - l.size) { Random.nextInt(1, 81) }.takeWhile { it != v })
        }

        val mines = l.toList()

        for (m in mines) {
            val x = (m - 1) % 9
            val y = (m - 1) / 9
            if (x != x0 && y != y0) {
                mineslist.add(Pair(x, y)) // FIXME
                minefield[x][y].hasMine = true
            }
        }
    }


    fun play(x: Int, y: Int, command: String) {
        if (!firstCellIsExplored) {
            firstCellIsExplored = true

            // init mines after 1st cell
            initMines(this.nbMines, x, y)

            // init number of neighbours
            initNeighbours()
        }

        when (command) {
            "mine" -> playMine(x, y)
            "free" -> playExplore(x, y)
        }
        printMinefield()

        if (checkWin()) {
            println("Congratulations! You found all the mines!")
            exitProcess(0)
        }
    }

    private fun playExplore(x: Int, y: Int) {


        if (!minefield[x][y].explored) {

            if (minefield[x][y].hasMine) {
                minefield[x][y].explored = true
                printMinefield(true)
                println("You stepped on a mine and failed!")
                exitProcess(0)
            } else {
                if (!minefield[x][y].hasMine) {
                    if (minefield[x][y].neighbours == 0) {
                        minefield[x][y].explored = true
                        for (neighbour in neighboursList(x, y)) {
                            if (stack.search(neighbour) < 0) {
                                stack.push(neighbour)
                            }
                        }
                    } else {
                        minefield[x][y].explored = true
                    }
                }
            }
        }
        while (stack.isNotEmpty()) {
            val c = stack.pop()
            playExplore(c.first, c.second)
        }
    }


    private fun neighboursList(positionX0: Int, positionY0: Int): List<Pair<Int, Int>> {
        val x0 = if (positionX0 > 0) positionX0 - 1 else 0
        val xf = if (positionX0 < 8) positionX0 + 1 else 8
        val y0 = if (positionY0 > 0) positionY0 - 1 else 0
        val yf = if (positionY0 < 8) positionY0 + 1 else 8

        val nList = mutableListOf<Pair<Int, Int>>()

        for (y in y0..yf) {
            for (x in x0..xf) {
                if (!minefield[x][y].explored && !minefield[x][y].hasMine && !(x == positionX0 && y == positionY0)) {
                    nList.add(Pair(x, y))
                }
            }
        }
        return nList
    }


    private fun getNbMinesNearCell(positionX0: Int, positionY0: Int): Int {
        var n = 0
        val x0 = if (positionX0 > 0) positionX0 - 1 else 0
        val xf = if (positionX0 < 8) positionX0 + 1 else 8

        val y0 = if (positionY0 > 0) positionY0 - 1 else 0
        val yf = if (positionY0 < 8) positionY0 + 1 else 8

        for (x in x0..xf) {
            for (y in y0..yf) {
                if (minefield[x][y].hasMine && !(x == positionX0 && y == positionY0)) {
                    n++
                }
            }
        }
        return n
    }

    fun checkWin(): Boolean {
        return minefield.flatMap { it.asIterable() }
            .all { (it.hasMine && it.isMarked) || (!it.hasMine && !it.isMarked) || (it.hasMine && !it.explored) || (!it.hasMine && it.explored) }
    }

    private fun printMinefield(failed: Boolean = false) {
        println()
        println(" │123456789│")
        println("—│—————————│")
        for (y in 0..8) {
            print("${y + 1}|")
            for (x in 0..8) {

                if (minefield[x][y].explored) {
                    if (minefield[x][y].hasMine && minefield[x][y].neighbours == 0) {
                        print("/")
                    } else if (minefield[x][y].hasMine && minefield[x][y].neighbours > 0) {
                        print(minefield[x][y].neighbours.digitToChar())
                    } else if (!minefield[x][y].hasMine && minefield[x][y].neighbours == 0) {
                        print("/")
                    } else if (!minefield[x][y].hasMine && minefield[x][y].neighbours > 0) {
                        print(minefield[x][y].neighbours.digitToChar())
                    }
                } else {
                    if (minefield[x][y].isMarked) {
                        print('*')
                    } else {
                        print(".")
                    }
                }
            }
            print("|")
            println()
        }
        println("—│—————————│")
    }
}

fun main() {

    print("How many mines do you want on the field? ")
    val nbMines = readln().toInt()

    val minesweeper = Minesweeper(nbMines)
    minesweeper.init()

    while (true) {
        try {
            print("Set/unset mines marks or claim a cell as free: ")
            val coordinates = readln().split(" ")

            val x = coordinates[0].toInt()
            val y = coordinates[1].toInt()
            val command = coordinates[2]
            minesweeper.play(x - 1, y - 1, command)

        } catch (e: Exception) {
            continue
        }
    }
}



