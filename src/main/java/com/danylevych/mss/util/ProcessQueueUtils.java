package com.danylevych.mss.util;

import static com.danylevych.mss.util.StringUtils.splitIntegers;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparingInt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.danylevych.mss.model.PCB;
import com.danylevych.mss.model.ProcessQueue;

public class ProcessQueueUtils {

    private static Random random = new Random();

    private ProcessQueueUtils() {

    }

    public static ProcessQueue readInputFile(String inputFile) {
        ProcessQueue events = new ProcessQueue();
        AtomicInteger pid = new AtomicInteger();

        try (Stream<String> lines = Files.lines(Path.of(inputFile))) {
            lines.map(line -> line.split(" ")).forEach(job -> {
                final int arrivalTime = parseInt(job[0]);

                List<Integer> cpuBurstsTime = splitIntegers(job[1]);
                if (cpuBurstsTime.isEmpty()) {
                    throw new IllegalStateException(Arrays.toString(job));
                }

                List<Integer> ioBurstsTime =
                        job.length > 2 ? splitIntegers(job[2])
                                       : new ArrayList<>();

                PCB event = new PCB(pid.getAndIncrement(),
                        arrivalTime,
                        cpuBurstsTime,
                        ioBurstsTime);

                events.add(event);
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return events;
    }

    public static ProcessQueue generateEvents(int nEvents) {
        ProcessQueue events = new ProcessQueue();

        for (int i = 0; i < nEvents; i++) {
            List<Integer> cpuBurstsTime = new ArrayList<>();
            for (int j = 0; j < cpuBurstsTime.size(); j++) {
                cpuBurstsTime.add(random.nextInt(10) + 1);
            }

            // cpuBurstsTime.length - 1
            List<Integer> ioBurstsTime = new ArrayList<>();
            for (int j = 0; j < ioBurstsTime.size(); j++) {
                ioBurstsTime.add(random.nextInt(5) + 1);
            }

            events.add(new PCB(i, 0, cpuBurstsTime, ioBurstsTime));
        }

        return events;
    }

    public static ProcessQueue
           getSmallestQueue(Collection<ProcessQueue> queues) {
        return queues.stream().min(comparingInt(List::size)).orElseThrow();
    }

}
