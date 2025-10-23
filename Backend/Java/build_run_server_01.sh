#!/bin/sh
#
# process-check.sh
# ㄴ반납/재설치시 검사되는 로직과 동일한 프로세스 검사 스크립트입니다.
# ㄴ본 스크립트는 irteamsu에서만 작동합니다.
# ㄴ스크립트 사용 문의 : https://yobi.navercorp.com/AskSEG/posts
#
HOSTNAME=$(/bin/hostname)
function PROCESS () {
    local process
    local CNT=0
    local DOCKERS
    # 체크할 process
    # docker는 docker ps 로 직접 확인하자
    local TEMP=`grep /bin/bash /etc/passwd | cut -f1 -d: | grep -vE 'root|nbpmon|sysact|idcact|www'`
    local USER=`echo "/"$TEMP"/"|sed 's/ /\/|\//g'`
    local REG1=$USER"|/usr/local|cube|httpd|nginx|owfs|papyrus|hdb|java|python|redis"
    # 예외 처리할 process
    local REG2="box|master|palantir|rotatelogs|(deleted)|mini_agent|saveIOutil|filebeat|ambari-server|ambari-agent|cmf-server|cmf-agent|oagent|xagent|tcollector|cu-node|MymonAgent|mini_agent_watch|mini_status|mini_get_stmt_digest|mmm_agentd|owfs_fuse|owfsmdcache|npot-agent|agent_controller_linux|owfs_io_mon|TriggeredScheduler|SA-linux-64|SR-linux-64|prov2-agent"
    for i in /proc/[[:digit:]]*/exe
    do
        process=$(readlink ${i} 2> /dev/null)
        [ -z "${process}" ] && continue
        ! [[ "${process}" =~ ${REG1} ]] && continue
        CMDLINE=$(echo ${i} | cut -d '/' -f'1,2,3')"/cmdline"
        process_cmd=$(cat ${CMDLINE})
        [[ "${process_cmd}" =~ ${REG2} ]] && continue
        grep -vE "${REG2}" <<< "${process}" >& /dev/null
        if [ $? -eq 0 ]; then
            let "CNT++"
            PID=$(echo ${i} | awk -F'/' '{print $3}')
            echo ${process}
        fi
    done
    DOCKERS=$(docker ps 2> /dev/null | grep -v "CONTAINER ID " | wc -l)
    let "CNT += ${DOCKERS}"
    if (( ${CNT} > 0 )); then
        if (( ${DOCKERS} > 0 )); then
            docker ps --format "table {{.ID}}\t{{.Image}}\t{{.Status}}" | grep -v "CONTAINER ID"
        fi
        process_result="${CNT} process is running. ${bggred}${bold}Reject the Ticket!${normal}"
    else
        process_result="OK"
    fi
}
SUDO=""
[[ "$EUID" -ne 0 ]] && SUDO="sudo"
RESULT=$($SUDO bash -c "$(declare -f PROCESS); PROCESS")
echo "$RESULT"
RESCNT=$(echo "$RESULT" | grep -v "^$" | wc -l)
if [ ${RESCNT} -eq 0 ];then
    echo "$HOSTNAME | PRCS[${RESCNT} process is running.]";
    exit 0
else
    echo "$HOSTNAME | PRCS[${RESCNT} process is running.]";
    exit 1
fi