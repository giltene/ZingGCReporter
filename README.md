ZingGCReporter
===========

ZingGCReporter collects and reports GC activity stats when running
on a ZingJ JVM. It uses the Zing Java Memory Management MXBeans to
collect relevant information and summerize it in periodic reports.

Options:

- intervalLengthMsec [-i intervalLengthMsec, default: 10000]: Controsl the length of
the periodic reporting intervals

- logFile [-l logFileName, default: <unspecified>] specifies the log file to
output to. Outsputs to standard output when unspecified

- runTimeMsec [-t runTimeMsec, default: 0]: When run as main (not as an agent)
and runTimeMsec is specified as non-zero, ZingGCReporter will exit
after the configured amount of time

- versbose [-v , default: false]: Causes verbose output e.g. when GC events are received

ZingGCReporter will typically be added to existing Java applications as a
javaagent. E.g. a typical command line will be:

```
java ... -javaagent:ZingGCReporter.jar MyApp myAppArgs
```

Or, when parameters need to be set:

```
java ... -javaagent:ZingGCReporter.jar="-i 5000" MyApp myAppArgs
```
For a quick demosntration or test ZingGCReporter can be combined with e.g. 
[HeapFragger](https://github.com/giltene/HeapFragger). For example, the
following command: 


```
$JAVA_HOME/bin/java -Xloggc:ggc.log -Xmx5g -XX:+UseZingMXBeans -javaagent:ZingGCReporter.jar="-i 5000" -jar Hearagger.jar -a 1024 -s 1024
```

Results in output that looks somewhat like this:

```
Interval[Sun Dec 09 14:14:41 PST 2018, start/length:     0.000/5.000 sec] GC Util% [New/Old]:  1.20%/ 1.48%, longest Pause: 0.000344 sec, allocation/promotion: 191.706/0.000 MB/sec, completed GCs new/old: 1/1, longest completed GC new/old: 0.060/0.074 sec
Interval[Sun Dec 09 14:14:46 PST 2018, start/length:     5.000/5.000 sec] GC Util% [New/Old]: 48.98%/49.07%, longest Pause: 0.000284 sec, allocation/promotion: 470.071/34.362 MB/sec, completed GCs new/old: 2/2, longest completed GC new/old: 1.052/1.053 sec
Interval[Sun Dec 09 14:14:51 PST 2018, start/length:    10.000/5.000 sec] GC Util% [New/Old]: 92.35%/59.22%, longest Pause: 0.003758 sec, allocation/promotion: 999.283/232.038 MB/sec, completed GCs new/old: 8/2, longest completed GC new/old: 1.916/1.992 sec
Interval[Sun Dec 09 14:14:56 PST 2018, start/length:    15.000/5.000 sec] GC Util% [New/Old]: 22.54%/40.95%, longest Pause: 0.003380 sec, allocation/promotion: 1041.482/16.000 MB/sec, completed GCs new/old: 23/1, longest completed GC new/old: 0.069/2.577 sec
Interval[Sun Dec 09 14:15:01 PST 2018, start/length:    20.000/5.000 sec] GC Util% [New/Old]: 23.92%/ 0.00%, longest Pause: 0.004002 sec, allocation/promotion: 1037.749/19.200 MB/sec, completed GCs new/old: 25/0, longest completed GC new/old: 0.055/0.000 sec
Interval[Sun Dec 09 14:15:06 PST 2018, start/length:    25.000/5.000 sec] GC Util% [New/Old]: 31.10%/ 0.00%, longest Pause: 0.001441 sec, allocation/promotion: 1040.036/18.800 MB/sec, completed GCs new/old: 32/0, longest completed GC new/old: 0.071/0.000 sec
Interval[Sun Dec 09 14:15:11 PST 2018, start/length:    30.000/5.000 sec] GC Util% [New/Old]: 50.21%/56.14%, longest Pause: 0.000426 sec, allocation/promotion: 616.530/26.148 MB/sec, completed GCs new/old: 13/0, longest completed GC new/old: 0.339/0.000 sec
Interval[Sun Dec 09 14:15:16 PST 2018, start/length:    35.000/5.000 sec] GC Util% [New/Old]: 22.30%/15.14%, longest Pause: 0.003654 sec, allocation/promotion: 1038.055/43.852 MB/sec, completed GCs new/old: 17/1, longest completed GC new/old: 0.371/3.564 sec
Interval[Sun Dec 09 14:15:21 PST 2018, start/length:    40.000/5.000 sec] GC Util% [New/Old]: 12.70%/ 0.00%, longest Pause: 0.003556 sec, allocation/promotion: 1036.153/16.400 MB/sec, completed GCs new/old: 13/0, longest completed GC new/old: 0.055/0.000 sec
Interval[Sun Dec 09 14:15:26 PST 2018, start/length:    45.000/5.000 sec] GC Util% [New/Old]: 12.44%/ 0.00%, longest Pause: 0.003516 sec, allocation/promotion: 1037.418/16.400 MB/sec, completed GCs new/old: 13/0, longest completed GC new/old: 0.054/0.000 sec
Interval[Sun Dec 09 14:15:31 PST 2018, start/length:    50.000/5.000 sec] GC Util% [New/Old]: 11.11%/ 0.00%, longest Pause: 0.003542 sec, allocation/promotion: 880.282/13.200 MB/sec, completed GCs new/old: 12/0, longest completed GC new/old: 0.050/0.000 sec
Interval[Sun Dec 09 14:15:36 PST 2018, start/length:    55.000/5.000 sec] GC Util% [New/Old]: 53.52%/57.42%, longest Pause: 0.003742 sec, allocation/promotion: 948.003/58.800 MB/sec, completed GCs new/old: 20/1, longest completed GC new/old: 0.261/2.871 sec
Interval[Sun Dec 09 14:15:41 PST 2018, start/length:    60.000/5.000 sec] GC Util% [New/Old]:  6.96%/ 0.00%, longest Pause: 0.000455 sec, allocation/promotion: 1030.817/14.800 MB/sec, completed GCs new/old: 7/0, longest completed GC new/old: 0.054/0.000 sec
Interval[Sun Dec 09 14:15:46 PST 2018, start/length:    65.000/5.000 sec] GC Util% [New/Old]: 23.83%/ 0.00%, longest Pause: 0.003576 sec, allocation/promotion: 853.630/58.436 MB/sec, completed GCs new/old: 8/0, longest completed GC new/old: 0.252/0.000 sec
Interval[Sun Dec 09 14:15:51 PST 2018, start/length:    70.000/5.000 sec] GC Util% [New/Old]: 10.56%/ 0.00%, longest Pause: 0.000538 sec, allocation/promotion: 1033.405/15.964 MB/sec, completed GCs new/old: 11/0, longest completed GC new/old: 0.056/0.000 sec
Interval[Sun Dec 09 14:15:56 PST 2018, start/length:    75.000/5.000 sec] GC Util% [New/Old]: 12.46%/ 0.00%, longest Pause: 0.003532 sec, allocation/promotion: 1032.286/18.400 MB/sec, completed GCs new/old: 12/0, longest completed GC new/old: 0.055/0.000 sec
Interval[Sun Dec 09 14:16:01 PST 2018, start/length:    80.000/5.000 sec] GC Util% [New/Old]: 12.26%/ 0.00%, longest Pause: 0.004026 sec, allocation/promotion: 1033.195/12.800 MB/sec, completed GCs new/old: 12/0, longest completed GC new/old: 0.057/0.000 sec
Interval[Sun Dec 09 14:16:06 PST 2018, start/length:    85.000/5.000 sec] GC Util% [New/Old]: 12.49%/43.02%, longest Pause: 0.003609 sec, allocation/promotion: 1032.195/16.800 MB/sec, completed GCs new/old: 13/0, longest completed GC new/old: 0.059/0.000 sec
Interval[Sun Dec 09 14:16:11 PST 2018, start/length:    90.000/5.000 sec] GC Util% [New/Old]:  7.31%/12.01%, longest Pause: 0.003806 sec, allocation/promotion: 1029.822/11.600 MB/sec, completed GCs new/old: 7/1, longest completed GC new/old: 0.059/2.751 sec
Interval[Sun Dec 09 14:16:16 PST 2018, start/length:    95.000/5.000 sec] GC Util% [New/Old]: 19.72%/ 0.00%, longest Pause: 0.003723 sec, allocation/promotion: 875.956/59.180 MB/sec, completed GCs new/old: 6/0, longest completed GC new/old: 0.319/0.000 sec
Interval[Sun Dec 09 14:16:21 PST 2018, start/length:   100.000/5.000 sec] GC Util% [New/Old]:  8.47%/ 0.00%, longest Pause: 0.000915 sec, allocation/promotion: 1034.240/13.620 MB/sec, completed GCs new/old: 9/0, longest completed GC new/old: 0.051/0.000 sec
Interval[Sun Dec 09 14:16:26 PST 2018, start/length:   105.000/5.000 sec] GC Util% [New/Old]: 18.31%/ 0.00%, longest Pause: 0.003750 sec, allocation/promotion: 917.867/15.600 MB/sec, completed GCs new/old: 10/0, longest completed GC new/old: 0.167/0.000 sec
Interval[Sun Dec 09 14:16:31 PST 2018, start/length:   110.000/5.000 sec] GC Util% [New/Old]: 26.78%/68.54%, longest Pause: 0.000691 sec, allocation/promotion: 1039.026/56.800 MB/sec, completed GCs new/old: 16/1, longest completed GC new/old: 0.308/3.427 sec
Interval[Sun Dec 09 14:16:36 PST 2018, start/length:   115.000/5.000 sec] GC Util% [New/Old]:  4.62%/ 0.00%, longest Pause: 0.000695 sec, allocation/promotion: 1033.478/12.000 MB/sec, completed GCs new/old: 4/0, longest completed GC new/old: 0.065/0.000 sec
Interval[Sun Dec 09 14:16:41 PST 2018, start/length:   120.000/5.000 sec] GC Util% [New/Old]:  6.65%/ 0.00%, longest Pause: 0.000642 sec, allocation/promotion: 908.235/12.400 MB/sec, completed GCs new/old: 4/0, longest completed GC new/old: 0.162/0.000 sec
Interval[Sun Dec 09 14:16:46 PST 2018, start/length:   125.000/5.000 sec] GC Util% [New/Old]: 12.76%/ 0.00%, longest Pause: 0.003811 sec, allocation/promotion: 1033.432/62.800 MB/sec, completed GCs new/old: 6/0, longest completed GC new/old: 0.259/0.000 sec
Interval[Sun Dec 09 14:16:51 PST 2018, start/length:   130.000/5.000 sec] GC Util% [New/Old]:  8.99%/ 0.00%, longest Pause: 0.000658 sec, allocation/promotion: 946.822/12.800 MB/sec, completed GCs new/old: 5/0, longest completed GC new/old: 0.151/0.000 sec
Interval[Sun Dec 09 14:16:56 PST 2018, start/length:   135.000/5.000 sec] GC Util% [New/Old]: 11.52%/50.34%, longest Pause: 0.000407 sec, allocation/promotion: 1033.056/59.600 MB/sec, completed GCs new/old: 7/1, longest completed GC new/old: 0.204/2.517 sec
Interval[Sun Dec 09 14:17:01 PST 2018, start/length:   140.000/5.000 sec] GC Util% [New/Old]: 10.09%/ 0.00%, longest Pause: 0.000550 sec, allocation/promotion: 944.005/59.600 MB/sec, completed GCs new/old: 4/0, longest completed GC new/old: 0.247/0.000 sec
Interval[Sun Dec 09 14:17:06 PST 2018, start/length:   145.000/5.000 sec] GC Util% [New/Old]:  4.39%/ 0.00%, longest Pause: 0.000561 sec, allocation/promotion: 992.184/12.000 MB/sec, completed GCs new/old: 4/0, longest completed GC new/old: 0.058/0.000 sec
Interval[Sun Dec 09 14:17:11 PST 2018, start/length:   150.000/5.000 sec] GC Util% [New/Old]: 12.92%/ 0.00%, longest Pause: 0.000467 sec, allocation/promotion: 1005.941/58.800 MB/sec, completed GCs new/old: 6/0, longest completed GC new/old: 0.218/0.000 sec
Interval[Sun Dec 09 14:17:16 PST 2018, start/length:   155.000/5.000 sec] GC Util% [New/Old]:  5.26%/50.29%, longest Pause: 0.000544 sec, allocation/promotion: 1027.791/12.400 MB/sec, completed GCs new/old: 5/1, longest completed GC new/old: 0.058/2.514 sec
Interval[Sun Dec 09 14:17:21 PST 2018, start/length:   160.000/5.000 sec] GC Util% [New/Old]:  9.64%/ 0.00%, longest Pause: 0.000412 sec, allocation/promotion: 955.541/59.600 MB/sec, completed GCs new/old: 4/0, longest completed GC new/old: 0.224/0.000 sec
Interval[Sun Dec 09 14:17:26 PST 2018, start/length:   165.000/5.000 sec] GC Util% [New/Old]:  4.91%/ 0.00%, longest Pause: 0.003780 sec, allocation/promotion: 1034.160/13.200 MB/sec, completed GCs new/old: 4/0, longest completed GC new/old: 0.064/0.000 sec
Interval[Sun Dec 09 14:17:31 PST 2018, start/length:   170.000/5.000 sec] GC Util% [New/Old]:  6.01%/ 0.00%, longest Pause: 0.000640 sec, allocation/promotion: 971.691/11.600 MB/sec, completed GCs new/old: 4/0, longest completed GC new/old: 0.130/0.000 sec
Interval[Sun Dec 09 14:17:36 PST 2018, start/length:   175.000/5.000 sec] GC Util% [New/Old]: 11.17%/53.78%, longest Pause: 0.000512 sec, allocation/promotion: 1034.964/61.200 MB/sec, completed GCs new/old: 6/1, longest completed GC new/old: 0.224/2.689 sec
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
