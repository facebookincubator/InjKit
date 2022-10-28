# Copyright (c) Meta Platforms, Inc. and affiliates.
#
# This source code is licensed under the MIT license found in the
# LICENSE file in the root directory of this source tree.

# This source code is licensed under the MIT license found in the
# LICENSE file in the root directory of this source tree.

#!/bin/bash

LICENSE_HEADER_FILE="license-header.txt"

shopt -s globstar nullglob

tmp_file=$(mktemp)
trap "{ rm -f ""$tmp_file""; }" EXIT

if [[ ! -f "$LICENSE_HEADER_FILE" ]]; then
  echo "No '$LICENSE_HEADER_FILE' file found. Run this script from the root directory"
  exit 1
fi

function convert_header_of {
  source_file="$1"

  cat "$LICENSE_HEADER_FILE" > "$tmp_file"
  grep -v "//.*Copyright" "$source_file" >> "$tmp_file"
  
  cp "$tmp_file" "$source_file"
}

for source_file in **/*.java; do
  echo -n "processing $source_file... "

  copyright_header_count="$(grep -c "Copyright" < "$source_file")"
  mit_in_header_count="$(head -n 3 "$source_file" | grep -c "^//.*MIT")"
  
  if ((copyright_header_count == 1)) && ((mit_in_header_count == 1)); then
    # Nothing to do, we've got the MIT header.
    echo "OK"
    continue
  elif ((copyright_header_count == 1)) && ((mit_in_header_count == 0)); then
    # We've got a non-mit header. Convert.
    echo -n "Converting..."
    convert_header_of "$source_file"
    echo "OK"
  else
    # In a somewhat weird state.
    echo "ERROR: copyright_header_count=$copyright_header_count," \
        "mit_in_header_count=$mit_in_header_count"
  fi
    
done

