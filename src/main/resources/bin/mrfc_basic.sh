#!/bin/bash
HOME=/home/urmqgen

PATH_TO_JAR=$HOME/urmqgen/lib/urmqgen-jar-with-dependencies.jar
JAVA_OPT="-Dlogback.configurationFile=$HOME/urmqgen/config/logback.xml"

SCENARIO_PATH=$HOME/urmqgen/scenario
SCENARIO_FILE=$SCENARIO_PATH/mrfc_basic.xml

KEYWORD=dialogId
TYPE=proto

# protoBuf Configuration
PROTO_FILE=$HOME/urmqgen/proto/mrfp-external-msg-1.0.3.jar
PKG_BASE=com.uangel.protobuf.mrfp.external

# RabbitMQ Configuration
LOCAL_Q=MRFC
HOST=192.168.5.224
PORT=5672
USER=mrfp
PW=mBDdk2WFOtCNY9gRTAIOUSMRuhIHO/Cq

TARGET_Q=EX_MFIF
TARGET_HOST=192.168.5.224
TARGET_PORT=5672
TARGET_USER=mrfp
TARGET_PW=mBDdk2WFOtCNY9gRTAIOUSMRuhIHO/Cq

RMQ_THREAD_SIZE=5
RMQ_QUEUE_SIZE=10

THREAD_SIZE=5
MAX_SESSION=1


/usr/bin/java $JAVA_OPT $DEBUG -classpath $PATH_TO_JAR com.uangel.URmqGenMain -sf $SCENARIO_FILE -k $KEYWORD -t $TYPE-pf $PROTO_FILE -pkg $PKG_BASE -rl $LOCAL_Q -rh $HOST -ru $USER -rp $PORT -rpw $PW-rt $TARGET_Q -rth $TARGET_HOST -rtu $TARGET_USER -rtp $TARGET_PORT -rtpw $TARGET_PW-rts $RMQ_THREAD_SIZE -rqs $RMQ_QUEUE_SIZE -ts $THREAD_SIZE -m $MAX_SESSION 2>> stdout