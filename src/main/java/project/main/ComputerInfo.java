package project.main;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        cpu = new CPU(hardware, new Canvas(85,35));
        gpu = new GPU(hardware);
        memory = new Memory(hardware);

//        service = Executors.newSingleThreadScheduledExecutor();

    }

    public void startTimeline() {
        service.scheduleAtFixedRate(this::update, 0, UPDATE_PERIOD, TimeUnit.MILLISECONDS);
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(UPDATE_PERIOD), event -> draw()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void update() {
        cpu.getCpuFrequency();
//        cpu.getCpuUsage(UPDATE_PERIOD);
    }

    public void draw() {
        cpu.draw();
    }

    public LinkedList<Canvas> setCanvas() {
        LinkedList<Canvas> nodes = new LinkedList<>();

        nodes.add(cpu.getCanvas());

        return nodes;
    }

}

class CPU {
    private CentralProcessor cpu;
    private Canvas canvas;
    private GraphicsContext gc;
    private double usage;
    private float frequency, maxFrequency;
    private Queue<Float> frequencyHistory = new LinkedList<>();

    CPU(HardwareAbstractionLayer hardware) {
        this.cpu = hardware.getProcessor();
        this.frequency = 0;
        this.usage = 0;
        this.maxFrequency = cpu.getMaxFreq();
    }

    CPU(HardwareAbstractionLayer hardware, Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.cpu = hardware.getProcessor();
        this.frequency = 0;
        this.usage = 0;
        this.maxFrequency = cpu.getMaxFreq();
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

    public void draw() {

    }

    public Canvas getCanvas() {
        return canvas;
    }

    public double getUsage() {
        return usage;
    }

    public float getFrequency() {
        return frequency;
    }
}

class Memory {
    private GlobalMemory mem;
    private long total, available, inUse;
    private Canvas canvas;
    private GraphicsContext gc;

    Memory(HardwareAbstractionLayer hardware) {
        this.mem = hardware.getMemory();
    }

    Memory(HardwareAbstractionLayer hardware, Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
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
        long total = mem.getTotal();
        long available = mem.getAvailable();
        long inUse = total - available;

        memInfo.add(total);
        memInfo.add(available);
        memInfo.add(inUse);

        return memInfo;
    }
}

class GPU {
    private List<GraphicsCard> gpu;
    private Canvas canvas;
    private GraphicsContext gc;

    GPU(HardwareAbstractionLayer hardware) {
        this.gpu = hardware.getGraphicsCards();
    }

    GPU(HardwareAbstractionLayer hardware, Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.gpu = hardware.getGraphicsCards();
    }

    public LinkedList<String> getGpuInfo() {
        LinkedList<String> gpuInfo = new LinkedList<>();

        gpuInfo.add("Model: " + gpu.get(0));
        gpuInfo.add("VRAM: " + gpu.get(1));

        return gpuInfo;
    }
}
