#!/usr/bin/env bash

(
basedir="$(cd "$(dirname "$0")"; git rev-parse --show-toplevel)"
cd $basedir
while IFS=' ' read search banned; do
    echo "Banning $banned from $search"
    grep -nrE -- "import $banned" src/main/{java,kotlin}/"$search"|sed -E 's/^(.*):([0-9]+):(.*)/::error file=\1,line=\2::Illegal \3/g'|tee -a temp
done<<<$(cat .github/workflows/illegal-imports.txt|sed -E 's/#.*//;/^\s*$/d')
found=$(wc -l temp|cut -d ' ' -f 1)
rm -f temp
echo "# Found $found invalid imports. Check the files tab for more information.">>"$GITHUB_STEP_SUMMARY"
if [[ "$found" -ne 0 ]]; then
    exit 1
fi
)
