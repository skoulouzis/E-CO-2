set terminal svg 
set output 'simple.1.svg'

unset border
set polar
set angles degrees
set term pngcairo enhanced size 800,800
set output 'graph_eff_10_3_accuracy_spider.png'
set xtics axis
set ytics axis
set grid polar 90
set style line 10 lt 1 lc 0 lw 0.3 # line style for the grid
set grid ls 10
set xrange[-1.5:1.5]
set yrange[-1.5:1.5]
set size square
set lmargin 12
set rmargin 12
set key font ',12'
set title font ',20'

set_label(x, text) = sprintf("set label '%s' at (1.8*cos(%f)), (1.7*sin(%f)) center", text, x, x) #this places a label on the outside
eval set_label(0, "Answer 1")
eval set_label(90, "Answer 2")
eval set_label(180, "Answer 3")
eval set_label(270, "Answer 4")

set linetype 1 lc rgb 'blue' lw 2 pt 7 ps 2 # right, post
set linetype 2 lc rgb 'red' lw 2 pt 7 ps 2 # wrong, post

plot "answers_in_post.csv" using 1:2 title '(IN, R2)' with lp lt 1