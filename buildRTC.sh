#!/bin/sh
#
#

#------------------------------------------------------------
# find_ros_jar
#
# Searches and adds jar files for ROS.
#------------------------------------------------------------
find_ros_jar()
{
    if test ! "x$ROS_MAVEN_PATH" = "x" ; then
        for file in `find $ROS_MAVEN_PATH -name '*.jar'`;do
            jar_files=$jar_files":"$file
        done
        echo "------------------------------------------------------------"
        echo $jar_files
        echo "------------------------------------------------------------"
    fi
}
#------------------------------------------------------------
# find_anthome
#
# This function estimate ANT_HOME from ant, which usually
# is a symbolic link to $ANT_HOME/bin/ant.
#------------------------------------------------------------
find_anthome()
{
    if test ! "x$ANT_HOME" = "x" ; then
        if test -d $ANT_HOME && test -f $ANT_HOME/bin/ant ; then
            return 0
        fi
        echo "ant cannot be found under ANT_HOME: $ANT_HOME"
    fi
    echo "Valid Environment variable ANT_HOME is not set. Searching..."
    tmp=`readlink -e $(which ant)`
    ant_path=`dirname $tmp | sed 's/\/bin$//'`
    if test "x$ant_path" = "x" ; then
        echo "Ant not found. Please install Ant and set ANT_HOME."
        exit 1
    fi
    export ANT_HOME=$ant_path
    return 0
}

find_anthome

echo "------------------------------------------------------------"
echo "Environment variables:"
echo "ANT_HOME: $ANT_HOME"
echo "------------------------------------------------------------"

find_ros_jar

if test $# -eq 0 ; then
    var_for="buildAllLinux"
else
    var_for=$1
fi

#
#
#
cd jp.go.aist.rtm.RTC
#ant $var_for -lib $ANT_HOME/lib
ant $var_for -lib $ANT_HOME/lib -lib $jar_files
if [ $? -ne 0 ];
then 
    exit 1
fi
echo "--"
cd ..
