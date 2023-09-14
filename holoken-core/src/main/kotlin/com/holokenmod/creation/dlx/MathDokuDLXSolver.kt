package com.holokenmod.creation.dlx

import com.holokenmod.grid.Grid

class MathDokuDLXSolver {
    suspend fun solve(grid: Grid): Int {
        return MathDokuDLX(grid).solve(DLX.SolveType.MULTIPLE)
    }
}
