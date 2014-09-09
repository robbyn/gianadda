GIANADDA_PATH=$(dirname "$0")
java "-Dresource-base=$GIANADDA_PATH" -jar "$GIANADDA_PATH/gianadda.jar" $*
