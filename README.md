# Minesweeper_destroyer
The main goal of the project is to create a Python program to solve the Minesweeper game
using
+ Screen reader
+ Logic math : an algorithm to enumerate all possible configurations then using logic math to
choose the best result
+ Probability: Sometimes luck is needed to win the game, we would then use probability
to optimize our luck and secure victory
+ Graph theory: BFS to optimize the process
## Project description and implementation detail
Please check the report file for more detail 
## How to run 
*Be noted that Minesweeper_destroyer only works on Minesweeper of windows 10
Step 1: Turn on Minesweeper and put it in full screen mode 
Step 2: In src/base/Base.java, fill in your screen resolution in the 2 varibles ScreenWidth and ScreenHeight
Step 3: execute src/MSolver.java/ and quickly minimize the process to reveal minesweeper screen
Step 4: Enjoy
## Some note 
Please be noted that minesweeper is still a game of luck (the highest winrate of the best player in minesweeper.online is only 39% in 100 games). Thus in many case in hard mode, the bot still loses some match, yet it is not because the bot is not optimal, it is usually because of 50-50 situtation. The current winrate of the bot is 43% in hard mode. Please check the report file for more detail
