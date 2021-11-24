package base

import base.Base
import kotlin.math.abs

data class coordinate (var r:Int = 0,var c:Int = 0)
class regionList {
    var Reg = ArrayList<coordinate>()
    fun addMem(x:Int,y:Int){
         Reg.add(coordinate(x,y))
    }
}
class TankSolver {
    companion object {

    var temp = regionList()
    var  xx  = listOf(-1,1,0,0,1,1,-1,-1)
    var  yy  = listOf(0,0,1,-1,1,-1,1,-1)
    var mark = Array<Array<Boolean>>(Base.BoardHeight){Array<Boolean>(Base.BoardWidth){ i -> false }}
    var tank_checker  = Base.onScreen.clone()


    fun countNumberOfboom (i:Int,j:Int):Int{
        var count = 0
        for (k in 0..xx.size-1){
             if (tank_checker[i+xx[k]][j+yy[k]] == -3)
                 count++
        }
        return count
    }




        fun Solver() {
            // Be extra sure it's consistent


            // Be extra sure it's consistent
            Thread.sleep(100)
            Base.robot.mouseMove(0, 0)
            Thread.sleep(20)
            Base.updateOnScreen()
            Base.robot.mouseMove(Base.mouseLocX, Base.mouseLocY)
            //dumpPosition();
            //dumpPosition();
            if (!Base.checkConsistency()) return

            println ("enter solver")

            var success = false
            var regions = findRegion()
            var s = 0
            var prob_best = 0.0 // Store information about the best probability
            var prob_besttile = -1
            var prob_best_s = -1

            for (x in regions) {
                //clearing
                solutions.clear()
                Backtracking(x.Reg, 0)

                var MineDef = true // Definitely a mine
                var EmptyDef = true  // Definitely not a mine
                for (y in 0..x.Reg.size-1) {
                    for (i in solutions) {
                        if (!MineDef && !EmptyDef)
                            break
                        if (i[y]) EmptyDef = false
                        if (!i[y]) MineDef = false
                    }

                    if (MineDef) {
                        Base.flags[x.Reg[y].r][x.Reg[y].c] = true
                        Base.flagOn(x.Reg[y].r, x.Reg[y].c)
                    }

                    if (EmptyDef) {
                        success = true
                        Base.clickOn(x.Reg[y].r, x.Reg[y].c)
                    }
                }

                if (success) continue
                // probability start here
                // adapt from luckytoilet/ DavidNHill
                if (solutions.isEmpty()){
                    println("die")
                    return
                }

                // Calculate probabilities, in case we need it
                if (success) continue
                var maxEmpty = -10000
                var iEmpty = -1
                for (i in 0..regions.size-1) {
                    var nEmpty = 0
                    for (sln in solutions) {
                        if (!sln[i]) nEmpty++
                    }
                    if (nEmpty > maxEmpty) {
                        maxEmpty = nEmpty
                        iEmpty = i
                    }
                }
                val probability = (maxEmpty.toDouble() / solutions.size) as Double

                if (probability > prob_best) {
                    prob_best = probability
                    prob_besttile = iEmpty
                    prob_best_s = s
                }

                s++
            }
            println ("prob used ")
            val q: coordinate = regions.get(prob_best_s).Reg[prob_besttile]
            val qi: Int = q.r
            val qj: Int = q.c
            Base.clickOn(qi, qj)

        }

        //ck to check if a square is already progressed by backtracking
        var ck = Array<Array<Boolean>>(Base.BoardHeight) { Array<Boolean>(Base.BoardWidth) { i -> false } }

        var solution = Array<Boolean>(Base.BoardHeight * Base.BoardWidth + 1) { i -> false }
        var solutions = ArrayList<Array<Boolean>>()
        fun Backtracking(Region: ArrayList<coordinate>, time: Int) {

            //check if everything is still valid
            for (i in Region)
                if (ck[i.r][i.c]) {
                    for (j in 0..xx.size - 1)
                        if (Base.onScreen(i.r + xx[j], i.c + yy[j]) > 0) {
                            // current considered square
                            var tx = i.r + xx[j]
                            var ty = i.r + yy[j]

                            //check if any square around it is unfinished
                            for (k in 0..xx.size - 1)
                                if (Base.onScreen(tx + xx[k], ty + yy[k]) == -1 && !ck[tx + xx[k]][ty + yy[k]])
                                    continue

                            var numMine = countNumberOfboom(tx, ty)

                            if (numMine > Base.onScreen(tx, ty))
                                return

                            if (numMine < Base.onScreen(tx, ty)) // case a number square between 2 regions is uncertain
                                return

                        }
                }

            // if end of a case
            if (time == Region.size) {
                solutions.add(solution)
                return
            }

            // check that this square is already proccessed
            ck[Region[time].r][Region[time].c] = true

            // case there's a mine
            solution[time] = true
            tank_checker[Region[time].r][Region[time].c] = -3
            Backtracking(Region, time + 1)

            //case there's no mine
            solution[time] = false
            tank_checker[Region[time].r][Region[time].c] = 0
            Backtracking(Region, time + 1)

            ck[Region[time].r][Region[time].c] = false
        }


        // find region------
        fun DFS(i: Int, j: Int) {
            temp.addMem(i, j)
            mark[i][j] = true
            for (k in 0..7)
                if (Base.onScreen(i + xx[k], j + yy[k]) != -10 && Base.onScreen[i + xx[k]][j + yy[k]] > 0) {
                    for (l in 0..7)
                        if (Base.onScreen(
                                i + xx[l],
                                j + yy[l]
                            ) != -10 && Base.onScreen[i + xx[l]][j + yy[l]] == -1 && !mark[i + xx[l]][j + yy[l]]
                        )
                            if (abs(xx[k] - xx[l]) <= 1 && abs(yy[k] - yy[l]) <= 1)
                                DFS(i + xx[l], j + yy[l])
                }
        }

        fun findRegion(): ArrayList<regionList> {
            var Res = ArrayList<regionList>()
            for (i in 0..Base.BoardHeight-1)
                for (j in 0..Base.BoardWidth-1)
                    if (!mark[i][j] && Base.onScreen[i][j] == -1 && Base.countFreeSquaresAround(Base.onScreen,i,j) > 0) {
                        temp.Reg.clear();
                        DFS(i, j);
                        Res.add(temp)
                    }
            return Res
        }
    }
}