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

public final class ProcessQueueUtils {

    private static Random random = new Random();

    private ProcessQueueUtils() {

    }

    public static ProcessQueue readInputFile(String inputFile) {
        ProcessQueue events = new ProcessQueue();
        AtomicInteger pid = new AtomicInteger();

        try (Stream<String> lines = Files.lines(Path.of(inputFile))) {
            lines.map(line -> line.split(" ")).forEach(job -> {
                List<Integer> cpuBurstsTime = splitIntegers(job[1]);
                if (cpuBurstsTime.isEmpty()) {
                    throw new IllegalStateException(Arrays.toString(job));
                }

                List<Integer> ioBurstsTime =
                        job.length > 2 ? splitIntegers(job[2])
                                       : new ArrayList<>();

                final int arrivalTime = parseInt(job[0]);
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
            final int nCpuBursts = random.nextInt(3) + 1;
            List<Integer> cpuBurstsTime = new ArrayList<>(nCpuBursts);
            for (int j = 0; j < nCpuBursts; j++) {
                cpuBurstsTime.add(random.nextInt(5) + 1);
            }

            final int nIoBursts = nCpuBursts - 1;
            List<Integer> ioBurstsTime = new ArrayList<>(nIoBursts);
            if (nCpuBursts > 2) {
                for (int j = 0; j < nIoBursts; j++) {
                    ioBurstsTime.add(random.nextInt(5) + 1);
                }
            }

            final int arrivalTime = random.nextInt(5);
            events.add(new PCB(i, arrivalTime, cpuBurstsTime, ioBurstsTime));
        }

        return events;
    }

    public static ProcessQueue getSmallestQueue(
            Collection<ProcessQueue> queues) {
        return queues.stream().min(comparingInt(List::size)).orElseThrow();
    }

}
