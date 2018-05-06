
review_list_path=$1
cmd=$2
filename=$3
note_name=$4

./gradlew run -PappArgs="['$review_list_path', '$cmd', '$filename', '$note_name']"
