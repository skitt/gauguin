package org.piepmeyer.gauguin.grid

import org.piepmeyer.gauguin.options.GameOptionsVariant
import org.piepmeyer.gauguin.options.GameVariant
import kotlin.time.Duration

class Grid(
    val variant: GameVariant,
) {
    val cells = createCells()
    var cages: List<GridCage> = mutableListOf()
    var selectedCell: GridCell? = null

    var playTime: Duration = Duration.ZERO
    var solvedFirstTimeOfKind = false
    var solvedBestTimeOfKind = false

    var startedToBePlayed = false
    var isActive = false
    var creationDate: Long = 0
        private set

    constructor(variant: GameVariant, creationDate: Long) : this(variant) {
        this.creationDate = creationDate
    }

    private fun createCells(): List<GridCell> {
        val listOfCells = mutableListOf<GridCell>()

        var cellnum = 0
        for (row in 0 until variant.height) {
            for (column in 0 until variant.width) {
                listOfCells += GridCell(cellnum++, row, column)
            }
        }

        return listOfCells.toList()
    }

    val gridSize: GridSize
        get() = variant.gridSize

    fun getCage(
        row: Int,
        column: Int,
    ): GridCage? {
        return if (!isValidCell(row, column)) {
            null
        } else {
            cells[column + row * variant.width].cage
        }
    }

    fun invalidsHighlighted(): List<GridCell> {
        return cells.filter {
            it.isInvalidHighlight
        }
    }

    fun cheatedHighlighted(): List<GridCell> {
        return cells.filter {
            it.isCheated
        }
    }

    fun markInvalidChoices() {
        cells.forEach { it.isInvalidHighlight = it.shouldBeHighlightedInvalid() }
    }

    fun isSolved(): Boolean = !cells.any { !it.isUserValueCorrect }

    fun numberOfMistakes(): Int {
        return cells.count { it.isUserValueSet && !it.isUserValueCorrect }
    }

    private fun getNumValueInRow(ocell: GridCell): Int {
        return cells.count {
            it.row == ocell.row &&
                it.userValue == ocell.userValue
        }
    }

    private fun getNumValueInCol(ocell: GridCell): Int {
        return cells.count {
            it.column == ocell.column &&
                it.userValue == ocell.userValue
        }
    }

    fun getPossiblesInRowCol(cell: GridCell): List<GridCell> {
        return cells.filter { it.isPossible(cell.userValue) }
            .filter { it.row == cell.row || it.column == cell.column }
    }

    fun getCellAt(
        row: Int,
        column: Int,
    ): GridCell? {
        if (!isValidCell(row, column)) {
            return null
        }

        return getValidCellAt(row, column)
    }

    fun getValidCellAt(
        row: Int,
        column: Int,
    ): GridCell {
        return cells[column + row * variant.width]
    }

    private fun isValidCell(
        row: Int,
        column: Int,
    ): Boolean {
        return row >= 0 && row < variant.height && column >= 0 && column < variant.width
    }

    fun clearAllCages() {
        for (cell in cells) {
            cell.cage = null
        }
        cages = mutableListOf()
    }

    fun getCell(index: Int): GridCell {
        return cells[index]
    }

    fun addCage(cage: GridCage) {
        cages = cages + cage
    }

    fun clearUserValues() {
        for (cell in cells) {
            cell.clearUserValue()
            cell.isCheated = false
        }

        selectedCell?.let {
            it.isSelected = false
        }
    }

    fun clearLastModified() {
        for (cell in cells) {
            cell.isLastModified = false
        }
    }

    override fun toString(): String = GridToString(this).printGrid()

    fun isUserValueUsedInSameRow(
        cellIndex: Int,
        value: Int,
    ): Boolean {
        val startIndex = cellIndex - cellIndex % variant.width
        for (index in startIndex until startIndex + variant.width) {
            if (index != cellIndex && cells[index].userValue == value) {
                return true
            }
        }
        return false
    }

    fun isUserValueUsedInSameColumn(
        cellIndex: Int,
        value: Int,
    ): Boolean {
        var index = cellIndex % variant.width
        while (index < variant.surfaceArea) {
            if (index != cellIndex && cells[index].userValue == value) {
                return true
            }
            index += variant.width
        }
        return false
    }

    fun isValueUsedInSameRow(
        cellIndex: Int,
        value: Int,
    ): Boolean {
        val startIndex = cellIndex - cellIndex % variant.width
        for (index in startIndex until startIndex + variant.width) {
            if (index != cellIndex && cells[index].value == value) {
                return true
            }
        }
        return false
    }

    fun isValueUsedInSameColumn(
        cellIndex: Int,
        value: Int,
    ): Boolean {
        var index = cellIndex % variant.width
        while (index < variant.surfaceArea) {
            if (index != cellIndex && cells[index].value == value) {
                return true
            }
            index += variant.width
        }
        return false
    }

    fun addPossiblesAtNewGame() {
        cells.forEach { it.possibles = variant.possibleDigits }
    }

    fun userValueChanged() {
        updateDuplicatedNumbersInRowOrColumn()
    }

    fun updateDuplicatedNumbersInRowOrColumn() {
        cells.forEach {
            it.duplicatedInRowOrColumn = it.isUserValueSet && (getNumValueInCol(it) > 1 || getNumValueInRow(it) > 1)
        }
    }

    fun isCheated(): Boolean {
        return cells.any { it.isCheated }
    }

    fun hasCellsWithSinglePossibles(): Boolean {
        return cells.any { it.possibles.size == 1 }
    }

    val options: GameOptionsVariant
        get() = variant.options

    fun belongsCellToTheEastOfFirstCellToSameCage(
        cage: GridCage,
        distance: Int,
    ): Boolean {
        val cellToTheEast =
            getCellAt(
                cage.cells.first().row,
                cage.cells.first().column + distance,
            )

        return cellToTheEast?.cage == cage
    }
}
