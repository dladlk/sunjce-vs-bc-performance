package com.mercell.rd.jca;

import java.util.ArrayList;
import java.util.List;

public class Timer {

	private static boolean WRITE_STEPS = false;

	private long firstStart = 0;

	private long curStart = 0;
	private String curStep;

	private List<RecordData> records = new ArrayList<RecordData>();

	public Timer() {
		this.firstStart = System.currentTimeMillis();
	}

	public static class RecordData {

		private String name;
		private long duration;
		private long start;

		public RecordData(long start, String name, long duration) {
			this.start = start;
			this.name = name;
			this.duration = duration;
		}

		public String getName() {
			return name;
		}

		public long getDuration() {
			return duration;
		}

		public String toString() {
			return String.format("%s\t%d", this.name, this.duration);
		}

		protected long getStart() {
			return start;
		}
	}

	public void start(String step) {
		this.curStep = step;
		this.curStart = System.currentTimeMillis();
	}

	public long stop() {
		long dur = System.currentTimeMillis() - curStart;
		RecordData recordData = new RecordData(curStart, curStep, dur);
		records.add(recordData);
		if (WRITE_STEPS) {
			System.out.println("Done " + recordData);
		}
		this.curStart = 0;
		this.curStep = null;
		return dur;
	}

	public long finish() {
		if (curStart != 0) {
			stop();
		}
		return System.currentTimeMillis() - firstStart;
	}

	public long stopStart(String step) {
		long dur = this.stop();
		this.start(step);
		return dur;
	}

	public String toReport() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < records.size(); i++) {
			RecordData recordData = records.get(i);
			sb.append(i);
			sb.append(".\t");
			sb.append(recordData);
			sb.append("\n");
		}
		return sb.toString();
	}
}
