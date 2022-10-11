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


output="$(dirname $(dirname $(readlink -f "$0")))/src/main/resources/moulberry.key"

echo processing rsa input key from $1, and outputting to $output

tempfile="$(mktemp)"
ssh-keygen -f "$1" -e -m pkcs8 > "$tempfile"
openssl rsa -pubin -in "$tempfile" -outform der > $output

echo saved x509 public key at $output

