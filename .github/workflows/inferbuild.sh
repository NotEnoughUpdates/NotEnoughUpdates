#!/bin/bash
# Copyright (C) 2022 NotEnoughUpdates contributors
#
# This file is part of NotEnoughUpdates.
#
# NotEnoughUpdates is free software: you can redistribute it
# and/or modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation, either
# version 3 of the License, or (at your option) any later version.
#
# NotEnoughUpdates is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
#

gradlecommand="./gradlew clean test --no-daemon"

currentcommit=$(git log --pretty=%s -1)
mkdir -p ciwork

case $1 in
feature)
  echo "::group::Gradle build on $currentcommit"
  infer capture -- $gradlecommand
  echo "::endgroup::"

  echo "::group::Infer analyzering on $currentcommit"
  infer analyze
  echo "::endgroup::"

  cp infer-out/report.json ciwork/report-feature.json
  ;;
base)
  echo "::group::Gradle build on $currentcommit"
  infer capture --reactive -- $gradlecommand
  echo "::endgroup::"

  echo "::group::Infer analyzation on $currentcommit"
  infer analyze --reactive
  echo "::endgroup::"
  ;;
report)
  infer reportdiff --report-current ciwork/report-feature.json --report-previous infer-out/report.json
  jq -r '.[] | select(.severity == "ERROR") | ("::error file="+.file +",line=" +(.line|tostring)+"::" + .qualifier)' <infer-out/differential/introduced.json
  jq -r '.[] | select(.severity == "WARNING") | ("::warning file="+.file +",line=" +(.line|tostring)+"::" + .qualifier)' <infer-out/differential/introduced.json
  fixcount=$(jq -r "length" <infer-out/differential/fixed.json)
  unfixcount=$(jq -r "length" <infer-out/differential/introduced.json)
  othercount=$(jq -r "length" <infer-out/differential/preexisting.json)
  echo "This PR fixes $fixcount potential bug(s), introduces $unfixcount potential bug(s). (Total present in feature branch: $((unfixcount + othercount)))" >>$GITHUB_STEP_SUMMARY
  if [[ $unfixcount != 1 ]]; then
    exit 1
  fi
  ;;
esac
