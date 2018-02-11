#!/bin/bash

clean_file_not_in_svn() {
	file_arr=(`svn status $1`)
	len=${#file_arr[*]}

	j=0

	for ((i=0; i<$len; i++)); do
		if [[ "${file_arr[$i]}" = "?" ]]; then
			i=`expr $i + 1`
			file_to_clean_arr[$j]=${file_arr[$i]}
			j=`expr $i + 1`
		fi
	done

	for item in ${file_to_clean_arr[*]} ; do
		echo "rm -rf $item"
		rm -rf $item
	done
}


##################### Main Loop ############################
if [[ -d "$1" ]];then
	clean_file_not_in_svn $1
fi
