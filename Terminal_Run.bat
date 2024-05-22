::This is the Batch File that executes the game's .jar file with a terminal.
::You can run the game this way to see printed exceptions, such as game crashes.

::Opens Terminal and dosn't display input.
@echo off

::Sets CLASSPATH.
set CLASSPATH=.

::Runs the .jar file, executing the game.
java -jar Output.jar

::Close terminal on enter press.
pause
