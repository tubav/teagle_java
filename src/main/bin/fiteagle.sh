#!/usr/bin/env bash

_log="`mktemp -t fiteagle.XXXXX`"

function testFITeagle {  
  echo "Testing FITeagle (this might take a while)..."
  mvn -B test > "${_log}" 2>&1
  if [ "0" != "$?" ]; then
    echo >&2 "FAILED. Please have a look at '${_log}'."
    exit 3
  fi
}

function runFITeagle {
  main_dir="./delivery/xmlrpc"  
  echo "Starting FITeagle..."
  cd "${main_dir}"
  [ -x ./src/main/bin/ssl_create_server_cert.sh ] || { echo "ERROR 5"; exit 5; }
  [ -f target/jetty-ssl.keystore ] || ./src/main/bin/ssl_create_server_cert.sh
  mvn -B -q jetty:run
  if [ "0" != "$?" ]; then
    echo >&2 "FAILED. Please have a look above."
    exit 3
  fi
}

function usage {
    echo "Usage: ./src/main/bin/fiteagle.sh test|start"
}

case $1 in
    test) testFITeagle;;
    start) runFITeagle;;
    *) usage
esac
