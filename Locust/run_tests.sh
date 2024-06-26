#!/bin/bash
# add [[ -e ~/.profile ]] && emulate sh -c 'source ~/.profile' to .zshrc
pip3 install -r requirements.txt

run_locust() {
locust -f locust_example3.py --headless --only-summary --users "$1" --spawn-rate 0.1 -t "$2" -H http://192.168.50.241:8080 --csv-full-history --csv report_"$1"_"$2".csv
}

#generate_report() {
#  python reporter.py report_"$1"_"$2".csv_stats_history.csv report_"$1"_"$2".html
#}

users=(5 50 300)
durations=("30s" "1m" "5m")

#for i in {0..2}
#do
#  run_locust "${users[$i]}" "${durations[$i]}" && sleep 10
#done

run_locust "20" "5m"


#for i in {0..2}
#do
#  generate_report "${users[$i]}" "${durations[$i]}"
#done