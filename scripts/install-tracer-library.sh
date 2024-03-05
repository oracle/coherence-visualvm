#!/bin/bash
#
# Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#
#
# Purpose: Build and install the org-graalvm-visualvm-modules-tracer plugin as it is not available in Maven.
set -e
echo "Installing required tracer VisualVM dependencies"
TEMP_DIR=`mktemp -d`
echo "Temp dir = ${TEMP_DIR}"

trap "rm -rf $TEMP_DIR 2>&1 > /dev/null" 0 1 2 3

cd $TEMP_DIR
echo "Cloning VisualVM..."
git clone https://github.com/oracle/visualvm.git
cd visualvm
git checkout 2.1.7

curl -Lo /tmp/nb140_platform_20230511.zip https://github.com/oracle/visualvm/releases/download/2.1.7/nb140_platform_20230511.zip
cd visualvm
unzip /tmp/nb140_platform_20230511.zip

echo "Building VisualVM..."
ant build-zip

cd ../plugins
ant build

MODULE_NAME=org-graalvm-visualvm-modules-tracer

TRACER=`find . -name ${MODULE_NAME}.jar | sed 1q`
FULL_PATH=`pwd`/${TRACER}

echo "Installing ${FULL_PATH}"

set -x
mvn install:install-file -Dfile=${FULL_PATH} -DgroupId=org.graalvm.visualvm.modules -DartifactId=org-graalvm-visualvm-modules-tracer -Dversion=2.1 -Dpackaging=jar

ls -l ~/.m2/repository/org/graalvm/visualvm/modules/org-graalvm-visualvm-modules-tracer/2.1



