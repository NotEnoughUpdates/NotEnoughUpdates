#!/bin/bash
#
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

set -x

COLOR_SUCCESS=8040199
COLOR_WORKING=7472302
COLOR_ERROR=14960972
case "$STATUS" in
  WORKING)
    color="$COLOR_WORKING"
    status_message="Build started."
    ;;
  FAILURE)
    color="$COLOR_ERROR"
    status_message="Build failed."
    ;;
  SUCCESS)
    color="$COLOR_SUCCESS"
    status_message="Build succeeded."
    to_upload=$(echo build/libs/*-dep.jar)
    upload_name=NotEnoughUpdates-beta-dep.jar
    ;;
esac

author_name="$ACTOR"
commit_hash=$(git log -1 --pretty=format:'%h')
commit_subject=$(git log -1 --pretty=format:'%s')
commit_body=$(git log -1 --pretty=format:'%b')
commit_date=$(git log -1 --pretty=format:'%ct')

author_avatar="https://github.com/$author_name.png"

#language=json
read -r -d '' structure <<-"EOF"
{
  "content": $status,
  "username": $username,
  "avatar_url": $avatar_url,
  "embeds": [
    {
      "color": $color,
      "url": $url,
      "title": $subject,
      "description": $body,
      "footer": {
        "text": $ref
      }
    }
  ],
  "allowed_mentions": {
    "parse": []
  }
}
EOF
json=$(jq -n \
  --arg body "$commit_body" \
  --arg status "$status_message" \
  --arg subject "$commit_subject" \
  --arg username "$author_name" \
  --arg avatar_url "$author_avatar" \
  --argjson color "$color" \
  --arg url "$GIT_URL" \
  --arg ref "$REF_NAME" \
  "$structure")

function make_request() {
  if [ "$to_upload" != "" ]; then
    upload_arg="-F"
  fi
  curl -X $1 -H "Content-Type: multipart/form-data" -F "payload_json=$json" "$upload_arg" "$upload_name=@$to_upload" "$WEBHOOK_URL$2?wait=true"
}

echo "Should replace message with id: <$MESSAGE_ID>"
if [ "$MESSAGE_ID" != "" ]; then
  discord_output=$(make_request PATCH "/messages/$MESSAGE_ID")
  RESULT=$?
else
  discord_output=$(make_request POST)
  RESULT=$?
fi

if [ $RESULT != 0 ]; then
  echo "$discord_output"
  exit 1
fi
echo "Message sent to discord."
echo "$discord_output" | jq .
id_string=$(echo "$discord_output" | jq .id)
echo "MESSAGE_ID=${id_string//\"/}" >> $GITHUB_OUTPUT
