#
# File:    Readme.txt
# Project: Speckle Tracking Echocardiography, Master's Project
# Date:    05/13/2015
# Author:  Juraj Strecha, xstrec01@stud.fit.vutbr.cz,
#                         duri.strecha@gmail.com
#
# 1. INSTALLATION
# ===============
# To run the application a Java JRE and OpenCV library have to be set up.
#
# 1.1 Java 7 JRE
# --------------
# 1.1.1 Windows
#
# Download an executable jre-7u79-windows-i586.exe (for x86) or jre-7u79-windows-x64.exe (for x64)
# from http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html. Run the
# executable. Make sure that environment variable $PATH contains "C:\Program Files\Java\jre7\bin"
# - press WINDOWS + R, type 'cmd', type 'set PATH=%PATH%;C:Program Files\Java\jre7\bin'
# (single quotes excluded). Test if Java interpreter works by running a command 'java -version'.
#   
# 1.1.2 Linux
# Check if there is a java already installed by running command 'java -version'. If you get an error,
# or there is a OpenJDK Runtime Environment(IcedTea) you must install the VM environment.
# If it says Java version "1.7.X_XX" you don't have to install anything and can proceed to OpenCV
# installation part.
# Remove previous Java using 'sudo apt-get purge openjdk-\*' or 'sudo yum purge openjdk-\*'.
# Download a file jre-7u79-linux-i586.tar.gz or jre-7u79-linux-x64.tar.gz from
# http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html.
# Make directory for Java using 'sudo mkdir /usr/local/java'. Extract the installation files using
# 'sudo tar zxvf jre-7u79-linux-i586.tar.gz /usr/local/java' or
# 'sudo tar zxvf jre-7u79-linux-x64.tar.gz /usr/local/java' command. Edit the file running 
# 'sudo gedit /etc/profile'. Paste follwing lines at the end of the file and save:
# JAVA_HOME=/usr/local/java/jre1.7.0_79
# PATH=$PATH:$HOME/bin:$JAVA_HOME/bin
# export JAVA_HOME
# export PATH
#
# Inform the system about Java version running 'sudo update-alternatives --install "/usr/bin/java" "java" "/usr/local/java/jre1.7.0_79/bin/java" 1' and 'sudo update-alternatives --set java /usr/local/java/jre1.7.0_79/bin/java'.
# Reboot the system and check correct installation using 'java -version' which should print
# a message with correct java version on the first line.

# 1.2 OpenCV 2.4.10
# -----------------
# 1.2.1 Windows
#
# 1.2.2 Linux
#    Prerequisities:
# [compiler] sudo apt-get install build-essential or sudo yum install build-essential
# [required] sudo apt-get install cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev or sudo yum install cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev

#
# 2. RUN
# ======
# 2.1 Windows
# 
#
# 2.2 Linux
# 
