/*
 Copyright (c) 2018 Azul Systems
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 THE POSSIBILITY OF SUCH DAMAGE.
 */

import javax.management.*;
import javax.management.openmbean.CompositeData;

import com.azul.zing.management.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ZingGCReporter extends Thread {

    PrintStream log;

    class ZingGCReporterConfiguration {

        public boolean verbose = false;
        public String logFileName = null;
        public long runTimeMsec = 0;
        public long intervalLengthMsec = 10000;
        public double intervalLengthSec = 10.0;

        public void parseArgs(String[] args) {
            for (int i = 0; i < args.length; ++i) {
                if (args[i].equals("-v")) {
                    verbose = true;
                } else if (args[i].equals("-i")) {
                    intervalLengthMsec = Long.parseLong(args[++i]);
                } else if (args[i].equals("-t")) {
                    runTimeMsec = Long.parseLong(args[++i]);
                } else if (args[i].equals("-l")) {
                    logFileName = args[++i];
                } else {
                    System.err.println("Usage: java ZingGCReporter [-v] " +
                            "[-i intervalLengthMsec] [-t runTimeMsec] [-l logFileName]");
                    System.exit(1);
                }
            }
            intervalLengthSec = Math.max(0.01, intervalLengthMsec / 1000.0);
        }
    }

    ZingGCReporterConfiguration config = new ZingGCReporterConfiguration();

    private ZingGCReporter(String[] args) throws FileNotFoundException {
        this.setName("ZingGCReporter");
        config.parseArgs(args);
        if (config.logFileName != null) {
            log = new PrintStream(new FileOutputStream(config.logFileName), false);
        } else {
            log = System.out;
        }
        this.setDaemon(true);
    }


    static final BlockingQueue<GCDetails> gcDetailsNotifications = new LinkedBlockingQueue<>();

    static final long MB = 1024 * 1024;
    static final long PAGE_SIZE = 2 * MB;
    static final long vmStartTimeMillis = ManagementFactory.getTimeMXBean().getUnixTimeOfJVMStartMillis();

    static final LinkedList<IntervalData> pendingIntervals = new LinkedList<>();

    static double latestReportedIntervalEndTime = 0.0;

    static boolean pendingOldgen = false;

    public static class IntervalData {
        double startTime;   // Seconds since JVM start
        double endTime;     // Seconds since JVM start

        double newgenGcDuration;    // In Seconds
        double oldgenGcDuration;    // In seconds

        double longestPause;        // In seconds

        long newgenCompletedCount;
        long oldgenCompletedCount;

        double longestCompletedNewgenDuration;  // In seconds
        double longestCompletedOldgenDuration;  // In seconds

        long allocated; // In bytes
        long promoted;  // In buytes

        IntervalData(final double startTime, final double endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    void enqueueIntervalsUpToTime(final double endTime) {
        double latestPendingIntervalEndTime =
                (pendingIntervals.peekLast() != null) ?
                        pendingIntervals.peekLast().endTime :
                        latestReportedIntervalEndTime;

        while(latestPendingIntervalEndTime < endTime) {
            pendingIntervals.add(
                    new IntervalData(latestPendingIntervalEndTime,
                            latestPendingIntervalEndTime + config.intervalLengthSec)
            );
            latestPendingIntervalEndTime += config.intervalLengthSec;
        }
        if (config.verbose) log.format("enqueueIntervals: %d in queue.\n", pendingIntervals.size());
    }

    void outputCompletedIntervalUpToTime(final double endTime) {
        if (config.verbose) log.format("outputIntervals(endTime = %4.3f): %d in queue. pendingOldgen = %b\n",
                endTime, pendingIntervals.size(), pendingOldgen);
        if (pendingOldgen)
            return; // In-flight Oldgen can still add information for pending intervals
        if (config.verbose && (pendingIntervals.peek() != null)) {
            log.format("  head endTime = %4.3f\n", pendingIntervals.peek().endTime);
        }
        while ((pendingIntervals.peek() != null) && pendingIntervals.peek().endTime < endTime) {
            // All information about the interval at the head of the queue has been attributed to it.
            IntervalData interval = pendingIntervals.remove();
            log.format("Interval[%s, start/length: %9.3f/%4.3f sec] ",
                    new Date((long)(interval.startTime * 1000) + vmStartTimeMillis),
                    interval.startTime,
                    config.intervalLengthSec);
            log.format("GC Util%% [New/Old]: %5.2f%%/%5.2f%%, ",
                    (100 * interval.newgenGcDuration / config.intervalLengthSec),
                    (100 * interval.oldgenGcDuration / config.intervalLengthSec));
            log.format("longest Pause: %8.6f sec, ", interval.longestPause);
            log.format("allocation/promotion: %5.3f/%5.3f MB/sec, ",
                    interval.allocated / (config.intervalLengthSec * MB), interval.promoted / (config.intervalLengthSec * MB));
            log.format("completed GCs new/old: %d/%d, ",
                    interval.newgenCompletedCount, interval.oldgenCompletedCount);
            log.format("longest completed GC new/old: %4.3f/%4.3f sec\n",
                    interval.longestCompletedNewgenDuration, interval.longestCompletedOldgenDuration);
        }

    }

    static void attributePauses(List<PauseDetails> pauses) {
        for (PauseDetails pause : pauses) {
            double pauseStartTime =  pause.getElapsedTimeSinceJVMStartSec();
            double pauseEndTime  = pauseStartTime + pause.getDurationSec();
            for (IntervalData interval : pendingIntervals) {
                interval.longestPause = Math.max(
                        interval.longestPause,
                        durationWithinInterval(interval, pauseStartTime, pauseEndTime)
                );
            }
        }
    }

    static double durationWithinInterval(IntervalData interval, double timePeriodStart, double timePeriodEnd) {
        double start = Math.max(interval.startTime, timePeriodStart);
        double end = Math.min(interval.endTime, timePeriodEnd);
        double duration = (end > start) ? end - start :  0;
        return duration;
    }

    static double portionWithinInterval(IntervalData interval, double timePeriodStart, double timePeriodEnd) {
        double timePeriodDuration = timePeriodEnd - timePeriodStart;
        return (timePeriodDuration == 0) ? 0 :
                durationWithinInterval(interval, timePeriodStart, timePeriodEnd) / timePeriodDuration;
    }

    static boolean endsInInterval(IntervalData interval, double endTime) {
        return (interval.startTime <= endTime) && (endTime < interval.endTime);
    }

    static boolean overlapsWithInterval(IntervalData interval, double startTime, double endTime) {
        return (interval.startTime <= endTime) && (startTime < interval.endTime);

    }

    public void newgenHandler(GCDetails gcDetails) {
        try {
            if (config.verbose) log.format("Got GCDetails for %s ended at %4.3f\n",
                    gcDetails.getGarbageCollectionType(), gcDetails.getEndTime());

            if (gcDetails.getGarbageCollectionType().equals("NTO")) {
                pendingOldgen = true;
            }

            if (config.verbose) log.format("newgen enqueueing needed intervals:\n");

            enqueueIntervalsUpToTime(gcDetails.getEndTime());

            double promotedDuringCollection =
                    gcDetails.getPromotedPages() * PAGE_SIZE;

            double allocatedDuringCollection =
                    gcDetails.getAllocationRateDuringCollection() *
                            gcDetails.getCollectionDuration() * MB;

            double allocatedBeforeCollection =
                    gcDetails.getAllocationRateBetweenEndOfPreviousAndStart() *
                            gcDetails.getTimeFromEndOfPreviousToStart() * MB;

            double allocatedBetweenCollectionEnds =
                    allocatedDuringCollection + allocatedBeforeCollection;

            double endTimeOfPreviousCollection =
                    gcDetails.getStartTime() - gcDetails.getTimeFromEndOfPreviousToStart();

            // Attribute pause times to intervals:
            attributePauses(gcDetails.getCollectorRequiredPauseDetails());

            for (IntervalData interval : pendingIntervals) {

                // Attribute duration across spanned intervals:
                interval.newgenGcDuration +=
                        durationWithinInterval(interval, gcDetails.getStartTime(), gcDetails.getEndTime());

                // Attribute allocation proportionally to intervals spanned between collections ends:
                interval.allocated += allocatedBetweenCollectionEnds *
                        portionWithinInterval(interval, endTimeOfPreviousCollection, gcDetails.getEndTime());

                // Attribute promotion proportionally to intervals spanned by this collection:
                interval.promoted += promotedDuringCollection *
                        portionWithinInterval(interval, gcDetails.getStartTime(), gcDetails.getEndTime());

                // Attribute newgen completion & promotion info to specific interval:
                if (endsInInterval(interval, gcDetails.getEndTime())) {
                    interval.newgenCompletedCount++;
                    interval.longestCompletedNewgenDuration =
                            Math.max(interval.longestCompletedNewgenDuration,
                                    gcDetails.getCollectionDuration());
                }

                // Attribute worst App delay observed between newgen collection ends as
                // a pause (conservatively, to all spanned intervals):
                if (overlapsWithInterval(interval, endTimeOfPreviousCollection, gcDetails.getEndTime())) {
                    interval.longestPause =
                            Math.max(interval.longestPause, gcDetails.getMaximumApplicationThreadDelay());
                }
            }

            if (config.verbose) log.format("outputting newgen completed intervals:\n");

            outputCompletedIntervalUpToTime(gcDetails.getEndTime());

        } catch (RuntimeException ex) {
            System.err.println("Exception during newgen info processing:" + ex);
            ex.printStackTrace();
        }
    }

    public void oldgenHandler(GCDetails gcDetails) {
        try {
            if (config.verbose) log.format("Got GCDetails for %s ended at %4.3f\n",
                    gcDetails.getGarbageCollectionType(), gcDetails.getEndTime());

            pendingOldgen = false;

            enqueueIntervalsUpToTime(gcDetails.getEndTime());

            // Attribute pause times to intervals:
            attributePauses(gcDetails.getCollectorRequiredPauseDetails());

            for (IntervalData interval : pendingIntervals) {

                // Attribute duration across spanned intervals:
                interval.oldgenGcDuration +=
                        durationWithinInterval(interval, gcDetails.getStartTime(), gcDetails.getEndTime());

                // Attribute oldgen completion info to specific interval:
                if (endsInInterval(interval, gcDetails.getEndTime())) {
                    interval.oldgenCompletedCount++;
                    interval.longestCompletedOldgenDuration =
                            Math.max(interval.longestCompletedOldgenDuration,
                                    gcDetails.getCollectionDuration());
                }
            }

            if (config.verbose) log.format("outputting oldgen completed intervals:\n");

            outputCompletedIntervalUpToTime(gcDetails.getEndTime());

        } catch (RuntimeException ex) {
            System.err.println("Exception during oldgen info processing:" + ex);
            ex.printStackTrace();
        }
    }

    static class GCNotificationListener implements NotificationListener {
        @Override
        public void handleNotification(Notification notification, Object handback) {
            try {
                if (notification.getType().equals(GarbageCollectorNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                    GarbageCollectorNotificationInfo info =
                            GarbageCollectorNotificationInfo.from((CompositeData) notification.getUserData());
                    GCDetails gcDetails = info.getGCDetails();
                    gcDetailsNotifications.add(gcDetails);
                }
            } catch (RuntimeException ex) {
                System.err.println("Exception during notification processing:" + ex);
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            // Construct the ObjectName for the GPGC New and GPGC Old collector
            ObjectName mbeanNameNewGen = new ObjectName("com.azul.zing:type=GarbageCollector,name=GPGC New");
            ObjectName mbeanNameOldGen = new ObjectName("com.azul.zing:type=GarbageCollector,name=GPGC Old");

            // Create the notification listeners
            NotificationListener gcNotificationListener = new GCNotificationListener();

            // Add notification listeners for the MXBeans
            mbeanServer.addNotificationListener(mbeanNameNewGen, gcNotificationListener, null, null);
            mbeanServer.addNotificationListener(mbeanNameOldGen, gcNotificationListener, null, null);
        } catch (Exception ex) {
            System.err.println("Exception during ZingGCStatsReporter mbean registration:" + ex);
            ex.printStackTrace();
            // We've failed to set up, terminate thread:
            return;
        }

        if (config.verbose) log.println("Ok, ready to see some GCs...");

        while (true) {
            try {
                GCDetails gcDetails = gcDetailsNotifications.take();
                if (gcDetails.getGarbageCollectionType().equals("Old")) {
                    oldgenHandler(gcDetails);
                } else {
                    newgenHandler(gcDetails);
                }
            } catch (Exception ex) {
                System.err.println("Exception in GCNotificationHandler handling loop:" + ex);
                ex.printStackTrace();
            }
        }
    }

    private static void nap(long millis) {
        try {
            long startMillis = System.currentTimeMillis();
            while (System.currentTimeMillis() - startMillis < millis) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ZingGCReporter commonMain(String[] args) {
        ZingGCReporter zingGCReporter = null;
        try {
            zingGCReporter = new ZingGCReporter(args);

            if (zingGCReporter.config.verbose) {
                zingGCReporter.log.print("Executing: ZingGCStatsReporter");
                for (String arg : args) {
                    zingGCReporter.log.print(" " + arg);
                }
                zingGCReporter.log.println("");
            }
            zingGCReporter.start();
        } catch (FileNotFoundException e) {
            System.err.println("ZingGCStatsReporter: Failed to open log file.");
        }
        return zingGCReporter;
    }

    public static void premain(String argsString, java.lang.instrument.Instrumentation inst) {
        String[] args = (argsString != null) ? argsString.split("[ ,;]+") : new String[0];
        commonMain(args);
    }

    public static void main(String[] args) {
        final ZingGCReporter zingGCReporter = commonMain(args);

        if (zingGCReporter != null) {
            try {
                if (zingGCReporter.config.runTimeMsec != 0) {
                    // If configured with a limited run time, exit when it is over;
                    nap(zingGCReporter.config.runTimeMsec);
                    return;
                }
                // The ZingGCReporter thread, on it's own, will not keep the JVM from exiting. If nothing else
                // is running (i.e. we we are the main class), then keep main thread from exiting
                // until the HiccupMeter thread does...
                zingGCReporter.join();
            } catch (InterruptedException e) {
                if (zingGCReporter.config.verbose) {
                    zingGCReporter.log.println("ZingGCStatsReporter main() interrupted");
                }
            }
        }
    }
}