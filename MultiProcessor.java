import java.util.HashSet;
import java.util.Set;

// 定义缓存行的状态
enum CacheLineState {
    INVALID,   // 失效
    SHARED,    // 共享
    EXCLUSIVE  // 独占
}

// 定义主存类
class MainMemory {
    private final String[] data;

    public MainMemory(int size) {
        data = new String[size];
        for (int i = 0; i < 32; i++) {
            data[i] = String.valueOf(i);
        }
    }

    public String readData(int address) {
        return data[address];
    }

    public void writeData(int address, String newData) {
        data[address] = newData;
    }
}

// 定义处理器类
class Processor {
    private final String id;
    private final CacheLineState[] cacheStates; // 每个缓存行的状态
    private final String[] cacheData;           // 每个缓存行的数据
    private final int[] cacheAddress;           // 用于存储每个缓存行中的主存地址
    private final MainMemory mainMemory;

    public Processor(String id, MainMemory mainMemory) {
        this.id = id;
        this.cacheStates = new CacheLineState[4]; // 每个处理器有4个缓存行
        this.cacheData = new String[4];            // 数据缓存
        this.cacheAddress = new int[4];
        for (int i = 0; i < 4; i++) {
            cacheStates[i] = CacheLineState.INVALID;
            cacheData[i] = "";
            cacheAddress[i] = 32;
        }
        this.mainMemory = mainMemory;
    }

    public String readData(int address) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        if (checkCache(address)) {
            System.out.println("处理器:" + id + " 读:" + address + "  cache命中");
        } else {
            System.out.println("处理器:" + id + " 读:" + address + "  cache未命中 读主存...");
            checkOtherProcessorsCache(address);
            updateCache(address, mainMemory.readData(address), CacheLineState.SHARED);
        }
        return cacheData[cacheIndex];
    }

    public void writeData(int address, String newData) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        if (checkCache(address)) {
            System.out.println("处理器:" + id + " 写:" + address + " cache命中");
            checkOtherProcessorsCache(address);
            updateCache(address, newData, CacheLineState.EXCLUSIVE);
        } else {
            System.out.println("处理器:" + id + " 写:" + address + " cache未命中");
            checkOtherProcessorsCache(address);
            updateCache(address, newData, CacheLineState.EXCLUSIVE);
        }
    }

    private void checkOtherProcessorsCache(int address) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        for (Processor p : MultiProcessor.processors) {
            if (!this.equals(p)) {
                if (p.cacheStates[cacheIndex] == CacheLineState.EXCLUSIVE) {
                    p.writeBack(address);
                    p.updateCache(address, cacheData[cacheIndex], CacheLineState.SHARED);
                    return;
                }
                if (p.cacheStates[cacheIndex] == CacheLineState.SHARED) {
                    p.updateCache(address, "", CacheLineState.INVALID);
                }
            }
        }
    }

    private void writeBack(int address) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        System.out.println("处理器:" + id + " 写回主存:" + address);
        mainMemory.writeData(address, cacheData[cacheIndex]);
    }

    public void updateCache(int address, String newData, CacheLineState newState) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        if (newState == CacheLineState.INVALID) {
            System.out.println("处理器:" + id + " Cache:" + cacheIndex + " 作废" + " 状态: " + newState);
            cacheData[cacheIndex] = "";
            cacheStates[cacheIndex] = CacheLineState.INVALID;
            cacheAddress[cacheIndex] = 32;
            return;
        }
        System.out.println("处理器:" + id + " Cache:" + cacheIndex + " 更新主存地址:" + address + " 状态: " + newState);
        cacheData[cacheIndex] = newData;
        cacheStates[cacheIndex] = newState;
        cacheAddress[cacheIndex] = address;
    }

    public boolean checkCache(int address) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        // 处理缓存命中的逻辑
        return cacheAddress[cacheIndex] == address;
    }
}

public class MultiProcessor {
    static MainMemory mainMemory;
    static Processor p1;
    static Processor p2;
    static Processor p3;
    static Processor p4;
    static Set<Processor> processors = new HashSet<>();

    static {
        mainMemory = new MainMemory(32);
        p1 = new Processor("A", mainMemory);
        p2 = new Processor("B", mainMemory);
        p3 = new Processor("C", mainMemory);
        p4 = new Processor("D", mainMemory);
        processors.add(p1);
        processors.add(p2);
        processors.add(p3);
        processors.add(p4);
    }

    public static void main(String[] args) {
        // 示例操作序列
        p1.readData(0);
        p2.writeData(0, "New Data from Processor 2");
        p1.readData(0);
        p3.readData(2);
        p4.writeData(2, "New Data from Processor 4");
        p3.readData(2);
    }
}
