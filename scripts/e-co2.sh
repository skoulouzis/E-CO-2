#!/bin/bash
# e-co2
#

case $1 in
    start)
        /bin/bash /usr/local/bin/e-co2-start.sh
    ;;
    stop)
        /bin/bash /usr/local/bin/e-co2-stop.sh
    ;;
    restart)
        /bin/bash /usr/local/bin/e-co2-stop.sh
        /bin/bash /usr/local/bin/e-co2-start.sh
    ;;
esac
exit 0