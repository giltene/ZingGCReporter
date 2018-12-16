ZingGCReporter
===========

ZingGCReporter is a simple java agent that collects and reports GC
activity stats when running on a Zing JVM. It uses the Zing Java
Memory Management MXBeans to collect relevant information and
summarize it in periodic reports that are meant to easily fit into
time-series and other/similar stats logging and monitoring
operational workflows.

Options:
---
`
[-v] [-i intervalLengthMsec] [-t runTimeMsec] [-l logFileName]`


- intervalLengthMsec [-i intervalLengthMsec, default: 10000]: Control the
length of the periodic reporting intervals

- logFile [-l logFileName, default: <unspecified>] specifies the log file to
output to. Outputs to standard output when unspecified

- runTimeMsec [-t runTimeMsec, default: 0]: When run as main (not as an agent)
and runTimeMsec is specified as non-zero, ZingGCReporter will exit
after the configured amount of time

- verbose [-v , default: false]: Causes verbose output e.g. when GC
events are received

Note that options can use either spaces, commas, or semicolons
as delimeters. E.g. "-i 1000 -v", "-i,1000,-v", and "-i;1000;-v" are
all equivalent. This can be useful when e.g. javaagent parameters are
included in the launching command line that are sensitive to the use
of spaces within parameters.


Running:
---
ZingGCReporter will typically be added to existing Java applications as a
javaagent. E.g. a typical command line will be:

```
java ... -javaagent:ZingGCReporter.jar MyApp myAppArgs
```

Or, when parameters need to be set:

```
java ... -javaagent:ZingGCReporter.jar="-i 5000 -l gcstats.log" MyApp myAppArgs
```
For a quick demonstration or test ZingGCReporter can be combined with e.g. 
[HeapFragger](https://github.com/giltene/HeapFragger). For example, the
following command: 


```
$JAVA_HOME/bin/java -Xloggc:ggc.log -Xmx5g -XX:+UseZingMXBeans -javaagent:ZingGCReporter.jar="-i 5000" -jar HeapFragger.jar -a 1024 -s 1024
```

Results in output that looks somewhat like this:

```
Interval[Sat Dec 15 19:05:24 PST 2018, start/length:     0.000/5.000 sec] GC Util% [New/Old]:  0.72%/ 1.57%, Pause [Longest/Total]: 0.000309/0.001109 sec, allocation/promotion: 116.837/0.000 MB/sec, Lowest Oldgen used:    0.000 MB, completed GCs new/old: 1/1, longest completed GC new/old: 0.036/0.078 sec
Interval[Sat Dec 15 19:05:29 PST 2018, start/length:     5.000/5.000 sec] GC Util% [New/Old]: 47.16%/47.87%, Pause [Longest/Total]: 0.000359/0.003104 sec, allocation/promotion: 515.324/34.180 MB/sec, Lowest Oldgen used:    4.000 MB, completed GCs new/old: 2/2, longest completed GC new/old: 1.090/1.095 sec
Interval[Sat Dec 15 19:05:34 PST 2018, start/length:    10.000/5.000 sec] GC Util% [New/Old]: 89.21%/62.39%, Pause [Longest/Total]: 0.003443/0.006932 sec, allocation/promotion: 995.442/231.820 MB/sec, Lowest Oldgen used:  228.000 MB, completed GCs new/old: 7/2, longest completed GC new/old: 1.836/2.155 sec
Interval[Sat Dec 15 19:05:39 PST 2018, start/length:    15.000/5.000 sec] GC Util% [New/Old]: 36.14%/41.09%, Pause [Longest/Total]: 0.003607/0.019989 sec, allocation/promotion: 722.327/12.800 MB/sec, Lowest Oldgen used: 1330.000 MB, completed GCs new/old: 12/1, longest completed GC new/old: 0.292/2.610 sec
Interval[Sat Dec 15 19:05:44 PST 2018, start/length:    20.000/5.000 sec] GC Util% [New/Old]: 88.52%/ 8.82%, Pause [Longest/Total]: 0.003846/0.082377 sec, allocation/promotion: 1060.384/76.796 MB/sec, Lowest Oldgen used: 1384.000 MB, completed GCs new/old: 84/0, longest completed GC new/old: 0.373/0.000 sec
Interval[Sat Dec 15 19:05:49 PST 2018, start/length:    25.000/5.000 sec] GC Util% [New/Old]: 48.24%/44.46%, Pause [Longest/Total]: 0.004049/0.025374 sec, allocation/promotion: 1047.061/30.445 MB/sec, Lowest Oldgen used: 1458.000 MB, completed GCs new/old: 53/1, longest completed GC new/old: 0.071/2.664 sec
Interval[Sat Dec 15 19:05:54 PST 2018, start/length:    30.000/5.000 sec] GC Util% [New/Old]: 24.03%/ 0.00%, Pause [Longest/Total]: 0.003826/0.018877 sec, allocation/promotion: 551.375/12.042 MB/sec, Lowest Oldgen used: 1512.000 MB, completed GCs new/old: 10/0, longest completed GC new/old: 0.241/0.000 sec
Interval[Sat Dec 15 19:05:59 PST 2018, start/length:    35.000/5.000 sec] GC Util% [New/Old]: 58.67%/ 0.00%, Pause [Longest/Total]: 0.000335/0.003969 sec, allocation/promotion: 533.054/5.517 MB/sec, Lowest Oldgen used: 1570.000 MB, completed GCs new/old: 11/0, longest completed GC new/old: 0.381/0.000 sec
Interval[Sat Dec 15 19:06:04 PST 2018, start/length:    40.000/5.000 sec] GC Util% [New/Old]: 71.00%/ 0.00%, Pause [Longest/Total]: 0.003593/0.013950 sec, allocation/promotion: 668.673/6.000 MB/sec, Lowest Oldgen used: 1596.000 MB, completed GCs new/old: 13/0, longest completed GC new/old: 0.286/0.000 sec
Interval[Sat Dec 15 19:06:09 PST 2018, start/length:    45.000/5.000 sec] GC Util% [New/Old]: 79.48%/ 0.00%, Pause [Longest/Total]: 0.003749/0.012302 sec, allocation/promotion: 781.006/9.600 MB/sec, Lowest Oldgen used: 1626.000 MB, completed GCs new/old: 15/0, longest completed GC new/old: 0.297/0.000 sec
Interval[Sat Dec 15 19:06:14 PST 2018, start/length:    50.000/5.000 sec] GC Util% [New/Old]: 76.34%/ 0.00%, Pause [Longest/Total]: 0.000394/0.009713 sec, allocation/promotion: 768.556/6.434 MB/sec, Lowest Oldgen used: 1674.000 MB, completed GCs new/old: 14/0, longest completed GC new/old: 0.302/0.000 sec
Interval[Sat Dec 15 19:06:19 PST 2018, start/length:    55.000/5.000 sec] GC Util% [New/Old]: 73.44%/ 0.00%, Pause [Longest/Total]: 0.003891/0.018045 sec, allocation/promotion: 795.320/13.863 MB/sec, Lowest Oldgen used: 1708.000 MB, completed GCs new/old: 14/0, longest completed GC new/old: 0.282/0.000 sec
Interval[Sat Dec 15 19:06:24 PST 2018, start/length:    60.000/5.000 sec] GC Util% [New/Old]: 64.64%/54.89%, Pause [Longest/Total]: 0.000458/0.006909 sec, allocation/promotion: 860.931/45.304 MB/sec, Lowest Oldgen used: 1382.000 MB, completed GCs new/old: 15/1, longest completed GC new/old: 0.334/2.744 sec
Interval[Sat Dec 15 19:06:29 PST 2018, start/length:    65.000/5.000 sec] GC Util% [New/Old]: 26.53%/ 0.00%, Pause [Longest/Total]: 0.000771/0.005410 sec, allocation/promotion: 866.793/6.400 MB/sec, Lowest Oldgen used: 1402.000 MB, completed GCs new/old: 6/0, longest completed GC new/old: 0.237/0.000 sec
Interval[Sat Dec 15 19:06:34 PST 2018, start/length:    70.000/5.000 sec] GC Util% [New/Old]: 48.38%/ 0.00%, Pause [Longest/Total]: 0.000686/0.009520 sec, allocation/promotion: 585.012/47.600 MB/sec, Lowest Oldgen used: 1494.000 MB, completed GCs new/old: 6/0, longest completed GC new/old: 0.530/0.000 sec
Interval[Sat Dec 15 19:06:39 PST 2018, start/length:    75.000/5.000 sec] GC Util% [New/Old]: 50.75%/ 0.00%, Pause [Longest/Total]: 0.003962/0.012856 sec, allocation/promotion: 613.207/4.400 MB/sec, Lowest Oldgen used: 1676.000 MB, completed GCs new/old: 6/0, longest completed GC new/old: 0.517/0.000 sec
Interval[Sat Dec 15 19:06:44 PST 2018, start/length:    80.000/5.000 sec] GC Util% [New/Old]: 46.44%/ 0.00%, Pause [Longest/Total]: 0.003547/0.011783 sec, allocation/promotion: 779.053/3.600 MB/sec, Lowest Oldgen used: 1692.000 MB, completed GCs new/old: 7/0, longest completed GC new/old: 0.395/0.000 sec
Interval[Sat Dec 15 19:06:49 PST 2018, start/length:    85.000/5.000 sec] GC Util% [New/Old]: 41.93%/ 0.00%, Pause [Longest/Total]: 0.000874/0.006657 sec, allocation/promotion: 791.709/5.200 MB/sec, Lowest Oldgen used: 1706.000 MB, completed GCs new/old: 7/0, longest completed GC new/old: 0.332/0.000 sec
Interval[Sat Dec 15 19:06:54 PST 2018, start/length:    90.000/5.000 sec] GC Util% [New/Old]: 54.12%/43.51%, Pause [Longest/Total]: 0.003747/0.021057 sec, allocation/promotion: 789.994/50.000 MB/sec, Lowest Oldgen used: 1734.000 MB, completed GCs new/old: 8/0, longest completed GC new/old: 0.377/0.000 sec
Interval[Sat Dec 15 19:06:59 PST 2018, start/length:    95.000/5.000 sec] GC Util% [New/Old]: 31.79%/10.01%, Pause [Longest/Total]: 0.003692/0.008596 sec, allocation/promotion: 838.978/50.400 MB/sec, Lowest Oldgen used: 1496.000 MB, completed GCs new/old: 6/1, longest completed GC new/old: 0.413/2.676 sec
Interval[Sat Dec 15 19:07:04 PST 2018, start/length:   100.000/5.000 sec] GC Util% [New/Old]: 28.60%/ 0.00%, Pause [Longest/Total]: 0.000635/0.004978 sec, allocation/promotion: 876.662/6.400 MB/sec, Lowest Oldgen used: 1570.000 MB, completed GCs new/old: 6/0, longest completed GC new/old: 0.247/0.000 sec
Interval[Sat Dec 15 19:07:09 PST 2018, start/length:   105.000/5.000 sec] GC Util% [New/Old]: 23.31%/ 0.00%, Pause [Longest/Total]: 0.000659/0.004968 sec, allocation/promotion: 859.374/5.200 MB/sec, Lowest Oldgen used: 1604.000 MB, completed GCs new/old: 5/0, longest completed GC new/old: 0.247/0.000 sec
Interval[Sat Dec 15 19:07:14 PST 2018, start/length:   110.000/5.000 sec] GC Util% [New/Old]: 22.59%/ 0.00%, Pause [Longest/Total]: 0.000767/0.008617 sec, allocation/promotion: 890.276/4.800 MB/sec, Lowest Oldgen used: 1624.000 MB, completed GCs new/old: 5/0, longest completed GC new/old: 0.232/0.000 sec
Interval[Sat Dec 15 19:07:19 PST 2018, start/length:   115.000/5.000 sec] GC Util% [New/Old]: 39.37%/17.09%, Pause [Longest/Total]: 0.000778/0.008024 sec, allocation/promotion: 823.312/95.061 MB/sec, Lowest Oldgen used: 1876.000 MB, completed GCs new/old: 7/0, longest completed GC new/old: 0.336/0.000 sec
Interval[Sat Dec 15 19:07:24 PST 2018, start/length:   120.000/5.000 sec] GC Util% [New/Old]: 31.36%/38.98%, Pause [Longest/Total]: 0.003750/0.012843 sec, allocation/promotion: 880.923/50.813 MB/sec, Lowest Oldgen used: 1422.000 MB, completed GCs new/old: 7/1, longest completed GC new/old: 0.321/2.803 sec
Interval[Sat Dec 15 19:07:29 PST 2018, start/length:   125.000/5.000 sec] GC Util% [New/Old]: 24.58%/ 0.00%, Pause [Longest/Total]: 0.000696/0.006786 sec, allocation/promotion: 714.026/4.126 MB/sec, Lowest Oldgen used: 1658.000 MB, completed GCs new/old: 5/0, longest completed GC new/old: 0.447/0.000 sec
Interval[Sat Dec 15 19:07:34 PST 2018, start/length:   130.000/5.000 sec] GC Util% [New/Old]: 35.85%/ 0.00%, Pause [Longest/Total]: 0.003777/0.007941 sec, allocation/promotion: 675.181/2.000 MB/sec, Lowest Oldgen used: 1674.000 MB, completed GCs new/old: 4/0, longest completed GC new/old: 0.512/0.000 sec
Interval[Sat Dec 15 19:07:39 PST 2018, start/length:   135.000/5.000 sec] GC Util% [New/Old]: 37.96%/ 0.00%, Pause [Longest/Total]: 0.003436/0.009061 sec, allocation/promotion: 703.241/5.098 MB/sec, Lowest Oldgen used: 1692.000 MB, completed GCs new/old: 4/0, longest completed GC new/old: 0.415/0.000 sec
Interval[Sat Dec 15 19:07:44 PST 2018, start/length:   140.000/5.000 sec] GC Util% [New/Old]: 33.12%/ 0.00%, Pause [Longest/Total]: 0.000632/0.006553 sec, allocation/promotion: 831.013/2.502 MB/sec, Lowest Oldgen used: 1710.000 MB, completed GCs new/old: 6/0, longest completed GC new/old: 0.347/0.000 sec
Interval[Sat Dec 15 19:07:49 PST 2018, start/length:   145.000/5.000 sec] GC Util% [New/Old]: 26.24%/ 0.00%, Pause [Longest/Total]: 0.003495/0.006993 sec, allocation/promotion: 817.620/3.468 MB/sec, Lowest Oldgen used: 1728.000 MB, completed GCs new/old: 4/0, longest completed GC new/old: 0.299/0.000 sec
Interval[Sat Dec 15 19:07:54 PST 2018, start/length:   150.000/5.000 sec] GC Util% [New/Old]: 29.98%/ 0.00%, Pause [Longest/Total]: 0.000660/0.009139 sec, allocation/promotion: 887.938/5.332 MB/sec, Lowest Oldgen used: 1742.000 MB, completed GCs new/old: 6/0, longest completed GC new/old: 0.294/0.000 sec
Interval[Sat Dec 15 19:07:59 PST 2018, start/length:   155.000/5.000 sec] GC Util% [New/Old]: 34.92%/36.51%, Pause [Longest/Total]: 0.003673/0.017599 sec, allocation/promotion: 848.998/50.400 MB/sec, Lowest Oldgen used: 1772.000 MB, completed GCs new/old: 6/0, longest completed GC new/old: 0.424/0.000 sec
Interval[Sat Dec 15 19:08:04 PST 2018, start/length:   160.000/5.000 sec] GC Util% [New/Old]: 23.81%/11.11%, Pause [Longest/Total]: 0.003729/0.017833 sec, allocation/promotion: 893.032/74.400 MB/sec, Lowest Oldgen used: 1326.000 MB, completed GCs new/old: 4/1, longest completed GC new/old: 0.411/2.381 sec
Interval[Sat Dec 15 19:08:09 PST 2018, start/length:   165.000/5.000 sec] GC Util% [New/Old]: 19.03%/ 0.00%, Pause [Longest/Total]: 0.003445/0.009601 sec, allocation/promotion: 880.868/3.600 MB/sec, Lowest Oldgen used: 1706.000 MB, completed GCs new/old: 4/0, longest completed GC new/old: 0.249/0.000 sec
Interval[Sat Dec 15 19:08:14 PST 2018, start/length:   170.000/5.000 sec] GC Util% [New/Old]: 22.01%/ 0.00%, Pause [Longest/Total]: 0.003712/0.012038 sec, allocation/promotion: 886.008/4.065 MB/sec, Lowest Oldgen used: 1724.000 MB, completed GCs new/old: 4/0, longest completed GC new/old: 0.234/0.000 sec
Interval[Sat Dec 15 19:08:19 PST 2018, start/length:   175.000/5.000 sec] GC Util% [New/Old]: 19.31%/ 0.00%, Pause [Longest/Total]: 0.000703/0.002973 sec, allocation/promotion: 911.102/3.535 MB/sec, Lowest Oldgen used: 1738.000 MB, completed GCs new/old: 5/0, longest completed GC new/old: 0.237/0.000 sec
```

Building:
---------

Note that to build ZingGCReporter outside of a Zing environment, the maven setup
expects to find a copy of `ZingJMM.jar` in \<basedir>/lib . You can generally
find a copy of `ZingJMM.jar` (which includes the Zing JMXBean APIs) at 
`$JAVA_HOME/etc/extensions/mxbeans/agents/ZingJMM.jar` and copy it into your
\<basedir>/lib. Then just run:
```
mvn clean install
```

And you should find a feshly built ZingGCReporter.jar in your base directory.
