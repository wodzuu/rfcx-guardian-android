#!/usr/bin/env bash

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

export ROLE=$1;
export KEY_PSWD=$2;

cd $SCRIPT_DIR/../;

if [ ! -d tmp ]; then
  echo "creating tmp directory";
  mkdir tmp;
fi

cd rfcx-guardian-$ROLE;
rm local.properties build.xml proguard-project.txt;

echo "generating build configuration...";
export BUILD_CONFIG=`android update project -p . -n rfcx-guardian-$ROLE`;

echo "key.store=$SCRIPT_DIR/rfcx-guardian.jks" >> local.properties;
echo "key.alias=rfcx-guardian-android-$ROLE" >> local.properties;
echo "key.store.password=$KEY_PSWD" >> local.properties;
echo "key.alias.password=$KEY_PSWD" >> local.properties;

echo "setting up build process...";
export ANT_CLEAN=`ant clean`;
echo "building apk...";
export APK_PATH=`ant release | grep 'Release Package:' | cut -d':' -f 2 | cut -d' ' -f 2`;
export APK_VERSION=`cat AndroidManifest.xml | grep 'android:versionName=' | cut -d'"' -f 2`;
cp $APK_PATH ../tmp/$ROLE-$APK_VERSION.apk;

adb push ../tmp/$ROLE-$APK_VERSION.apk /data/data/org.rfcx.guardian.updater/files/$ROLE-$APK_VERSION.apk;
adb shell pm install -r /data/data/org.rfcx.guardian.updater/files/$ROLE-$APK_VERSION.apk;
adb shell rm /data/data/org.rfcx.guardian.updater/files/$ROLE-$APK_VERSION.apk;

cd $SCRIPT_DIR/../;