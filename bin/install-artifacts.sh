#!/usr/bin/env bash
#
#  Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
#  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
#  This code is free software; you can redistribute it and/or modify it
#  under the terms of the GNU General Public License version 2 only, as
#  published by the Free Software Foundation.  Oracle designates this
#  particular file as subject to the "Classpath" exception as provided
#  by Oracle in the LICENSE file that accompanied this code.
#
#  This code is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
#  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
#  version 2 for more details (a copy is included in the LICENSE file that
#  accompanied this code).
#
#  You should have received a copy of the GNU General Public License version
#  2 along with this work; if not, write to the Free Software Foundation,
#  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
#  Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
#  or visit www.oracle.com if you need additional information or have any
#  questions.
#
#
# Generate VisualVM 2.0 Artifacts
# Author: Oracle Corporation
# Date: 2020-09-09

#
# Pre-Requisites
# - Java JDK 1.8
# - Ant version >= 1.9.9
# - Maven 3.6.3+

set -e
set -o pipefail

# check ant
ANT_VERSION=`ant -version | egrep '1.9|1.10'`
if [ -z "$ANT_VERSION" ] ; then
   echo "You must have ant version > 1.9.9"
   exit 1
fi

# check Java
if [ -z "`java -version 2>&1 | grep '1.8'`" ] ; then
   echo "Java version must be 1.8"
   exit 1
fi

echo "Checking for Maven"
which mvn 
if [ $? -ne 0 ] ; then
   echo "Maven must be in the PATH"
   exit 1
fi

if [ $# -ne 1 ] ; then
   echo "Usage: $0 directory-to-clone into"
   exit 1
fi

DIR=$1

if [ -d $DIR -o -f $DIR ] ; then
   echo "Directory $DIR exists, please choose another directory."
   exit 1
fi

git clone https://github.com/oracle/visualvm.git $DIR

cd $DIR

git checkout release204

cd visualvm

unzip nb113_platform_19062020.zip

ant build-zip

cd dist

unzip visualvm.zip

cd ..

ant nbms

mvn -DnetbeansInstallDirectory=dist/visualvm   \
    -DnetbeansNbmDirectory=build/updates   \
    -DgroupIdPrefix=org.graalvm.visualvm  \
    -DforcedVersion=RELEASE204 org.apache.netbeans.utilities:nb-repository-plugin:populate
