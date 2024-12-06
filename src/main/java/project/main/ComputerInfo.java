package project.main;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

public class ComputerInfo {
    private static final int UPDATE_PERIOD = 1000;
    static final byte MAX_SECONDS = 60;

    private SystemInfo system;
    private HardwareAbstractionLayer hardware;
    private CPU cpu;
    private GPU gpu;
    private Memory memory;

    private ScheduledExecutorService service;

    ComputerInfo() {
        system = new SystemInfo();
        hardware = system.getHardware();
        cpu = new CPU(hardware);
        gpu = new GPU(hardware);
        memory = new Memory(hardware);
    }

    CPU getCpu() {
        return cpu;
    }

    GPU getGpu() {
        return gpu;
    }

    Memory getMemory() {
        return memory;
    }
}

class CPU {
    private CentralProcessor cpu;
    private double usage;
    private float frequency;
    private Queue<Float> frequencyHistory = new LinkedList<>();

    CPU(HardwareAbstractionLayer hardware) {
        this.cpu = hardware.getProcessor();
        this.frequency = 0;
        this.usage = 0;
    }

    public LinkedList<String> getCpuInfo() {
        LinkedList<String> cpuInfo = new LinkedList<>();

        cpuInfo.add("Model: " + cpu.getProcessorIdentifier().getModel());
        cpuInfo.add("Core Count: " + cpu.getLogicalProcessorCount());
        cpuInfo.add("Clock: " + Arrays.toString(cpu.getCurrentFreq()));

        return cpuInfo;
    }

    public void getCpuUsage(long period) {
        byte coreCount = 0;
        for (double core : cpu.getProcessorCpuLoad(period)) {
            usage += core;
            coreCount++;
        }

        usage /= coreCount;
    }

    public void getCpuFrequency() {
        byte coreCount = 0;
        for (long core : cpu.getCurrentFreq()) {
            frequency += core;
            coreCount++;
        }

        frequency /= coreCount;
        if (frequencyHistory.size() >= ComputerInfo.MAX_SECONDS) frequencyHistory.poll();
        frequencyHistory.offer(frequency);
    }

    public double getUsage() {
        return usage;
    }

    public float getFrequency() {
        return frequency;
    }
}

class Memory {
    private final GlobalMemory mem;
    private long total, available, inUse;

    Memory(HardwareAbstractionLayer hardware) {
        this.mem = hardware.getMemory();
    }

    public LinkedList<String> getMemInfo() {
        LinkedList<String> memInfo = new LinkedList<>();
        this.total = mem.getTotal();
        this.available = mem.getAvailable();
        this.inUse = total - available;

        memInfo.add("Total: " + total);
        memInfo.add("Available: " + available);
        memInfo.add("In Use: " + inUse);

        return memInfo;
    }

    public LinkedList<Long> getMemUsage() {
        LinkedList<Long> memInfo = new LinkedList<>();

        memInfo.add(total);
        memInfo.add(available);
        memInfo.add(inUse);

        return memInfo;
    }

    long getTotal() {
        return total;
    }

    long getAvailable() {
        return available;
    }

    long getInUse() {
        return inUse;
    }
}

class GPU {
    private List<GraphicsCard> gpu;

    GPU(HardwareAbstractionLayer hardware) {
        this.gpu = hardware.getGraphicsCards();
    }

    public LinkedList<String> getGpuInfo() {
        LinkedList<String> gpuInfo = new LinkedList<>();

        gpuInfo.add("Model: " + gpu.get(0));
        gpuInfo.add("VRAM: " + gpu.get(1));

        return gpuInfo;
    }
}
