package com.danylevych.mss.model;

import static com.danylevych.mss.util.PerfEval.averageWaitTime;
import static com.danylevych.mss.util.PerfEval.cpusUsage;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.danylevych.mss.model.sheduler.Sheduler;
import com.danylevych.mss.model.sheduler.impl.FCFS;

public class ComputerTest {

	@Test
	public void testFCFS() {
		final int nCpu = 2;
		final boolean hasGlobalQueue = true;
		ProcessQueue jobs = new ProcessQueue();

		final int nJobs = 4;
		final List<Integer> cpuBurstsTime = asList(100, 50, 50, 50);

		for (int i = 0; i < nJobs; i++) {
			PCB job = new PCB(i, 0, asList(cpuBurstsTime.get(i)), new ArrayList<>());

			jobs.add(job);
		}

		Sheduler sheduler = new FCFS(hasGlobalQueue, nCpu);
		Computer computer = new Computer(jobs, sheduler, nCpu);

		computer.run();

		assertTrue(sheduler.getGlobalQueue().isEmpty());
		assertTrue(computer.getIoWaitingQueue().isEmpty());
		assertEquals(hasGlobalQueue, computer.getSheduler().hasGlobalQueue());

		final double[] expectedCpuUtil = { 100.0, 66.6 };
		double[] actualCpuUtil = cpusUsage(computer);		
		for (int i = 0; i < actualCpuUtil.length; i++) {
			assertEquals(expectedCpuUtil[i], actualCpuUtil[i], 0.1);
		}
		
		assertEquals(150, computer.getClock());

		List<Integer> actualWaits = computer.getJobs().stream().map(PCB::getWaitingTime).toList();

		assertEquals(asList(0, 0, 50, 100), actualWaits);
		assertEquals(150.0 / nJobs, averageWaitTime(computer), 0.1);
	}

}
