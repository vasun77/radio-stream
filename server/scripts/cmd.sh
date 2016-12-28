#!/usr/bin/env bash

DAEMON=radio-stream

stop() {
    echo "Received SIGINT or SIGTERM. Shutting down $DAEMON"
    # Get PID
    pid=$(cat /var/run/$DAEMON/$DAEMON.pid)
    # Set TERM
    kill -SIGTERM "${pid}"
    # Wait for exit
    wait "${pid}"
    # All done.
    echo "Done."
}

echo "Running radio-stream..."
trap stop SIGINT SIGTERM
beet radio &
pid="$!"
mkdir -p /var/run/$DAEMON && echo "${pid}" > /var/run/$DAEMON/$DAEMON.pid

echo "******************************************************************"
echo " Radio Stream server is up and listening on: http://localhost:80"
echo " User: radio"
echo " Pass: $NGINX_PASSWORD"
echo "******************************************************************"
echo ""

wait "${pid}" && exit $?