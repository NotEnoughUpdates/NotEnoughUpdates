#!/bin/bash

if [[ -z $1 ]];
then
  GIT_ROOT=$(git rev-parse --show-toplevel 2> /dev/null)
  if [ -d "$GIT_ROOT" ]; then
    LOG_DIR="$GIT_ROOT/run/logs"
    DETECTOR_SRC_PATH="$GIT_ROOT/src/main/java/io/github/moulberry/notenoughupdates/miscfeatures/CrystalMetalDetectorSolver.java"
  else
    unset GIT_ROOT
    echo "No git root found, looking for MineCraft in default location..."
    LOG_DIR=~/.minecraft/logs
    [ ! -d "$LOG_DIR" ] && { echo "MineCraft log directory not found - exiting"; exit 1; }
    echo "Found MineCraft log directory: $LOG_DIR"
  fi
  LOG_PATH="$LOG_DIR/latest.log"
else
  LOG_PATH="$1"
fi

[ ! -f "$LOG_PATH" ] && { echo "$LOG_PATH does not exist"; exit 1; }

# Get locations in the format:
# <long> x=<byte>, y=<byte>, z=<byte>
LOCATIONS="$(sed -n 's/.*Relative: BlockPos{\(.*\)} (\(.*\))/\2L, \/\/ \1/p' "$LOG_PATH")"
if [ -v "$DETECTOR_SRC_PATH" ]; then
  SRC_LOCATIONS=
else
  SRC_LOCATIONS="$(sed -n 's/^\s*\(-\?[0-9]\+\)L,\s*\/\/ \(.*\)/\1L, \/\/ \2/p' "$DETECTOR_SRC_PATH")"
fi

sort < <(echo "$LOCATIONS"; echo "$SRC_LOCATIONS") | uniq | awk '{printf "%-18s %s %s %s %s \n", $1, $2, $3, $4, $5}'

# TODO: grab current list from source
