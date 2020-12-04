#!/bin/sh

# $1: dir
# $2: filter file suffix, eg: c cpp h java
# $3: target str
find_iperf() {
  #echo "in $1"
  files=$(ls "$1")
  for f in ${files}
  do
    if [[ -d "$1/$f" ]]; then
      find_iperf "$1/$f" "$2" "$3"
    elif [[ "${f##*.}" = $2 ]]; then
      strs=$(cat "$1/$f" | grep -nn "$3")
      if [ $? -eq 0 ]; then
        echo "in $1/$f---->$3"
        echo "$strs" | while read line;
        do
          echo "$line"
        done
      fi
    fi
  done
}

cur_dir=$(pwd)
echo "current dir:$cur_dir"
echo "target file type: .$1"
echo "target string: $2"
echo "----------------------------------------------------------------"
# $1: filter file suffix, eg: c cpp h java
# $2: target str
find_iperf "$cur_dir" "$1" "$2"