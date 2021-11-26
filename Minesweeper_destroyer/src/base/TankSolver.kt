package base

import base.Base
import base.TankSolver.Companion.tank_checker
import MSolver as MS
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
        var xx = listOf(-1, 1, 0, 0, 1, 1, -1, -1)
        var yy = listOf(0, 0, 1, -1, 1, -1, 1, -1)
        var mark = Array<Array<Boolean>>(Base.BoardHeight) { Array<Boolean>(Base.BoardWidth) { i -> false } }
        lateinit var tank_checker: Array<Array<Int>>


        fun countNumberOfboom(i: Int, j: Int): Int {
            var count = 0
            for (k in 0..xx.size - 1) {
                if (Base.onScreen(i + xx[k], j + yy[k]) != -10 && tank_checker[i + xx[k]][j + yy[k]] == -3)
                    count++
            }
            return count
        }

        //debug tool
        fun printRegion(x: regionList) {
            println("-------------------printing region, size: ${x.Reg.size}--")
            for (i in x.Reg)
                println("${i.r} ${i.c}")

            println("-------------------printing end-----------------------")
        }

        fun printOnScreen() {
            println("------printing onScreen---------")
            for (i in 0..Base.BoardHeight - 1) {
                for (j in 0..Base.BoardWidth - 1)
                if (tank_checker[i][j]>=0)
                {
                    print("  ${tank_checker[i][j]}")
                }
                else  print(" ${tank_checker[i][j]}")
                println()
            }
            println("-------------------------------")
        }

        fun printCk() {
            println("------printing ck---------")
            for (i in 0..Base.BoardHeight - 1) {
                for (j in 0..Base.BoardWidth - 1) {
                    print("${Base.flags[i][j]} ")
                }
                println()
            }
            println("-------------------------------")
        }

        fun printSolution (solution:Array<Boolean>,size:Int){
            print("Current Solutions: ")
            for (i in 0..size-1){
                print (" ${solution[i]}")
            }
            println()
        }

        fun Solver() {

            tank_checker = Array<Array<Int>>(Base.BoardHeight + 1) { i -> Array<Int>(Base.BoardWidth + 1) { i -> 0 } }
            // Be extra sure it's consistent
            for (i in 0..Base.BoardHeight - 1)
                for (j in 0..Base.BoardWidth - 1) {
                    tank_checker[i][j] = Base.onScreen(i, j)

                    if (Base.flags[i][j])
                        tank_checker[i][j] = -3
                }

              printOnScreen()
//            printCk()

            // Be extra sure it's consistent
            Thread.sleep(100)
            Base.robot.mouseMove(0, 0)
            Thread.sleep(20)
            Base.updateOnScreen()
            Base.robot.mouseMove(Base.mouseLocX, Base.mouseLocY)
            if (!Base.checkConsistency()) {
                println("Inconsistent!!!")
                return
            }

            println("enter solver")

            var success = false
            var regions = findRegion()
            println("Regions size: ${regions.size}")
            var s = 0
            var prob_best = 0.0 // Store information about the best probability
            var prob_besttile = -1
            var prob_best_s = -1

            for (x in regions) {
                success = false
                println("+++++++++++++++enter new Region, size: ${x.Reg.size}+++++++++++++++")
                //clearing
                solutions.clear()
                //******Delete later********
                printRegion(x)
                //**************************
                Backtracking(x.Reg, 0)

                var MineDef = true // Definitely a mine
                var EmptyDef = true  // Definitely not a mine

                println ("------------entering checking process----------")
                for (y in 0..x.Reg.size - 1) {
                    MineDef = true // Definitely a mine
                    EmptyDef = true  // Definitely not a mine

                    for (i in solutions) {
//                        printSolution(i,x.Reg.size)
//                        if (!MineDef && !EmptyDef)
//                            break
                        if (i[y]) EmptyDef = false
                        if (!i[y]) MineDef = false
                    }

                    if (MineDef) {
                        Base.flags[x.Reg[y].r][x.Reg[y].c] = true
                        Base.flagOn(x.Reg[y].r, x.Reg[y].c)
                    }

                    if (EmptyDef) {
                        success = true
                        println ("great success!! at ${x.Reg[y].r} ${x.Reg[y].c}")
                        Base.clickOn(x.Reg[y].r, x.Reg[y].c)
                    }
                }

                if (success) {
                    println("Tank success")
                    return
                }
                else continue

                print(solutions.size) // for debugging

                /*
                        // Take the guess, since we can't deduce anything useful
                    System.out.printf(
                            "TANK Solver guessing with probability %1.2f at step %d (%dms, %d cases)%s\n",
                            prob_best, numMines, tankTime, totalMultCases,
                            (borderOptimization?"":"*"));
                    Pair<Integer,Integer> q = segregated.get(prob_best_s).get(prob_besttile);
                    int qi = q.getFirst();
                    int qj = q.getSecond();
                    clickOn(qi,qj);
                 */

                // probability start here
                // adapt from luckytoilet/ DavidNHill
                if (solutions.isEmpty()) {
                    println("die")
                    return
                }
//
//                // Calculate probabilities, in case we need it
//                if (success) {
//                    continue
//                    println ("Tank success")
//                }
                var maxEmpty = -10000
                var iEmpty = -1
                for (i in 0..x.Reg.size - 1) {
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

                print("prob calc : ${probability}")

                if (probability > prob_best) {
                    prob_best = probability
                    prob_besttile = iEmpty
                    prob_best_s = s
                }

                s++
            }
            println("prob used ")
            Base.guessRandomly()
            print("RegionS: ${regions.size}, probbest: ${prob_best_s},prob_best_tile: ${prob_besttile}")
            MS.exit()
//            var q: coordinate = regions[prob_best_s].Reg[prob_besttile]
//            var qi: Int = q.r
//            var qj: Int = q.c
//            Base.clickOn(qi, qj)

        }

        //ck to check if a square is already progressed by backtracking
        var ck = Array<Array<Boolean>>(Base.BoardHeight) { Array<Boolean>(Base.BoardWidth) { i -> false } }

        var solution = Array<Boolean>(Base.BoardHeight * Base.BoardWidth + 1) { i -> false }
        var solutions = ArrayList<Array<Boolean>>()

        fun Backtracking(Region: ArrayList<coordinate>, time: Int) {
//            printOnScreen()
//            printCk()
            //check if everything is still valid
            for (i in Region)
                if (ck[i.r][i.c]) {
                    for (j in   0..xx.size - 1)
                        if (Base.onScreen(i.r + xx[j], i.c + yy[j]) > 0) {
                            // current considered square
                            var tx = i.r + xx[j]
                            var ty = i.c + yy[j]

                            //check if any square around it is unfinishe
                            var finished = true
                            for (k in 0..xx.size - 1)
                                if (Base.onScreen(
                                        tx + xx[k],
                                        ty + yy[k]
                                    ) != -10 && tank_checker[tx + xx[k]][ty + yy[k]] == -1 && !ck[tx + xx[k]][ty + yy[k]]
                                ) {
                                    finished = false
                                    break
                                }

                            if (!finished) continue

                            var numMine = countNumberOfboom(tx, ty)

                            if (numMine > Base.onScreen(tx, ty)) {
//                                println(
//                                    "Nope,${numMine} > onScreen[${tx}][${ty}] =${
//                                        Base.onScreen(
//                                            tx,
//                                            ty
//                                        )
//                                    } ,boom too large, not valid"
//                                )
                                return
                            }

                            if (numMine < Base.onScreen(tx, ty)) // case a number square between 2 regions is uncertain
                            {
//                                println(
//                                    "Nope,${numMine} < onScreen[${tx}][${ty}] =${
//                                        Base.onScreen(
//                                            tx,
//                                            ty
//                                        )
//                                    } boom too small, not valid"
//                                )
                                return
                            }

                        }
                }

                // if end of a case
                if (time == Region.size) {
                    println("Solution added :333333")
//                    printOnScreen()
                    printSolution(solution,Region.size)
                    Companion.solutions.add(solution.copyOf())
//                    solutionsDeepCopy(solution,Region.size)
                    println("solutions size: ${solutions.size}")
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
                tank_checker[Region[time].r][Region[time].c] = -1
                Backtracking(Region, time + 1)

                ck[Region[time].r][Region[time].c] = false
            }



            // find region------
            fun DFS(i: Int, j: Int) {
                Companion.temp.addMem(i, j)
                mark[i][j] = true
                for (k in 0..7)
                    if (Base.onScreen(i + xx[k], j + yy[k]) != -10 && tank_checker[i + xx[k]][j + yy[k]] > 0) {
                        for (l in 0..7)
                            if (Base.onScreen(i + xx[l], j + yy[l]) != -10 && tank_checker[i + xx[l]][j + yy[l]] == -1 && !mark[i + xx[l]][j + yy[l]]
                            )
                                if (abs(xx[k] - xx[l]) <= 1 && abs(yy[k] - yy[l]) <= 1)
                                    DFS(i + xx[l], j + yy[l])
                    }
            }

            fun findRegion(): ArrayList<regionList> {
                for (i in 0..Base.BoardHeight-1)
                    for (j in 0..Base.BoardWidth-1)
                        mark[i][j] = false

                var Res = ArrayList<regionList>()
                for (i in 0..Base.BoardHeight - 1)
                    for (j in 0..Base.BoardWidth - 1)
                        if (!mark[i][j] && tank_checker[i][j] == -1 && countOpenedSquareAround(i, j) > 0)
                        {
                            temp = regionList()
                            temp.Reg.clear()
                            DFS(i, j);
                            Res.add(temp)
                        }
                return Res
            }

            fun countOpenedSquareAround(i: Int, j: Int): Int {
                var count = 0
                for (x in 0..xx.size - 1)
                    if (Base.onScreen(i + xx[x], j + yy[x]) != -10 && tank_checker[i + xx[x]][j + yy[x]] > 0)
                        count++

                return count
            }

    }
}