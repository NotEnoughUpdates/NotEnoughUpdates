#
# Copyright (C) 2024 NotEnoughUpdates contributors
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

last_tag="$(git log --pretty='%H %D'|grep -oE 'tag: [^ ]+'|sed -E 's/tag: ([^ ,]+),?/\1/'|head -2|tail -1)"
echo "Generating notes from $last_tag"
TARGET_NAME="build/libs/NotEnoughUpdates-$GITHUB_REF_NAME.jar"
mv build/libs/*.jar "$TARGET_NAME"

read -r -d '' extra_notes <<EOF
Modrinth download: TBD

Do **NOT** trust any mod just because they publish a checksum associated with it. These check sums are meant to verify only that two files are identical. They are not a certificate of origin, or a guarantee for the author of these files.

sha256sum: \`$(sha256sum "$TARGET_NAME"|cut -f 1 -d ' '| tr -d '\n')\`
md5sum: \`$(md5sum "$TARGET_NAME"|cut -f 1 -d ' '| tr -d '\n')\`

EOF

preReleaseParam=""
if echo "$GITHUB_REF_NAME" | grep -E '.*\.0'>/dev/null; then
  preReleaseParam="--prerelease"
fi

gh release create -t "NotEnoughUpdates $GITHUB_REF_NAME" --verify-tag "$GITHUB_REF_NAME" --generate-notes \
  --draft --notes-start-tag "$last_tag" $preReleaseParam \
  --notes "$extra_notes" "$TARGET_NAME"

