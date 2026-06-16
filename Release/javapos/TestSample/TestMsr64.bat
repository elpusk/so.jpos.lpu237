@echo off

REM ***************************************
REM * Setup the ClassPath and Save the    *
REM * old Classpath                       *
REM ***************************************
set oldcp=%classpath%
set oldlp=%lp%
set classpath=..\jposlib\jpos114.jar;..\jposlib\xerces.jar;"%cd%"\

REM ***************************************
REM *  Add Device Specific jar's here...  *
REM ***************************************
REM set classpath=%classpath%;c:\path_to_service.jar
set classpath=%classpath%;..\lpu237lib\JposLpu237MsrSO.jar

REM ****************************************
REM * Setup the Library Path for JNI DLLs(tg_lpu237_jni.dll)  *
REM ****************************************
REM %lp% is the library path used when loading
REM java native methods
REM usually:
REM set lp=c:\windows\system32
REM set lp=%lp%;..\lib
set lp=%lp%;..\lpu237lib\x64

REM in this case we set it to the lib directory
REM if the library path is not set then the directory
REM where you have tg_lpu237_dll.dll loaded must be in
REM the executable PATH.
set path=%path%;..\lpu237lib\x64


REM *****************************************
REM *              RUN TestMSR              *
REM *****************************************
java -cp %classpath% -Djava.library.path=%lp% TestMSR

REM *****************************************
REM *       Restore the Old Classpath       *
REM *****************************************
set classpath=%oldcp%
set lp=%oldlp%
