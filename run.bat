@echo off
SETLOCAL

SET NAME=predictor
SET VERSION=1.0
SET SCALA_VERSION=2.11
SET D=%~dp0
SET JAR=%D%\target\scala-%SCALA_VERSION%\%NAME%-assembly-%VERSION%.jar
SET THREADS=4
SET _argcActual=0
FOR %%i in (%*) DO SET /A _argcActual+=1

SET ACTION=%1
SET OWNER=%2
SET REPOSITORY=%3

IF NOT EXIST %JAR% GOTO JarNotFound
IF NOT %_argcActual%==3 GOTO InvalidArgs
IF NOT %OWNER: =%==%OWNER% GOTO Whitespace
IF NOT %REPOSITORY: =%==%REPOSITORY% GOTO Whitespace
GOTO ValidArgs

:JarNotFound
ECHO JAR file does not exist.
ECHO Make sure you run `sbt assembly` to build the JAR executable.
GOTO end

:InvalidArgs
ECHO Wrong number of arguments, expected 3 arguments.
ECHO Usage: %0 action owner_name repository_name
GOTO end

:Whitespace
ECHO Whitespace is not supported.
GOTO end

:ValidArgs
SET PROPS=-Dfile.encoding=UTF8 -Drepository.owner=%OWNER% -Drepository.name=%REPOSITORY% -Dscala.concurrent.context.minThreads=%THREAD% -Dscala.concurrent.context.numThreads=%THREADS% -Dscala.concurrent.context.maxThreads=%THREADS%
java %PROPS% -jar %JAR% %ACTION%
:end

ENDLOCAL
